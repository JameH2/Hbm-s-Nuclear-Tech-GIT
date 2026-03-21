package com.hbm.items.armor;

import java.util.UUID;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Multimap;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.model.ModelArmorNCRPA;
import com.hbm.render.tileentity.IItemRendererProvider;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.client.IItemRenderer;

public class ArmorNCRPA extends ArmorFSBPowered implements IItemRendererProvider, IPAWeaponsProvider {

	public ArmorNCRPA(ArmorMaterial material, int slot, String texture, long maxPower, long chargeRate, long consumption, long drain) {
		super(material, slot, texture, maxPower, chargeRate, consumption, drain);
	}

	@SideOnly(Side.CLIENT)
	ModelArmorNCRPA[] models;

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, int armorSlot) {
		
		if(models == null) {
			models = new ModelArmorNCRPA[4];
			for(int i = 0; i < 4; i++) models[i] = new ModelArmorNCRPA(i);
		}
		
		return models[armorSlot];
	}

	private static final UUID speed = UUID.fromString("6ab858ba-d712-485c-bae9-e5e765fc555a");

	@Override
	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
		super.onArmorTick(world, player, stack);

		if(this != ModItems.ncrpa_plate) return;

		/// SPEED ///
		Multimap multimap = super.getAttributeModifiers(stack);
		multimap.put(SharedMonsterAttributes.movementSpeed.getAttributeUnlocalizedName(), new AttributeModifier(speed, "NCRPA SPEED", 0.1, 0));
		player.getAttributeMap().removeAttributeModifiers(multimap);

		if(player.isSprinting()) {
			player.getAttributeMap().applyAttributeModifiers(multimap);
		}
		
		if(this.hasFSBArmor(player)) {
			if(world.getTotalWorldTime() % 20 != 0) return;
			if(HbmPlayerProps.getData(player).enableHUD) player.addPotionEffect(new PotionEffect(Potion.nightVision.id, 300, 0, true));
		}
	}

	@Override public Item getItemForRenderer() { return this; }

	@Override
	public IItemRenderer getRenderer() {
		return new ItemRenderBase( ) {
			public void renderInventory() { setupRenderInv(); }
			public void renderNonInv() { setupRenderNonInv(); }
			public void renderCommon() {
				if(armorType == 0) GL11.glTranslated(0, 0.5, 0);
				renderStandard(ResourceManager.armor_ncr, armorType,
						ResourceManager.ncrpa_helmet, ResourceManager.ncrpa_chest, ResourceManager.ncrpa_arm, ResourceManager.ncrpa_leg,
						"Helmet,Eyes", "Chest", "LeftArm", "RightArm", "LeftLeg", "RightLeg", "LeftBoot", "RightBoot");
			}};
	}

	public static final ArmorNCRPAMelee meleeComponent = new ArmorNCRPAMelee();
	public static final ArmorNCRPARanged rangedComponent = new ArmorNCRPARanged();
	
	@Override
	public IPAMelee getMeleeComponent(EntityPlayer entity) {
		if(this.hasFSBArmorIgnoreCharge(entity)) return meleeComponent;
		return null;
	}

	@Override
	public IPARanged getRangedComponent(EntityPlayer entity) {
		if(this.hasFSBArmorIgnoreCharge(entity)) return rangedComponent;
		return null;
	}
}
