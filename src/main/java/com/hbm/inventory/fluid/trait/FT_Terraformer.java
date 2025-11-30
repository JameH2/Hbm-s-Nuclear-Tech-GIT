package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.blocks.ModBlocks;
import com.hbm.dim.SolarSystem;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumChatFormatting;


import java.io.IOException;
import java.util.List;

public class FT_Terraformer extends FluidTrait {

	private SolarSystem.Body target;

	public FT_Terraformer() {}

	public FT_Terraformer(SolarSystem.Body target) {
		this.target = target;
	}

	public Block getTargetBlock() {
		if(target == SolarSystem.Body.DUNA) return ModBlocks.duna_sands;
		if(target == SolarSystem.Body.LAYTHE) return ModBlocks.laythe_silt;
		return Blocks.stone;
	}

	@Override
	public void addInfo(List<String> info) {
		info.add(EnumChatFormatting.AQUA + "[Terraforming]");
	}

	@Override
	public void serializeJSON(JsonWriter writer) throws IOException {
		writer.name("block").value(target.name());
	}

	@Override
	public void deserializeJSON(JsonObject obj) {
		this.target = SolarSystem.Body.valueOf(obj.get("block").getAsString());
	}
}
