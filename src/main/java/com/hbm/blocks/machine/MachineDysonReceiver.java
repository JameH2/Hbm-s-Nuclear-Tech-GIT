package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonReceiver;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineDysonReceiver extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineDysonReceiver(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityDysonReceiver();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, false);
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

			if(!(te instanceof TileEntityDysonReceiver))
				return false;

			TileEntityDysonReceiver receiver = (TileEntityDysonReceiver) te;

			ItemStack heldStack = player.getHeldItem();

			if(heldStack != null && heldStack.getItem() == ModItems.sat_chip) {
				if(receiver.slots[0] != null)
					return false;

				receiver.slots[0] = heldStack.copy();
				receiver.markChanged();
				heldStack.stackSize = 0;
				world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
			} else if(heldStack == null && receiver.slots[0] != null) {
				if(player.inventory.addItemStackToInventory(receiver.slots[0].copy())) {
					receiver.slots[0] = null;
					receiver.markChanged();
					world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
				}
			}
		}

		return true;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 4, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x += dir.offsetX * o;
		z += dir.offsetZ * o;

		// Main structure
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {1, 0, 8, -4, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 4, 0, 1, 1}, this, dir);

		// Dish
		MultiblockHandlerXR.fillSpace(world, x, y + 10, z, new int[] {1, 0, 6, 6, 6, 6}, this, dir);

		// Tower
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {17, 0, 1, 1, 1, 1}, this, dir);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityDysonReceiver)) return;

		TileEntityDysonReceiver receiver = (TileEntityDysonReceiver) te;

		long energyOutput = 0;
		if(receiver.isReceiving) {
			energyOutput = TileEntityDysonReceiver.getEnergyOutput(receiver.swarmCount) / receiver.swarmConsumers * 20;
		}

		List<String> text = new ArrayList<String>();

		if(receiver.swarmId > 0) {
			text.add(I18nUtil.resolveKey("overlay.dyson_receiver.id", receiver.swarmId));
			text.add(I18nUtil.resolveKey("overlay.dyson_receiver.swarm_members", receiver.swarmCount));
			text.add(I18nUtil.resolveKey("overlay.dyson_receiver.consumers_consumers", receiver.swarmConsumers));
			text.add(I18nUtil.resolveKey("overlay.dyson_receiver.power_he", BobMathUtil.getShortNumber(energyOutput)));
		} else {
			text.add(I18nUtil.resolveKey("overlay.dyson_receiver.no_satellite_id_chip_installed"));
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
