package com.hbm.render.model;

import org.lwjgl.opengl.GL11;

import com.hbm.main.ResourceManager;

import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;

public class ModelEel extends ModelBase {

	@Override
	public void render(Entity entity, float limbSwing, float limbSwingAmount, float rotationYaw, float rotationHeadYaw, float rotationPitch, float scale) {
		super.render(entity, limbSwing, limbSwingAmount, rotationYaw, rotationHeadYaw, rotationPitch, scale);

		GL11.glPushMatrix();
		{

			double cy0 = Math.sin(limbSwing % (Math.PI * 2));
			double cy1 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.2);
			double cy2 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.4);
			double cy3 = Math.sin(limbSwing % (Math.PI * 2) - Math.PI * 0.6);

			GL11.glTranslatef(0, 1.5F, 0);
			GL11.glRotatef(180, 0, 0, 1);
			//GL11.glRotatef(-rotationPitch * 0.5F, 1, 1, 0);



			// Head
			GL11.glPushMatrix();
			{
				GL11.glRotatef(rotationPitch * 0.2F, -1, 0, 0);
				GL11.glRotatef(0, 0, 0, 0);
				ResourceManager.sifter_eel.renderPart("head");
				ResourceManager.sifter_eel.renderPart("jaw");

			}
			GL11.glPopMatrix();

			// Side fins
			GL11.glPushMatrix();
			{
				GL11.glRotated(cy0 * 20, 1, 0, 0);
				ResourceManager.sifter_eel.renderPart("finL");
			}
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			{
				GL11.glRotated(cy0 * -20, 1, 0, 0);
				ResourceManager.sifter_eel.renderPart("finR");
			}
			GL11.glPopMatrix();
	
			// Tail fin
			GL11.glPushMatrix();
			{
				ResourceManager.sifter_eel.renderPart("frontbodyseg");

				GL11.glRotated(cy1 * 10, 0, 1, 0);
				GL11.glRotated(cy2 * -2, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("topfinfront");


				ResourceManager.sifter_eel.renderPart("midbodyseg");
				GL11.glRotated(cy3 * -2 , 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("topfinmid");
				ResourceManager.sifter_eel.renderPart("topfinmidlast");
				GL11.glRotated(cy1 * -6 , 0, 1, 0);

				ResourceManager.sifter_eel.renderPart("midbodysegtwo");

				ResourceManager.sifter_eel.renderPart("bottomfinfront");
				ResourceManager.sifter_eel.renderPart("bottomfinmid");
				ResourceManager.sifter_eel.renderPart("bottomfinmidlast");
				GL11.glRotated(cy2 * -4 , 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("topfinlast");
				ResourceManager.sifter_eel.renderPart("bottomfinlast");
				ResourceManager.sifter_eel.renderPart("endbodyseg");
				GL11.glRotated(cy3 * -6 , 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("tail");

				ResourceManager.sifter_eel.renderPart("tailbodyseg");

				GL11.glRotated(cy1 * -3, 0, 1, 0);
				ResourceManager.sifter_eel.renderPart("tailtipseg");
				

			}
			
			GL11.glPopMatrix();

		}
		
		GL11.glPopMatrix();
	}

}

