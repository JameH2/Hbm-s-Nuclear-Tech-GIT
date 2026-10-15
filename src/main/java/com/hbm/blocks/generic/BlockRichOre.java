package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.ModItems;
import com.hbm.render.block.RenderBlockMultipass;
import com.hbm.saveddata.RichOreData;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

/**
 * Depletable ore (21 types): metadata is richness stage, final unit turns to stone.
 * Extends BlockOre only for rendering.
 */
public class BlockRichOre extends BlockOre implements ILookOverlay {

	public enum RichOreType {
		IRON(3.0F, 5.0F),
		COPPER(5.0F, 10.0F),
		ALUMINIUM(5.0F, 10.0F),
		ASBESTOS(5.0F, 15.0F),
		BERYLLIUM(5.0F, 15.0F),
		COAL(3.0F, 5.0F),
		DIAMOND(3.0F, 5.0F),
		FLUORITE(5.0F, 10.0F),
		GOLD(3.0F, 5.0F),
		LAPIS(3.0F, 5.0F),
		LEAD(5.0F, 10.0F),
		LIGNITE(5.0F, 15.0F),
		LITHIUM(5.0F, 10.0F),
		NITER(5.0F, 10.0F),
		REDSTONE(3.0F, 5.0F),
		SULFUR(5.0F, 10.0F),
		THORIUM(5.0F, 10.0F),
		TITANIUM(5.0F, 10.0F),
		TUNGSTEN(5.0F, 10.0F),
		URANIUM(5.0F, 10.0F),
		ZINC(5.0F, 10.0F);
		// quartz, coltan and cobalt rich ores deferred, see the blob generation TODO in HbmWorldGen

		public final float hardness;
		public final float resistance;

		private RichOreType(float hardness, float resistance) {
			this.hardness = hardness;
			this.resistance = resistance;
		}

		public String blockName() {
			return "ore_rich_" + this.name().toLowerCase(Locale.US);
		}

		// material id doubles as the autogen chunk metadata
		public int matId() {
			switch(this) {
			case IRON: return Mats.MAT_IRON.id;
			case COPPER: return Mats.MAT_COPPER.id;
			case ALUMINIUM: return Mats.MAT_ALUMINIUM.id;
			case ASBESTOS: return Mats.MAT_ASBESTOS.id;
			case BERYLLIUM: return Mats.MAT_BERYLLIUM.id;
			case COAL: return Mats.MAT_COAL.id;
			case DIAMOND: return Mats.MAT_DIAMOND.id;
			case FLUORITE: return Mats.MAT_FLUORITE.id;
			case GOLD: return Mats.MAT_GOLD.id;
			case LAPIS: return Mats.MAT_LAPIS.id;
			case LEAD: return Mats.MAT_LEAD.id;
			case LIGNITE: return Mats.MAT_LIGNITE.id;
			case LITHIUM: return Mats.MAT_LITHIUM.id;
			case NITER: return Mats.MAT_KNO.id;
			case REDSTONE: return Mats.MAT_REDSTONE.id;
			case SULFUR: return Mats.MAT_SULFUR.id;
			case THORIUM: return Mats.MAT_THORIUM.id;
			case TITANIUM: return Mats.MAT_TITANIUM.id;
		case TUNGSTEN: return Mats.MAT_TUNGSTEN.id;
		case URANIUM: return Mats.MAT_URANIUM.id;
		case ZINC: return Mats.MAT_ZINC.id;
		default: throw new IllegalStateException("unhandled rich ore " + this);
		}
	}
	}

	// units in a fresh block
	public static final int MAX_UNITS = 8;

	// at most one rich extraction per player per tick; fake players exempt
	private static final HashMap<UUID, long[]> veinCounts = new HashMap<>();

	public final RichOreType type;

	public static int clampMeta(int meta) {
		return Math.max(0, Math.min(meta, MAX_UNITS - 1));
	}

	public BlockRichOre(RichOreType type) {
		super(Material.rock);
		this.type = type;
		this.setHardness(type.hardness);
		this.setResistance(type.resistance);
		this.setStepSound(soundTypeStone);
	}

	public ItemStack getChunkStack() {
		return new ItemStack(ModItems.chunk_rich, 1, type.matId());
	}

	@Override
	public int getMobilityFlag() {
		return 2;
	}

	// metadata is richness, not a planet variant: background is always plain stone
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
		if(RenderBlockMultipass.currentPass == 0) {
			return stoneIcons[0];
		}

		return blockIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(RenderBlockMultipass.currentPass == 0) {
			return stoneIcons[0];
		}

