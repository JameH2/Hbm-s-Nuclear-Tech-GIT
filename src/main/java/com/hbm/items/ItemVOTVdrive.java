package com.hbm.items;

import java.util.List;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.entity.missile.EntityRideableRocket;
import com.hbm.entity.missile.EntityRideableRocket.RocketState;
import com.hbm.lib.RefStrings;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;

public class ItemVOTVdrive extends ItemEnumMulti {

	private IIcon[] overlays;

	public ItemVOTVdrive() {
		super(SolarSystem.Body.class, false, false);
		this.setMaxStackSize(1);
		this.canRepair = false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(stack, player, list, bool);

		Destination destination = getDestination(stack);

		if(destination.body == SolarSystem.Body.ORBIT) {
			String identifier = stack.stackTagCompound.getString("stationName");

			if(identifier.equals("")) identifier = "0x" + Integer.toHexString(new ChunkCoordIntPair(destination.x, destination.z).hashCode()).toUpperCase();

			list.add("Destination: ORBITAL STATION");
			list.add("Station: " + identifier);
			return;
		}

		int processingLevel = destination.body.getProcessingLevel(CelestialBody.getBody(player.worldObj));

		list.add("Destination: " + EnumChatFormatting.AQUA + I18nUtil.resolveKey("body." + destination.body.name));

		if(destination.x == 0 && destination.z == 0) {
			list.add(EnumChatFormatting.GOLD + "Needs destination coordinates!");
		} else if(!getProcessed(stack)) {
			// Display processing level info if not processed
			list.add("Process requirement: Level " + processingLevel);
			list.add(EnumChatFormatting.GOLD + "Needs processing!");
			list.add("Target coordinates: " + destination.x + ", " + destination.z);
		} else {
			// Display destination info if processed
			list.add(EnumChatFormatting.GREEN + "Processed!");
			list.add("Target coordinates: " + destination.x + ", " + destination.z);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		overlays = new IIcon[SolarSystem.Body.values().length];

		for(int i = 0; i < overlays.length; i++) {
			SolarSystem.Body body = SolarSystem.Body.values()[i];
			String name = body != SolarSystem.Body.ORBIT ? body.name : "orbit";
			overlays[i] = iconRegister.registerIcon(RefStrings.MODID + ":votv." + name);
		}

		itemIcon = iconRegister.registerIcon(RefStrings.MODID + ":votv_f"); // Base icon for unprocessed drives
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean requiresMultipleRenderPasses() {
		return true;
	}

	@Override
	public int getRenderPasses(int metadata) {
		return 2;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamageForRenderPass(int meta, int pass) {
		if(pass == 0) return this.getIconFromDamage(meta);
		return this.overlays[meta];
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		NBTTagCompound stackTag = new NBTTagCompound();
		stackTag.setInteger("x", 1);
		stackTag.setInteger("ax", 1);
		stackTag.setBoolean("Processed", true);
		for(int i = 0; i < theEnum.getEnumConstants().length; i++) {
			ItemStack stack = new ItemStack(item, 1, i);
			stack.stackTagCompound = stackTag;
			list.add(stack);
		}
	}

	public static SolarSystem.Body getBody(ItemStack stack) {
		return SolarSystem.Body.values()[stack.getItemDamage() % SolarSystem.Body.values().length];
	}

	public static Destination getDestination(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		SolarSystem.Body body = getBody(stack);
		int x = stack.stackTagCompound.getInteger("x");
		int z = stack.stackTagCompound.getInteger("z");
		return new Destination(body, x, z);
	}

	public static Target getTarget(ItemStack stack, World world) {
		if(stack == null) {
			return new Target(null, false, false);
		}

		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		Destination destination = getDestination(stack);

		if(destination.body == SolarSystem.Body.ORBIT) {
			if(world.isRemote) {
				CelestialBody body = CelestialBody.getBody(stack.stackTagCompound.getInteger("sDim"));
				boolean hasStation = stack.stackTagCompound.getBoolean("sHas");

				return new Target(body, true, hasStation);
			}

			OrbitalStation station = OrbitalStation.getStation(destination.x, destination.z);
			if(!station.hasStation) station.orbiting = CelestialBody.getBody(world);

			// The client can't get this information, so any time the server grabs it, serialize it to the itemstack
			stack.stackTagCompound.setString("stationName", station.name);
			stack.stackTagCompound.setInteger("sDim", station.orbiting.dimensionId);
			stack.stackTagCompound.setBoolean("sHas", station.hasStation);

			return new Target(station.orbiting, true, station.hasStation);
		} else {
			return new Target(destination.body.getBody(), false, true);
		}
	}

	public static void setCoordinates(ItemStack stack, int x, int z) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setInteger("x", x);
		stack.stackTagCompound.setInteger("z", z);
	}

	public static int getProcessingTier(ItemStack stack, CelestialBody from) {
		SolarSystem.Body body = getBody(stack);
		return body.getProcessingLevel(from);
	}

	public static boolean getProcessed(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		return stack.stackTagCompound.getBoolean("Processed");
	}

	public static void setProcessed(ItemStack stack, boolean processed) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setBoolean("Processed", processed);
	}

	// Returns an area for the Stardar to draw, so the player can pick a safe spot to land
	public static Destination getApproximateDestination(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		SolarSystem.Body body = getBody(stack);
		if(!stack.stackTagCompound.hasKey("ax") || !stack.stackTagCompound.hasKey("az")) {
			stack.stackTagCompound.setInteger("ax", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
			stack.stackTagCompound.setInteger("az", itemRand.nextInt(SpaceConfig.maxProbeDistance * 2) - SpaceConfig.maxProbeDistance);
		}
		int ax = stack.stackTagCompound.getInteger("ax");
		int az = stack.stackTagCompound.getInteger("az");
		return new Destination(body, ax, az);
	}

	public static void markCopied(ItemStack stack) {
		if(!stack.hasTagCompound())
			stack.stackTagCompound = new NBTTagCompound();

		stack.stackTagCompound.setBoolean("copied", true);
	}

	public static boolean wasCopied(ItemStack stack) {
		if(!stack.hasTagCompound()) return false;
		return stack.stackTagCompound.getBoolean("copied");
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		boolean isProcessed = getProcessed(stack);
		boolean onDestination = world.provider.dimensionId == getDestination(stack).body.getDimensionId();

		// If we're on the body (or in creative), immediately process
		if(!isProcessed && (player.capabilities.isCreativeMode || onDestination)) {
			isProcessed = true;
			setProcessed(stack, true);
		}

		ItemStack newStack = stack;

		if(isProcessed && player.ridingEntity != null && player.ridingEntity instanceof EntityRideableRocket) {
			EntityRideableRocket rocket = (EntityRideableRocket) player.ridingEntity;

			if(rocket.getRocket().stages.size() > 0 || world.provider.dimensionId == SpaceConfig.orbitDimension || rocket.isReusable()) {
				if(rocket.getState() == RocketState.LANDED || rocket.getState() == RocketState.AWAITING) {
					// Replace our held stack with the rocket drive and place our held drive into the rocket
					if(rocket.navDrive != null) {
						newStack = rocket.navDrive;
					} else {
						newStack.stackSize = 0;
					}

					rocket.navDrive = stack.copy();
					rocket.navDrive.stackSize = 1;

					if(!world.isRemote) {
						rocket.setState(RocketState.AWAITING);
					}

					world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:item.upgradePlug", 1.0F, 1.0F);
				}
			}
		}

		return newStack;
	}


	@Override
	public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float fx, float fy, float fz) {
		Destination destination = getDestination(stack);
		if(destination.body == SolarSystem.Body.ORBIT)
			return false;

		boolean onDestination = world.provider.dimensionId == destination.body.getDimensionId();
		if(!onDestination)
			return false;

		setCoordinates(stack, x, z);
		setProcessed(stack, true);

		if(!world.isRemote)
			player.addChatMessage(new ChatComponentText(EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + "Set landing coordinates to: " + x + ", " + z));

		return true;
	}

	public static class Destination {

		public int x;
		public int z;
		public SolarSystem.Body body;

		public Destination(SolarSystem.Body body, int x, int z) {
			this.body = body;
			this.x = x;
			this.z = z;
		}

		public ChunkCoordIntPair getChunk() {
			return new ChunkCoordIntPair(x >> 4, z >> 4);
		}

	}

	public static class Target {

		public CelestialBody body;
		public boolean inOrbit;
		public boolean isValid;

		public Target(CelestialBody body, boolean inOrbit, boolean isValid) {
			this.body = body;
			this.inOrbit = inOrbit;
			this.isValid = isValid;
		}

	}

}
