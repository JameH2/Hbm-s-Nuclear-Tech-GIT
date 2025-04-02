package com.hbm.items.armor;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.extprop.HbmPlayerProps;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.util.AstronomyUtil;

import cpw.mods.fml.common.network.NetworkRegistry.TargetPoint;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class JetpackBreak extends JetpackFueledBase {

	public static int maxFuel = 1200;

	public JetpackBreak(FluidType fuel, int maxFuel) {
		super(fuel, maxFuel);
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, int slot, String type) {
		return "hbm:textures/models/JetPackBlue.png";
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {

		HbmPlayerProps props = HbmPlayerProps.getData(player);
		float gravity = CelestialBody.getGravity(player);

		if(!world.isRemote) {

			if(getFuel(stack) > 0 && (props.isJetpackActive() || (!player.onGround && !player.isSneaking() && props.enableBackpack && gravity > 0))) {

	    		NBTTagCompound data = new NBTTagCompound();
	    		data.setString("type", "jetpack");
	    		data.setInteger("player", player.getEntityId());
				PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, player.posX, player.posY, player.posZ), new TargetPoint(world.provider.dimensionId, player.posX, player.posY, player.posZ, 100));
			}
		}

		if(getFuel(stack) > 0) {
			if(props.isJetpackActive()) {
				player.fallDistance = 0;

				if(gravity == 0) {
					Vec3 look = player.getLookVec();

					player.motionX += look.xCoord * 0.05;
					player.motionY += look.yCoord * 0.05;
					player.motionZ += look.zCoord * 0.05;
				} else if(player.motionY < 0.4D) {
					player.motionY += 0.1D * Math.max(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);
				}

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 5);

			} else if(!player.isSneaking() && !player.onGround && props.enableBackpack && gravity > 0) {
				player.fallDistance = 0;

				float thrustMultiplier = Math.max(gravity / AstronomyUtil.STANDARD_GRAVITY, 1);

				if(player.motionY < -1 * thrustMultiplier)
					player.motionY += 0.2D * thrustMultiplier;
				else if(player.motionY < -0.1 * thrustMultiplier)
					player.motionY += 0.1D * thrustMultiplier;
				else if(player.motionY < 0)
					player.motionY = 0;

				player.motionX *= 1.025D;
				player.motionZ *= 1.025D;

				world.playSoundEffect(player.posX, player.posY, player.posZ, "hbm:weapon.flamethrowerShoot", 0.25F, 1.5F);
				this.useUpFuel(player, stack, 10);
			}
		}
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {

    	list.add("Regular jetpack that will automatically hover mid-air.");
    	list.add("Sneaking will stop hover mode.");
    	list.add("Hover mode will consume less fuel and increase air-mobility.");

    	super.addInformation(stack, player, list, ext);
    }
}
