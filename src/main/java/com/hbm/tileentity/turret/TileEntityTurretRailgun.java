package com.hbm.tileentity.turret;

import api.hbm.energymk2.IEnergyReceiverMK2;
import api.hbm.fluidmk2.IFillableItem;
import api.hbm.redstoneoverradio.IRORInteractive;
import api.hbm.redstoneoverradio.IRORValueProvider;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.entity.projectile.EntityRailgunProjectile;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerTurretRailgun;
import com.hbm.inventory.gui.GUITurretRailgun;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.ItemAmmoRailgun;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.factory.XFactoryFolly;
import com.hbm.lib.Library;
import com.hbm.tileentity.TilePort.PortDef;
import com.hbm.main.MainRegistry;
import com.hbm.main.NTMSounds;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BobMathUtil;
import com.hbm.util.fauxpointtwelve.BlockPos;
import com.hbm.util.BufferUtil;
import com.hbm.util.fauxpointtwelve.DirPos;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "OpenComputers")})
public class TileEntityTurretRailgun extends TileEntityMachineBase implements IGUIProvider, IEnergyReceiverMK2, IControlReceiver, IRORInteractive, IRORValueProvider, SimpleComponent, CompatHandler.OCComponent {

	public final long maxPower = 5_000_000L;
	public long power;
	public final long consumption = 250_000L;
	public final long maxCharge = 100_000_000L;
	public long charge = 0L;

	public final float velYaw = 1F;
	public final float velPitch = 0.5F;
	public final float barrelLength = 5F;

	public double rotationPitch;
	public double rotationYaw;
	public double targetRotationYaw;

	public boolean active = false;
	public boolean charging = false;
	public boolean calculating = false;
	public ItemStack sabot = null;
	public int sabotType;

	// calculator variables
	// they all get replaced by them ammo's data, so except gravity and mass, they're just for reference
	public float v0 = 1000F; // initial velocity [m/s] fuck yeah
	public double bc = 0.2D; // ballistic coefficient. 0 would mean no drag, but don't put it, or it'll explode [kgf*m^-2]
	public final double m = 1D; // mass [kg] leave it at 1 or otherwise calculations will be off, because mc doesn't actually use realistic ballistics and only multiplies vector speed directly
	public double g = 20D; // gravity const [m*s^-2]
	public double theta = Math.PI / 2D;
	public double dtheta = Math.toRadians(1D);
	public double error_old = 0D;
	public int iterations = 0;
	public double t_max = 0D;
	public double x_target = 0D;

	private AudioWrapper audioCharging, audioSpoolUp;
	private boolean chargeReadyAudio = false;

	// albeit confusing, x & y are considered x & z in this context
	public Vector2d targetVector = null;
	public Vector2d targetDirectionVector = null;

	//only used by clients for interpolation
	public double lastRotationYaw;
	public double lastRotationPitch;

	public boolean graphMode = true; // true = rotation graph, false = ballistics graph
	public String status = "";

	public TileEntityTurretRailgun() {
		super(4);
	}

	@Override
	public String getName() {
		return "container.turretRailgun";
	}

	AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		if(bb == null) bb = AxisAlignedBB.getBoundingBox(
			xCoord - 3, yCoord - 1, zCoord - 3,
			xCoord + 3, yCoord + 3, zCoord + 3
		);
		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	public DirPos[] getConPos() {
		return new DirPos[] {
			new DirPos(xCoord + 3, yCoord, zCoord, Library.POS_X),
			new DirPos(xCoord - 3, yCoord, zCoord, Library.NEG_X),
			new DirPos(xCoord, yCoord, zCoord + 3, Library.POS_Z),
			new DirPos(xCoord, yCoord, zCoord - 3, Library.NEG_Z)
		};
	}


