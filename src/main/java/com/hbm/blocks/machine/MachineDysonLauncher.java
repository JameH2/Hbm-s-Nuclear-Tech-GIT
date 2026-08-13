package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.generic.BlockPlushie;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityDysonLauncher;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

// swarm shitter
public class MachineDysonLauncher extends BlockDummyable implements ILookOverlay, ITooltipProvider {

	public MachineDysonLauncher(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityDysonLauncher();
		if(meta == 6) return new TileEntityProxyCombo(true, false, false);
		if(meta >= 7) return new TileEntityProxyCombo(false, true, false);
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

			if(!(te instanceof TileEntityDysonLauncher))
				return false;

			TileEntityDysonLauncher launcher = (TileEntityDysonLauncher) te;

			ItemStack heldStack = player.getHeldItem();

			if(heldStack != null && heldStack.getItem() == ModItems.sat_chip) {
				if(launcher.slots[1] != null)
					return false;

				launcher.slots[1] = heldStack.copy();
				heldStack.stackSize = 0;
				world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
			} else if(heldStack == null && launcher.slots[1] != null) {
				if(player.inventory.addItemStackToInventory(launcher.slots[1].copy())) {
					launcher.slots[1] = null;
					launcher.markChanged();
					world.playSoundEffect(x, y, z, "hbm:item.upgradePlug", 1.0F, 1.0F);
				}
			}

			if(!launcher.sunsetOverdrive && heldStack != null && heldStack.getItem() instanceof ItemBlock) {
				Block block = ((ItemBlock) heldStack.getItem()).field_150939_a;
				if(block instanceof BlockPlushie && heldStack.getItemDamage() == BlockPlushie.PlushieType.FATO.ordinal()) {
					launcher.sunsetOverdrive = true;
					heldStack.stackSize--;
				}
			}
		}

		return true;
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		int sx = x;
		int sy = y;
		int sz = z;

		int ox = dir.offsetX;
		int oz = dir.offsetZ;

		x += ox * o;
		z += oz * o;

