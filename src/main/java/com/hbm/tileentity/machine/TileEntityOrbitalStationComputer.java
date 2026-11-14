package com.hbm.tileentity.machine;

import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import api.hbm.tile.IPropulsion;
import com.hbm.dim.CelestialBody;
import com.hbm.dim.orbit.OrbitalStation;
import com.hbm.dim.orbit.OrbitalStation.StationState;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.ThreeInts;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerOrbitalStationComputer;
import com.hbm.inventory.gui.GUIOrbitalStationComputer;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import com.hbm.util.Compat;
import cpw.mods.fml.common.Optional;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityOrbitalStationComputer extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, SimpleComponent, CompatHandler.OCComponent, IRORValueProvider, IRORInteractive {
	private OrbitalStation station;
	private int ror_error = 0;
	public boolean hasDrive;

	public TileEntityOrbitalStationComputer() {
		super(1);
	}

	public boolean travelTo(CelestialBody body, ItemStack drive) {
		if (station == null) return false;
		if (station.orbiting == body) return false;

		station.travelTo(worldObj, body);
		slots[0] = drive;
		markChanged();

		return true;
	}

	public boolean isTravelling() {
		if (station == null) return false;
		return station.state != StationState.ORBIT;
	}

	@Override
	public String getName() {
		return "container.orbitalStationComputer";
	}

	@Override
	public void updateEntity() {
		if(!worldObj.isRemote) {
			if (station == null && CelestialBody.inOrbit(worldObj)) station = OrbitalStation.getStationFromPosition(xCoord, zCoord);

			hasDrive = slots[0] != null;
			networkPackNT(50);
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeBoolean(hasDrive);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		hasDrive = buf.readBoolean();
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerOrbitalStationComputer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIOrbitalStationComputer(this);
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return true;
	}

	@Override
	public void receiveControl(EntityPlayer player, NBTTagCompound data) {
		if (station == null) return;

		if(data.hasKey("name")) {
			station.name = data.getString("name");
		}

		if(data.hasKey("gravity")) {
			station.gravityMultiplier = data.getBoolean("gravity") ? 1 : 0;
		}
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
				xCoord,
				yCoord,
				zCoord,
				xCoord + 1,
				yCoord + 2,
				zCoord + 1
			);
		}

		return bb;
	}

	// opencomputers integratingation
	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_station_computer";
	}

	@Callback(direct = true, doc = "function():table -- Gets all station information.")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getInfo(Context context, Arguments args) {
		if (station == null) return new Object[] {null};
		return new Object[] {station.name, station.hasEngines, station.errorsAt.size(), station.state.ordinal(), station.getUnscaledProgress(0), station.orbiting.name, station.target.name};
	}

	@Callback(direct = true, limit = 4, doc = "function():table -- Gets errors from propulsion sources.")
	@Optional.Method(modid = "OpenComputers")
	public Object[] getErrors(Context context, Arguments args) {
		// world's worst list usage
		List<List<String>> errors = new ArrayList<>();
		if (station == null) return new Object[] {0, errors.toArray()};

		for(ThreeInts errorAt : station.errorsAt) {
			// this might cause certain TEs to not show up but i didnt want OC to be chunkloading anything
			TileEntity error = Compat.getTileStandard(worldObj, errorAt.x, errorAt.y, errorAt.z);
			if(error == null || !(error instanceof IPropulsion)) continue;
			List<String> errs = new ArrayList<>();
			errs.add(error.getBlockType().getUnlocalizedName());
			((IPropulsion) error).addErrors(errs);
			errors.add(errs);
		}
		return new Object[] {errors.size(), errors.toArray()};
	}

	// RoR!!!!!
	@Override
	public String[] getFunctionInfo() {
		return new String[] {
			PREFIX_VALUE + "name",
			PREFIX_VALUE + "hasEngines",
			PREFIX_VALUE + "errors",
			PREFIX_VALUE + "error",
			PREFIX_VALUE + "state",
			PREFIX_VALUE + "progress",
			PREFIX_VALUE + "orbiting",
			PREFIX_VALUE + "target",
			PREFIX_FUNCTION + "getError" + NAME_SEPARATOR + "index",
		};
	}

	@Override
	public String provideRORValue(String name) {
		if (station == null) return null;
		if((PREFIX_VALUE + "name").equals(name))			return	"" + station.name;
		if((PREFIX_VALUE + "hasEngines").equals(name))		return	"" + (station.hasEngines ? 1 : 0);
		if((PREFIX_VALUE + "errors").equals(name))			return	"" + station.errorsAt.size();
		if((PREFIX_VALUE + "state").equals(name))			return	"" + station.state.ordinal();
		if((PREFIX_VALUE + "progress").equals(name))		return	"" + (int)(station.getUnscaledProgress(0)*100);
		if((PREFIX_VALUE + "orbiting").equals(name))		return	"" + station.orbiting.name;
		if((PREFIX_VALUE + "target").equals(name))			return	"" + station.target.name;
		if((PREFIX_VALUE + "error").equals(name)) {
			// not sure what use you'd have out of this
			// especially since you still have to get parts of the string which might not be so performant
			if (ror_error < 0 || ror_error >= station.errorsAt.size()) return null;

			ThreeInts errorAt = station.errorsAt.get(ror_error);
			if (errorAt == null) return null;
			TileEntity error = Compat.getTileStandard(worldObj, errorAt.x, errorAt.y, errorAt.z);
			if(error == null || !(error instanceof IPropulsion)) return null;
			List<String> errs = new ArrayList<>();
			((IPropulsion) error).addErrors(errs);

			String whatthefuck = error.getBlockType().getUnlocalizedName() + ";";
			for (int i = 0; i < errs.size(); i++) {
				if (i != errs.size()-1) whatthefuck = whatthefuck + errs.get(i) + ";";
				else whatthefuck = whatthefuck + errs.get(i);
			}
			return whatthefuck;
		}
		return null;
	}

	@Override
	public String runRORFunction(String name, String[] params) {
		if (station == null) return null;
		if((PREFIX_FUNCTION + "getError").equals(name) && params.length > 0) {
			try {
				int val = Integer.parseInt(params[0]);
				if (val < 0 || val >= station.errorsAt.size()) return null;
				ror_error = val;
			} catch(NumberFormatException e) {}
			return null;
		}
		return null;
	}
}
