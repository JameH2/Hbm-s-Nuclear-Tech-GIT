package com.hbm.items.weapon;

import api.hbm.fluidmk2.IFillableItem;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.entity.projectile.EntityRailgunProjectile;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.util.BobMathUtil;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;

import java.util.List;

public class ItemAmmoRailgun extends Item implements IFillableItem {

	public static RailgunSabot[] itemTypes = new RailgunSabot[ /* >>> */ 6 /* <<< */ ];
	public static final int TUNGSTEN = 0;
	public static final int DU = 1;
	public static final int NUKE = 2;
	public static final int DESH = 3;
	public static final int STARMETAL = 4;
	public static final int FLUID = 5;

	public ItemAmmoRailgun() {
		this.setHasSubtypes(true);
		this.setCreativeTab(MainRegistry.weaponTab);
		this.setTextureName(RefStrings.MODID + ":bolt");
		init();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List list) {
		list.add(new ItemStack(item, 1, TUNGSTEN));
		list.add(new ItemStack(item, 1, DU));
		list.add(new ItemStack(item, 1, NUKE));
		list.add(new ItemStack(item, 1, DESH));
		list.add(new ItemStack(item, 1, STARMETAL));
		list.add(new ItemStack(item, 1, FLUID));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool) {

		String r = EnumChatFormatting.RED + "";
		String y = EnumChatFormatting.YELLOW + "";

		switch(stack.getItemDamage()) {
			case TUNGSTEN:
				list.add(y + "Tungsten core APFSDS");
				break;
			case DU:
				list.add(y + "Depleted uranium core APFSDS");
				break;
			case NUKE:
				list.add(r + "bigass nuke FSDS");
				break;
			case DESH:
				list.add(y + "First impact results in a ricochet.");
				list.add(y + "APFSDS");
				break;
			case STARMETAL:
				list.add(y + "More aerodynamic T-APDSFS");
				break;
			case FLUID:
				list.add(y + "Fluid filled APDSFS");
				FluidSabot fluidSabot = (FluidSabot) itemTypes[FLUID];
				list.add(y + "> " + fluidSabot.getType(stack).getLocalizedName());
				list.add(y + "> " + fluidSabot.getFill(stack) + "/" + fluidSabot.getCapacity(stack) + "mB");
				break;
		}
		list.add("===================================");
		list.add(y + "Initial velocity: " + BobMathUtil.getShortNumber((long) itemTypes[stack.getItemDamage()].v0) + "m/s");
		list.add(y + "Ballistic coeff.: " + itemTypes[stack.getItemDamage()].bc + "kgf/m²");
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return "item.ammo_railgun_" + itemTypes[Math.abs(stack.getItemDamage()) % itemTypes.length].name;
	}

