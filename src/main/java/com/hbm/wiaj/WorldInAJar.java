package com.hbm.wiaj;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A hastily put together implementation of IBlockAccess in order to render things using ISBRH...
 * It can handle blocks, and not a whole lot else.
 * @author hbm
 */
public class WorldInAJar implements IBlockAccess {

	public int sizeX;
	public int sizeY;
	public int sizeZ;

	// when ripped from the world, the origin position of the original blocks
	public int posX;
	public int posY;
	public int posZ;

	public int lightlevel = 15 << 20;

	// should blocks below the top level have a reduced light level?
	public boolean dimLowerBlocks = false;

	private Block[][][] blocks;
	private short[][][] meta;
	private TileEntity[][][] tiles;

	public WorldInAJar(ByteBuf buf) {
		deserialize(buf);
	}

	public WorldInAJar(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

	public WorldInAJar(int x, int y, int z) {
		this.sizeX = x;
		this.sizeY = y;
		this.sizeZ = z;

		this.blocks = new Block[x][y][z];
		this.meta = new short[x][y][z];
		this.tiles = new TileEntity[x][y][z];
	}

	public WorldInAJar(int x1, int y1, int z1, int x2, int y2, int z2) {
		this(Math.abs(x2 - x1), Math.abs(y2 - y1), Math.abs(z2 - z1));
	}

	public void nuke() {

		this.blocks = new Block[sizeX][sizeY][sizeZ];
		this.meta = new short[sizeX][sizeY][sizeZ];
		this.tiles = new TileEntity[sizeX][sizeY][sizeZ];
	}

	private static WorldInAJar eatBlocks(World world, int x1, int y1, int z1, int x2, int y2, int z2, boolean consume, boolean blob) {
		WorldInAJar wiaj = new WorldInAJar(x1, y1, z1, x2, y2, z2);

		int minX = Math.min(x1, x2);
		int minY = Math.min(y1, y2);
		int minZ = Math.min(z1, z2);
		int maxX = Math.max(x1, x2);
		int maxY = Math.max(y1, y2);
		int maxZ = Math.max(z1, z2);

		wiaj.posX = minX;
		wiaj.posY = minY;
		wiaj.posZ = minZ;

		float radius = Math.max(wiaj.sizeX, Math.max(wiaj.sizeY, wiaj.sizeZ)) / 2;
		float radiusSqr = radius * radius;

		for(int x = minX; x <= maxX; x++)
		for(int y = minY; y <= maxY; y++)
		for(int z = minZ; z <= maxZ; z++) {
			if(blob) {
				float rx = x - minX - wiaj.sizeX / 2;
				float ry = y - minY - wiaj.sizeY / 2;
				float rz = z - minZ - wiaj.sizeZ / 2;

				if(ry < 0 && rx * rx + ry * ry + rz * rz > radiusSqr) continue;
			}

			wiaj.setBlock(x - minX, y - minY, z - minZ, world.getBlock(x, y, z), world.getBlockMetadata(x, y, z));
			if(consume) world.setBlockToAir(x, y, z);
		}

		return wiaj;
	}

	// chained world eater
	public static WorldInAJar munch(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		return eatBlocks(world, x1, y1, z1, x2, y2, z2, true, false);
	}

	public static WorldInAJar munchBlob(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		return eatBlocks(world, x1, y1, z1, x2, y2, z2, true, true);
	}

	// world eater in chains
	public static WorldInAJar repro(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		return eatBlocks(world, x1, y1, z1, x2, y2, z2, false, false);
	}

	private RenderBlocks renderer;

	// our disaster, our creation
	public void render() {
		if(renderer == null) {
			renderer = new RenderBlocks(this);
			renderer.enableAO = true;
		}

		GL11.glColor3f(1, 1, 1);
		RenderHelper.disableStandardItemLighting();

		Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		Tessellator.instance.startDrawingQuads();

		for(int ix = 0; ix < sizeX; ix++) {
			for(int iy = 0; iy < sizeY; iy++) {
				for(int iz = 0; iz < sizeZ; iz++) {
					try { renderer.renderBlockByRenderType(getBlock(ix, iy, iz), ix, iy, iz); } catch(Exception ex) { }
				}
			}
		}

		Tessellator.instance.draw();
		GL11.glShadeModel(GL11.GL_FLAT);
	}

	// nothing left, but remains

	@Override
	public Block getBlock(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return Blocks.air;

		return this.blocks[x][y][z] != null ? this.blocks[x][y][z] : Blocks.air;
	}

	public void setBlock(int x, int y, int z, Block b, int meta) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return;

		this.blocks[x][y][z] = b;
		this.meta[x][y][z] = (short)meta;
	}

	@Override
	public int getBlockMetadata(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return 0;

		return this.meta[x][y][z];
	}

