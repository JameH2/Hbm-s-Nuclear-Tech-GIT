package com.hbm.items.special;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.SolarSystem;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOre;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOreType;
import com.hbm.items.tool.ItemOreDensityScanner;
import com.hbm.util.i18n.I18nUtil;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraft.world.gen.NoiseGeneratorPerlin;

public class ItemBedrockOreBase extends Item {

	public ItemBedrockOreBase() {
		this.setHasSubtypes(true);
	}

	public static double getOreAmount(ItemStack stack, CelestialBedrockOreType type) {
		if(!stack.hasTagCompound()) return 1;
		NBTTagCompound data = stack.getTagCompound();
		return data.getDouble(type.suffix);
	}

	public static SolarSystem.Body getOreBody(ItemStack stack) {
		if(stack.getItemDamage() >= SolarSystem.Body.values().length || stack.getItemDamage() <= 0) return SolarSystem.Body.KERBIN;
		return SolarSystem.Body.values()[stack.getItemDamage()];
	}

	public static void setOreAmount(World world, ItemStack stack, int x, int z) {
		if(!stack.hasTagCompound()) stack.stackTagCompound = new NBTTagCompound();
		NBTTagCompound data = stack.getTagCompound();

		SolarSystem.Body body = CelestialBody.getEnum(world);

		stack.setItemDamage(body.ordinal());

		for(CelestialBedrockOreType type : CelestialBedrockOre.get(body).types) {
			data.setDouble(type.suffix, getOreLevel(world, x, z, type));
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		SolarSystem.Body body = getOreBody(stack);
		list.add("Mined on: " + I18nUtil.resolveKey("body." + body.name));

		for(CelestialBedrockOreType type : CelestialBedrockOre.get(body).types) {
			double amount = getOreAmount(stack, type);
			String typeName = StatCollector.translateToLocalFormatted("item.bedrock_ore.type." + type.suffix + ".name");
			list.add(typeName + ": " + ((int) (amount * 100)) / 100D + " (" + ItemOreDensityScanner.getColor(amount) + StatCollector.translateToLocalFormatted(ItemOreDensityScanner.translateDensity(amount)) + EnumChatFormatting.GRAY + ")");
		}
	}

	public static double getOreLevel(World world, int x, int z, CelestialBedrockOreType type) {
		long seed = world.getSeed() + world.provider.dimensionId;

		NoiseGeneratorPerlin level = getGenerator(seed);
		NoiseGeneratorPerlin ore = getGenerator(seed - 4096 + type.index);

		double scale = 0.01D;

		return MathHelper.clamp_double(Math.abs(level.func_151601_a(x * scale, z * scale) * ore.func_151601_a(x * scale, z * scale)) * 0.05, 0, 2);
	}

	private static Map<Long, NoiseGeneratorPerlin> generators = new HashMap<>();

	private static NoiseGeneratorPerlin getGenerator(long seed) {
		return generators.computeIfAbsent(seed, key -> new NoiseGeneratorPerlin(new Random(seed), 4));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		for(SolarSystem.Body body : SolarSystem.Body.values()) {
			if(body == SolarSystem.Body.ORBIT) continue;
			list.add(new ItemStack(item, 1, body.ordinal()));
		}
	}

}
