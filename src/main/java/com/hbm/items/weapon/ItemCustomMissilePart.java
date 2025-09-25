package com.hbm.items.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.entity.missile.EntityMissileCustom;
import com.hbm.items.special.ItemLootCrate;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class ItemCustomMissilePart extends Item {

	public PartType type;
	public PartSize top;
	public PartSize bottom;
	public Rarity rarity;
	public float health;
	public int mass = 0;
	private String title;
	private String author;
	private String witty;

	public ItemCustomMissilePart() {
		this.setCreativeTab(MainRegistry.missileTab);
	}

	public static HashMap<Integer, ItemCustomMissilePart> parts = new HashMap<>();

	/**
	 * == Chips ==
	 * [0]: inaccuracy
	 *
	 * == Warheads ==
	 * [0]: type
	 * [1]: strength/radius/cluster count
	 * [2]: weight
	 *
	 * == Fuselages ==
	 * [0]: type
	 * [1]: tank size
	 *
	 * == Stability ==
	 * [0]: inaccuracy mod
	 *
	 * == Thrusters ===
	 * [0]: type
	 * [1]: consumption
	 * [2]: lift strength
	 * ROCKET SPECIFIC
	 * [3]: thrust (N)
	 * [4]: ISP (s)
	 */
	public Object[] attributes;

	public enum PartType {
		CHIP,
		WARHEAD, // aka payload
		FUSELAGE,
		FINS,
		THRUSTER,
	}

	public enum PartSize {

		//for chips
		ANY,
		//for missile tips and thrusters
		NONE,
		//regular sizes, 1.0m, 1.5m and 2.0m
		SIZE_10(1.0),
		SIZE_15(1.5),
		SIZE_20(2.0),
		// Space-grade
		SIZE_25(2.5),
		SIZE_30(3.0);

		PartSize() {
			this.radius = 0;
		}

		PartSize(double radius) {
			this.radius = radius;
		}

		public double radius;
	}

	public enum WarheadType {
		HE("item.warhead.desc.he"),
		INC("item.warhead.desc.incendiary"),
		BUSTER("item.warhead.desc.bunker_buster"),
		CLUSTER("item.warhead.desc.cluster"),
		NUCLEAR("item.warhead.desc.nuclear"),
		TX("item.warhead.desc.thermonuclear"),
		N2("item.warhead.desc.n2"),
		BALEFIRE("item.warhead.desc.balefire"),
		SCHRAB("item.warhead.desc.schrab"),
		TAINT("item.warhead.desc.taint"),
		CLOUD("item.warhead.desc.cloud"),
		TURBINE("item.warhead.desc.turbine"),
		APOLLO("item.warhead.desc.apollo"),
		SATELLITE("item.warhead.desc.satellite"),

		//shit solution but it works. this allows traits to be attached to these empty dummy types, allowing for custom warheads
		CUSTOM0("item.warhead.custom0"),
		CUSTOM1("item.warhead.custom1"),
		CUSTOM2("item.warhead.custom2"),
		CUSTOM3("item.warhead.custom3"),
		CUSTOM4("item.warhead.custom4"),
		CUSTOM5("item.warhead.custom5"),
		CUSTOM6("item.warhead.custom6"),
		CUSTOM7("item.warhead.custom7"),
		CUSTOM8("item.warhead.custom8"),
		CUSTOM9("item.warhead.custom9");

		public final String unlocalizedName;
		WarheadType(String unlocalizedName){
			this.unlocalizedName = unlocalizedName;
		}
		/** Overrides that type's impact effect. Only runs serverside */
		public Consumer<EntityMissileCustom> impactCustom = null;
		/** Runs at the beginning of the missile's update cycle, both client and serverside. */
		public Consumer<EntityMissileCustom> updateCustom = null;
		/** Override for the warhead's name in the missile description */
		public String labelCustom = null;
	}

	public enum FuelType {
		ANY("item.custom_missile_part.fuel.any"), // Used by space-grade fuselages
		KEROSENE("item.custom_missile_part.fuel.kerosene"),
		SOLID("item.custom_missile_part.fuel.solid"),
		HYDROGEN("item.custom_missile_part.fuel.hydrogen"),
		XENON("item.custom_missile_part.fuel.xenon"),
		BALEFIRE("item.custom_missile_part.fuel.balefire"),
		HYDRAZINE("item.custom_missile_part.fuel.hydrazine"),
		METHALOX("item.custom_missile_part.fuel.methalox"),
		KEROLOX("item.custom_missile_part.fuel.kerolox"); // oxygen rather than peroxide

		public final String unlocalizedName;

		FuelType(String unlocalizedName) {
			this.unlocalizedName = unlocalizedName;
		}
	}

	public enum Rarity {

		COMMON("item.missile.part.rarity.common", EnumChatFormatting.GRAY),
		UNCOMMON("item.missile.part.rarity.uncommon", EnumChatFormatting.YELLOW),
		RARE("item.missile.part.rarity.rare", EnumChatFormatting.AQUA),
		EPIC("item.missile.part.rarity.epic", EnumChatFormatting.LIGHT_PURPLE),
		LEGENDARY("item.missile.part.rarity.legendary", EnumChatFormatting.DARK_GREEN),
		SEWS_CLOTHES_AND_SUCKS_HORSE_COCK("item.missile.part.rarity.strange", EnumChatFormatting.DARK_AQUA);

		private final String key;
		private final EnumChatFormatting color;

		Rarity(String key, EnumChatFormatting color) {
			this.key = key;
			this.color = color;
		}

		public String getDisplay() {
			return color + I18nUtil.resolveKey(key);
		}
	}

	public ItemCustomMissilePart makeChip(float inaccuracy) {

		this.type = PartType.CHIP;
		this.top = PartSize.ANY;
		this.bottom = PartSize.ANY;
		this.attributes = new Object[] { inaccuracy };

		parts.put(this.hashCode(), this);

		return this;
	}

	public ItemCustomMissilePart makeWarhead(WarheadType type, float punch, int mass, PartSize size) {

		this.type = PartType.WARHEAD;
		this.top = PartSize.NONE;
		this.bottom = size;
		this.mass = mass;
		this.attributes = new Object[] { type, punch };
		setTextureName(RefStrings.MODID + ":mp_warhead");

		parts.put(this.hashCode(), this);

		return this;
	}

	public ItemCustomMissilePart makeFuselage(FuelType type, int fuel, int mass, PartSize top, PartSize bottom) {

		this.type = PartType.FUSELAGE;
		this.top = top;
		this.bottom = bottom;
		this.mass = mass;
		attributes = new Object[] { type, fuel };
		setTextureName(RefStrings.MODID + ":mp_fuselage");

		parts.put(this.hashCode(), this);

		return this;
	}

	public ItemCustomMissilePart makeStability(float inaccuracy, PartSize size) {

		this.type = PartType.FINS;
		this.top = size;
		this.bottom = size;
		this.attributes = new Object[] { inaccuracy };
		setTextureName(RefStrings.MODID + ":mp_stability");

		parts.put(this.hashCode(), this);

		return this;
	}

	public ItemCustomMissilePart makeThruster(FuelType type, float consumption, float lift, PartSize size, int thrust, int mass, int isp) {

		this.type = PartType.THRUSTER;
		this.top = size;
		this.bottom = PartSize.NONE;
		this.mass = mass;
		this.attributes = new Object[] { type, consumption, lift, thrust, isp };
		setTextureName(RefStrings.MODID + ":mp_thruster");

		parts.put(this.hashCode(), this);

		return this;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		if(this == ModItems.rp_pod_20) return;

		if(title != null)
			list.add(EnumChatFormatting.DARK_PURPLE + "\"" + title + "\"");

		try {
			switch(type) {
				case CHIP:
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.inaccuracy") + ": " + EnumChatFormatting.GRAY + (Float)attributes[0] * 100 + "%");
					break;
				case WARHEAD:
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.size") + ": " + EnumChatFormatting.GRAY + getSize(bottom));
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.type") + ": " + EnumChatFormatting.GRAY + getWarhead());
					if(attributes[0] != WarheadType.APOLLO && attributes[0] != WarheadType.SATELLITE)
						list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.strength") + ": " + EnumChatFormatting.GRAY + (Float)attributes[1]);
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.mass") + ": " + EnumChatFormatting.GRAY + mass + "kg");
					break;
				case FUSELAGE:
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.topSize") + ": " + EnumChatFormatting.GRAY + getSize(top));
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.bottomSize") + ": " + EnumChatFormatting.GRAY + getSize(bottom));
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.fuelType") + ": " + EnumChatFormatting.GRAY + getFuelName());
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.fuelAmount") + ": " + EnumChatFormatting.GRAY + getTankSize() + "mB");
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.mass") + ": " + EnumChatFormatting.GRAY + mass + "kg");
					break;
				case FINS:
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.size") + ": " + EnumChatFormatting.GRAY + getSize(top));
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.inaccuracy") + ": " + EnumChatFormatting.GRAY + (Float)attributes[0] * 100 + "%");
					break;
				case THRUSTER:
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.size") + ": " + EnumChatFormatting.GRAY + getSize(top));
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.fuelType") + ": " + EnumChatFormatting.GRAY + getFuelName());
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.fuelConsumption") + ": " + EnumChatFormatting.GRAY + (Float)attributes[1] + "l/tick");
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.maxPayload") + ": " + EnumChatFormatting.GRAY + (Float)attributes[2] + "kg");
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.thrust") + ": " + EnumChatFormatting.GRAY + (Integer)attributes[3] + "N");
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.isp") + ": " + EnumChatFormatting.GRAY + (Integer)attributes[4] + "s");
					list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.mass") + ": " + EnumChatFormatting.GRAY + mass + "kg");
					break;
			}
		} catch(Exception ex) {
			list.add(I18nUtil.resolveKey("error.generic"));
		}

		// if(type != PartType.CHIP)
		// 	list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.health") + ": " + EnumChatFormatting.GRAY + health + "HP");

		if(this.rarity != null)
			list.add(EnumChatFormatting.BOLD + I18nUtil.resolveKey("item.missile.part.rarity") + ": " + EnumChatFormatting.GRAY + this.rarity.getDisplay());
		if(author != null)
			list.add(EnumChatFormatting.WHITE + "   " + I18nUtil.resolveKey("item.missile.part.by") + " " + author);
		if(witty != null)
			list.add(EnumChatFormatting.GOLD + "   " + EnumChatFormatting.ITALIC + "\"" + witty + "\"");
	}

	public String getSize(PartSize size) {

		switch(size) {
			case ANY:
				return I18nUtil.resolveKey("item.missile.part.size.any");
			case SIZE_10:
				return "1.0m";
			case SIZE_15:
				return "1.5m";
			case SIZE_20:
				return "2.0m";
			default:
				return I18nUtil.resolveKey("item.missile.part.size.none");
		}
	}

	public String getWarhead() {
		if(!(attributes[0] instanceof WarheadType)) return EnumChatFormatting.BOLD + I18nUtil.resolveKey("general.na");

		WarheadType type = (WarheadType) attributes[0];

		if(type.labelCustom != null) return type.labelCustom;

		switch(type) {
		case HE:
			return EnumChatFormatting.YELLOW + I18nUtil.resolveKey(WarheadType.HE.unlocalizedName);
		case INC:
			return EnumChatFormatting.GOLD + I18nUtil.resolveKey(WarheadType.INC.unlocalizedName);
		case CLUSTER:
			return EnumChatFormatting.GRAY + I18nUtil.resolveKey(WarheadType.CLUSTER.unlocalizedName);
		case BUSTER:
			return EnumChatFormatting.WHITE + I18nUtil.resolveKey(WarheadType.BUSTER.unlocalizedName);
		case NUCLEAR:
			return EnumChatFormatting.DARK_GREEN + I18nUtil.resolveKey(WarheadType.NUCLEAR.unlocalizedName);
		case TX:
			return EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey(WarheadType.TX.unlocalizedName);
		case N2:
			return EnumChatFormatting.RED + I18nUtil.resolveKey(WarheadType.N2.unlocalizedName);
		case BALEFIRE:
			return EnumChatFormatting.GREEN + I18nUtil.resolveKey(WarheadType.BALEFIRE.unlocalizedName);
		case SCHRAB:
			return EnumChatFormatting.AQUA + I18nUtil.resolveKey(WarheadType.SCHRAB.unlocalizedName);
		case TAINT:
			return EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey(WarheadType.TAINT.unlocalizedName);
		case CLOUD:
			return EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey(WarheadType.CLOUD.unlocalizedName);
		case TURBINE:
			return (System.currentTimeMillis() % 1000 < 500 ? EnumChatFormatting.RED : EnumChatFormatting.LIGHT_PURPLE) + I18nUtil.resolveKey(WarheadType.TURBINE.unlocalizedName);
		case APOLLO:
			return (System.currentTimeMillis() % 1000 < 500 ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + I18nUtil.resolveKey(WarheadType.APOLLO.unlocalizedName);
		case SATELLITE:
			return (System.currentTimeMillis() % 1000 < 500 ? EnumChatFormatting.GOLD : EnumChatFormatting.RED) + I18nUtil.resolveKey(WarheadType.SATELLITE.unlocalizedName);
		default:
			return EnumChatFormatting.BOLD + I18nUtil.resolveKey("general.na");
		}
	}

	public String getFuelName() {
		if(!(attributes[0] instanceof FuelType)) return EnumChatFormatting.BOLD + I18nUtil.resolveKey("general.na");

		switch((FuelType)attributes[0]) {
		case ANY:
			return EnumChatFormatting.GRAY + I18nUtil.resolveKey(FuelType.ANY.unlocalizedName);
		case KEROSENE:
			return EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey(FuelType.KEROSENE.unlocalizedName);
		case METHALOX:
			return EnumChatFormatting.YELLOW + I18nUtil.resolveKey(FuelType.METHALOX.unlocalizedName);
		case KEROLOX:
			return EnumChatFormatting.LIGHT_PURPLE + I18nUtil.resolveKey(FuelType.KEROLOX.unlocalizedName);
		case SOLID:
			return EnumChatFormatting.GOLD + I18nUtil.resolveKey(FuelType.SOLID.unlocalizedName);
		case HYDROGEN:
			return EnumChatFormatting.DARK_AQUA + I18nUtil.resolveKey(FuelType.HYDROGEN.unlocalizedName);
		case XENON:
			return EnumChatFormatting.DARK_PURPLE + I18nUtil.resolveKey(FuelType.XENON.unlocalizedName);
		case BALEFIRE:
			return EnumChatFormatting.GREEN + I18nUtil.resolveKey(FuelType.BALEFIRE.unlocalizedName);
		case HYDRAZINE:
			return EnumChatFormatting.AQUA + I18nUtil.resolveKey(FuelType.HYDRAZINE.unlocalizedName);
		default:
			return EnumChatFormatting.BOLD + I18nUtil.resolveKey("general.na");
		}
	}

	public FluidType getFuel() {
		if(!(attributes[0] instanceof FuelType)) return null;

		switch((FuelType)attributes[0]) {
		case KEROSENE:
			return Fluids.KEROSENE;
		case KEROLOX:
			return Fluids.KEROSENE;
		case METHALOX:
			return Fluids.GAS;
		case HYDROGEN:
			return Fluids.HYDROGEN;
		case XENON:
			return Fluids.XENON;
		case BALEFIRE:
			return Fluids.BALEFIRE;
		case HYDRAZINE:
			return Fluids.HYDRAZINE;
		case SOLID:
			return Fluids.NONE; // Requires non-fluid fuel
		default:
			return null;
		}
	}

	public FluidType getOxidizer() {
		if(!(attributes[0] instanceof FuelType)) return null;

		switch((FuelType)attributes[0]) {
		case KEROLOX:
		case HYDROGEN:
		case METHALOX:
			return Fluids.OXYGEN;
		case KEROSENE:
		case BALEFIRE:
			return Fluids.PEROXIDE;
		default:
			return null;
		}
	}

	public int getThrust() {
		if(type != PartType.THRUSTER) return 0;
		if(attributes[3] == null || !(attributes[3] instanceof Integer)) return 0;
		return (Integer) attributes[3];
	}

	public int getISP() {
		if(type != PartType.THRUSTER) return 0;
		if(attributes[4] == null || !(attributes[4] instanceof Integer)) return 0;
		return (Integer) attributes[4];
	}

	public int getTankSize() {
		if(type != PartType.FUSELAGE) return 0;
		if(!(attributes[1] instanceof Integer)) return 0;
		return (Integer) attributes[1];
	}

	//am i retarded?
	/* yes */
	public ItemCustomMissilePart copy() {

		ItemCustomMissilePart part = new ItemCustomMissilePart();
		part.type = this.type;
		part.top = this.top;
		part.bottom = this.bottom;
		part.health = this.health;
		part.attributes = this.attributes;
		part.health = this.health;
		part.mass = this.mass;
		part.setTextureName(this.iconString);

		return part;
	}

	public ItemCustomMissilePart setAuthor(String author) {
		this.author = author;
		return this;
	}

	public ItemCustomMissilePart setTitle(String title) {
		this.title = title;
		return this;
	}

	public ItemCustomMissilePart setWittyText(String witty) {
		this.witty = witty;
		return this;
	}

	public ItemCustomMissilePart setHealth(float health) {
		this.health = health;
		return this;
	}

	public ItemCustomMissilePart setRarity(Rarity rarity) {
		this.rarity = rarity;

		if(this.type == PartType.FUSELAGE) {
			if(this.top == PartSize.SIZE_10)
				ItemLootCrate.list10.add(this);
			if(this.top == PartSize.SIZE_15)
				ItemLootCrate.list15.add(this);
		} else {
			ItemLootCrate.listMisc.add(this);
		}
		return this;
	}

}
