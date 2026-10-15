package com.hbm.world.feature;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockRichOre;
import com.hbm.config.WorldConfig;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

public class RichOreBlob {

	public static boolean blobsActive() {
		return WorldConfig.enableRichOres;
	}

	public static boolean advancedBlobs() {
		return WorldConfig.enableRichOres && WorldConfig.richOreAdvanced;
	}

	public static boolean classicVeins() {
		return !advancedBlobs();
	}

	public static boolean overrideVanilla() {
		return WorldConfig.overworldOre && advancedBlobs();
	}

	private static final int COMMON = 6;
	private static final int UNCOMMON = 12;
	private static final int RARE = 24;
	private static final int VERYRARE = 48;

	private static final String[] JUNGLE = { "jungle", "jungleedge", "jungleedgem", "junglehills", "junglem" };
	private static final String[] COLDTAIGA = { "coldtaiga", "coldtaigahills", "coldtaigam", "megasprucetaiga", "megataiga", "megataigahills", "redwoodtaiga", "redwoodtaigahills", "redwoodtaigahillsm", "redwoodtaigam", "taiga", "taigam", "taigahills" };
	private static final String[] EXTREME = { "extremehills", "extremehillsm", "extremehills+", "extremehills+m", "icemountains" };
	private static final String[] BIRCH = { "birchforest", "birchforesthills", "birchforesthillsm", "birchforestm" };
	private static final String[] PLAINS = { "iceplains", "iceplainsspikes", "plains", "sunflowerplains" };
	private static final String[] SAVANNA = { "savanna", "savannam", "savannaplateau", "savannaplateaum" };
	private static final String[] DESERT = { "desert", "desertm", "deserthills" };
	private static final String[] MESA = { "mesa", "mesaplateau", "mesaplateauf", "mesaplateaufm", "mesaplateaum" };
	private static final String[] OCEAN = { "deepocean", "ocean" };
	private static final String[] FOREST = { "flowerforest", "forest", "foresthills" };
	private static final String[] ROOFED = { "roofedforest", "roofedforestm" };
	private static final String[] SWAMP = { "swampland", "swamplandm" };
	private static final String[] STONEBEACH = { "stonebeach" };

	// TODO: IMPLEMENT APPLIED ENERGISTICS 2 QUARTZ ORE SUPPORT LATER (rich quartz variant + certus processing; coltan/cobalt rich variants deferred with it)
	public static class VeinDef {
		public final Block rich;
		public final Block halo;
		public final int yMin;
		public final int yMax;
		public final int rarity;
		public final boolean flat;
		public final int blobSize;
		public final int cloudR;
		public final int cloudT;
		public final float cloudDensity;
		public final String[] biomes;

		public VeinDef(Block rich, Block halo, int yMin, int yMax, int rarity, boolean flat, int blobSize, int cloudR, int cloudT, float cloudDensity, String[]... biomeGroups) {
			this.rich = rich;
			this.halo = halo;
			this.yMin = yMin;
			this.yMax = yMax;
			this.rarity = rarity;
			this.flat = flat;
			this.blobSize = blobSize;
			this.cloudR = cloudR;
			this.cloudT = cloudT;
			this.cloudDensity = cloudDensity;

			int len = 0;
			for(String[] g : biomeGroups) len += g.length;
			this.biomes = new String[len];
			int i = 0;
			for(String[] g : biomeGroups) for(String s : g) this.biomes[i++] = s;
		}
	}

	private static String[] all() {
		return new String[0];
	}

