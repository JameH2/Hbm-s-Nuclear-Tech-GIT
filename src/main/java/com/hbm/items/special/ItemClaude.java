package com.hbm.items.special;

import java.util.List;

import com.hbm.entity.effect.EntityEMPBlast;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemClaude extends ItemMachineUpgrade {

	public ItemClaude() {
		super(UpgradeType.CLAUDE);
		this.tier = 1;
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote) {
			int x = (int) Math.floor(player.posX);
			int y = (int) Math.floor(player.posY);
			int z = (int) Math.floor(player.posZ);

			world.playSoundEffect(x, y, z, "hbm:weapon.sparkShoot", 5.0F, world.rand.nextFloat() * 0.2F + 0.9F);
			ExplosionNukeGeneric.empBlast(world, x, y, z, 50);
			EntityEMPBlast wave = new EntityEMPBlast(world, 50);
			wave.posX = x + 0.5;
			wave.posY = y + 0.5;
			wave.posZ = z + 0.5;
			world.spawnEntityInWorld(wave);

			if(!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
		}

		return stack;
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {
		return EnumRarity.epic;
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(stack, player, list, bool);
		String unloc = this.getUnlocalizedName() + ".flavor";
		String loc = I18nUtil.resolveKey(unloc);

		if(!unloc.equals(loc)) {
			String[] locs = loc.split("\\$");
			for(String s : locs) {
				list.add(s);
			}	
		}
	}
}
