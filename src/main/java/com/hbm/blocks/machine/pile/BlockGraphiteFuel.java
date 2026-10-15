package com.hbm.blocks.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.pile.TileEntityPileFuel;

import api.hbm.block.IBlowable;
import api.hbm.block.IToolable;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraft.util.ChatComponentTranslation;

@Deprecated
public class BlockGraphiteFuel extends BlockGraphiteDrilledTE implements IToolable, IBlowable {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityPileFuel pile = new TileEntityPileFuel();
		if((meta & 8) != 0)
			pile.progress = pile.maxProgress - 1000; // pu239 rods cringe :(

		return pile;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.blockIconAluminum = iconRegister.registerIcon(RefStrings.MODID + ":block_graphite_fuel_aluminum");
	}

	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}

	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		TileEntityPileFuel pile = (TileEntityPileFuel)world.getTileEntity(x, y, z);
		return MathHelper.clamp_int((pile.progress * 15) / (pile.maxProgress - 1000), 0, 15);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(!world.isRemote) {

			int meta = world.getBlockMetadata(x, y, z);

			if(tool == ToolType.SCREWDRIVER) {

				int cfg = meta & 3;

				if(side == cfg * 2 || side == cfg * 2 + 1) {
					world.setBlock(x, y, z, ModBlocks.block_graphite_drilled, meta & 7, 3);
					this.ejectItem(world, x, y, z, ForgeDirection.getOrientation(side), new ItemStack(getInsertedItem(meta)));
				}
			}

			if(tool == ToolType.HAND_DRILL) {
				TileEntityPileFuel pile = (TileEntityPileFuel) world.getTileEntity(x, y, z);
				player.addChatComponentMessage(new ChatComponentTranslation("chat.graphite_fuel.cp1_fuel_assembly", x, y, z).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.GOLD)));
				player.addChatComponentMessage(new ChatComponentTranslation("chat.graphite_fuel.heat", pile.heat, pile.maxHeat).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
				player.addChatComponentMessage(new ChatComponentTranslation("chat.graphite_fuel.depletion", pile.progress, pile.maxProgress).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
				player.addChatComponentMessage(new ChatComponentTranslation("chat.graphite_fuel.flux", pile.lastNeutrons).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
				if((meta & 8) == 8)
					player.addChatComponentMessage(new ChatComponentTranslation("chat.graphite_fuel.pu_239_rich").setChatStyle(new ChatStyle().setColor(EnumChatFormatting.DARK_GREEN)));
			}
		}

		return true;
	}

	@Override
	protected Item getInsertedItem(int meta) {
		return (meta & 8) == 8 ? ModItems.pile_rod_pu239 : ModItems.pile_rod_uranium;
	}

	@Override
	public void applyFan(World world, int x, int y, int z, ForgeDirection dir, int dist) {
		TileEntityPileFuel pile = (TileEntityPileFuel) world.getTileEntity(x, y, z);
		pile.heat -= pile.heat * 0.025;
	}
}
