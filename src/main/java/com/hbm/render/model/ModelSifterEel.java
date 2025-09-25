package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public class ModelSifterEel extends ModelBase {

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, rotationYaw, rotationHeadYaw, rotationPitch, scale);

		GL11.glPushMatrix();
		{

			double cy0 = Math.sin(limbSwing % (Math.PI * 2));
			double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.2);
			double cy2 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.4);
			double cy3 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.6);
			double cy4 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.9);
			double cy5 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.2);
			double cy6 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.5);
			double cy7 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 1.9);

			GL11.glRotatef(180.0F, 0, 0, 1);
			GL11.glTranslatef(0, -1.5F, 0);

			// Head
			GL11.glPushMatrix();
			{
				GL11.glTranslatef(0, 0, -0.5F);
				GL11.glRotatef(rotationPitch, 1, 0, 0);
				GL11.glRotatef(rotationHeadYaw, 0, 1, 0);
				GL11.glTranslatef(0, 0, 0.5F);
				ResourceManager.sifter_eel.renderPart("Head");
				ResourceManager.sifter_eel.renderPart("Jaw");

			}
			GL11.glPopMatrix();

			// Side fins
			GL11.glPushMatrix();
			{
				GL11.glRotated(cy0 * 20, 1, 0, 0);
				ResourceManager.sifter_eel.renderPart("FinL");
			}
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			{
				GL11.glRotated(cy0 * -20, 1, 0, 0);
				ResourceManager.sifter_eel.renderPart("FinR");
			}
			GL11.glPopMatrix();

			// Tail fin
			GL11.glPushMatrix();
			{
				ResourceManager.sifter_eel.renderPart("Body1");

				GL11.glRotated(cy1 * 10, 0, 1, 0);
				GL11.glRotated(cy2 * -2, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("Dorsal1");


				ResourceManager.sifter_eel.renderPart("Body2");
				GL11.glRotated(cy3 * -2, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("Dorsal2");
				ResourceManager.sifter_eel.renderPart("Dorsal3");
				GL11.glRotated(cy4 * -6, 0, 1, 0);

				ResourceManager.sifter_eel.renderPart("Body3");

				ResourceManager.sifter_eel.renderPart("Ventral1");
				ResourceManager.sifter_eel.renderPart("Ventral2");
				ResourceManager.sifter_eel.renderPart("Ventral3");
				GL11.glRotated(cy5 * -4, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("Dorsal4");
				ResourceManager.sifter_eel.renderPart("Ventral4");
				ResourceManager.sifter_eel.renderPart("Body4");
				GL11.glRotated(cy6 * -6, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("Body5");

				ResourceManager.sifter_eel.renderPart("Body6");

				GL11.glRotated(cy7 * -5, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("Tail");

			}

			GL11.glPopMatrix();

		}
		GL11.glPopMatrix();
	}

}
