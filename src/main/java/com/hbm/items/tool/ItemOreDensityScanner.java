package com.hbm.items.tool;

import baubles.api.BaubleType;
import baubles.api.IBauble;

import com.hbm.dim.CelestialBody;
import com.hbm.dim.WorldProviderCelestial;
import com.hbm.inventory.FluidStack;
import com.hbm.items.special.ItemBedrockOreBase;
import com.hbm.items.special.ItemBedrockOreNew;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOre;
import com.hbm.items.special.ItemBedrockOreNew.CelestialBedrockOreType;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacket;
import com.hbm.util.ChatBuilder;
import com.hbm.world.feature.BedrockOre;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemOreDensityScanner extends Item implements IBauble {

	@Override
	public void onUpdate(ItemStack stack, World world, Entity entity, int i, boolean bool) {
		tickScanner(world, entity);
	}

	private void tickScanner(World world, Entity entity) {
		if(!(entity instanceof EntityPlayerMP) || world.getTotalWorldTime() % 5 != 0) return;

		EntityPlayerMP player = (EntityPlayerMP) entity;

		double totalLevel = 0D;

		CelestialBody body = CelestialBody.getBody(world);

		for(CelestialBedrockOreType type : CelestialBedrockOre.get(body.getEnum()).types) {
			double level = ItemBedrockOreBase.getOreLevel(world, (int) Math.floor(player.posX), (int) Math.floor(player.posZ), type);
			PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(
					ChatBuilder.startTranslation("item.bedrock_ore.type." + type.suffix + ".name")
					.next(": " + ((int) (level * 100) / 100D) + " (")
					.nextTranslation(translateDensity(level)).color(getColor(level))
					.next(")").color(EnumChatFormatting.RESET).flush(),
			777 + type.index, 4000), player);
			totalLevel += level;
		}
		totalLevel /= CelestialBedrockOre.get(body.getEnum()).types.length;

		int tier = BedrockOre.getTier(totalLevel);
		FluidStack boreFluid = BedrockOre.getBoreFluid(totalLevel);

		if(world.provider instanceof WorldProviderCelestial && ((WorldProviderCelestial) world.provider).getBedrockAcid() != null) {
			boreFluid = ((WorldProviderCelestial) world.provider).getBedrockAcid();
		}

		ChatBuilder builder = ChatBuilder.startTranslation("item.siege_coin.desc1", tier).color(EnumChatFormatting.YELLOW);
		if(boreFluid != null) {
			builder.nextTranslation("chat.ore_density_scanner.1", boreFluid.fill)
			.nextTranslation(boreFluid.type.getUnlocalizedName());
		}

		PacketDispatcher.wrapper.sendTo(new PlayerInformPacket(builder.flush(), 777 + ItemBedrockOreNew.CelestialBedrockOre.getTotalTypeCount(), 4000), player);
	}

	@Override
	public BaubleType getBaubleType(ItemStack stack) {
		return BaubleType.BELT;
	}

	@Override
	public void onWornTick(ItemStack stack, EntityLivingBase entity) {
		tickScanner(entity.worldObj, entity);
	}

	@Override
	public void onEquipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public void onUnequipped(ItemStack stack, EntityLivingBase entity) { }

	@Override
	public boolean canEquip(ItemStack stack, EntityLivingBase entity) {
		return true;
	}

	@Override
	public boolean canUnequip(ItemStack stack, EntityLivingBase entity) {
		return true;
	}

	public static String translateDensity(double density) {
		if(density <= 0.1) return "item.ore_density_scanner.verypoor";
		if(density <= 0.35) return "item.ore_density_scanner.poor";
		if(density <= 0.75) return "item.ore_density_scanner.low";
		if(density >= 1.9) return "item.ore_density_scanner.excellent";
		if(density >= 1.65) return "item.ore_density_scanner.veryhigh";
		if(density >= 1.25) return "item.ore_density_scanner.high";
		return "item.ore_density_scanner.moderate";
	}

	public static EnumChatFormatting getColor(double density) {
		if(density <= 0.1) return EnumChatFormatting.DARK_RED;
		if(density <= 0.35) return EnumChatFormatting.RED;
		if(density <= 0.75) return EnumChatFormatting.GOLD;
		if(density > 2) return EnumChatFormatting.LIGHT_PURPLE; // only for BO items that got mined with fortune
		if(density >= 1.9) return EnumChatFormatting.AQUA;
		if(density >= 1.65) return EnumChatFormatting.BLUE;
		if(density >= 1.25) return EnumChatFormatting.GREEN;
		return EnumChatFormatting.YELLOW;
	}
}
