package com.hbm.items.machine;

import java.util.List;

import com.hbm.items.ItemEnumMulti;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.util.i18n.I18nUtil;

public class ItemCircuit extends ItemEnumMulti {

	public ItemCircuit() {
		super(EnumCircuitType.class, true, true);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, EnumCircuitType.VACUUM_TUBE.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.NUMITRON.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CAPACITOR.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CAPACITOR_TANTALIUM.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.ATOMIC_CLOCK.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.PCB.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.SILICON.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CHIP.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CHIP_BISMOID.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CHIP_QUANTUM.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.ANALOG.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.BASIC.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.ADVANCED.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CAPACITOR_BOARD.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.BISMOID.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.AVIONICS.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.AERO.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.GAAS.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.QUANTUM.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CRYSTAL.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CONTROLLER_CHASSIS.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CONTROLLER.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CONTROLLER_ADVANCED.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CONTROLLER_QUANTUM.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.CAPACITOR_LANTHANIUM.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.PROCESST1.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.PROCESST2.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.PROCESST3.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.GASCHIP.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.HFCHIP.ordinal()));
		list.add(new ItemStack(item, 1, EnumCircuitType.MOLYCHIP.ordinal()));
	}

	public static enum EnumCircuitType {
		VACUUM_TUBE,
		CAPACITOR,
		CAPACITOR_TANTALIUM,
		PCB,
		SILICON,
		GAAS,
		CHIP,
		CHIP_BISMOID,
		ANALOG,
		BASIC,
		ADVANCED,
		CAPACITOR_BOARD,
		BISMOID,
		AVIONICS,
		AERO,
		CONTROLLER_CHASSIS,
		CONTROLLER,
		CONTROLLER_ADVANCED,
		QUANTUM,
		CHIP_QUANTUM,
		CONTROLLER_QUANTUM,
		ATOMIC_CLOCK,
		CAPACITOR_LANTHANIUM,
		PROCESST1,
		PROCESST2,
		PROCESST3,
		GASCHIP,
		HFCHIP,
		MOLYCHIP,
		NUMITRON,
		CRYSTAL,
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {
		/*List<String> lines = new ArrayList();

		switch(stack.getItemDamage()) {
		case 0: lines.add(I18nUtil.resolveKey("gui.circuit.we_taught_filament_how_think")); break;
		case 1: lines.add("3300µF"); break;
		case 2: lines.add(I18nUtil.resolveKey("gui.circuit.text.03")); break;
		case 3: lines.add(I18nUtil.resolveKey("gui.circuit.laminated_sandwich_structure")); break;
		case 4: lines.add(I18nUtil.resolveKey("gui.circuit.text.02")); break;
		case 5: lines.add(I18nUtil.resolveKey("gui.circuit.less_tasty_than_it_sounds")); break;
		case 6: lines.add(I18nUtil.resolveKey("gui.circuit.text.04")); break;
		case 7: lines.add(I18nUtil.resolveKey("gui.circuit.one_final_act_of_goodwill")); lines.add(I18nUtil.resolveKey("gui.circuit.i_hear_words_interplay")); lines.add(I18nUtil.resolveKey("gui.circuit.objectively_better_one_time_i")); lines.add(I18nUtil.resolveKey("gui.circuit.up_chris_avellone_with_a_bazooka")); break;
		case 8: lines.add(I18nUtil.resolveKey("gui.circuit.100_lead_solder_rohs_compliant")); break;
		case 9: lines.add(I18nUtil.resolveKey("gui.circuit.red_means_better")); break;
		case 10: lines.add(I18nUtil.resolveKey("gui.circuit.uses_exceptionally_stanky_90s_ye")); break;
		case 11: lines.add(I18nUtil.resolveKey("gui.circuit.text.01")); break;
		}

		for(String line : lines) {
			list.add(EnumChatFormatting.ITALIC + line);
		}*/
	}
}
