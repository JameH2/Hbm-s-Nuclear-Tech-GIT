package com.hbm.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import com.hbm.config.GeneralConfig;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.main.MainRegistry;

import net.minecraft.tileentity.TileEntity;

/**
 * Debug tracing for the Forge fluid / AE2 integration (mapping registry, adapters,
 * dummies, container items, icons) and the Rotary Furnace "None" ghost-fluid issue.
 * Gated behind 1.46_enableFluidDebugLogging (default OFF) or -Dntm.fluidDebug=true,
 * side-effect free. Repro: enable, reproduce, grep the server log for [FluidDebug].
 */
public class FluidDebug {

	private static final ConcurrentHashMap<String, Long> lastLog = new ConcurrentHashMap<String, Long>();
	private static final ConcurrentHashMap<String, Long> suppressed = new ConcurrentHashMap<String, Long>();
	private static final long COOLDOWN_MS = 10_000L;
	private static final int MAX_FRAMES = 24;
	/** Tracking maps are pruned once they exceed this many keys so long sessions cannot leak. */
	private static final int MAX_TRACKED_KEYS = 2048;
	private static final long STALE_MS = 60_000L;

	public static boolean isEnabled() {
		if(GeneralConfig.enableFluidDebugLogging) return true;
		try {
			return Boolean.getBoolean("ntm.fluidDebug");
		} catch(Throwable t) {
			return false;
		}
	}

	public static String describe(FluidType type) {
		if(type == null) return "null";
		try {
			return type.getName() + "#" + type.getID();
		} catch(Throwable t) {
			return String.valueOf(type);
		}
	}

	public static String describeTank(FluidTank tank) {
		if(tank == null) return "null";
		try {
			return describe(tank.getTankType()) + " " + tank.getFill() + "/" + tank.getMaxFill() + "mB";
		} catch(Throwable t) {
			return "unreadable";
		}
	}

	public static String describeTile(TileEntity te) {
		if(te == null) return "null";
		try {
			String coords;
			try {
				int dim = te.getWorldObj() != null && te.getWorldObj().provider != null ? te.getWorldObj().provider.dimensionId : Integer.MIN_VALUE;
				coords = "dim=" + dim + " x=" + te.xCoord + " y=" + te.yCoord + " z=" + te.zCoord;
			} catch(Throwable t) {
				coords = "coords=?";
			}
			return te.getClass().getSimpleName() + "@" + coords;
		} catch(Throwable t) {
			return String.valueOf(te);
		}
	}

	public static String tileKey(TileEntity te) {
		if(te == null) return "null";
		try {
			int dim = te.getWorldObj() != null && te.getWorldObj().provider != null ? te.getWorldObj().provider.dimensionId : Integer.MIN_VALUE;
			return dim + ":" + te.xCoord + "," + te.yCoord + "," + te.zCoord;
		} catch(Throwable t) {
			return Integer.toHexString(System.identityHashCode(te));
		}
	}

	public static String describeForgeStack(net.minecraftforge.fluids.FluidStack stack) {
		if(stack == null) return "null";
		try {
			String name = stack.getFluid() == null ? "null-fluid" : stack.getFluid().getName();
			return name + " x" + stack.amount;
		} catch(Throwable t) {
			return "unreadable";
		}
	}

	public static String describeForgeFluid(net.minecraftforge.fluids.Fluid fluid) {
		if(fluid == null) return "null";
		try {
			return fluid.getName();
		} catch(Throwable t) {
			return String.valueOf(fluid);
		}
	}

	/** First stack frame outside our own code: usually the AE2/ae2fc caller. */
	public static String callerHint() {
		try {
			for(StackTraceElement el : Thread.currentThread().getStackTrace()) {
				String cls = el.getClassName();
				if(cls.equals(FluidDebug.class.getName()) || cls.equals(Thread.class.getName())) continue;
				if(cls.startsWith("java.") || cls.startsWith("sun.") || cls.startsWith("jdk.")) continue;
				if(cls.startsWith("com.hbm.") || cls.startsWith("api.hbm.") || cls.startsWith("api.ntm1of90.")) continue;
				return el.toString();
			}
		} catch(Throwable t) { }
		return "?";
	}

	/** Throttled event line; reports how many similar lines were suppressed since the last one. */
	public static void event(String key, String msg) {
		if(!isEnabled()) return;
		try {
			pruneIfNeeded();
			if(shouldLog(key)) {
				Long dropped = suppressed.remove(key);
				MainRegistry.logger.warn("[FluidDebug] " + msg + (dropped != null && dropped > 0 ? " (+" + dropped + " similar suppressed)" : ""));
			} else {
				Long count = suppressed.get(key);
				suppressed.put(key, count == null ? 1L : count + 1L);
			}
		} catch(Throwable t) { }
	}

	/**
	 * Lazy variant of {@link #event(String, String)}. The message supplier is only invoked when
	 * debugging is enabled, so expensive descriptions/stack traces are never built otherwise.
	 */
	public static void event(String key, Supplier<String> msg) {
		if(!isEnabled()) return;
		event(key, msg.get());
	}

	/** Throttled event line with a filtered stack trace (rare, important events only). */
	public static void eventStack(String key, String msg) {
		if(!isEnabled()) return;
		try {
			pruneIfNeeded();
			if(shouldLog(key)) {
				Long dropped = suppressed.remove(key);
				MainRegistry.logger.warn("[FluidDebug] " + msg + (dropped != null && dropped > 0 ? " (+" + dropped + " similar suppressed)" : "") + "\n" + filteredStack());
			} else {
				Long count = suppressed.get(key);
				suppressed.put(key, count == null ? 1L : count + 1L);
			}
		} catch(Throwable t) { }
	}

