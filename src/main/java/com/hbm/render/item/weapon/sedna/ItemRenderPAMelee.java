package com.hbm.render.item.weapon.sedna;

import org.lwjgl.opengl.GL11;

import com.hbm.items.armor.IPAMelee;
import com.hbm.items.armor.IPAWeaponsProvider;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;

public class ItemRenderPAMelee extends ItemRenderWeaponBase {
	
	@Override public boolean isAkimbo(EntityLivingBase entity) { return true; }
	
	@Override protected float getSwayMagnitude(ItemStack stack) { return 2F; }
	@Override protected float getSwayPeriod(ItemStack stack) { return 0.5F; }

	@Override
	public void setupFirstPerson(ItemStack stack) {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.setupFirstPerson(stack);
	}
	
	@Override
	public void renderFirstPerson(ItemStack stack) {
		IPAMelee component = IPAWeaponsProvider.getMeleeComponentClient();
		if(component != null) component.renderFirstPerson(stack);
	}

	@Override public void setupThirdPerson(ItemStack stack) { }
	@Override public void setupThirdPersonAkimbo(ItemStack stack) { }

	@Override
	public void setupInv(ItemStack stack) {
		GL11.glAlphaFunc(GL11.GL_GREATER, 0F);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glScaled(1, 1, -1);
		GL11.glTranslated(8, 8, 0);
		double scale = 2.5D;
		GL11.glScaled(scale, scale, scale);
	}

	@Override
	public void setupModTable(ItemStack stack) {
		double scale = -12.5D;
		GL11.glScaled(scale, scale, scale);
		GL11.glRotated(90, 0, 1, 0);
		GL11.glTranslated(0, -0.5, 1);
	}

	@Override
	public void renderInv(ItemStack stack) {
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.ncrpa_arm);

		GL11.glPushMatrix();
		double scale = 0.3125D;
		GL11.glScaled(scale, scale, scale);
		
		GL11.glRotated(135, 0, 0, 1);
		GL11.glRotated(135, 0, 1, 0);
		GL11.glTranslated(0, -5.5, 0);
		GL11.glTranslated(-3.5, 0, 0);
		ResourceManager.armor_ncr.renderPart("Leftarm");
		GL11.glTranslated(7, 1, -1);
		ResourceManager.armor_ncr.renderPart("RightArm");
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
	}
	
	public void renderOther(ItemStack stack, ItemRenderType type, Object... data) {
		if(type == type.EQUIPPED) return;
		
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glShadeModel(GL11.GL_SMOOTH);
		
		Minecraft.getMinecraft().renderEngine.bindTexture(ResourceManager.ncrpa_arm);

		GL11.glPushMatrix();
		double scale = 0.3125D;
		GL11.glScaled(scale, scale, scale);
		
		GL11.glRotated(90, 1, 0, 0);
		GL11.glTranslated(0, -5.5, 0);
		GL11.glTranslated(-2, 0, 0);
		ResourceManager.armor_ncr.renderPart("Leftarm");
		GL11.glTranslated(4, 0, 0);
		ResourceManager.armor_ncr.renderPart("RightArm");
		GL11.glPopMatrix();
		
		GL11.glShadeModel(GL11.GL_FLAT);
	}
}