	//shaky, we may kick tile entities entirely and rely on outside-the-world tile actors for rendering
	//might still come in handy for manipulating things using dummy tiles, like cable connections
	@Override
	public TileEntity getTileEntity(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return null;

		return this.tiles[x][y][z];
	}

	public void setTileEntity(int x, int y, int z, TileEntity tile) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return;

		this.tiles[x][y][z] = tile;
	}

	//always render fullbright, if the situation requires it we could add a very rudimentary system that
	//darkens blocks if there is a solid one above
	@Override
	@SideOnly(Side.CLIENT)
	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int blockBrightness) {
		if(dimLowerBlocks) return Math.max(lightlevel - ((sizeY - y - 1) << 20), 0);
		return lightlevel;
	}

	//redstone could theoretically be implemented, but we will wait for now
	@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int dir) {
		return 0;
	}

	@Override
	public boolean isAirBlock(int x, int y, int z) {
		return this.getBlock(x, y, z).isAir(this, x, y, z);
	}

	//biomes don't matter to us, if the situation requires it we could implement a primitive biome mask
	@Override
	@SideOnly(Side.CLIENT)
	public BiomeGenBase getBiomeGenForCoords(int x, int z) {
		return BiomeGenBase.plains;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getHeight() {
		return this.sizeY;
	}

	@Override
	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
		return getBlock(x, y, z).isSideSolid(this, x, y, z, side);
	}

	public void readFromNBT(NBTTagCompound nbt) {
		if(!nbt.hasKey("blocks")) {
			sizeX = sizeY = sizeZ = 0;
			blocks = new Block[0][0][0];
			meta = new short[0][0][0];
			tiles = new TileEntity[0][0][0];
			return;
		}

		sizeX = nbt.getInteger("sizeX");
		sizeY = nbt.getInteger("sizeY");
		sizeZ = nbt.getInteger("sizeZ");

		posX = nbt.getInteger("posX");
		posY = nbt.getInteger("posY");
		posZ = nbt.getInteger("posZ");

		int[] blockIds = nbt.getIntArray("blocks");
		int[] metaIds = nbt.getIntArray("data");

		blocks = new Block[sizeX][sizeY][sizeZ];
		meta = new short[sizeX][sizeY][sizeZ];
		tiles = new TileEntity[sizeX][sizeY][sizeZ];

		int i = 0;

		for(int x = 0; x < sizeX; x++)
		for(int y = 0; y < sizeY; y++)
		for(int z = 0; z < sizeZ; z++) {
			blocks[x][y][z] = Block.getBlockById(blockIds[i]);
			meta[x][y][z] = (short)metaIds[i];

			// no tiles yet, yabba dabba doo

			i++;
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("sizeX", sizeX);
		nbt.setInteger("sizeY", sizeY);
		nbt.setInteger("sizeZ", sizeZ);

		nbt.setInteger("posX", posX);
		nbt.setInteger("posY", posY);
		nbt.setInteger("posZ", posZ);

		int size = sizeX * sizeY * sizeZ;
		int[] blockIds = new int[size];
		int[] metaIds = new int[size];

		int i = 0;

		for(int x = 0; x < sizeX; x++)
		for(int y = 0; y < sizeY; y++)
		for(int z = 0; z < sizeZ; z++) {
			blockIds[i] = Block.getIdFromBlock(blocks[x][y][z]);
			metaIds[i] = meta[x][y][z];

			// no tiles yet, eeby deeby

			i++;
		}

		nbt.setIntArray("blocks", blockIds);
		nbt.setIntArray("data", metaIds);
	}

	public void serialize(ByteBuf buf) {
		buf.writeShort(sizeX);
		buf.writeShort(sizeY);
		buf.writeShort(sizeZ);

		buf.writeInt(posX);
		buf.writeInt(posY);
		buf.writeInt(posZ);

		for(int x = 0; x < sizeX; x++)
		for(int y = 0; y < sizeY; y++)
		for(int z = 0; z < sizeZ; z++) {
			buf.writeInt(Block.getIdFromBlock(blocks[x][y][z]));
			buf.writeShort(meta[x][y][z]);
		}
	}

	public void deserialize(ByteBuf buf) {
		sizeX = buf.readShort();
		sizeY = buf.readShort();
		sizeZ = buf.readShort();

		posX = buf.readInt();
		posY = buf.readInt();
		posZ = buf.readInt();

		blocks = new Block[sizeX][sizeY][sizeZ];
		meta = new short[sizeX][sizeY][sizeZ];
		tiles = new TileEntity[sizeX][sizeY][sizeZ];

		for(int x = 0; x < sizeX; x++)
		for(int y = 0; y < sizeY; y++)
		for(int z = 0; z < sizeZ; z++) {
			blocks[x][y][z] = Block.getBlockById(buf.readInt());
			meta[x][y][z] = buf.readShort();
		}
	}

}
