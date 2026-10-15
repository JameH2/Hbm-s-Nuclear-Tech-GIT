package com.hbm.items.machine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.annotation.CheckForNull;

import com.hbm.config.VersatileConfig;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRTGPelletDepleted.DepletedRTGMaterial;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class ItemRTGPellet extends Item {

	private short heat = 0;
	private boolean doesDecay = false;
	private ItemStack decayItem = null;
	private long lifespan = 0;

	public static final List<ItemRTGPellet> pelletList = new ArrayList();

	public ItemRTGPellet(int heatIn) {
		heat = (short) heatIn;
		setMaxStackSize(1);
		pelletList.add(this);
	}

	private static final String[] facts = new String[] {
			"item.rtg.fact.0",
			"item.rtg.fact.1",
			"item.rtg.fact.2",
			"item.rtg.fact.3",
			"item.rtg.fact.4",
			"item.rtg.fact.5",
			"item.rtg.fact.6",
			"item.rtg.fact.7",
			"item.rtg.fact.8",
			"item.rtg.fact.9",
			"item.rtg.fact.10",
			"item.rtg.fact.11",
			"item.rtg.fact.12",
			"item.rtg.fact.13",
			"item.rtg.fact.14",
			"item.rtg.fact.15"
	};

	public ItemRTGPellet setDecays(DepletedRTGMaterial mat, long life) {
		doesDecay = true;
		decayItem = new ItemStack(ModItems.pellet_rtg_depleted, 1, mat.ordinal());
		lifespan = life;
		return this;
	}

	public long getMaxLifespan() {
		return lifespan;
	}

	public short getHeat() {
		return heat;
	}

	@CheckForNull
	public ItemStack getDecayItem() {
		return decayItem == null ? null : decayItem.copy();
	}

	public boolean getDoesDecay() {
		return this.doesDecay;
	}

	public static ItemStack handleDecay(ItemStack stack, ItemRTGPellet instance) {
		if (instance.getDoesDecay() && VersatileConfig.rtgDecay()) {
			if (instance.getLifespan(stack) <= 0)
				return instance.getDecayItem();
			else
				instance.decay(stack);
		}

		return stack;
	}

	public void decay(ItemStack stack) {
		if (stack != null && stack.getItem() instanceof ItemRTGPellet) {
			if (!((ItemRTGPellet) stack.getItem()).getDoesDecay())
				return;
			if (stack.hasTagCompound())
				stack.stackTagCompound.setLong("PELLET_DEPLETION", getLifespan(stack) - 1);
			else {
				stack.stackTagCompound = new NBTTagCompound();
				stack.stackTagCompound.setLong("PELLET_DEPLETION", getMaxLifespan());
			}
		}
	}

	public long getLifespan(ItemStack stack)
	{
		if (stack != null && stack.getItem() instanceof ItemRTGPellet)
		{
			if (stack.hasTagCompound())
				return stack.stackTagCompound.getLong("PELLET_DEPLETION");
			else
			{
				stack.stackTagCompound = new NBTTagCompound();
				stack.stackTagCompound.setLong("PELLET_DEPLETION", getMaxLifespan());
				return getMaxLifespan();
			}
		}
		return 0;
	}

	public static short getScaledPower(ItemRTGPellet fuel, ItemStack stack) {
		return (short) Math.ceil(fuel.getHeat() * ((double)fuel.getLifespan(stack) / (double)fuel.getMaxLifespan()));
	}

	@Override
	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(!world.isRemote && this == ModItems.pellet_rtg) {
			player.addChatComponentMessage(new ChatComponentTranslation(facts[world.rand.nextInt(facts.length)]).setChatStyle(new ChatStyle().setColor(EnumChatFormatting.YELLOW)));
			world.playSoundAtEntity(player, "random.orb", 1.0F, 1.0F);
		}

		return stack;
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		return getDoesDecay() && getLifespan(stack) != getMaxLifespan();
	}

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		final ItemRTGPellet instance = (ItemRTGPellet) stack.getItem();
		return 1D - (double)instance.getLifespan(stack) / (double)instance.getMaxLifespan();
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		super.addInformation(stack, player, list, bool);
		list.add(I18nUtil.resolveKey(this.getUnlocalizedName().concat(".desc")));
		final ItemRTGPellet instance = (ItemRTGPellet) stack.getItem();
		list.add(I18nUtil.resolveKey("desc.item.rtgHeat", instance.getDoesDecay() && VersatileConfig.scaleRTGPower() ? getScaledPower(instance, stack) : instance.getHeat()));
		if (instance.getDoesDecay()) {
			list.add(I18nUtil.resolveKey("desc.item.rtgDecay", I18nUtil.resolveKey(instance.getDecayItem().getUnlocalizedName() + ".name")));
			list.add(BobMathUtil.toPercentage(instance.getLifespan(stack), instance.getMaxLifespan()));
			if (bool) {
				list.add(I18nUtil.resolveKey("desc.item.rtg_pellet.extended_info"));
				list.add(String.format(Locale.US, I18nUtil.resolveKey("gui.rtgpellet.ticks"), instance.getLifespan(stack), instance.getMaxLifespan()));
				final String[] timeLeft = BobMathUtil.ticksToDate(instance.getLifespan(stack));
				final String[] maxLife = BobMathUtil.ticksToDate(instance.getMaxLifespan());
				list.add(String.format(Locale.US, I18nUtil.resolveKey("gui.rtgpellet.time_remaining_y_d_h"), (Object[]) timeLeft));
				list.add(String.format(Locale.US, I18nUtil.resolveKey("gui.rtgpellet.maximum_life_y_d_h"), (Object[]) maxLife));
			}
		}
	}

	public String getData() {
		return String.format(Locale.US, "%s (%s HE/t) %s", I18nUtil.resolveKey(getUnlocalizedName().concat(".name")), getHeat(), (getDoesDecay() ? " (decays)" : ""));
	}

	public static HashMap<ItemStack, ItemStack> getRecipeMap() {
		HashMap<ItemStack, ItemStack> map = new HashMap<ItemStack, ItemStack>();

		for(ItemRTGPellet pellet : pelletList) {
			if(pellet.decayItem != null) {
				map.put(new ItemStack(pellet), pellet.decayItem.copy());
			}
		}

		return map;
	}
}