		return blockIcon;
	}

	@Override
	public boolean canSilkHarvest(World world, EntityPlayer player, int x, int y, int z, int meta) {
		return false;
	}

	@Override
	public Item getItemDropped(int meta, java.util.Random rand, int fortune) {
		return getChunkStack().getItem();
	}

	@Override
	public int quantityDropped(java.util.Random rand) {
		return 1;
	}

	@Override
	public int quantityDroppedWithBonus(int fortune, java.util.Random rand) {
		if(!allowFortune) return 1;
		return OreRichnessHelper.rollChunkCount(rand, fortune);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int meta, int fortune) {
		ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
		ItemStack chunk = getChunkStack();
		chunk.stackSize = allowFortune ? OreRichnessHelper.rollChunkCount(world.rand, fortune) : 1;
		drops.add(chunk);
		return drops;
	}

	@Override
	public int damageDropped(int meta) {
		return type.matId();
	}

	@Override
	public boolean removedByPlayer(World world, EntityPlayer player, int x, int y, int z, boolean willHarvest) {
		// creative and clearers remove freely; map entry goes with the block
		if(player.capabilities.isCreativeMode) {
			world.setBlockToAir(x, y, z);
			return true;
		}
		if(!willHarvest) return super.removedByPlayer(world, player, x, y, z, willHarvest);

		// the server only sips one unit and keeps the block: stay put client-side too,
		// or vein-mining ghosts the whole vein until the next block update
		if(world.isRemote) return false;

		if(!(player instanceof FakePlayer) && !allowVeinBreak(world, player)) {
			// over the per-tick vein budget: keep the block and resync anyone who predicted a break
			world.setBlockMetadataWithNotify(x, y, z, world.getBlockMetadata(x, y, z), 3);
			return false;
		}

		OreRichnessHelper.consumeOneUnit(world, x, y, z);
		this.harvestBlock(world, player, x, y, z, world.getBlockMetadata(x, y, z));

		return false;
	}

	private static boolean allowVeinBreak(World world, EntityPlayer player) {
		long tick = world.getTotalWorldTime();
		long[] entry = veinCounts.get(player.getUniqueID());

		if(entry == null || entry[0] != tick) {
			if(veinCounts.size() > 1024) veinCounts.clear();
			entry = new long[] { tick, 0 };
			veinCounts.put(player.getUniqueID(), entry);
		}

		if(entry[1] >= 1) return false;
		entry[1]++;
		return true;
	}

	// explosions release all remaining units at once (capped); the generic chance-drop path is disabled below
	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, int x, int y, int z, Explosion explosion) {
		if(!world.isRemote) {
			int units = Math.min(OreRichnessHelper.getUnitsRemaining(world, x, y, z), 64 * 8);

			while(units > 0) {
				int n = Math.min(units, 64);
				ItemStack chunk = getChunkStack();
				chunk.stackSize = n;
				this.dropBlockAsItem(world, x, y, z, chunk);
				units -= n;
			}

			RichOreData.forWorld(world).remove(x, y, z);
		}

		super.onBlockExploded(world, x, y, z, explosion);
	}

	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		if(!world.isRemote) RichOreData.forWorld(world).remove(x, y, z);
	}

	// any non-drain removal drops its map entry, so reused coords start clean
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if(!world.isRemote) RichOreData.forWorld(world).remove(x, y, z);
		super.breakBlock(world, x, y, z, block, meta);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {
		return new ItemStack(this, 1, clampMeta(world.getBlockMetadata(x, y, z)));
	}

	// placement seeds the map from the item stage, so pick-place stays honest
	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int meta = clampMeta(stack.getItemDamage());
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);
		if(!world.isRemote) RichOreData.forWorld(world).set(x, y, z, Math.max(1, (meta + 1) * OreRichnessHelper.getMaxUnits(this) / MAX_UNITS));
	}

	@Override
	public int getSubCount() {
		return MAX_UNITS;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, MAX_UNITS - 1));
	}

	// tiers by stage: 5-7 rich, 2-4 half-depleted, 0-1 almost depleted, material name from Mats
	@Override
	public String getOverrideDisplayName(ItemStack stack) {
		int meta = clampMeta(stack.getItemDamage());
		NTMMaterial mat = Mats.matById.get(type.matId());
		String matName = mat != null ? StatCollector.translateToLocal(mat.getUnlocalizedName()) : type.name();
		String tier = meta >= 5 ? "name" : meta >= 2 ? "half.name" : "low.name";
		return StatCollector.translateToLocalFormatted("tile.ore_rich." + tier, matName).trim();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(RenderGameOverlayEvent.Pre event, World world, int x, int y, int z) {
		int meta = clampMeta(world.getBlockMetadata(x, y, z));
		String title = getOverrideDisplayName(new ItemStack(this, 1, meta));
		if(title == null) title = StatCollector.translateToLocal(this.getUnlocalizedName() + ".name");

		List<String> text = new ArrayList<String>();
		text.add(((meta + 1) * 100 / MAX_UNITS) + "%");

		ILookOverlay.printGeneric(event, title, 0xffff00, 0x404000, text);
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		int meta = clampMeta(stack.getItemDamage());
		list.add(EnumChatFormatting.GOLD + "" + ((meta + 1) * 100 / MAX_UNITS) + "%");
	}
}