		// LEG FUCKER
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 14, 1, 0, 6, -5}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 14, 1, 0, -5, 6}, sx, sy, sz, dir)) return false;

		// BACKDISH CRAPPER
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 2, y + 7, z + oz * 2, new int[] {2, 0, 0, 3, 2, 2}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 1, y + 6, z + oz * 1, new int[] {2, 0, 0, 3, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 0, y + 5, z - oz * 0, new int[] {2, 0, 0, 3, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 1, y + 4, z - oz * 1, new int[] {2, 0, 0, 3, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 2, y + 3, z - oz * 2, new int[] {2, 0, 0, 3, 2, 2}, sx, sy, sz, dir)) return false;

		// DISH SHITTER
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {2, 0, 0, 0, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 0, 0, 2, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 5, y + 13, z + oz * 5, new int[] {2, 0, 0, 2, 10, 5}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 4, y + 12, z + oz * 4, new int[] {2, 0, 0, 2, 10, 7}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 3, y + 11, z + oz * 3, new int[] {2, 0, 0, 2, 10, 8}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 2, y + 10, z + oz * 2, new int[] {2, 0, 0, 2, 10, 9}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + ox * 1, y + 9, z + oz * 1, new int[] {2, 0, 0, 2, 10, 10}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 0, y + 8, z - oz * 0, new int[] {2, 0, 0, 2, 10, 10}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 1, y + 7, z - oz * 1, new int[] {2, 0, 0, 2, 10, 10}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 2, y + 6, z - oz * 2, new int[] {2, 0, 0, 2, 10, 10}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 3, y + 5, z - oz * 3, new int[] {2, 0, 0, 2, 10, 10}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 4, y + 4, z - oz * 4, new int[] {2, 0, 0, 2, 9, 9}, sx, sy, sz, dir)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 3, y + 3, z - oz * 3, new int[] {2, 0, 2, 0, 8, 8}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 4, y + 2, z - oz * 4, new int[] {2, 0, 2, 0, 7, 7}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 7, y + 2, z - oz * 7, new int[] {1, 0, 0, 0, 6, 6}, sx, sy, sz, dir)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, x, y + 2, z, new int[] {0, 0, 8, -8, 3, 3}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x, y + 1, z, new int[] {1, 1, 8, -8, 1, 1}, sx, sy, sz, dir)) return false;

		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 7, y, z - oz * 7, new int[] {1, 0, 0, 0, 4, 4}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 6, y, z - oz * 6, new int[] {1, 0, 0, 0, 5, 5}, sx, sy, sz, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x - ox * 5, y, z - oz * 5, new int[] {1, 0, 0, 0, 6, 6}, sx, sy, sz, dir)) return false;

		// BASE PIECE (OF ASS)
		if(!MultiblockHandlerXR.checkSpace(world, x, y, z, new int[] {3, 0, 7, 0, 2, 2}, sx, sy, sz, dir)) return false;

		return true;
	}

	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

		int ox = dir.offsetX;
		int oz = dir.offsetZ;
		int rx = rot.offsetX;
		int rz = rot.offsetZ;

		x += ox * o;
		z += oz * o;

		// LEG FUCKER
		MultiblockHandlerXR.fillSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 14, 1, 0, 6, -5}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 14, 1, 0, -5, 6}, this, dir);

		// BACKDISH CRAPPER
		MultiblockHandlerXR.fillSpace(world, x + ox * 2, y + 7, z + oz * 2, new int[] {2, 0, 0, 3, 2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 1, y + 6, z + oz * 1, new int[] {2, 0, 0, 3, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 0, y + 5, z - oz * 0, new int[] {2, 0, 0, 3, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 1, y + 4, z - oz * 1, new int[] {2, 0, 0, 3, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 2, y + 3, z - oz * 2, new int[] {2, 0, 0, 3, 2, 2}, this, dir);

		// DISH SHITTER
		MultiblockHandlerXR.fillSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {2, 0, 0, 0, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 6, y + 14, z + oz * 6, new int[] {0, 0, 0, 2, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 5, y + 13, z + oz * 5, new int[] {2, 0, 0, 2, 10, 5}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 4, y + 12, z + oz * 4, new int[] {2, 0, 0, 2, 10, 7}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 3, y + 11, z + oz * 3, new int[] {2, 0, 0, 2, 10, 8}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 2, y + 10, z + oz * 2, new int[] {2, 0, 0, 2, 10, 9}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + ox * 1, y + 9, z + oz * 1, new int[] {2, 0, 0, 2, 10, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 0, y + 8, z - oz * 0, new int[] {2, 0, 0, 2, 10, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 1, y + 7, z - oz * 1, new int[] {2, 0, 0, 2, 10, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 2, y + 6, z - oz * 2, new int[] {2, 0, 0, 2, 10, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 3, y + 5, z - oz * 3, new int[] {2, 0, 0, 2, 10, 10}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 4, y + 4, z - oz * 4, new int[] {2, 0, 0, 2, 9, 9}, this, dir);

		MultiblockHandlerXR.fillSpace(world, x - ox * 3, y + 3, z - oz * 3, new int[] {2, 0, 2, 0, 8, 8}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 4, y + 2, z - oz * 4, new int[] {2, 0, 2, 0, 7, 7}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 7, y + 2, z - oz * 7, new int[] {1, 0, 0, 0, 6, 6}, this, dir);

		MultiblockHandlerXR.fillSpace(world, x, y + 2, z, new int[] {0, 0, 8, -8, 3, 3}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 1, z, new int[] {1, 1, 8, -8, 1, 1}, this, dir);

		MultiblockHandlerXR.fillSpace(world, x - ox * 7, y, z - oz * 7, new int[] {1, 0, 0, 0, 4, 4}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 6, y, z - oz * 6, new int[] {1, 0, 0, 0, 5, 5}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - ox * 5, y, z - oz * 5, new int[] {1, 0, 0, 0, 6, 6}, this, dir);

		// BASE PIECE (OF ASS)
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, 0, 7, 0, 2, 2}, this, dir);


		// and finally, extra metas
		makeExtra(world, x - ox * 0 - rx * 2, y, z - oz * 0 - rz * 2);
		makeExtra(world, x - ox * 1 - rx * 2, y, z - oz * 1 - rz * 2);
		makeExtra(world, x - ox * 2 - rx * 2, y, z - oz * 2 - rz * 2);
		makeExtra(world, x - ox * 3 - rx * 2, y, z - oz * 3 - rz * 2);
		makeExtra(world, x - ox * 4 - rx * 2, y, z - oz * 4 - rz * 2);

		makeExtra(world, x - ox * 8, y, z - oz * 8);
	}

	@Override
	public int[] getDimensions() {
		// just the major offsets, for placing on walls and ceilings (rare)
		return new int[] {16, 0, 8, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityDysonLauncher)) return;

		TileEntityDysonLauncher launcher = (TileEntityDysonLauncher) te;

		List<String> text = new ArrayList<String>();

		if(launcher.swarmId > 0) {
			text.add("ID: " + launcher.swarmId);
			text.add("Swarm: " + launcher.swarmCount + " members");
			text.add((launcher.power < TileEntityDysonLauncher.MAX_POWER ? EnumChatFormatting.RED : EnumChatFormatting.GREEN) + "Power: " + BobMathUtil.getShortNumber(launcher.power) + "HE");
		} else {
			text.add("No Satellite ID-Chip installed!");
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		addStandardInfo(stack, player, list, ext);
	}

}