	public static void standardExplosion(EntityRailgunProjectile shell, MovingObjectPosition mop, float size, float rangeMod, boolean breaksBlocks) {
		Vec3 vec = Vec3.createVectorHelper(shell.motionX, shell.motionY, shell.motionZ).normalize();
		ExplosionVNT xnt = new ExplosionVNT(shell.worldObj, mop.hitVec.xCoord - vec.xCoord, mop.hitVec.yCoord - vec.yCoord, mop.hitVec.zCoord - vec.zCoord, size);
		if(breaksBlocks) {
			xnt.setBlockAllocator(new BlockAllocatorStandard(48));
			xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop().withBlockEffect(new BlockMutatorDebris(ModBlocks.block_slag, 1)));
		}
		xnt.setEntityProcessor(new EntityProcessorCross(7.5D).withRangeMod(rangeMod));
		xnt.setPlayerProcessor(new PlayerProcessorStandard());
		xnt.explode();
		shell.killAndClear();
	}

	public abstract class RailgunSabot {
		public final String name;
		public final ResourceLocation texture;
		public double bc;
		public float v0;

		public RailgunSabot(String name, String texture, double bc, float v0) {
			this.name = name;
			this.texture = new ResourceLocation(RefStrings.MODID + ":textures/models/projectiles/" + texture + ".png");
			this.bc = bc; // ballistic coefficient [kgf*m^-2]
			this.v0 = v0; // initial velocity [m/s]
		}

		public abstract void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop);

		public void onUpdate(EntityRailgunProjectile sabot) { }
	}

	// specifically made for the fluid sabot
	//TODO: fix this so to be an actual fillable item. Hypothesis: it gets casted to RailgunSabot in the array, so when the item gets actually registered it doesn't actually inherit any function
	private class FluidSabot extends RailgunSabot implements IFillableItem {

		public short maxFill;
		public short fill;
		public FluidType type;

		public FluidSabot(String name, String texture, double bc, float v0, short maxFill) {
			super(name, texture, bc, v0);
			this.maxFill = maxFill;
		}

		public void initNBT(ItemStack stack) {
			stack.stackTagCompound = new NBTTagCompound();
			this.setFill(stack, Fluids.NONE, (short) 0); // sets "type" and "fill" NBT
			stack.stackTagCompound.setShort("capacity", this.maxFill); // set "capacity"
		}

		public FluidType getType(ItemStack stack) {
			if(!stack.hasTagCompound()) {
				initNBT(stack);
			}

			return Fluids.fromID(stack.stackTagCompound.getShort("type"));
		}

		public short getCapacity(ItemStack stack) {
			if(!stack.hasTagCompound()) {
				initNBT(stack);
			}

			return stack.stackTagCompound.getShort("capacity");
		}

		public void setFill(ItemStack stack, FluidType type, short fill) {
			if(!stack.hasTagCompound())
				initNBT(stack);

			stack.stackTagCompound.setShort("type", (short) type.getID());
			stack.stackTagCompound.setShort("fill", fill);
			// hackiest way possible of doing this award
			// can't access the nbt tags so we have this now smh
			this.type = type;
			this.fill = fill;
		}

		@Override
		public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) { }

		@Override
		public boolean acceptsFluid(FluidType type, ItemStack stack) {
			return (type == this.getType(stack) || this.getFill(stack) == 0) && (!type.isAntimatter()) && type.isDispersable();
		}

		@Override
		public int tryFill(FluidType type, int amount, ItemStack stack) {
			if(!acceptsFluid(type, stack))
				return amount;

			if(this.getFill(stack) == 0)
				this.setFill(stack, type, (short) 0);

			int req = this.getCapacity(stack) - this.getFill(stack);
			int toFill = Math.min(req, amount);

			this.setFill(stack, type, (short) (this.getFill(stack) + toFill));

			return amount - toFill;
		}

		@Override
		public boolean providesFluid(FluidType type, ItemStack stack) {
			return this.getType(stack) == type;
		}

		@Override
		public int tryEmpty(FluidType type, int amount, ItemStack stack) {
			if(providesFluid(type, stack)) {
				int toUnload = Math.min(amount, this.getFill(stack));
				this.setFill(stack, type, (short) (this.getFill(stack) - toUnload));
				if(this.getFill(stack) == 0)
					this.setFill(stack, Fluids.NONE, (short) 0);
				return toUnload;
			}
			return amount;
		}

		@Override
		public FluidType getFirstFluidType(ItemStack stack) {
			return this.getType(stack);
		}

		@Override
		public int getFill(ItemStack stack) {
			if(!stack.hasTagCompound())
				initNBT(stack);

			return stack.stackTagCompound.getShort("fill");
		}
	}

	private void init() {
		// tungsten sabot
		this.itemTypes[TUNGSTEN] = new RailgunSabot("tungsten", "railgun_tungsten", 0.2D, 1000F) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				standardExplosion(sabot, mop, 10F, 3F, true);
				ExplosionCreator.composeEffect(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 5, 0, 20, 0.75F, 1F, -2F, 150);
			}
		};

		// depleted uranium sabot
		this.itemTypes[DU] = new RailgunSabot("du", "railgun_du", 0.2D, 1100F) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				standardExplosion(sabot, mop, 15F, 3F, true);
				ExplosionCreator.composeEffect(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
			}
		};

		// nuke sabot
		this.itemTypes[NUKE] = new RailgunSabot("nuke", "railgun_nuke", 0.2D, 750F) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				EntityNukeExplosionMK3 ex = new EntityNukeExplosionMK3(sabot.worldObj);
				ex.posX = mop.hitVec.xCoord + 0.5;
				ex.posY = mop.hitVec.yCoord + 0.5;
				ex.posZ = mop.hitVec.zCoord + 0.5;
				ex.destructionRange = 50;
				ex.speed = BombConfig.blastSpeed;
				ex.coefficient = 1.0F;
				ex.waste = false;
				sabot.worldObj.spawnEntityInWorld(ex);

				sabot.worldObj.playSoundEffect(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, "random.explode", 100000.0F, 1.0F);

				EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(sabot.worldObj, 50);
				cloud.posX = mop.hitVec.xCoord;
				cloud.posY = mop.hitVec.yCoord;
				cloud.posZ = mop.hitVec.zCoord;
				sabot.worldObj.spawnEntityInWorld(cloud);

				sabot.worldObj.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(sabot.worldObj, BombConfig.missileRadius, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord));
				EntityNukeTorex.statFacStandard(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 150);
				sabot.setDead();
			}
		};

		// desh sabot, ricochets at the first landing
		this.itemTypes[DESH] = new RailgunSabot("desh", "railgun_desh", 0.2D, 1000F) {
			boolean firstLanding = false;

			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				if (!this.firstLanding) {
					this.firstLanding = true;

					Vec3 face = null;

					switch(mop.sideHit) {
						case 0: face = Vec3.createVectorHelper(0, -1, 0); break;
						case 1: face = Vec3.createVectorHelper(0, 1, 0); break;
						case 2: face = Vec3.createVectorHelper(0, 0, 1); break;
						case 3: face = Vec3.createVectorHelper(0, 0, -1); break;
						case 4: face = Vec3.createVectorHelper(-1, 0, 0); break;
						case 5: face = Vec3.createVectorHelper(1, 0, 0); break;
					}

					if (face != null) {
						Vec3 vel = Vec3.createVectorHelper(sabot.motionX, sabot.motionY, sabot.motionZ);
						vel.normalize();

						double angle = Math.toRadians(BobMathUtil.getCrossAngle(vel, face));
						// get the fuck back
						sabot.posX -= sabot.motionX;
						sabot.posY -= sabot.motionY;
						sabot.posZ -= sabot.motionZ;

						sabot.motionX *= -1D;
						sabot.motionY = sabot.motionY * -1D * Math.sin(2D * angle);
						sabot.motionZ *= -1D;

						sabot.setPosition(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
						sabot.worldObj.playSoundAtEntity(sabot, "hbm:weapon.ricochet", 40F, 1F);
					}
				} else {
					standardExplosion(sabot, mop, 15F, 3F, true);
					ExplosionCreator.composeEffect(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
				}
			}
		};

		// starmetal sabot
		this.itemTypes[STARMETAL] = new RailgunSabot("starmetal", "railgun_starmetal", 0.1D, 1500F) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				standardExplosion(sabot, mop, 15F, 3F, true);
				ExplosionCreator.composeEffect(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 7, 0, 20, 0.85F, 1F, -2F, 200);
			}
		};

		// fluid sabot
		this.itemTypes[FLUID] = new FluidSabot("fluid", "railgun_fluid", 0.2D, 1000F, (short) 1_000) {
			public void onImpact(EntityRailgunProjectile sabot, MovingObjectPosition mop) {
				ExplosionCreator.composeEffect(sabot.worldObj, mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord, 10, 2F, 0.5F, 25F, 5, 0, 20, 0.75F, 1F, -2F, 150);

				if(sabot.fluidType.isDispersable() && sabot.fluidFill > 0) {
					EntityMist mist = new EntityMist(sabot.worldObj);
					mist.setType(sabot.fluidType);
					mist.setPosition(sabot.posX, sabot.posY, sabot.posZ);
					mist.setArea(10, 5);
					mist.setDuration(sabot.fluidFill * 200 / this.maxFill);
					sabot.worldObj.spawnEntityInWorld(mist);
					sabot.setDead();
				}
			}
		};
	}

	private FluidSabot fluidSabot(ItemStack stack) {
		return Math.abs(stack.getItemDamage()) == FLUID && itemTypes[FLUID] instanceof FluidSabot ? (FluidSabot) itemTypes[FLUID] : null;
	}

	@Override
	public boolean acceptsFluid(FluidType type, ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot != null && sabot.acceptsFluid(type, stack);
	}

	@Override
	public int tryFill(FluidType type, int amount, ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot == null ? amount : sabot.tryFill(type, amount, stack);
	}

	@Override
	public boolean providesFluid(FluidType type, ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot != null && sabot.providesFluid(type, stack);
	}

	@Override
	public int tryEmpty(FluidType type, int amount, ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot == null ? 0 : sabot.tryEmpty(type, amount, stack);
	}

	@Override
	public FluidType getFirstFluidType(ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot == null ? Fluids.NONE : sabot.getFirstFluidType(stack);
	}

	@Override
	public int getFill(ItemStack stack) {
		FluidSabot sabot = fluidSabot(stack);
		return sabot == null ? 0 : sabot.getFill(stack);
	}
}
