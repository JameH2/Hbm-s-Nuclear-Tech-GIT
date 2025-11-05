package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.inventory.recipes.loader.GenericRecipes;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemBlueprintFolder extends Item {

	@SideOnly(Side.CLIENT) protected IIcon iconDiscover;
	@SideOnly(Side.CLIENT) protected IIcon iconSecret;
	@SideOnly(Side.CLIENT) protected IIcon iconWar;

	public ItemBlueprintFolder() {
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister reg) {
		super.registerIcons(reg);
		this.iconDiscover = reg.registerIcon(this.getIconString() + "_discover");
		this.iconSecret = reg.registerIcon(this.getIconString() + "_secret");
		this.iconWar = reg.registerIcon(this.getIconString() + "_war");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIconFromDamage(int meta) {
		if(meta == 1) return iconDiscover;
		if(meta == 2) return iconSecret;
		if(meta == 3) return iconWar;
		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		for(int i = 0; i < 4; i++) {
			if(i == 2) continue; // hidden scret
			list.add(new ItemStack(item, 1, i));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(world.isRemote) return stack;

		List<String> pools = new ArrayList();

		for(String pool : GenericRecipes.blueprintPools.keySet()) {
			if(stack.getItemDamage() == 0 && pool.startsWith(GenericRecipes.POOL_PREFIX_ALT)) pools.add(pool);
			if(stack.getItemDamage() == 1 && pool.startsWith(GenericRecipes.POOL_PREFIX_DISCOVER)) pools.add(pool);
			if(stack.getItemDamage() == 2 && pool.startsWith(GenericRecipes.POOL_PREFIX_SECRET)) pools.add(pool);
			if(stack.getItemDamage() == 3 && pool.startsWith(GenericRecipes.POOL_PREFIX_WAR)) pools.add(pool);
		}

		if(!pools.isEmpty()) {
			stack.stackSize--;

			String chosen = pools.get(player.getRNG().nextInt(pools.size()));
			ItemStack blueprint = ItemBlueprints.make(chosen);

			if(!player.inventory.addItemStackToInventory(blueprint))
				player.dropPlayerItemWithRandomChoice(blueprint, false);
		}

		return stack;
	}
}