	public String SabotsNames(int type) {
		switch (type) {
			case (ItemAmmoRailgun.TUNGSTEN):
				return "TUNGSTEN";
			case (ItemAmmoRailgun.DU):
				return "DU";
			case (ItemAmmoRailgun.DESH):
				return "DESH";
			case (ItemAmmoRailgun.NUKE):
				return "NUCLEAR";
			case (ItemAmmoRailgun.STARMETAL):
				return "STARMETAL";
			case (ItemAmmoRailgun.FLUID):
				return "FLUID";
			default:
				return "Empty";
		}
	}

	@Override
	public void updateEntity() {
		if (!worldObj.isRemote) {
			this.power = Library.chargeTEFromItems(slots, 0, this.power, this.getMaxPower());

			this.setupPowerPorts(new PortDef[] {
					new PortDef().atPos(new BlockPos(xCoord, yCoord, zCoord)).withCon(getConPos())
			});
			this.updateAllPorts();
			this.receivePower();

			this.status = "IDLE";
			this.sabotType = -1;

			this.sabot = this.getSabotLoaded();
			this.chargeReadyAudio = false;

			// unit target vector finding
			if (this.active && this.power > 0 && this.sabot != null) {
				this.sabotType = this.sabot.getItemDamage();
				ItemAmmoRailgun.RailgunSabot sabotData = ItemAmmoRailgun.itemTypes[this.sabotType];
				this.v0 = sabotData.v0;
				this.bc = sabotData.bc;

				if (this.targetVector != null) {
					Vector2d deltaTarget = new Vector2d(
						this.targetVector.x - xCoord,
						this.targetVector.y - zCoord
					);
					if (deltaTarget.length() > (this.bc != 0.0D ? (this.v0 / this.bc * (0.8D + this.v0 * 0.0001D)) : (this.v0 * this.v0 / this.g))) {
						// this is a simplification of the max distance any shell can fire, works fine as long as the v0 is somewhat >500 with 0.1 < bc < 0.2
						// basically if it's bigger it nullifies the target, removing the possibility of the turret shooting at 90°
						// it's fine like this, but it really needs a generalized way and not this crappy calculation
						// yeah in case the bc is 0 for some reason, it just uses the max range in a vacuum
						this.targetVector = null;
					} else {
						this.targetDirectionVector = new Vector2d(deltaTarget); // feed me straight slop
						this.targetDirectionVector.normalize();

						// finding optimal launch angle
						if (this.calculating) {
							this.status = "COMPUTING";
							this.x_target = deltaTarget.length();
							this.calculating = calculateAngleTrajectory();
							if (!this.calculating) {
								this.charging = true; // angle found, commit to charging
								this.targetRotationYaw = -Math.atan2(targetDirectionVector.y, targetDirectionVector.x);
							}
						}
					}
				}

				// charging & aligning
				if (this.charging) {
					this.status = "CHARGING";
					long delta = Math.min(consumption, this.power);
					if (this.charge < maxCharge && delta > 0L) {
						this.power -= delta;
						this.charge += delta;
					}
					turnTowardsAngle(this.theta, this.targetRotationYaw);

					// audio shit
					if (!this.chargeReadyAudio && ((maxCharge - this.charge) / consumption <= 120)) this.chargeReadyAudio = true;
				}

				// Oh yea here we go! Big big bullet
				if (this.charge >= maxCharge) this.fire(this.v0);
			}

			// slowly discharge the buffer if nothing is happening
			if (this.charge > 0L && !(this.charging || this.calculating)) this.charge *= 0.98;

			this.networkPackNT(250);
		} else {
			this.lastRotationPitch = this.rotationPitch;
			this.lastRotationYaw = this.rotationYaw;

			//this will fix the interpolation error when the turret crosses the 360° point
			if(Math.abs(this.lastRotationYaw - this.rotationYaw) > Math.PI) {

				if(this.lastRotationYaw < this.rotationYaw)
					this.lastRotationYaw += Math.PI * 2;
				else
					this.lastRotationYaw -= Math.PI * 2;
			}

			audioCharging = playAudio(
				audioCharging,
				MainRegistry.proxy.getLoopedSound("hbm:alarm.containerAlarm", xCoord, yCoord, zCoord, 1F, 1000F, 1.0F, 20),
				this.charging,
				100F,
				1F
			);
			audioSpoolUp = playAudio(
				audioSpoolUp,
				MainRegistry.proxy.getLoopedSound(NTMSounds.NUKE_CHARGE, xCoord, yCoord, zCoord, 1F, 250F, 1.0F, 20),
				this.chargeReadyAudio,
				120F,
				1F
			);
		}
	}

