package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.tileentity.machine.TileEntityConveyorCombinator;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.common.util.ForgeDirection;

public class MachineConveyorCombinator extends BlockDummyable implements IConveyorBelt, ILookOverlay, IToolable, ITooltipProvider {

	public MachineConveyorCombinator(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityConveyorCombinator();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;
		if(world.isRemote) return true;

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null) return false;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityConveyorCombinator)) return false;

		TileEntityConveyorCombinator combinator = (TileEntityConveyorCombinator) te;

		combinator.mode = combinator.mode == TileEntityConveyorCombinator.MODE_3x3 ? TileEntityConveyorCombinator.MODE_2x2 : TileEntityConveyorCombinator.MODE_3x3;
		combinator.markChanged();
		combinator.networkPackNT(50);

		world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "hbm:item.upgradePlug", 1.0F, 1.0F);

		return true;
	}

	@Override
	public Vec3 getTravelLocation(World world, int x, int y, int z, Vec3 itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3 snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		Vec3 dest = Vec3.createVectorHelper(snap.xCoord - dir.offsetX * speed, snap.yCoord - dir.offsetY * speed, snap.zCoord - dir.offsetZ * speed);
		Vec3 motion = Vec3.createVectorHelper((dest.xCoord - itemPos.xCoord), (dest.yCoord - itemPos.yCoord), (dest.zCoord - itemPos.zCoord));
		double len = motion.lengthVector();
		return Vec3.createVectorHelper(itemPos.xCoord + motion.xCoord / len * speed, itemPos.yCoord + motion.yCoord / len * speed, itemPos.zCoord + motion.zCoord / len * speed);
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3 itemPos) {
		int meta = world.getBlockMetadata(x, y, z) - offset;
		return ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP);
	}

	@Override
	public Vec3 getClosestSnappingPosition(World world, int x, int y, int z, Vec3 itemPos) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		itemPos.xCoord = MathHelper.clamp_double(itemPos.xCoord, x, x + 1);
		itemPos.zCoord = MathHelper.clamp_double(itemPos.zCoord, z, z + 1);
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemPos.xCoord;
		if(dir.offsetZ != 0) posZ = itemPos.zCoord;
		return Vec3.createVectorHelper(posX, y + 0.25, posZ);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3 itemPos) {
		return world.getBlock(x, y, z) == this;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);

		if(!(te instanceof TileEntityConveyorCombinator)) return;

		TileEntityConveyorCombinator combinator = (TileEntityConveyorCombinator) te;
		List<String> text = new ArrayList();

		String mode = combinator.mode == TileEntityConveyorCombinator.MODE_2x2 ? "tile.machine_conveyor_combinator.mode2x2" : "tile.machine_conveyor_combinator.mode3x3";
		text.add(I18nUtil.resolveKey("tile.machine_conveyor_combinator.mode") + ": " + I18nUtil.resolveKey(mode));

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}
