package com.hbm.items.tool;

import java.util.List;

import com.hbm.entity.logic.EntityBomber;
import com.hbm.lib.Library;
import com.hbm.main.NTMSounds;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import com.hbm.world.WorldUtil;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraft.util.ChatComponentTranslation;

public class ItemBombCaller extends Item {

	public ItemBombCaller() {
		super();
		this.setHasSubtypes(true);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		list.add(I18nUtil.resolveKey("item.bomb_caller.desc1"));

		switch (stack.getItemDamage()) {
			case 0: list.add(I18nUtil.resolveKey("item.bomb_caller.desc2")); break;
			case 1: list.add(I18nUtil.resolveKey("item.bomb_caller.desc3")); break;
			case 2: list.add(I18nUtil.resolveKey("item.bomb_caller.desc4")); break;
			case 3: list.add(I18nUtil.resolveKey("item.bomb_caller.desc5")); break;
			case 4: list.add(I18nUtil.resolveKey("item.bomb_caller.desc6")); break;
			case 5: list.add(I18nUtil.resolveKey("item.bomb_caller.desc7")); break;
			case 6: list.add(I18nUtil.resolveKey("item.bomb_caller.desc8")); break;
			case 7: list.add(I18nUtil.resolveKey("item.bomb_caller.desc9")); break;
			case 8: list.add(I18nUtil.resolveKey("item.bomb_caller.desc10")); break;
			default: list.add(I18nUtil.resolveKey("item.bomb_caller.desc11"));

		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player)
	{
		MovingObjectPosition pos = Library.rayTrace(player, 500, 1);
		int x = pos.blockX;
		int y = pos.blockY;
		int z = pos.blockZ;

		boolean b2 = false;

		if(!world.isRemote) {
			EntityBomber bomber;
			switch(stack.getItemDamage()) {

				case 1: bomber = EntityBomber.statFacNapalm(world, x, y, z); break;
				case 2: bomber = EntityBomber.statFacChlorine(world, x, y, z); break;
				case 3: bomber = EntityBomber.statFacOrange(world, x, y, z); break;
				case 4: bomber = EntityBomber.statFacABomb(world, x, y, z); break;
				case 5: bomber = EntityBomber.statFacStinger(world, x, y, z); break;
				case 6: bomber = EntityBomber.statFacBoxcar(world, x, y, z); break;
				case 7: bomber = EntityBomber.statFacPC(world, x, y, z); break;
				case 8: bomber = EntityBomber.statFacCV(world, x, y, z);
				b2 = true; break;
				default: bomber = EntityBomber.statFacCarpet(world, x, y, z);

			}
			WorldUtil.loadAndSpawnEntityInWorld(bomber);
			world.playSoundAtEntity(player, NTMSounds.TECH_BLEEP, 1.0F, 1.0F);

			if(b2) {
				player.addChatMessage(new ChatComponentTranslation("chat.bomb_caller.rerouted_civilian_traffic"));
			} else {
				player.addChatMessage(new ChatComponentTranslation("chat.bomb_caller.called_airstrike"));
			}

		}

		stack.stackSize -= 1;

		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, 0));
		list.add(new ItemStack(item, 1, 1));
		list.add(new ItemStack(item, 1, 2));
		list.add(new ItemStack(item, 1, 3));
		list.add(new ItemStack(item, 1, 4));
		list.add(new ItemStack(item, 1, 8));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack stack) {
		return stack.getItemDamage() >= 4;
	}
}
