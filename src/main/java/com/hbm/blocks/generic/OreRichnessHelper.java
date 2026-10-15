package com.hbm.blocks.generic;

import java.util.Random;

import com.hbm.blocks.generic.BlockRichOre.RichOreType;
import com.hbm.config.WorldConfig;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.saveddata.RichOreData;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

// shared extraction logic for rich ores: one drain costs exactly one unit.
// used by hand mining, the excavator and the mining laser.
public class OreRichnessHelper {

	public static boolean isRichOre(Block b) {
		return b instanceof BlockRichOre;
	}

	public static boolean isRichOre(World world, int x, int y, int z) {
		return isRichOre(world.getBlock(x, y, z));
	}

	public static int getMaxUnits(BlockRichOre ore) {
		switch(ore.type) {
		case IRON: return Math.max(1, WorldConfig.richIronUnits);
		case COPPER: return Math.max(1, WorldConfig.richCopperUnits);
		case ALUMINIUM: return Math.max(1, WorldConfig.richAluminiumUnits);
		case ASBESTOS: return Math.max(1, WorldConfig.richAsbestosUnits);
		case BERYLLIUM: return Math.max(1, WorldConfig.richBerylliumUnits);
		case COAL: return Math.max(1, WorldConfig.richCoalUnits);
		case DIAMOND: return Math.max(1, WorldConfig.richDiamondUnits);
		case FLUORITE: return Math.max(1, WorldConfig.richFluoriteUnits);
		case GOLD: return Math.max(1, WorldConfig.richGoldUnits);
		case LAPIS: return Math.max(1, WorldConfig.richLapisUnits);
		case LEAD: return Math.max(1, WorldConfig.richLeadUnits);
		case LIGNITE: return Math.max(1, WorldConfig.richLigniteUnits);
		case LITHIUM: return Math.max(1, WorldConfig.richLithiumUnits);
		case NITER: return Math.max(1, WorldConfig.richNiterUnits);
		case REDSTONE: return Math.max(1, WorldConfig.richRedstoneUnits);
		case SULFUR: return Math.max(1, WorldConfig.richSulfurUnits);
		case THORIUM: return Math.max(1, WorldConfig.richThoriumUnits);
		case TITANIUM: return Math.max(1, WorldConfig.richTitaniumUnits);
		case TUNGSTEN: return Math.max(1, WorldConfig.richTungstenUnits);
		case URANIUM: return Math.max(1, WorldConfig.richUraniumUnits);
		case ZINC: return Math.max(1, WorldConfig.richZincUnits);
		default: throw new IllegalStateException("unhandled rich ore " + ore.type);
		}
	}

	// exact units left; untouched blocks read as the configured full value
	public static int getUnitsRemaining(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		if(!(b instanceof BlockRichOre)) return 0;

		BlockRichOre ore = (BlockRichOre) b;
		Integer stored = RichOreData.forWorld(world).get(x, y, z);

		if(stored == null) return getMaxUnits(ore);
		if(stored.intValue() <= 0) {
			RichOreData.forWorld(world).remove(x, y, z);
			return 0;
		}
		return Math.min(stored.intValue(), getMaxUnits(ore));
	}

	// display stage 0..7 for an exact count; identical to meta+1 while max is 8
	public static int stageFor(int units, int max) {
		if(units <= 0) return 0;
		return (int) Math.min(BlockRichOre.MAX_UNITS - 1, (units * (long) BlockRichOre.MAX_UNITS - 1) / Math.max(1, max));
	}

	// fortune affects output count only, never depletion rate
	public static int rollChunkCount(Random rand, int fortune) {
		fortune = Math.max(0, fortune);

		if(fortune > 0) {
			int mult = rand.nextInt(fortune + 2) - 1;
			return Math.max(mult, 0) + 1;
		}
		return 1;
	}

	// decrements richness, or turns to stone on the final unit. server only
	public static boolean consumeOneUnit(World world, int x, int y, int z) {
		Block b = world.getBlock(x, y, z);
		if(!(b instanceof BlockRichOre)) return false;
		if(world.isRemote) return false;

		BlockRichOre ore = (BlockRichOre) b;
		int left = getUnitsRemaining(world, x, y, z) - 1;
		RichOreData data = RichOreData.forWorld(world);

		if(left <= 0) {
			data.remove(x, y, z);
			world.setBlock(x, y, z, Blocks.stone, 0, 3);
		} else {
			data.set(x, y, z, left);
			world.setBlockMetadataWithNotify(x, y, z, stageFor(left, getMaxUnits(ore)), 3);
		}

		return true;
	}

	// extracts one unit. returns the chunk, or null if inapplicable
	public static ItemStack drainOneUnit(World world, int x, int y, int z, int fortune) {
		Block b = world.getBlock(x, y, z);
		if(!(b instanceof BlockRichOre)) return null;

		ItemStack chunk = ((BlockRichOre) b).getChunkStack();
		chunk.stackSize = rollChunkCount(world.rand, fortune);

		if(!consumeOneUnit(world, x, y, z)) return null;

		return chunk;
	}
}