	public static final VeinDef[] DEFS = {
		new VeinDef(ModBlocks.ore_rich_iron, Blocks.iron_ore, 16, 52, UNCOMMON, false, WorldConfig.richIronBlobSize, 13, 6, 0.16F, PLAINS, SAVANNA),
		new VeinDef(ModBlocks.ore_rich_copper, ModBlocks.ore_copper, 16, 52, UNCOMMON, false, WorldConfig.richCopperBlobSize, 11, 5, 0.16F, BIRCH, PLAINS),
		new VeinDef(ModBlocks.ore_rich_aluminium, ModBlocks.ore_aluminium, 0, 60, UNCOMMON, false, WorldConfig.richAluminiumBlobSize, 11, 5, 0.16F, JUNGLE, COLDTAIGA),
		new VeinDef(ModBlocks.ore_rich_asbestos, ModBlocks.ore_asbestos, 16, 48, RARE, false, WorldConfig.richAsbestosBlobSize, 9, 4, 0.15F, COLDTAIGA),
		new VeinDef(ModBlocks.ore_rich_beryllium, ModBlocks.ore_beryllium, 0, 40, VERYRARE, false, WorldConfig.richBerylliumBlobSize, 7, 3, 0.14F, EXTREME),
		new VeinDef(ModBlocks.ore_rich_coal, Blocks.coal_ore, 20, 60, UNCOMMON, false, WorldConfig.richCoalBlobSize, 13, 4, 0.18F, EXTREME, STONEBEACH),
		new VeinDef(ModBlocks.ore_rich_diamond, Blocks.diamond_ore, 5, 13, VERYRARE, true, WorldConfig.richDiamondBlobSize, 0, 0, 0, EXTREME, COLDTAIGA),
		new VeinDef(ModBlocks.ore_rich_fluorite, ModBlocks.ore_fluorite, 16, 48, RARE, false, WorldConfig.richFluoriteBlobSize, 11, 5, 0.16F, OCEAN, ROOFED),
		new VeinDef(ModBlocks.ore_rich_gold, Blocks.gold_ore, 0, 40, RARE, false, WorldConfig.richGoldBlobSize, 7, 3, 0.14F, DESERT, JUNGLE),
		new VeinDef(ModBlocks.ore_rich_lapis, Blocks.lapis_ore, 12, 20, RARE, true, WorldConfig.richLapisBlobSize, 0, 0, 0, all()),
		new VeinDef(ModBlocks.ore_rich_lead, ModBlocks.ore_lead, 16, 48, RARE, false, WorldConfig.richLeadBlobSize, 9, 4, 0.15F, FOREST),
		new VeinDef(ModBlocks.ore_rich_lignite, ModBlocks.ore_lignite, 26, 54, COMMON, true, WorldConfig.richLigniteBlobSize, 0, 0, 0, SWAMP),
		new VeinDef(ModBlocks.ore_rich_lithium, ModBlocks.ore_lithium, 16, 48, RARE, false, WorldConfig.richLithiumBlobSize, 9, 4, 0.15F, MESA),
		new VeinDef(ModBlocks.ore_rich_niter, ModBlocks.ore_niter, 16, 48, RARE, false, WorldConfig.richNiterBlobSize, 9, 4, 0.15F, DESERT),
		new VeinDef(ModBlocks.ore_rich_redstone, Blocks.redstone_ore, 0, 32, UNCOMMON, false, WorldConfig.richRedstoneBlobSize, 9, 4, 0.15F, BIRCH, FOREST, MESA, SAVANNA),
		new VeinDef(ModBlocks.ore_rich_sulfur, ModBlocks.ore_sulfur, 10, 50, COMMON, false, WorldConfig.richSulfurBlobSize, 11, 5, 0.16F, MESA, SWAMP),
		new VeinDef(ModBlocks.ore_rich_thorium, ModBlocks.ore_thorium, 0, 40, UNCOMMON, false, WorldConfig.richThoriumBlobSize, 7, 3, 0.14F, SAVANNA),
		new VeinDef(ModBlocks.ore_rich_titanium, ModBlocks.ore_titanium, 0, 40, RARE, false, WorldConfig.richTitaniumBlobSize, 7, 3, 0.14F, DESERT, JUNGLE, MESA),
		new VeinDef(ModBlocks.ore_rich_tungsten, ModBlocks.ore_tungsten, 16, 48, RARE, false, WorldConfig.richTungstenBlobSize, 9, 4, 0.15F, EXTREME),
		new VeinDef(ModBlocks.ore_rich_uranium, ModBlocks.ore_uranium, 0, 48, UNCOMMON, false, WorldConfig.richUraniumBlobSize, 7, 3, 0.14F, OCEAN, COLDTAIGA),
		new VeinDef(ModBlocks.ore_rich_zinc, ModBlocks.ore_zinc, 0, 40, UNCOMMON, false, WorldConfig.richZincBlobSize, 7, 3, 0.14F, FOREST),
	};

	public static void generateBlobs(World world, Random rand, int chunkX, int chunkZ) {
		if(!blobsActive()) return;
		boolean advanced = advancedBlobs();

		for(VeinDef def : DEFS) {
			if(rand.nextInt(advanced ? def.rarity * Math.max(1, WorldConfig.richAdvRarityMult) : def.rarity) != 0) continue;

			int x = chunkX + rand.nextInt(16);
			int z = chunkZ + rand.nextInt(16);

			BiomeGenBase biome = world.getWorldChunkManager().getBiomeGenAt(x, z);
			if(biome == null || !matches(biome, def.biomes)) continue;

			int y = def.yMin + rand.nextInt(def.yMax - def.yMin + 1);
			if(world.getBlock(x, y, z) != Blocks.stone) continue;

			if(advanced && def.cloudR > 0) fillOreCloud(world, rand, x, y, z, def);
			growVein(world, rand, x, y, z, def, advanced);
		}
	}

