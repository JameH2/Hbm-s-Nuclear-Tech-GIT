package com.hbm.items.machine;

import java.util.List;

import com.hbm.dim.CelestialBody;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.saveddata.satellites.Satellite;

import com.hbm.util.i18n.I18nUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;

public class ItemSatellite extends ItemCustomMissilePart implements ISatChip {

	private boolean canLaunchByHand;

	public ItemSatellite() {
		this(16_000);
	}

	public ItemSatellite(int mass) {
		makeWarhead(WarheadType.SATELLITE, 15F, mass, PartSize.SIZE_20);
		if(mass <= 16_000) canLaunchByHand = true;
	}

	public ItemSatellite(int mass, WarheadType type) {
		makeWarhead(type, 15F, mass, PartSize.SIZE_20);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(itemstack, player, list, bool);

		list.add(I18nUtil.resolveKey("item.sat.desc.frequency") + ": " + getFreq(itemstack));

		if(this == ModItems.sat_foeq)
			list.add(I18nUtil.resolveKey("item.sat.desc.foeq"));

		if(this == ModItems.sat_gerald) {
			list.add(I18nUtil.resolveKey("item.sat.desc.gerald.single_use"));
			list.add(I18nUtil.resolveKey("item.sat.desc.gerald.orbital_module"));
			list.add(I18nUtil.resolveKey("item.sat.desc.gerald.melter"));
		}

		if(this == ModItems.sat_laser)
			list.add(I18nUtil.resolveKey("item.sat.desc.laser"));

		if(this == ModItems.sat_mapper)
			list.add(I18nUtil.resolveKey("item.sat.desc.mapper"));

		if(this == ModItems.sat_miner)
			list.add(I18nUtil.resolveKey("item.sat.desc.miner"));

		if(this == ModItems.sat_lunar_miner)
			list.add(I18nUtil.resolveKey("item.sat.desc.lunar_miner"));

		if(this == ModItems.sat_radar)
			list.add(I18nUtil.resolveKey("item.sat.desc.radar"));

		if(this == ModItems.sat_resonator)
			list.add(I18nUtil.resolveKey("item.sat.desc.resonator"));

		if(this == ModItems.sat_scanner)
			list.add(I18nUtil.resolveKey("item.sat.desc.scanner"));

		if(this == ModItems.sat_war)
			list.add(I18nUtil.resolveKey("item.sat.desc.war"));

		if(this == ModItems.sat_dyson_relay)
			list.add(I18nUtil.resolveKey("item.sat.desc.dyson_relay"));

		if(canLaunchByHand) {
			list.add(EnumChatFormatting.GOLD + I18nUtil.resolveKey("item.sat.desc.launch_by_hand"));

			if(CelestialBody.inOrbit(player.worldObj))
				list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.sat.desc.deploy_orbit"));
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {
		if(!canLaunchByHand) return stack;
		if(!CelestialBody.inOrbit(world)) return stack;

		if(!world.isRemote) {
			int targetDimensionId = CelestialBody.getTarget(world, (int)player.posX, (int)player.posZ).body.dimensionId;
			WorldServer targetWorld = DimensionManager.getWorld(targetDimensionId);
			if(targetWorld == null) {
				DimensionManager.initDimension(targetDimensionId);
				targetWorld = DimensionManager.getWorld(targetDimensionId);

				if(targetWorld == null) return stack;
			}

			Satellite.orbit(targetWorld, Satellite.getIDFromItem(stack.getItem()), getFreq(stack), player.posX, player.posY, player.posZ);

			player.addChatMessage(new ChatComponentText("Satellite launched successfully!"));
		}

		stack.stackSize--;

		return stack;
	}

}
