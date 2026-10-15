package com.hbm.items.tool;

import java.util.List;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectWeapon;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.inventory.gui.GUIScreenPager;
import com.hbm.items.IItemControlReceiver;
import com.hbm.main.MainRegistry;
import com.hbm.main.ServerProxy;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.RTTYSystem;
import com.hbm.tileentity.network.RTTYSystem.RTTYChannel;
import com.hbm.util.BaublesCompat;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemRTTYPager extends Item implements IItemControlReceiver, IGUIProvider, IBauble {

	public static final String KEY_CHANNEL = "chan";

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean held) {
		tickPager(stack, world, entity, slot);
	}

	private void tickPager(ItemStack stack, World world, Entity entity, int slot) {
		if(!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(KEY_CHANNEL)) return;
		if(!(entity instanceof EntityPlayerMP) || world.isRemote) return;

		String channelFreq = stack.stackTagCompound.getString(KEY_CHANNEL);
		RTTYChannel chan = RTTYSystem.listen(world, channelFreq);

		if(chan != null && chan.timeStamp >= world.getTotalWorldTime() - 1) {
			int alive = entity.ticksExisted % 1000;
			String message = EnumChatFormatting.GOLD + "[ " + channelFreq + " (" + alive + ") ] " + EnumChatFormatting.YELLOW + chan.signal;

			if("selfdestruct".equals(chan.signal + "")) {
				ExplosionVNT vnt = new ExplosionVNT(world, entity.posX, entity.posY + entity.height / 2, entity.posZ, 5, null);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 50).setupPiercing(5F, 0.5F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
				vnt.explode();
				stack.stackSize--;
				return;
			}

			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(message, ServerProxy.ID_PAGER_DYN + slot, 5_000), (EntityPlayerMP) entity);
		}
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) { return BaubleType.BELT; }

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		int slot = entity instanceof EntityPlayer ? BaublesCompat.getCombinedSlot((EntityPlayer) entity, stack) : 0;
		tickPager(stack, entity.worldObj, entity, slot);
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity) { return true; }

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity) { return true; }

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		if(!stack.hasTagCompound() || !stack.stackTagCompound.hasKey(KEY_CHANNEL) || stack.stackTagCompound.getString(KEY_CHANNEL).isEmpty()) {
			list.add(EnumChatFormatting.RED + I18nUtil.resolveKey("desc.item.rtty_pager.no_channel"));
		} else {
			list.add(EnumChatFormatting.YELLOW + I18nUtil.resolveKey("item.rtty_pager.desc2", stack.stackTagCompound.getString(KEY_CHANNEL)));
		}
	}

	@Override
	public void receiveControl(EntityPlayer player, ItemStack stack, NBTTagCompound data) {
		if(data.hasKey("chan")) {
			if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
			stack.stackTagCompound.setString(KEY_CHANNEL, data.getString(KEY_CHANNEL));
		}
	}

	@Override public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) { return null; }
	@Override public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) { return new GUIScreenPager(player.getHeldItem()); }
}