	// big diffuse normal-ore lens with ragged stochastic edges; the rich core overwrites its heart after
	private static void fillOreCloud(World world, Random rand, int x, int y, int z, VeinDef def) {
		int r = def.cloudR;
		int t = Math.max(2, def.cloudT);

		for(int dx = -r; dx <= r; dx++) {
			for(int dz = -r; dz <= r; dz++) {
				for(int dy = -t; dy <= t; dy++) {
					if((dx * dx + dz * dz) / (float) (r * r) + (dy * dy) / (float) (t * t) > 1.0F) continue;
					if(rand.nextFloat() > def.cloudDensity) continue;
					int px = x + dx, py = y + dy, pz = z + dz;
					if(py < 1 || py > 250) continue;
					if(!world.blockExists(px, py, pz)) continue;
					if(world.getBlock(px, py, pz) == Blocks.stone) {
						world.setBlock(px, py, pz, def.halo, 0, 2);
					}
				}
			}
		}
	}

	// grows a face-connected vein from placed neighbors; non-stone is a dead end
	private static void growVein(World world, Random rand, int x, int y, int z, VeinDef def, boolean advanced) {
		int size = Math.max(1, def.blobSize);
		int target = Math.min((size + rand.nextInt(size)) * (advanced ? 2 : 1), 512);
		int bound = advanced ? 12 : 8;
		int vRad = def.flat ? 2 : 4;
		if(advanced && def.cloudR > 0) {
			bound = Math.min(bound, def.cloudR);
			vRad = Math.min(vRad, Math.max(2, def.cloudT));
		}

		HashSet<Triplet<Integer, Integer, Integer>> seen = new HashSet<>();
		ArrayList<Triplet<Integer, Integer, Integer>> frontier = new ArrayList<>();

		Triplet<Integer, Integer, Integer> seed = new Triplet<>(x, y, z);
		seen.add(seed);
		frontier.add(seed);
		world.setBlock(x, y, z, def.rich, BlockRichOre.MAX_UNITS - 1, 2);

		int placed = 1;
		int attempts = 0;

		while(placed < target && attempts < target * 4 && !frontier.isEmpty()) {
			attempts++;

			Triplet<Integer, Integer, Integer> cur = frontier.get(rand.nextInt(frontier.size()));
			ForgeDirection dir = ForgeDirection.VALID_DIRECTIONS[rand.nextInt(6)];

			int nx = cur.getX() + dir.offsetX;
			int ny = cur.getY() + dir.offsetY;
			int nz = cur.getZ() + dir.offsetZ;

			if(ny < 1 || ny > 250) continue;
			if(Math.abs(nx - x) > bound || Math.abs(nz - z) > bound) continue;
			if(Math.abs(ny - y) > vRad) continue;
			if(advanced && def.cloudR > 0) {
				float er = def.cloudR, et = Math.max(2, def.cloudT);
				if((float) ((nx - x) * (nx - x) + (nz - z) * (nz - z)) / (er * er) + (float) ((ny - y) * (ny - y)) / (et * et) > 1.0F) continue;
			}
			if(!world.blockExists(nx, ny, nz)) continue;

			Triplet<Integer, Integer, Integer> next = new Triplet<>(nx, ny, nz);
			if(!seen.add(next)) continue;

			Block b = world.getBlock(nx, ny, nz);
			if(b == Blocks.stone || b == def.halo) {
				world.setBlock(nx, ny, nz, def.rich, BlockRichOre.MAX_UNITS - 1, 2);
				frontier.add(next);
				placed++;
			}
		}
	}

	private static boolean matches(BiomeGenBase biome, String[] list) {
		if(list.length == 0) return true;
		if(biome.biomeName == null) return false;
		String name = norm(biome.biomeName);
		for(String s : list) if(name.equals(s)) return true;
		return false;
	}

	private static String norm(String s) {
		StringBuilder sb = new StringBuilder(s.length());
		for(int i = 0; i < s.length(); i++) {
			char c = s.charAt(i);
			if(c == ' ' || c == '_') continue;
			if(c >= 'A' && c <= 'Z') c = (char) (c + 32);
			sb.append(c);
		}
		return sb.toString();
	}
}