	private AudioWrapper playAudio(AudioWrapper audio, AudioWrapper sound, boolean condition, float volume, float pitch) {
		if (condition) {
			if(audio == null) {
				audio = sound;
				audio.startSound();
			} else if(!audio.isPlaying()) {
				audio = sound;
				audio.startSound();
			}
			audio.keepAlive();
			audio.updatePitch(pitch);
			audio.updateVolume(this.getVolume(volume));
		} else {
			if(audio != null) {
				audio.stopSound();
				audio = null;
			}
		}
		return audio;
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);

		// te's
		buf.writeBoolean(this.active);
		buf.writeLong(this.power);
		buf.writeLong(this.charge);
		BufferUtil.writeVec2d(buf, this.targetVector);
		BufferUtil.writeVec2d(buf, this.targetDirectionVector);
		buf.writeBoolean(this.charging);
		buf.writeDouble(this.rotationPitch);
		buf.writeDouble(this.rotationYaw);
		buf.writeBoolean(this.calculating);

		// client/gui
		buf.writeBoolean(this.chargeReadyAudio);
		buf.writeBoolean(this.graphMode);
		BufferUtil.writeString(buf, this.status);
		buf.writeInt(this.sabotType);

		// solver
		buf.writeDouble(this.theta);
		buf.writeDouble(this.dtheta);
		buf.writeDouble(this.error_old);
		buf.writeInt(this.iterations);
		buf.writeDouble(this.t_max);
		buf.writeDouble(this.x_target);
		buf.writeFloat(this.v0);
		buf.writeDouble(this.bc);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);

		this.active = buf.readBoolean();
		this.power = buf.readLong();
		this.charge = buf.readLong();
		this.targetVector = BufferUtil.readVec2d(buf);
		this.targetDirectionVector = BufferUtil.readVec2d(buf);
		this.charging = buf.readBoolean();
		this.rotationPitch = buf.readDouble();
		this.rotationYaw = buf.readDouble();
		this.calculating = buf.readBoolean();

		this.chargeReadyAudio = buf.readBoolean();
		this.graphMode = buf.readBoolean();
		this.status = BufferUtil.readString(buf);
		this.sabotType = buf.readInt();

		this.theta = buf.readDouble();
		this.dtheta = buf.readDouble();
		this.error_old = buf.readDouble();
		this.iterations = buf.readInt();
		this.t_max = buf.readDouble();
		this.x_target = buf.readDouble();
		this.v0 = buf.readFloat();
		this.bc = buf.readDouble();
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		this.active = nbt.getBoolean("active");
		this.power = nbt.getLong("power");
		this.charge = nbt.getLong("charge");
		this.charging = nbt.getBoolean("charging");
		this.targetVector = nbt.hasKey("targetX", 99) && nbt.hasKey("targetZ", 99) ? new Vector2d(
			nbt.getDouble("targetX"),
			nbt.getDouble("targetZ")
		) : null;
		this.rotationPitch = nbt.getDouble("rotationPitch");
		this.rotationYaw = nbt.getDouble("rotationYaw");
		this.theta = nbt.getDouble("theta");
		this.targetRotationYaw = nbt.getDouble("targetRotationYaw");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("active", this.active);
		nbt.setLong("power", this.power);
		nbt.setLong("charge", this.charge);
		nbt.setBoolean("charging", this.charging);
		if (this.targetVector != null) {
			nbt.setDouble("targetX", this.targetVector.x);
			nbt.setDouble("targetZ", this.targetVector.y);
		} else {
			nbt.removeTag("targetX");
			nbt.removeTag("targetZ");
		}
		nbt.setDouble("rotationPitch", this.rotationPitch);
		nbt.setDouble("rotationYaw", this.rotationYaw);
		nbt.setDouble("theta", this.theta);
		nbt.setDouble("targetRotationYaw", this.targetRotationYaw);
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerTurretRailgun(player.inventory, this);
	}

	@Override
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUITurretRailgun(player.inventory, this);
	}

	@Override
	public long getPower() {
		return this.power;
	}

	@Override
	public void setPower(long power) {
		this.power = power;
	}

	@Override
	public long getMaxPower() {
		return this.maxPower;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(EntityPlayer player, NBTTagCompound data) {
		if (data.hasKey("activeChange")) this.active = data.getBoolean("activeChange");
		if (data.hasKey("graphModeChange")) this.graphMode = data.getBoolean("graphModeChange");
		if (data.hasKey("dumpCharge") && this.charge > 0L) dumpCharge();
		if (data.hasKey("dataInput")) {
			String dataInput = data.getString("dataInput");
			dataInput = dataInput.toLowerCase();
			String[] params = dataInput.split(PARAM_SEPARATOR);
			if (!this.charging) {
				// set coords
				if (params.length == 2) {
					try {
						this.targetVector = new Vector2d(
							Integer.parseInt(params[0]),
							Integer.parseInt(params[1])
						);
						return;
					} catch (NumberFormatException e) {}
				} else if (dataInput.equals("fire") && this.targetVector != null && this.sabot != null) {
					commit();
					return;
				}
			}
			params = dataInput.split(NAME_SEPARATOR);
			if (params.length > 1) {
				if ((params[0]).equals("pitch")) {
					try {
						this.theta = Math.toRadians(MathHelper.clamp_int(Integer.parseInt(params[1]), -13, 90));
						this.markChanged();
					} catch (NumberFormatException e) {
					}
					return;
				}
				if ((params[0]).equals("yaw")) {
					try {
						this.targetRotationYaw = Math.toRadians(Integer.parseInt(params[1]) % 360);
						this.markChanged();
					} catch (NumberFormatException e) {
					}
					return;
				}
			}
			if (dataInput.equals("abort") && (this.calculating || this.charging)) abort();
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if (i == 0) return true; // battery
		if (i >= 1 && i <= 3 && stack.getItem() == ModItems.ammo_railgun) return true; // ammo slots
		return false;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(int meta) {
		return new int[] { 1, 2, 3 };
	}

	@Override
	public boolean canConnect(ForgeDirection dir) {
		return dir != ForgeDirection.DOWN;
	}

	// the initial velocity is in m/s
	private void spawnBullet(BulletConfig bullet, ItemStack projectile, float velocity) {

		Vec3 bulletVector = Vec3.createVectorHelper(barrelLength, 0D, 0D);
		bulletVector.rotateAroundZ((float) -this.rotationPitch);
		bulletVector.rotateAroundY((float) this.rotationYaw);

		EntityBulletBeamBase proj = new EntityBulletBeamBase(worldObj, bullet, 0F);
		proj.posX = xCoord + bulletVector.xCoord;
		proj.posY = yCoord + bulletVector.yCoord + 2.25;
		proj.posZ = zCoord + bulletVector.zCoord;
		proj.setRotationsFromVector(bulletVector);
		proj.beamLength = MathHelper.clamp_double(velocity/4D, 5D, 50D); // atomize whatever is in-front of the barrel for some blocks
		EntityRailgunProjectile uglyAssProj = new EntityRailgunProjectile(worldObj);
		uglyAssProj.setLocationAndAngles(proj.posX, proj.posY, proj.posZ, 0F, 0F);
		uglyAssProj.setThrowableHeading(bulletVector.xCoord, bulletVector.yCoord, bulletVector.zCoord, velocity/20F, 0F);
		uglyAssProj.setType(projectile.getItemDamage());
		if (projectile.getItemDamage() == ItemAmmoRailgun.FLUID) {
			// feed the loaded sabot's NBT fluid into the projectile so the fluid sabot payload
			// actually spawns meta consistent with what was loaded, instead of the shared instance fields
			uglyAssProj.fluidType = IFillableItem.getFluidType(projectile);
			uglyAssProj.fluidFill = (short) IFillableItem.getFluidFill(projectile);
		}

		worldObj.spawnEntityInWorld(proj); // first delete, then spawn the actual bullet
		worldObj.spawnEntityInWorld(uglyAssProj);
	}

	private void turnTowardsAngle(double targetPitch, double targetYaw) {

		double turnYaw = Math.toRadians(this.velYaw);
		double turnPitch = Math.toRadians(this.velPitch);
		double pi2 = Math.PI * 2;

		//if we are about to overshoot the target by turning, just snap to the correct rotation
		if(Math.abs(this.rotationPitch - targetPitch) < turnPitch || Math.abs(this.rotationPitch - targetPitch) > pi2 - turnPitch) {
			this.rotationPitch = targetPitch;
		} else {
			if(targetPitch > this.rotationPitch)
				this.rotationPitch += turnPitch;
			else
				this.rotationPitch -= turnPitch;
		}

		double deltaYaw = (targetYaw - this.rotationYaw) % pi2;

		//determines what direction the turret should turn
		//used to prevent situations where the turret would do almost a full turn when
		//the target is only a couple degrees off while being on the other side of the 360° line
		int dir = 0;

		if(deltaYaw < -Math.PI)
			dir = 1;
		else if(deltaYaw < 0)
			dir = -1;
		else if(deltaYaw > Math.PI)
			dir = -1;
		else if(deltaYaw > 0)
			dir = 1;

		if(Math.abs(this.rotationYaw - targetYaw) < turnYaw || Math.abs(this.rotationYaw - targetYaw) > pi2 - turnYaw)
			this.rotationYaw = targetYaw;
		else
			this.rotationYaw += turnYaw * dir;

		this.rotationYaw = this.rotationYaw % pi2;
		this.rotationPitch = this.rotationPitch % pi2;
	}

	public ItemStack getSabotLoaded() {
		for(int i = 1; i < 4; i++) {
			if(slots[i] != null) {
				if(slots[i].getItem() == ModItems.ammo_railgun) {
					return slots[i];
				}
			}
		}
		return null;
	}

	private void consumeAmmo(Item ammo) {
		for(int i = 1; i < 4; i++) {
			if(slots[i] != null && slots[i].getItem() == ammo) {
				this.decrStackSize(i, 1);
				return;
			}
		}
		this.markDirty();
	}

	// commence firing sequence
	private void commit() {
		this.calculating = true;
		this.theta = Math.PI / 2D;
		this.dtheta = Math.toRadians(1D);
		this.iterations = 0;
	}

	// abort all operations
	private void abort() {
		this.calculating = false;
		this.charging = false;
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:block.shutdown", 10F, 1F);
	}

	// this doesn't check for correct alignment. do i care? absolutely not, it's intended, be careful with ts
	private void dumpCharge() {
		this.fire((float) (this.v0 * Math.sqrt((double) this.charge / (double) maxCharge)));
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, "hbm:block.spark", 10F, 1F);
	}

	private void fire(float velocity) {
		this.spawnBullet(XFactoryFolly.folly_sm, this.sabot, velocity);
		this.consumeAmmo(ModItems.ammo_railgun);
		worldObj.playSoundEffect(xCoord, yCoord, zCoord, NTMSounds.GUN_PLEASE_REMOVE_MY_EARDRUMS_THANKS, 100F, 1F);
		this.charge = 0L;
		this.charging = false;
	}

	public double x_i(double time, double mass, double velX, double bCoeff) {
		return mass*velX/bCoeff*(1D-Math.exp(-(bCoeff/mass*time)));
	}

	public double y_i(double time, double mass, double velY, double bCoeff, double gravity) {
		return mass/bCoeff*(velY+mass*gravity/bCoeff)*(1D-Math.exp(-(bCoeff/mass*time)))-mass*gravity*time/bCoeff;
	}

	// full ass linear drag optimal angle solver, yes i had to manually solve the equations to make this and yes that's a PD controller
	private boolean calculateAngleTrajectory() {
		// all these constants are fine-tuned, making kp, kd or dt smaller will make them more precise, or tol bigger
		double dt = 0.01D; // delta time
		double kp = 0.001D; // p controller const
		double kd = 0.00001D; // d controller const
		double tol = Math.toRadians(1000000D); // can't be perfect so have to do this, guh. tolerance

		double v0x, v0y, A, k, x, D;
		double error, derivate;

		double pi2 = Math.PI / 2D;
		double d10 = Math.toRadians(10D);

		// capped so not to exceed 30 seconds of calculations, by that time it's either approximating by a reasonable margin or just failing to get a result
		if (Math.abs(Math.floor(this.dtheta * tol) / tol) != 0D && this.theta > 0D && this.iterations < 600) {
			v0x = this.v0*Math.cos(this.theta);
			v0y = this.v0*Math.sin(this.theta);
			A = v0y + this.m * g / this.bc;
			D = g / A;
			k = -this.bc / (this.m * D);
			this.t_max = this.m / this.bc * BobMathUtil.lambert_w(k * Math.exp(k), 0, 0) + 1D / D;
			x = x_i(this.t_max, this.m, v0x, this.bc);

			this.theta -= this.dtheta;
			this.theta = MathHelper.clamp_double(this.theta, -Math.toRadians(13D), pi2);
			error = this.x_target - x;
			derivate = (error - this.error_old) / dt;
			this.dtheta = Math.toRadians(kp * error + kd * derivate);
			this.dtheta = MathHelper.clamp_double(this.dtheta, -d10, d10);

			this.error_old = error;
			this.iterations++;

			return true;
		}
		return false;
	}

	@SideOnly(Side.CLIENT)
	protected List<ItemStack> ammoStacks;

	@SideOnly(Side.CLIENT)
	public List<ItemStack> getAmmoTypesForDisplay() {

		if(ammoStacks != null) return ammoStacks;

		ammoStacks = new ArrayList();

		List list = new ArrayList();
		ModItems.ammo_railgun.getSubItems(ModItems.ammo_railgun, MainRegistry.weaponTab, list);
		this.ammoStacks.addAll(list);

		return ammoStacks;
	}

	@Override
	@Optional.Method(modid = "OpenComputers")
	public String getComponentName() {
		return "ntm_turret_railgun";
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getAngle(Context context, Arguments args) {
		return new Object[] {Math.toDegrees(this.rotationPitch), Math.toDegrees(this.rotationYaw)};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getPos(Context context, Arguments args) {
		return new Object[] {xCoord, yCoord, zCoord};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getEnergyInfo(Context context, Arguments args) {
		return new Object[] {this.getPower(), this.getMaxPower()};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getBufferInfo(Context context, Arguments args) {
		return new Object[] {this.charge, this.maxCharge};
	}

	@Callback(direct = true)
	@Optional.Method(modid = "OpenComputers")
	public Object[] getLoadedSabot(Context context, Arguments args) {
		return new Object[] {SabotsNames(this.sabotType)};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setPitch(Context context, Arguments args) {
		this.theta = MathHelper.clamp_double(args.checkDouble(0), Math.toRadians(-13D), Math.PI);
		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setYaw(Context context, Arguments args) {
		this.targetRotationYaw = args.checkDouble(0) % Math.PI * 2;
		return new Object[] {};
	}

	@Callback(direct = false, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setDirection(Context context, Arguments args) {
		Vec3 directionVector = Vec3.createVectorHelper(args.checkDouble(0), args.checkDouble(1), args.checkDouble(2));
		this.theta = Math.asin(directionVector.yCoord / directionVector.lengthVector());
		this.targetRotationYaw = -Math.atan2(directionVector.zCoord, directionVector.xCoord);
		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setTarget(Context context, Arguments args) {
		this.targetVector = new Vector2d(args.checkDouble(0), args.checkDouble(1));
		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] setActive(Context context, Arguments args) {
		this.active = args.checkBoolean(0);
		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] fire(Context context, Arguments args) {
		this.commit();
		return new Object[] {};
	}

	@Callback(direct = true, limit = 4)
	@Optional.Method(modid = "OpenComputers")
	public Object[] dumpCharge(Context context, Arguments args) {
		this.dumpCharge();
		return new Object[] {};
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
			PREFIX_VALUE + "pitch",
			PREFIX_VALUE + "yaw",
			PREFIX_VALUE + "target",
			PREFIX_VALUE + "charge",
			PREFIX_VALUE + "loadedSabot",
			PREFIX_FUNCTION + "setActive" + NAME_SEPARATOR + "active (0 or 1)",
			PREFIX_FUNCTION + "pitch" + NAME_SEPARATOR + "angle (-13-90)",
			PREFIX_FUNCTION + "yaw" + NAME_SEPARATOR + "angle (0-360)",
			PREFIX_FUNCTION + "enqueue" + NAME_SEPARATOR + "x" + PARAM_SEPARATOR + "z",
			PREFIX_FUNCTION + "fire",
			PREFIX_FUNCTION + "abort",
			PREFIX_FUNCTION + "dump",
		};
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "setActive").equals(name) && params.length > 0) {
			try { this.active = (Integer.parseInt(params[0]) == 1); this.markChanged(); } catch(NumberFormatException e) {}
		}
		if((PREFIX_FUNCTION + "pitch").equals(name) && params.length > 0) {
			try { this.theta = Math.toRadians(MathHelper.clamp_int(Integer.parseInt(params[0]), -13, 90)); this.markChanged(); } catch(NumberFormatException e) {}
		}
		if((PREFIX_FUNCTION + "yaw").equals(name) && params.length > 0) {
			try { this.targetRotationYaw = Math.toRadians(Integer.parseInt(params[0])%360); this.markChanged(); } catch(NumberFormatException e) {}
		}
		if((PREFIX_FUNCTION + "enqueue").equals(name) && params.length > 1) {
			try {
				this.targetVector = new Vector2d(Integer.parseInt(params[0]), Integer.parseInt(params[1]));
				this.markChanged();
			} catch(NumberFormatException e) {}
		}
		if ((PREFIX_FUNCTION + "fire").equals(name)) commit();
		if ((PREFIX_FUNCTION + "abort").equals(name)) abort();
		if ((PREFIX_FUNCTION + "dump").equals(name)) dumpCharge();

		return null;
	}

	@Override
	public String provideRORValue(String name) {
		if((PREFIX_VALUE + "pitch").equals(name))		return "" + (int) Math.toDegrees(this.rotationPitch);
		if((PREFIX_VALUE + "yaw").equals(name))			return "" + (int) Math.toDegrees(this.rotationYaw);
		if((PREFIX_VALUE + "target").equals(name))		return "" + this.targetVector != null ? ((int) this.targetVector.x + ":" + (int) this.targetVector.y) : "Not set";
		if((PREFIX_VALUE + "charge").equals(name))		return "" + (int) (this.charge * 100 / this.maxCharge);
		if((PREFIX_VALUE + "loadedSabot").equals(name)) return "" + SabotsNames(this.sabotType);
		return null;
	}
}
