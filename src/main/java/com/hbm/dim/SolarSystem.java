package com.hbm.dim;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.NotImplementedException;

import com.hbm.config.SpaceConfig;
import com.hbm.dim.trait.CBT_Atmosphere;
import com.hbm.dim.trait.CBT_Temperature;
import com.hbm.dim.trait.CBT_Water;
import com.hbm.dim.trait.CelestialBodyTrait.CBT_BATTLEFIELD;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDysonReceiver;
import com.hbm.util.AstronomyUtil;
import com.hbm.util.BobMathUtil;

import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class SolarSystem {

	public static CelestialBody kerbol;

	// How much to scale celestial objects when rendering
	public static final double RENDER_SCALE = 180;
	public static final double SUN_RENDER_SCALE = 4;

	public static final double ORRERY_MAX_RADIUS = 20_000;
	public static final double ORRERY_MIN_RADIUS = 2_000;


	public static void init() {
		// All values pulled directly from KSP, most values are auto-converted to MC friendly ones
		kerbol = new CelestialBody("kerbol")
			.withMassRadius(1.757e28F, 261_600)
			.withRotationalPeriod(432_000)
			.withTexture(new ResourceLocation("textures/environment/sun.png"))
			.withShader(new ResourceLocation(RefStrings.MODID, "shaders/blackhole.frag"), 3) // Only shows when CBT_Destroyed
			.withSatellites(

				new CelestialBody("moho", SpaceConfig.mohoDimension, Body.MOHO)
					.withMassRadius(2.526e21F, 250)
					.withOrbitalParameters(5_263_138, 0.2F, 15.0F, 7.0F, 70.0F)
					.withRotationalPeriod(210_000)
					.withColor(0.4863F, 0.4F, 0.3456F)
					.withBlockTextures(RefStrings.MODID + ":moho_stone", "", "", "")
					.withAxialTilt(30F)
					.withTraits(new CBT_Temperature(200)),

				new CelestialBody("eve", SpaceConfig.eveDimension, Body.EVE)
					.withMassRadius(1.224e23F, 700)
					.withOrbitalParameters(9_832_684, 0.01F, 0.0F, 2.1F, 15.0F)
					.withRotationalPeriod(80_500)
					.withColor(0.408F, 0.298F, 0.553F)
					.withBlockTextures(RefStrings.MODID + ":eve_stone_2", "", "", "")
					.withMinProcessingLevel(2)
					.withTraits(new CBT_Atmosphere(Fluids.EVEAIR, 5D), new CBT_Temperature(400), new CBT_Water(Fluids.MERCURY))
					.withSatellites(

						new CelestialBody("gilly")
							.withMassRadius(1.242e17F, 13)
							.withOrbitalParameters(31_500, 0.55F, 10.0F, 12.0F, 80.0F)
							.withRotationalPeriod(28_255)
							.withTexture(new ResourceLocation(RefStrings.MODID, "textures/misc/space/planet.png"))

					),

				new CelestialBody("kerbin", 0, Body.KERBIN) // overworld
					.withMassRadius(5.292e22F, 600)
					.withOrbitalParameters(13_599_840, 0.0F, 0.0F, 0.0F, 0.0F)
					.withRotationalPeriod(21_549)
					.withColor(0.608F, 0.914F, 1.0F)
					.withTraits(new CBT_Atmosphere(Fluids.AIR, 1D), new CBT_Water())
					.withCityMask(new ResourceLocation(RefStrings.MODID, "textures/misc/space/kerbin_mask.png"))
					.withBiomeMask(new ResourceLocation(RefStrings.MODID, "textures/misc/space/kerbin_biomes.png"))
					.withSatellites(

						new CelestialBody("mun", SpaceConfig.moonDimension, Body.MUN)
							.withMassRadius(9.76e20F, 200)
							.withOrbitalParameters(12_000, 0.054F, 0.0F, 5.15F, 17.0F)
							.withRotationalPeriod(138_984)
							.withTidalLockingTo("kerbin")
							.withBlockTextures(RefStrings.MODID + ":moon_rock", "", "", ""),

						new CelestialBody("minmus", SpaceConfig.minmusDimension, Body.MINMUS)
							.withMassRadius(2.646e19F, 60)
							.withOrbitalParameters(47_000, 0, 38.0F, 6.0F, 78.0F)
							.withRotationalPeriod(40_400)
							.withBlockTextures(RefStrings.MODID + ":minmus_stone", "", "", "")
							.withTraits(new CBT_Water(Fluids.MILK))

					),

				new CelestialBody("duna", SpaceConfig.dunaDimension, Body.DUNA)
					.withMassRadius(4.515e21F, 320)
					.withOrbitalParameters(20_726_155, 0.05F, 0.0F, 0.06F, 135.5F)
					.withRotationalPeriod(65_518)
					.withTidalLockingTo("ike")
					.withColor(0.6471f, 0.2824f, 0.1608f)
					.withBlockTextures(RefStrings.MODID + ":duna_rock", "", "", "")
					.withTraits(new CBT_Atmosphere(Fluids.DUNAAIR, 0.1D))
					.withCityMask(new ResourceLocation(RefStrings.MODID, "textures/misc/space/duna_mask.png"))
					.withSatellites(

						new CelestialBody("ike", SpaceConfig.ikeDimension, Body.IKE)
							.withMassRadius(2.782e20F, 130)
							.withOrbitalParameters(3_200, 0.03F, 0.0F, 0.2F, 0.0F)
							.withBlockTextures(RefStrings.MODID + ":ike_stone", "", "", "")
							.withRotationalPeriod(65_518)
							.withTidalLockingTo("duna")
							.withTraits(new CBT_Water(Fluids.BROMINE))

					),

				new CelestialBody("dres", SpaceConfig.dresDimension, Body.DRES)
					.withMassRadius(3.219e20F, 138)
					.withOrbitalParameters(40_839_348, 0.145F, 90.0F, 5.0F, 280.0F)
					.withRotationalPeriod(34_800)
					.withBlockTextures(RefStrings.MODID + ":dresbase", "", "", "")
					.withMinProcessingLevel(2),


				new CelestialBody("jool")
					.withMassRadius(4.233e24F, 6_000)
					.withOrbitalParameters(68_773_560, 0.05F, 0.0F, 1.304F, 52.0F)
					.withRotationalPeriod(36_000)
					.withColor(0.4588f, 0.6784f, 0.3059f)
					.withGas(Fluids.JOOLGAS)
					.withSatellites(

						new CelestialBody("laythe", SpaceConfig.laytheDimension, Body.LAYTHE)
							.withMassRadius(2.94e22F, 500)
							.withOrbitalParameters(27_184, 0.0288F, 0.0F, 0.348F, 0.0F)
							.withRotationalPeriod(52_981)
							.withTidalLockingTo("jool")
							.withMinProcessingLevel(3)
							.withTraits(new CBT_Atmosphere(Fluids.AIR, 0.45D).and(Fluids.XENON, 0.15D), new CBT_Water())
							.withCityMask(new ResourceLocation(RefStrings.MODID, "textures/misc/space/laythe_mask.png")),

						new CelestialBody("vall") //probably
							.withMassRadius(3.109e21F, 300)
							.withOrbitalParameters(43_152, 0.111F, 342.9F, 7.48F, 128.0F)
							.withRotationalPeriod(105_962),

						new CelestialBody("tylo") // what value is this planet gonna add???
							.withMassRadius(4.233e22F, 600)
							.withOrbitalParameters(68_500, 0.002F, 0.0F, 0.3F, 0.0F)
							.withRotationalPeriod(211_926),

						new CelestialBody("bop")
							.withMassRadius(3.726e19F, 65)
							.withOrbitalParameters(128_500, 0.235F, 25.0F, 15F, 10.0F)
							.withRotationalPeriod(544_507),

						new CelestialBody("pol")
							.withMassRadius(1.081e19F, 44)
							.withOrbitalParameters(179_890, 0.171F, 15.0F, 4.25F, 2.0F)
							.withRotationalPeriod(901_902)

					),

				new CelestialBody("sarnus")
					.withMassRadius(1.223e24F, 5_300)
					.withOrbitalParameters(125_798_522, 0.0534F, 0.0F, 2.02F, 184.0F)
					.withRotationalPeriod(28_500)
					.withColor(1f, 0.6862f, 0.5882f)
					.withRings(10.0F, 3, 0.6F, 0.4F, 0.3F)
					.withGas(Fluids.SARNUSGAS)
					.withSatellites(

					new CelestialBody("hale") //no
						.withMassRadius(1.2166e16F, 6)
						.withOrbitalParameters(10_488, 0, 0.0F, 1.0F, 55.0F)
						.withRotationalPeriod(23_555),

					new CelestialBody("ovok") //nah
						.withMassRadius(4.233e17F, 26)
						.withOrbitalParameters(12_169, 0.01F, 0.0F, 1.5F, 55.0F)
						.withRotationalPeriod(29_440),

					new CelestialBody("eeloo") //will add
						.withMassRadius(1.115e21F, 210)
						.withOrbitalParameters(19_106, 0.0034F, 0.0F, 2.3F, 55.0F)
						.withRotationalPeriod(57_915),

					new CelestialBody("slate") //not you tho
						.withMassRadius(2.965e22F, 540)
						.withOrbitalParameters(42_593, 0.04F, 0.0F, 2.3F, 55.0F)
						.withRotationalPeriod(192_771),

					new CelestialBody("tekto", SpaceConfig.tektoDimension, Body.TEKTO)
						.withMassRadius(2.883e21F, 480)
						.withOrbitalParameters(67_355, 0.028F, 0.0F, 9.4F, 55.0F)
						.withRotationalPeriod(57_915)
						.withAxialTilt(25F)
						.withMinProcessingLevel(3)
						.withTraits(new CBT_Atmosphere(Fluids.TEKTOAIR, 1.5F), new CBT_Water(Fluids.CCL)) // :)
						.withBlockTextures(RefStrings.MODID + ":basalt", "", "", "")

				),

				new CelestialBody("neidon")
					.withMassRadius(2.1228e23F, 2_145)
					.withOrbitalParameters(409_355_192, 0.0534F, 0.0F, 2.02F, 184.0F)
					.withRotationalPeriod(40_250)
					.withColor(1f, 0.6862f, 0.5882f)
					.withSatellites(

					new CelestialBody("thatmo")
						.withMassRadius(2.788e21F, 286)
						.withOrbitalParameters(32_301, 0.0534F, 0.0F, 4.02F, 284.0F)
						.withRotationalPeriod(306_443)
						.withTraits(new CBT_Atmosphere(Fluids.NITROGEN, 0.005F), new CBT_BATTLEFIELD()),

					new CelestialBody("nissee") // words cannot express how much i actually fear this moon whenever im passing by it when playing opm. theres more that meets the eye and no one is brave enough to admit that
						.withMassRadius(5.951e18F, 30)
						.withOrbitalParameters(487_744, 0.0534F, 0.0F, 45.02F, 84.0F)
						.withRotationalPeriod(27_924)
						.withMinProcessingLevel(3)
					)
			);

		runTests();
	}

	// Simple enum used for blocks and items
	public enum Body {
		ORBIT(""),
		KERBIN("kerbin"),
		MUN("mun"),
		MINMUS("minmus"),
		DUNA("duna"),
		MOHO("moho"),
		DRES("dres"),
		EVE("eve"),
		IKE("ike"),
		LAYTHE("laythe"),
		TEKTO("tekto");
		//THATMO("thatmo"); sit this one out buddy :)

		public String name;

		Body(String name) {
			this.name = name;
		}

		// memoising, since ore rendering would be horrendous otherwise
		private CelestialBody body;
		public CelestialBody getBody() {
			if(this == ORBIT)
				return null;

			if(body == null)
				body = CelestialBody.getBody(name);

			return body;
		}

		public int getProcessingLevel(CelestialBody from) {
			if(this == ORBIT) return 0;
			return getBody().getProcessingLevel(from);
		}

		public String getStoneTexture() {
			if(this == ORBIT) return null;
			return getBody().stoneTexture;
		}

		public int getDimensionId() {
			if(this == ORBIT) return SpaceConfig.orbitDimension;
			return getBody().dimensionId;
		}
	}

	public static class AstroMetric {

		// Convert a solar system into a set of metrics defining their position and size in the sky for a given body

		public double distance;
		public double angle;
		public double inclination;
		public double apparentSize;
		public double phase;
		public double phaseObscure;

		protected Vec3 position;

		public CelestialBody body;

		public AstroMetric(CelestialBody body, Vec3 position) {
			this.body = body;
			this.position = position;
		}

	}

	public static class OrreryMetric {

		// Similar to above, but just for exaggerated positions + orbital paths
		public Vec3 position;
		public Vec3[] orbitalPath;

		public CelestialBody body;

		private static final int PATH_RESOLUTION = 32;

		public OrreryMetric(CelestialBody body, Vec3 position) {
			this.body = body;
			this.position = position;
			this.orbitalPath = new Vec3[PATH_RESOLUTION];
		}

	}

	/**
	 * Celestial mechanics
	 */

	// Generates a map of positions and orbital paths, with non-linear scaling to compress
	public static List<OrreryMetric> calculatePositionsOrrery(World world, float partialTicks) {
		List<OrreryMetric> metrics = new ArrayList<OrreryMetric>();

		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursiveOrrery(metrics, null, CelestialBody.getBody(world).getStar(), ticks, 0);

		return metrics;
	}

	// Create an ordered list for rendering all bodies within the system, minus the parent star
	public static List<AstroMetric> calculateMetricsFromBody(World world, float partialTicks, CelestialBody body, float solarAngle) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		// You know not the horrors I have suffered through, in order to fix tidal locking even betterer
		double rotOffset = -120.0F;
		CelestialBody tidalLockedBody = body.tidallyLockedTo != null ? CelestialBody.getBody(body.tidallyLockedTo) : null;
		if(tidalLockedBody != null) {
			// If locked to parent, adjust child
			if(tidalLockedBody.getPlanet() != body) {
				tidalLockedBody = body;
				rotOffset += 180.0F;
			}
		}

		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, body.getStar(), ticks, tidalLockedBody, solarAngle * 360.0, rotOffset);

		// Get the metrics from a given body
		calculateMetricsFromBody(metrics, body);

		// Sort by increasing distance
		metrics.sort((a, b) -> {
			return (int)(b.distance - a.distance);
		});

		return metrics;
	}

	public static List<AstroMetric> calculateMetricsFromSatellite(World world, float partialTicks, CelestialBody orbiting, double altitude) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, orbiting.getStar(), ticks);

		// Add our orbiting satellite position
		Vec3 position = calculatePositionSatellite(orbiting, altitude, ticks);
		for(AstroMetric metric : metrics) {
			if(metric.body == orbiting) {
				position = position.addVector(metric.position.xCoord, metric.position.yCoord, metric.position.zCoord);
				break;
			}
		}

		// Get the metrics from the orbiting position
		calculateMetricsFromPosition(metrics, position);

		// Sort by increasing distance
		metrics.sort((a, b) -> {
			return (int)(b.distance - a.distance);
		});

		return metrics;
	}

	public static List<AstroMetric> calculateMetricsBetweenSatelliteOrbits(World world, float partialTicks, CelestialBody from, CelestialBody to, double fromAltitude, double toAltitude, double t) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, from.getStar(), ticks);

		// Add our orbiting satellite position
		Vec3 fromPos = calculatePositionSatellite(from, fromAltitude, ticks);
		Vec3 toPos = calculatePositionSatellite(to, toAltitude, ticks);
		for(AstroMetric metric : metrics) {
			if(metric.body == from) {
				fromPos = fromPos.addVector(metric.position.xCoord, metric.position.yCoord, metric.position.zCoord);
			}
			if(metric.body == to) {
				toPos = toPos.addVector(metric.position.xCoord, metric.position.yCoord, metric.position.zCoord);
			}
		}

		// Lerp smoothly between the two positions (maybe a fancy circular lerp somehow?)
		Vec3 position = lerp(fromPos, toPos, t);

		// Get the metrics from the orbiting position
		calculateMetricsFromPosition(metrics, position);

		// Sort by increasing distance
		metrics.sort((a, b) -> {
			return (int)(b.distance - a.distance);
		});

		return metrics;
	}

	// Also expensive, but used infrequently by the server to calculate orbital transfer time
	public static double calculateDistanceBetweenTwoBodies(World world, CelestialBody from, CelestialBody to) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = (double)world.getTotalWorldTime() * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, from.getStar(), ticks);

		Vec3 fromPos = Vec3.createVectorHelper(0, 0, 0);
		Vec3 toPos = Vec3.createVectorHelper(0, 0, 0);
		for(AstroMetric metric : metrics) {
			if(metric.body == from) fromPos = metric.position;
			if(metric.body == to) toPos = metric.position;
		}

		return fromPos.distanceTo(toPos);
	}

	private static Vec3 lerp(Vec3 from, Vec3 to, double t) {
		double x = BobMathUtil.clampedLerp(from.xCoord, to.xCoord, t);
		double y = BobMathUtil.clampedLerp(from.yCoord, to.yCoord, t);
		double z = BobMathUtil.clampedLerp(from.zCoord, to.zCoord, t);

		return Vec3.createVectorHelper(x, y, z);
	}

	// Recursively calculate the XYZ position of all planets from polar coordinates + time
	private static void calculatePositionsRecursive(List<AstroMetric> metrics, AstroMetric parentMetric, CelestialBody body, double ticks) {
		calculatePositionsRecursive(metrics, parentMetric, body, ticks, null, 0, 0);
	}

	private static void calculatePositionsRecursive(List<AstroMetric> metrics, AstroMetric parentMetric, CelestialBody body, double ticks, CelestialBody lockBody, double solarAngle, double rotOffset) {
		Vec3 parentPosition = parentMetric != null ? parentMetric.position : Vec3.createVectorHelper(0, 0, 0);

		for(CelestialBody satellite : body.satellites) {
			Vec3 position = satellite == lockBody
				? calculatePositionFromAngle(satellite, solarAngle + calculateSiderealAngle(satellite, ticks) - Math.toDegrees(satellite.argumentPeriapsis) - Math.toDegrees(satellite.ascendingNode) + rotOffset)
				: calculatePositionFromTime(satellite, ticks);
			position = position.addVector(parentPosition.xCoord, parentPosition.yCoord, parentPosition.zCoord);
			AstroMetric metric = new AstroMetric(satellite, position);

			metrics.add(metric);

			calculatePositionsRecursive(metrics, metric, satellite, ticks, lockBody, solarAngle, rotOffset);
		}
	}

	// Creates a "mock" system, with positions neatly divided
	private static void calculatePositionsRecursiveOrrery(List<OrreryMetric> metrics, OrreryMetric parentMetric, CelestialBody body, double ticks, int depth) {
		Vec3 parentPosition = parentMetric != null ? parentMetric.position : Vec3.createVectorHelper(0, 0, 0);

		double distance = Math.min(body.radiusKm, ORRERY_MAX_RADIUS) * 1.42;
		for(CelestialBody satellite : body.satellites) {
			double extraDistance = MathHelper.clamp_double(satellite.radiusKm, ORRERY_MIN_RADIUS, ORRERY_MAX_RADIUS) * ((1 + satellite.eccentricity) * 2) * 1.42;
			for(CelestialBody inner : satellite.satellites) {
				extraDistance += MathHelper.clamp_double(inner.radiusKm, ORRERY_MIN_RADIUS, ORRERY_MAX_RADIUS) * ((1 + inner.eccentricity) * 2) * 2;
			}

			// distance is combined radii * hypotenuse (so cubes don't intersect at edges)
			distance += extraDistance;

			Vec3 position = calculatePositionFromTime(satellite, ticks, distance);
			position = position.addVector(parentPosition.xCoord, parentPosition.yCoord, parentPosition.zCoord);

			OrreryMetric metric = new OrreryMetric(satellite, position);
			for(int i = 0; i < metric.orbitalPath.length; i++) {
				metric.orbitalPath[i] = calculatePositionFromAngle(satellite, (float)i / metric.orbitalPath.length * 360, distance)
					.addVector(parentPosition.xCoord, parentPosition.yCoord, parentPosition.zCoord);
			}

			metrics.add(metric);

			distance += extraDistance;

			calculatePositionsRecursiveOrrery(metrics, metric, satellite, ticks, depth + 1);
		}
	}

	// Calculates the position of the body around its parent
	private static Vec3 calculatePositionFromTime(CelestialBody body, double ticks) {
		return calculatePositionFromTime(body, ticks, body.semiMajorAxisKm);
	}

	private static Vec3 calculatePositionFromTime(CelestialBody body, double ticks, double semiMajorAxis) {
		// Get mean anomaly, or how far (in radians) a planet has gone around its parent
		double yearTicks = body.getOrbitalPeriod() * (double)AstronomyUtil.TICKS_IN_DAY;
		double meanAnomaly = 2 * Math.PI * (ticks / yearTicks);

		return calculatePosition(body, meanAnomaly, semiMajorAxis);
	}

	private static Vec3 calculatePositionFromAngle(CelestialBody body, double angle) {
		return calculatePosition(body, Math.toRadians(angle), body.semiMajorAxisKm);
	}

	private static Vec3 calculatePositionFromAngle(CelestialBody body, double angle, double semiMajorAxis) {
		return calculatePosition(body, Math.toRadians(angle), semiMajorAxis);
	}

	private static Vec3 calculatePosition(CelestialBody body, double meanAnomaly, double semiMajorAxis) {
		double eccentricAnomaly = calculateEccentricAnomaly(meanAnomaly, body.eccentricity);

		// Orbital plane
		double x = semiMajorAxis * (Math.cos(eccentricAnomaly) - body.eccentricity);
		double y = semiMajorAxis * body.semiMinorAxisFactor * Math.sin(eccentricAnomaly);
		double z = 0;

		// Rotate by argument of periapsis
		double px = x;
		x = Math.cos(body.argumentPeriapsis) * px - Math.sin(body.argumentPeriapsis) * y;
		y = Math.sin(body.argumentPeriapsis) * px + Math.cos(body.argumentPeriapsis) * y;

		// Rotate by inclination
		z = Math.sin(body.inclination) * y;
		y = Math.cos(body.inclination) * y;

		// Rotate by longitude of ascending node
		px = x;
		x = Math.cos(body.ascendingNode) * px - Math.sin(body.ascendingNode) * y;
		y = Math.sin(body.ascendingNode) * px + Math.cos(body.ascendingNode) * y;

		return Vec3.createVectorHelper(x, y, z);
	}

	// Same but for an arbitrary satellite around a body
	private static Vec3 calculatePositionSatellite(CelestialBody body, double altitude, double ticks) {
		// The mean anomaly (angular position in circular orbit) measured in radians
		double orbitalPeriod = 2 * Math.PI * Math.sqrt((altitude * altitude * altitude) / (AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg));
		orbitalPeriod /= (double)AstronomyUtil.SECONDS_IN_KSP_DAY;
		double orbitTicks = orbitalPeriod * (double)AstronomyUtil.TICKS_IN_DAY;
		double meanAnomaly = 2 * Math.PI * (ticks / orbitTicks);

		double x = altitude / 1000 * Math.cos(meanAnomaly);
		double y = altitude / 1000 * Math.sin(meanAnomaly);

		return Vec3.createVectorHelper(x, y, 0);
	}

	private static final int ECCENTRICITY_ITERATION_COUNT = 4;

	// Eccentric anomaly from mean, calculated backwards from:
	//    M = E - e * sin(E)
	// There is no analytical solution for the inverse of this function, so we iterate until close
	private static double calculateEccentricAnomaly(double meanAnomaly, float eccentricity) {
		double eccentricAnomaly = meanAnomaly;
		for(int i = 0; i < ECCENTRICITY_ITERATION_COUNT; i++) {
			eccentricAnomaly = meanAnomaly + eccentricity * Math.sin(eccentricAnomaly);
		}

		return eccentricAnomaly;
	}

	// Calculates the metrics for a given body in the system
	private static void calculateMetricsFromBody(List<AstroMetric> metrics, CelestialBody body) {
		AstroMetric from = null;
		for(AstroMetric metric : metrics) {
			if(metric.body == body) {
				from = metric;
				break;
			}
		}

		for(AstroMetric to : metrics) {
			if(from == to)
				continue;

			calculateMetric(to, from.position);
		}
	}

	private static void calculateMetricsFromPosition(List<AstroMetric> metrics, Vec3 position) {
		for(AstroMetric to : metrics) {
			calculateMetric(to, position);
		}
	}

	private static void calculateMetric(AstroMetric metric, Vec3 position) {
		// Calculate distance between bodies, for sorting
		metric.distance = position.distanceTo(metric.position);

		// Calculate apparent size, for scaling in render
		metric.apparentSize = getApparentSize(Math.min(metric.body.radiusKm, 3_000), metric.distance);

		// Calculate angle in relation to 0, 0 (sun position, origin)
		metric.angle = getApparentAngleDegrees(position, metric.position);

		// Calculate angle above/below the orbital plane
		metric.inclination = getApparentInclinationDegrees(position, metric.position);

		// Calculate the current phase of the body (for crescent shading)
		metric.phase = getPhase(position, metric.position);

		// Calculate phase obscuring for eclipses
		metric.phaseObscure = getPhaseObscure(position, metric.position);
	}

	private static double getApparentSize(double radius, double distance) {
		return 2D * (float)Math.atan((2D * radius) / (2D * distance)) * RENDER_SCALE;
	}

	private static double getApparentAngleDegrees(Vec3 from, Vec3 to) {
		double angleToOrigin = Math.atan2(-from.yCoord, -from.xCoord);
		double angleToTarget = Math.atan2(to.yCoord - from.yCoord, to.xCoord - from.xCoord);

		return MathHelper.wrapAngleTo180_double(Math.toDegrees(angleToOrigin - angleToTarget));
	}

	private static double getApparentInclinationDegrees(Vec3 from, Vec3 to) {
		double x = from.xCoord - to.xCoord;
		double y = from.yCoord - to.yCoord;
		double planeDistance = Math.sqrt(x * x + y * y);
		double offsetDistance = from.zCoord - to.zCoord;

		return MathHelper.wrapAngleTo180_double(Math.toDegrees(Math.atan2(offsetDistance, planeDistance)));
	}

	private static double getPhase(Vec3 from, Vec3 to) {
		return getApparentAngleDegrees(to, from) / 180.0;
	}

	private static double getPhaseObscure(Vec3 from, Vec3 to) {
		return Math.min(Math.abs(getPhase(from, to)), 1.0F - Math.abs(getApparentInclinationDegrees(from, to) / 180.0F));
	}

	// Calculates how large to render the sun in the sky from a given vantage point
	public static double calculateSunSize(CelestialBody from) {
		if(from.parent == null) return 0;
		if(from.parent.parent != null) return calculateSunSize(from.parent);
		return getApparentSize(from.parent.radiusKm, from.semiMajorAxisKm);
	}

	// Gets angle for a single planet, good for locking tidal bodies
	public static double calculateSingleAngle(List<AstroMetric> metrics, CelestialBody from, CelestialBody to) {
		AstroMetric metricFrom = null;
		AstroMetric metricTo = null;

		for(AstroMetric metric : metrics) {
			if(metric.body == from) {
				metricFrom = metric;
			} else if(metric.body == to) {
				metricTo = metric;
			}
		}

		return getApparentAngleDegrees(metricFrom.position, metricTo.position);
	}

	public static double calculateSingleAngle(World world, double partialTicks, List<AstroMetric> metrics, CelestialBody orbiting, double altitude) {
		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Add our orbiting satellite position
		Vec3 from = calculatePositionSatellite(orbiting, altitude, ticks);
		Vec3 to = Vec3.createVectorHelper(0, 0, 0);
		for(AstroMetric metric : metrics) {
			if(metric.body == orbiting) {
				to = metric.position;
				from = from.addVector(to.xCoord, to.yCoord, to.zCoord);
				break;
			}
		}

		return getApparentAngleDegrees(from, to);
	}

	// Expensive, but there is only one call on server for this, everything else is client side
	public static double calculateSingleAngle(World world, CelestialBody from, CelestialBody to) {
		List<AstroMetric> metrics = new ArrayList<AstroMetric>();

		double ticks = ((double)world.getTotalWorldTime()) * (double)AstronomyUtil.TIME_MULTIPLIER;

		// Get our XYZ coordinates of all bodies
		calculatePositionsRecursive(metrics, null, from.getStar(), ticks);

		AstroMetric metricFrom = null;
		AstroMetric metricTo = null;

		for(AstroMetric metric : metrics) {
			if(metric.body == from) {
				metricFrom = metric;
			} else if(metric.body == to) {
				metricTo = metric;
			}
		}

		return getApparentAngleDegrees(metricFrom.position, metricTo.position);
	}

	public static double calculateSiderealAngle(World world, float partialTicks, CelestialBody body) {
		double ticks = ((double)world.getTotalWorldTime() + partialTicks) * (double)AstronomyUtil.TIME_MULTIPLIER;

		return calculateSiderealAngle(body, ticks);
	}

	public static double calculateSiderealAngle(CelestialBody body, double ticks) {
		body = body.getPlanet();

		Vec3 position = calculatePositionFromTime(body, ticks);

		return Math.toDegrees(Math.atan2(position.yCoord, position.xCoord));
	}


	/**
	 * Delta-V Calcs
	 */

	// Get a number of buckets of fuel required to travel somewhere, (halved, since we're assuming bipropellant)
	public static int getCostBetween(CelestialBody from, CelestialBody to, int mass, int thrust, int isp, boolean fromOrbit, boolean toOrbit) {
		double fromDrag = getAtmosphericDrag(from.getTrait(CBT_Atmosphere.class));
		double toDrag = getAtmosphericDrag(to.getTrait(CBT_Atmosphere.class));

		double launchDV = fromOrbit ? 0 : SolarSystem.getLiftoffDeltaV(from, mass, thrust, fromDrag);
		double travelDV = SolarSystem.getDeltaVBetween(from, to);
		double landerDV = toOrbit ? 0 : SolarSystem.getLandingDeltaV(to, mass, thrust, toDrag);

		double totalDV = launchDV + travelDV + landerDV;

		return getFuelCost(totalDV, mass, isp);
	}

	public static int getFuelCost(double deltaV, int mass, int isp) {
		// Get the fraction of the rocket that must be fuel in order to achieve the deltaV
		double g0 = 9.81;
		double exhaustVelocity = isp * g0;
		double massFraction = 1 - Math.exp(-(deltaV / exhaustVelocity));

		// Get the mass of a rocket that has that fraction, and the mass of the propellant
		double totalMass = mass / (1 - massFraction);
		double propellantMass = totalMass - mass;
		double propellantVolume = propellantMass / 2; // two propellants

		return propellantVolume + 100 > Integer.MAX_VALUE ? Integer.MAX_VALUE : MathHelper.ceiling_double_int(propellantVolume * 0.01D) * 100;
	}

	private static double getAtmosphericDrag(CBT_Atmosphere atmosphere) {
		if(atmosphere == null) return 0;
		double pressure = atmosphere.getPressure();
		return Math.log(pressure + 1.0D) / 10.0D;
	}

	// Provides the deltaV required to get into orbit, ignoring losses due to atmospheric friction
	// Make sure to convert from kN to N (kilonewtons to newtons) before calling these two functions
	public static double getLiftoffDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
		return calculateSurfaceToOrbitDeltaV(body, craftMassKg, craftThrustN, atmosphericDrag, false);
	}

	// Uses aerobraking if an atmosphere is present
	public static double getLandingDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag) {
		return calculateSurfaceToOrbitDeltaV(body, craftMassKg, craftThrustN, atmosphericDrag, atmosphericDrag > 0.006);
	}

	private static double calculateSurfaceToOrbitDeltaV(CelestialBody body, float craftMassKg, float craftThrustN, double atmosphericDrag, boolean lossesOnly) {
		float gravity = body.getSurfaceGravity();
		double orbitalDeltaV = Math.sqrt((AstronomyUtil.GRAVITATIONAL_CONSTANT * body.massKg) / (body.radiusKm * 1_000));
		double thrustToWeightRatio = craftThrustN / (craftMassKg * gravity);

		if(thrustToWeightRatio < 1)
			return Double.MAX_VALUE;

		// We have to find out how long the burn will take to get our "gravity tax"
		// Shorter burns have less gravity losses, meaning higher thrust is desirable
		double acceleration = (thrustToWeightRatio - 1) * gravity;
		double timeToOrbit = orbitalDeltaV / acceleration;
		double gravityLosses = gravity * timeToOrbit * 2; // No perfect burns

		if(lossesOnly)
			return gravityLosses * (1 - atmosphericDrag); // drag helps on the way down

		return orbitalDeltaV + gravityLosses * (1 + atmosphericDrag); // and hinders on the way up
	}

	// Provides the deltaV required to transfer from the orbit of one body to the orbit of another
	// Does not currently support travelling to the main body (Sol)
	// Our structure doesn't currently require this, but if it does, go annoy Mellow to add it lmao
	public static double getDeltaVBetween(CelestialBody start, CelestialBody end) {
		return calculateHohmannTransfer(start, end);
	}

	// This calculates the entire transfer cost, adding together the cost of two burns
	private static double calculateHohmannTransfer(CelestialBody start, CelestialBody end) {
		if(start == end) {
			// Transfer to self, ignore

			return 0;
		} else if(start.parent == null || end.parent == null) {
			throw new NotImplementedException("Transfers to and from solar bodies not supported");
		} else if(start.parent == end.parent) {
			// Intersystem transfer

			double firstBurnCost = calculateSingleHohmannTransfer(start.parent.massKg, start.semiMajorAxisKm, end.semiMajorAxisKm, start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
			double secondBurnCost = calculateSingleHohmannTransfer(start.parent.massKg, end.semiMajorAxisKm, start.semiMajorAxisKm, end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

			return firstBurnCost + secondBurnCost;
		} else if (start == end.parent) {
			// Transferring from parent body to moon

			double firstBurnCost = calculateSingleHohmannTransfer(start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, end.semiMajorAxisKm);
			double secondBurnCost = calculateSingleHohmannTransfer(start.massKg, end.semiMajorAxisKm, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);

			return firstBurnCost + secondBurnCost;
		} else if(start.parent == end) {
			// Transferring from moon to parent body

			double firstBurnCost = calculateSingleHohmannTransfer(end.massKg, start.semiMajorAxisKm, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, start.massKg, start.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM);
			double secondBurnCost = calculateSingleHohmannTransfer(end.massKg, end.radiusKm + AstronomyUtil.DEFAULT_ALTITUDE_KM, start.semiMajorAxisKm);

			return firstBurnCost + secondBurnCost;
		} else {
			// Complex transfer (moon -> moon, moon -> other planet)

			CelestialBody commonParent = getCommonParent(start, end);
			CelestialBody fromBody = start;
			CelestialBody toBody = end;
			float currentFromOrbitRadius = fromBody.semiMajorAxisKm;
			float currentToOrbitRadius = toBody.semiMajorAxisKm;

			double burnCost = 0;

			// Go up the tree from start
			while(fromBody.parent != commonParent) {
				burnCost += calculateSingleHohmannTransfer(fromBody.parent.massKg, fromBody.semiMajorAxisKm, fromBody.semiMajorAxisKm, fromBody.massKg, fromBody.semiMajorAxisKm);
				currentFromOrbitRadius = fromBody.semiMajorAxisKm;
				fromBody = fromBody.parent;
			}

			// Go up the tree from end
			while(toBody.parent != commonParent) {
				burnCost += calculateSingleHohmannTransfer(toBody.parent.massKg, toBody.semiMajorAxisKm, toBody.semiMajorAxisKm, toBody.massKg, toBody.semiMajorAxisKm);
				currentToOrbitRadius = toBody.semiMajorAxisKm;
				toBody = toBody.parent;
			}

			// Transfer interplanetary
			burnCost += calculateSingleHohmannTransfer(commonParent.massKg, fromBody.semiMajorAxisKm, toBody.semiMajorAxisKm, fromBody.massKg, currentFromOrbitRadius);
			burnCost += calculateSingleHohmannTransfer(commonParent.massKg, toBody.semiMajorAxisKm, fromBody.semiMajorAxisKm, toBody.massKg, currentToOrbitRadius);

			return burnCost;
		}
	}

	private static CelestialBody getCommonParent(CelestialBody start, CelestialBody end) {
		CelestialBody startParent = start.parent;

		while(startParent != null) {
			CelestialBody endParent = end.parent;
			while(endParent != null) {
				if(startParent == endParent)
					return startParent;

				endParent = endParent.parent;
			}
			startParent = startParent.parent;
		}

		throw new InvalidParameterException("Bodies aren't in the same solar system");
	}


	// All transfer math is commutative, injection burn (getting onto the transfer orbit) takes the exact same dV as
	// the insertion burn (entering the target orbit)

	// Calculate orbit to orbit transfer around a parent body, without any need to escape an inner gravity well.
	// This is used to transfer from low orbit to a moon.
	private static double calculateSingleHohmannTransfer(float parentMassKg, float fromRadiusKm, float toRadiusKm) {
		double parentGravitationalParameter = parentMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

		// We're finding the dv to transfer between these circular orbits
		double startOrbitalRadius = fromRadiusKm * 1_000;
		double endOrbitalRadius = toRadiusKm * 1_000;

		// The semimajor axis of the transfer orbit (average distance of orbit)
		double transferSemiMajorAxis = (startOrbitalRadius + endOrbitalRadius) / 2;

		// Our current orbital velocity around the parent (planet orbital velocity)
		double currentOrbitalVelocity = Math.sqrt(parentGravitationalParameter / startOrbitalRadius);

		// The velocity we need to get onto our transfer orbit
		double requiredVelocity = Math.sqrt(parentGravitationalParameter * ((2 / startOrbitalRadius) - (1 / transferSemiMajorAxis)));

		// The true velocity we need to add to get onto our transfer orbit (desired energy minus the energy we already have)
		return Math.abs(requiredVelocity - currentOrbitalVelocity);
	}

	// Calculate orbit to orbit transfer around a parent body, escaping from a well.
	// This is used for interplanetary transfers.
	private static double calculateSingleHohmannTransfer(float parentMassKg, float fromRadiusKm, float toRadiusKm, float fromMassKg, float parkingOrbitRadiusKm) {
		// First we get our required velocity change ignoring the body we're currently orbiting
		double hyperbolicVelocity = calculateSingleHohmannTransfer(parentMassKg, fromRadiusKm, toRadiusKm);

		double fromGravitationalParameter = fromMassKg * AstronomyUtil.GRAVITATIONAL_CONSTANT;

		// Our current orbital radius and velocity around the starting body
		double parkingOrbitRadius = parkingOrbitRadiusKm * 1_000;
		double parkingOrbitVelocity = Math.sqrt(fromGravitationalParameter / parkingOrbitRadius);

		// The amount of energy needed to escape the start body to get onto our transfer orbit
		double escapeVelocity = Math.sqrt((2 * fromGravitationalParameter) / parkingOrbitRadius);
		double escapeHyperVelocity = Math.sqrt(hyperbolicVelocity * hyperbolicVelocity + escapeVelocity * escapeVelocity);

		// The first half of the required dV to get to our destination!
		return escapeHyperVelocity - parkingOrbitVelocity;
	}

	public static void runTests() {
		CelestialBody kerbin = CelestialBody.getBody("kerbin");
		CelestialBody eve = CelestialBody.getBody("eve");
		CelestialBody duna = CelestialBody.getBody("duna");
		CelestialBody mun = CelestialBody.getBody("mun");
		CelestialBody minmus = CelestialBody.getBody("minmus");
		CelestialBody ike = CelestialBody.getBody("ike");

		float deltaIVMass = 500_000;
		float RD180RocketThrust = 7_887 * 1_000;

		MainRegistry.logger.info("Kerbin launch cost: " + getLiftoffDeltaV(kerbin, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Eve launch cost: " + getLiftoffDeltaV(eve, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Duna launch cost: " + getLiftoffDeltaV(duna, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Mun launch cost: " + getLiftoffDeltaV(mun, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Minmus launch cost: " + getLiftoffDeltaV(minmus, deltaIVMass, RD180RocketThrust, 0));
		MainRegistry.logger.info("Ike launch cost: " + getLiftoffDeltaV(ike, deltaIVMass, RD180RocketThrust, 0));

		MainRegistry.logger.info("Kerbin -> Eve cost: " + getDeltaVBetween(kerbin, eve) + " - should be: " + (950+90+80+1330));
		MainRegistry.logger.info("Kerbin -> Duna cost: " + getDeltaVBetween(kerbin, duna) + " - should be: " + (950+130+250+360));
		MainRegistry.logger.info("Kerbin -> Ike cost: " + getDeltaVBetween(kerbin, ike) + " - should be: " + (950+130+250+30+180));
		MainRegistry.logger.info("Eve -> Duna cost: " + getDeltaVBetween(eve, duna));
		MainRegistry.logger.info("Kerbin -> Mun cost: " + getDeltaVBetween(kerbin, mun) + " - should be: " + (860+310));
		MainRegistry.logger.info("Kerbin -> Minmus cost: " + getDeltaVBetween(kerbin, minmus) + " - should be: " + (930+160));
		MainRegistry.logger.info("Mun -> Kerbin cost: " + getDeltaVBetween(mun, kerbin) + " - should be: " + (860+310));
		MainRegistry.logger.info("Minmus -> Kerbin cost: " + getDeltaVBetween(minmus, kerbin) + " - should be: " + (930+160));
		MainRegistry.logger.info("Minmus -> Ike cost: " + getDeltaVBetween(minmus, ike));

		MainRegistry.logger.info("Kerbin orbital period: " + kerbin.getOrbitalPeriod() + " - should be: " + 426);
		MainRegistry.logger.info("Eve orbital period: " + eve.getOrbitalPeriod() + " - should be: " + 261);
		MainRegistry.logger.info("Mun orbital period: " + mun.getOrbitalPeriod() + " - should be: " + 6);

		TileEntityDysonReceiver.runTests();
	}

}
