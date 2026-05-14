package com.hbm.items.machine;

import java.util.List;
import java.util.Locale;

import com.hbm.dim.CelestialBody;
import com.hbm.inventory.gui.GUIScreenSatSettings;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.ISatChip;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemCustomMissilePart;
import com.hbm.main.MainRegistry;
import com.hbm.saveddata.satellites.Satellite;
import com.hbm.tileentity.IGUIProvider;

import com.hbm.util.i18n.I18nUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import org.lwjgl.input.Keyboard;

public class ItemSatellite extends ItemCustomMissilePart implements ISatChip, IGUIProvider, IItemControlReceiver {

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

	@Override
	public void onCreated(ItemStack stack, World world, EntityPlayer player) {
		Satellite.ensureItemData(stack);
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		int start = list.size();
		super.getSubItems(item, tab, list);
		for(int i = start; i < list.size(); i++) {
			Satellite.ensureItemData((ItemStack) list.get(i));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(itemstack, player, list, bool);

		if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.frequency"), Integer.toString(getFreq(itemstack))));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.owner"), Satellite.getOwner(itemstack)));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.speed"), formatOrbitSpeed(itemstack) + "km/s"));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.altitude"), formatValue(Satellite.getAltitude(itemstack)) + "km"));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.inclination"), formatValue(Satellite.getInclination(itemstack)) + "°"));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.phase"), formatPhaseOffset(itemstack) + "°"));
			list.add(formatTooltipEntry(I18nUtil.resolveKey("item.sat.desc.color"), getHexColor(itemstack)));
		} else {
			list.add(EnumChatFormatting.DARK_GRAY + "" + EnumChatFormatting.ITALIC + "Hold <" + EnumChatFormatting.YELLOW + "" + EnumChatFormatting.ITALIC + "LSHIFT" + EnumChatFormatting.DARK_GRAY
					+ "" + EnumChatFormatting.ITALIC + "> to display more info");
		}

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
		if(player.isSneaking()) {
			if(world.isRemote) player.openGui(MainRegistry.instance, 0, world, 0, 0, 0);
			return stack;
		}

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

			Satellite.orbit(targetWorld, Satellite.getIDFromItem(stack.getItem()), getFreq(stack), player.posX, player.posY, player.posZ, stack);

			player.addChatMessage(new ChatComponentText("Satellite launched successfully!"));
		}

		stack.stackSize--;

		return stack;
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenSatSettings(player);
	}

	@Override
	public void receiveControl(ItemStack stack, NBTTagCompound data) {
		int r = MathHelper.clamp_int(data.getInteger("satColorR"), 0, 255);
		int g = MathHelper.clamp_int(data.getInteger("satColorG"), 0, 255);
		int b = MathHelper.clamp_int(data.getInteger("satColorB"), 0, 255);
		Satellite.setColor(stack, r / 255F, g / 255F, b / 255F);
		Satellite.setAltitude(stack, MathHelper.clamp_float(data.getFloat("satAltitude"), Satellite.MIN_ALTITUDE_KM, Satellite.MAX_ALTITUDE_KM));
		Satellite.setPhaseOffset(stack, data.getFloat("satPhaseOffset"));
		Satellite.setInclination(stack, MathHelper.clamp_float(data.getFloat("satInclination"), Satellite.MIN_INCLINATION, Satellite.MAX_INCLINATION));
		Satellite.setOwner(stack, data.getString("satOwner"));
		Satellite.setBlinking(stack, data.getBoolean("satIsBlinking"));
		Satellite.setBlinkPeriod(stack, data.getFloat("satBlink"));
	}

	private static String formatValue(float value) {
		if(value == (int)value) return Integer.toString((int)value);
		return Float.toString(value);
	}

	private static String formatOrbitSpeed(ItemStack stack) {
		float orbitSpeed = Satellite.getOrbitSpeedKmPerSecond(Satellite.getAltitude(stack));
		return formatValue(Math.round(orbitSpeed * 10.0F) / 10.0F);
	}

	private static String formatPhaseOffset(ItemStack stack) {
		float phaseOffset = Satellite.normalizePhaseOffset(Satellite.getPhaseOffset(stack));
		return formatValue(Math.round(phaseOffset * 10.0F) / 10.0F);
	}

	private static String getHexColor(ItemStack stack) {
		int r = MathHelper.clamp_int(Math.round(Satellite.getColorR(stack) * 255F), 0, 255);
		int g = MathHelper.clamp_int(Math.round(Satellite.getColorG(stack) * 255F), 0, 255);
		int b = MathHelper.clamp_int(Math.round(Satellite.getColorB(stack) * 255F), 0, 255);
		return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b);
	}

	private static String formatTooltipEntry(String key, String value) {
		String safeValue = value != null ? value : "";
		return EnumChatFormatting.YELLOW + key + ": " + EnumChatFormatting.GOLD + safeValue;
	}

}