	/** Lazy variant of {@link #eventStack(String, String)}. */
	public static void eventStack(String key, Supplier<String> msg) {
		if(!isEnabled()) return;
		eventStack(key, msg.get());
	}

	/** Logs whenever a tank ends up as NONE with fill > 0. Quiet otherwise. */
	public static void checkTank(FluidTank tank, String op, FluidType oldType, int oldFill) {
		if(!isEnabled() || tank == null) return;
		try {
			FluidType newType = tank.getTankType();
			int newFill = tank.getFill();
			if(newType == Fluids.NONE && newFill > 0) {
				eventStack("ghost|" + op + "|" + System.identityHashCode(tank),
					"GHOST FLUID after " + op
					+ ": NONE with fill " + oldFill + " -> " + newFill
					+ " (prevType=" + describe(oldType) + ")"
					+ " tank@" + Integer.toHexString(System.identityHashCode(tank))
					+ " max=" + tank.getMaxFill());
			}
		} catch(Throwable t) { }
	}

	/** Logs type changes that wipe content or involve NONE (e.g. resets to NONE on empty tanks). */
	public static void logTypeChange(FluidTank tank, String op, FluidType oldType, int oldFill) {
		if(!isEnabled() || tank == null || oldType == null) return;
		try {
			FluidType newType = tank.getTankType();
			if(newType == oldType) return;
			if(oldFill > 0 || newType == Fluids.NONE || oldType == Fluids.NONE) {
				eventStack("typechange|" + op + "|" + System.identityHashCode(tank),
					"TYPE CHANGE via " + op
					+ ": " + describe(oldType) + " x" + oldFill
					+ " -> " + describe(newType) + " x" + tank.getFill()
					+ " tank@" + Integer.toHexString(System.identityHashCode(tank)));
			}
		} catch(Throwable t) { }
	}

	/** Per-tick validation of the Rotary's fixed tanks (tanks[1] = STEAM, tanks[2] = SPENTSTEAM). */
	public static void checkRotaryTanks(TileEntity te, FluidTank[] tanks) {
		if(!isEnabled() || te == null || tanks == null || tanks.length < 3) return;
		try {
			boolean bad = false;
			if(tanks[0] != null && tanks[0].getTankType() == Fluids.NONE && tanks[0].getFill() > 0) bad = true;
			if(tanks[1] == null || tanks[1].getTankType() != Fluids.STEAM) bad = true;
			if(tanks[2] == null || tanks[2].getTankType() != Fluids.SPENTSTEAM) bad = true;
			if(tanks[1] != null && tanks[1].getTankType() == Fluids.NONE && tanks[1].getFill() > 0) bad = true;
			if(tanks[2] != null && tanks[2].getTankType() == Fluids.NONE && tanks[2].getFill() > 0) bad = true;

			if(bad) {
				event("rotary|" + tileKey(te),
					"ROTARY STATE " + describeTile(te)
					+ " tanks[0]=" + describeTank(tanks[0])
					+ " tanks[1]=" + describeTank(tanks[1]) + " (expect STEAM)"
					+ " tanks[2]=" + describeTank(tanks[2]) + " (expect SPENTSTEAM)");
			}
		} catch(Throwable t) { }
	}

	/** Rotary tank states right after NBT load / packet deserialize (loads are rare, always log). */
	public static void logRotaryLoad(TileEntity te, FluidTank[] tanks, String op) {
		if(!isEnabled() || te == null || tanks == null || tanks.length < 3) return;
		try {
			MainRegistry.logger.info("[FluidDebug] ROTARY LOAD (" + op + ") " + describeTile(te)
				+ " tanks[0]=" + describeTank(tanks[0])
				+ " tanks[1]=" + describeTank(tanks[1])
				+ " tanks[2]=" + describeTank(tanks[2]));
		} catch(Throwable t) { }
	}

	private static boolean shouldLog(String key) {
		long now = System.currentTimeMillis();
		Long last = lastLog.get(key);
		if(last == null || now - last > COOLDOWN_MS) {
			lastLog.put(key, now);
			return true;
		}
		return false;
	}

	/**
	 * Keeps the tracking maps bounded on long-running servers: drops cooldown entries that are
	 * older than {@link #STALE_MS}, and clears the suppression counters once they grow past
	 * {@link #MAX_TRACKED_KEYS}. Only runs once a map actually grows large, so the common path
	 * stays allocation-free.
	 */
	private static void pruneIfNeeded() {
		try {
			if(lastLog.size() > MAX_TRACKED_KEYS) {
				long cutoff = System.currentTimeMillis() - STALE_MS;
				lastLog.entrySet().removeIf(e -> e.getValue() == null || e.getValue() < cutoff);
				if(lastLog.size() > MAX_TRACKED_KEYS) lastLog.clear();
			}
			if(suppressed.size() > MAX_TRACKED_KEYS) {
				suppressed.clear();
			}
		} catch(Throwable t) { }
	}

	private static String filteredStack() {
		try {
			StackTraceElement[] stack = Thread.currentThread().getStackTrace();
			StringBuilder sb = new StringBuilder();
			int kept = 0;
			for(StackTraceElement el : stack) {
				String cls = el.getClassName();
				if(cls.equals(FluidDebug.class.getName())) continue;
				if(cls.equals(Thread.class.getName())) continue;
				if(cls.startsWith("java.") || cls.startsWith("sun.") || cls.startsWith("jdk.")) continue;
				sb.append("    at ").append(el.toString()).append("\n");
				if(++kept >= MAX_FRAMES) break;
			}
			return sb.toString();
		} catch(Throwable t) {
			return "    <stack unavailable>";
		}
	}
}
