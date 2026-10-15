package com.hbm.blocks.generic;

import com.hbm.inventory.gui.GUIScreenBobble;
import com.hbm.items.special.ItemPlasticScrap.ScrapType;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.world.gen.nbt.INBTTileEntityTransformable;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityNoteFX;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockBobble extends BlockContainer implements IGUIProvider, INBTBlockTransformable {

	public BlockBobble() {
		super(Material.iron);
	}

	@Override
	public int getRenderType() {
		return -1;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public Item getItemDropped(int i, Random rand, int j) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z, EntityPlayer player) {

		TileEntityBobble entity = (TileEntityBobble) world.getTileEntity(x, y, z);

		if(entity != null) {
			return new ItemStack(this, 1, entity.type.ordinal());
		}

		return super.getPickBlock(target, world, x, y, z, player);
	}

	@Override
	public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			if(!world.isRemote) {
				TileEntityBobble entity = (TileEntityBobble) world.getTileEntity(x, y, z);
				if(entity != null) {
					EntityItem item = new EntityItem(world, x + 0.5, y, z + 0.5, new ItemStack(this, 1, entity.type.ordinal()));
					item.motionX = 0;
					item.motionY = 0;
					item.motionZ = 0;
					world.spawnEntityInWorld(item);
				}
			}
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int meta) {
		player.addStat(StatList.mineBlockStatArray[getIdFromBlock(this)], 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;

		} else {
			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for(int i = 1; i < BobbleType.values().length; i++)
			list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack stack) {
		int meta = MathHelper.floor_double((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
		world.setBlockMetadataWithNotify(x, y, z, meta, 2);

		TileEntityBobble bobble = (TileEntityBobble) world.getTileEntity(x, y, z);
		bobble.type = BobbleType.values()[Math.abs(stack.getItemDamage()) % BobbleType.values().length];
		bobble.markDirty();
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		float f = 0.0625F;
		this.setBlockBounds(5.5F * f, 0.0F, 5.5F * f, 1.0F - 5.5F * f, 0.625F, 1.0F - 5.5F * f);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		this.setBlockBoundsBasedOnState(world, x, y, z);
		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY, z + this.maxZ);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
		TileEntityBobble te = (TileEntityBobble) world.getTileEntity(x, y, z);
		if(te != null && te.type == BobbleType.SOWTH) {
			if(rand.nextInt(2) == 0) return;
			double noteX = x + 0.3 + rand.nextDouble() * 0.4;
			double noteZ = z + 0.3 + rand.nextDouble() * 0.4;
			double noteY = y + rand.nextDouble() * 0.4;
			EntityNoteFX fx = new EntityNoteFX(world, noteX, noteY, noteZ, rand.nextDouble(), -10.0, 0.0, 0.5F);
			Minecraft.getMinecraft().effectRenderer.addEffect(fx);
		}
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return (meta + coordBaseMode * 4) % 16;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBobble();
	}

	public static class TileEntityBobble extends TileEntity implements INBTTileEntityTransformable {

		public BobbleType type = BobbleType.NONE;

		@Override
		public boolean canUpdate() {
			return false;
		}

		@Override
		public Packet getDescriptionPacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.func_148857_g());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.type = BobbleType.values()[Math.abs(nbt.getByte("type")) % BobbleType.values().length];
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setByte("type", (byte) type.ordinal());
		}

		@Override
		public void transformTE(World world, int coordBaseMode) {
			type = BobbleType.values()[world.rand.nextInt(BobbleType.values().length - 1) + 1];
		}
	}

	public static enum BobbleType {

		NONE(			"null",								"null",			null,														null,																								false,	ScrapType.BOARD_BLANK),
		STRENGTH(		"Strength",							"Strength",		null,														"bobble.strength.contribution",													false,	ScrapType.BRIDGE_BIOS),
		PERCEPTION(		"Perception",						"Perception",	null,														"bobble.perception.contribution",												false,	ScrapType.BRIDGE_NORTH),
		ENDURANCE(		"Endurance",						"Endurance",	null,														"bobble.endurance.contribution",														false,	ScrapType.BRIDGE_SOUTH),
		CHARISMA(		"Charisma",							"Charisma",		null,														"bobble.charisma.contribution",														false,	ScrapType.BRIDGE_IO),
		INTELLIGENCE(	"Intelligence",						"Intelligence",	null,														"bobble.intelligence.contribution",						false,	ScrapType.BRIDGE_BUS),
		AGILITY(		"Agility",							"Agility",		null,														"bobble.agility.contribution",													false,	ScrapType.BRIDGE_CHIPSET),
		LUCK(			"Luck",								"Luck",			null,														"bobble.luck.contribution",																false,	ScrapType.BRIDGE_CMOS),
		BOB(			"Robert \"The Bobcat\" Katzinsky",	"HbMinecraft",	"bobble.bob.contribution",									"bobble.bob.inscription" + System.getProperty("user.name"),										false,	ScrapType.CPU_SOCKET),
		FRIZZLE(		"Frooz",							"Frooz",		"bobble.frizzle.contribution",											"bobble.frizzle.inscription",																					true,	ScrapType.CPU_CLOCK),
		PU238(			"Pu-238",							"Pu-238",		"bobble.pu238.contribution",							null,																								false,	ScrapType.CPU_REGISTER),
		VT(				"VT-6/24",							"VT-6/24",		"bobble.vt.contribution",			"bobble.vt.inscription",																		true,	ScrapType.CPU_EXT),
		DOC(			"The Doctor",						"Doctor17PH",	"bobble.doc.contribution",						"bobble.doc.inscription",														true,	ScrapType.CPU_CACHE),
		BLUEHAT(		"The Blue Hat",						"The Blue Hat",	"bobble.bluehat.contribution",													"bobble.bluehat.inscription",													true,	ScrapType.MEM_16K_A),
		PHEO(			"Pheo",								"Pheonix",		"bobble.pheo.contribution",	"bobble.pheo.inscription",						true,	ScrapType.MEM_16K_B),
		ADAM29(			"Adam29",							"Adam29",		"bobble.adam29.contribution",							"bobble.adam29.inscription",	true,	ScrapType.MEM_16K_C),
		UFFR(			"UFFR",								"UFFR",			"bobble.uffr.contribution",							"bobble.uffr.inscription",																						false,	ScrapType.MEM_SOCKET),
		VAER(			"vaer",								"vaer",			"bobble.vaer.contribution",													"bobble.vaer.inscription",											true,	ScrapType.MEM_16K_D),
		NOS(			"Dr Nostalgia",						"Dr Nostalgia",	"bobble.nos.contribution",									"bobble.nos.inscription",					true,	ScrapType.BOARD_TRANSISTOR),
		DRILLGON(		"Drillgon200",						"Drillgon200",	"bobble.drillgon.contribution",												null,																								false,	ScrapType.CPU_LOGIC),
		CIRNO(			"Cirno",							"Cirno",		"bobble.cirno.contribution",						"bobble.cirno.inscription",																			true,	ScrapType.BOARD_BLANK),
		GWEN(			"Gwen",								"Gwen",			"bobble.gwen.contribution",											"bobble.gwen.inscription",																					true,	ScrapType.BOARD_BLANK),
		JUICE(			"Juicy_Lad",						"Juicy_Lad",	"bobble.juice.contribution",					"bobble.juice.inscription",								true,	ScrapType.BOARD_BLANK),
		JAMESH_2(		"JamesH_2",							"JamesH_2",		"bobble.jamesh_2.contribution",										"bobble.jamesh_2.inscription",																						true,	ScrapType.BOARD_BLANK),
		PEEP(			"Peep",								"LePeeperSauvage",	"bobble.peep.contribution",											"bobble.peep.inscription",											true,	ScrapType.CPU_CLOCK),
		MICROWAVE(		"Microwave",						"Microwave",		"bobble.microwave.contribution",		"bobble.microwave.inscription",                                                                    true, ScrapType.BRIDGE_BIOS),
		MELLOW(			"MELLOWARPEGGIATION",				"Mellow",			"bobble.mellow.contribution",						"bobble.mellow.inscription",												true,	ScrapType.CARD_PROCESSOR),
		MRKIMKIMORA(	"MrKimkimora",						"MrKimkimora",		"bobble.mrkimkimora.contribution",						"bobble.mrkimkimora.inscription",												false,	ScrapType.BOARD_BLANK),
		ABEL(			"Abel1502", 						"Abel1502", 		"bobble.abel.contribution", 	"bobble.abel.inscription",																				true,	ScrapType.CPU_REGISTER),
		BUFKA(			"Bufka2011",	                    "Bufka2011",	    "bobble.bufka.contribution",									     "bobble.bufka.inscription",										                                        true,	ScrapType.CPU_SOCKET),
		SOWTH(			"Sowth",	                        "Comosellama8098",	"bobble.sowth.contribution",							     "bobble.sowth.inscription",										                            true,	ScrapType.CPU_SOCKET),
		DVIVYN(			"Dvivyn",	                        "bablodima228",	    "bobble.dvivyn.contribution",	 "bobble.dvivyn.inscription",						    true,	ScrapType.CPU_SOCKET),
		FELIX(			"Felix228_1",	                    "Shamans_Jackal_2000",	    "bobble.felix.contribution",			 "bobble.felix.inscription",										                                            true,	ScrapType.CPU_SOCKET),
		ANIVIA(			"AniviaTai",	                    "AniviaFlome",	    "bobble.anivia.contribution",							         "bobble.anivia.inscription",										                            true,	ScrapType.CPU_SOCKET),
		SKIPPY(			"_SkippyPlaysMc_",	                "Skippy",	        "bobble.skippy.contribution",			 "balls",										                                                    true,	ScrapType.CPU_SOCKET),
		VITYA2127(		"Vitya2127",						"Vitya2127",		"bobble.vitya2127.contribution",				 "bobble.vitya2127.inscription",																						false,	ScrapType.BOARD_BLANK);


		public String name;			//the title of the tooltip
		public String label;		//the name engraved in the socket
		public String contribution;	//what contributions this person has made, if applicable
		public String inscription;	//the flavor text
		public boolean skinLayers;
		public ScrapType scrap;

		private BobbleType(String name, String label, String contribution, String inscription, boolean layers, ScrapType scrap) {
			this.name = name;
			this.label = label;
			this.contribution = contribution;
			this.inscription = inscription;
			this.skinLayers = layers;
			this.scrap = scrap;
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenBobble((TileEntityBobble) world.getTileEntity(x, y, z));
	}
}
