#version 120

uniform float phase;
uniform float offset;

uniform sampler2D bodyTex;
uniform sampler2D cityMask;
uniform int useBodyAlphaMask;
uniform float cloudTintStrength;
uniform float cloudLightningStrength;
uniform float atmosphereAlpha;
uniform float atmosphereTime;
uniform float eveFlashStrength;
uniform float patternOffset;
uniform float impactTime;
uniform int nukeShockCount;
uniform float nukeShockTime[4];
uniform float nukeShockCenterX[4];
uniform float nukeShockCenterY[4];
uniform float nukeShockStrength[4];
uniform int atmosphereStyle;
uniform int lightningMode;

const float PIXEL_GRID = 16.0;
const int MAX_NUKE_SHOCKS = 4;
const vec2 IMPACT_CENTER = vec2(0.25, 0.7);
const float IMPACT_RECOVERY_TIME_SCALE = 1.5;
const float NUKE_RECOVERY_TIME_SCALE = 1.5;
#define PI 3.1415926538

float hash(vec2 p) {
	return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float noise(vec2 p) {
	vec2 i = floor(p);
	vec2 f = fract(p);
	vec2 u = f * f * (3.0 - 2.0 * f);

	return mix(
		mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
		mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x),
		u.y
	);
}

float fbm(vec2 p) {
	float value = 0.0;
	float amplitude = 0.5;

	for (int i = 0; i < 4; i++) {
		value += noise(p) * amplitude;
		p = p * 2.02 + vec2(17.13, -11.7);
		amplitude *= 0.5;
	}

	return value;
}

vec4 getImpactField(vec2 localUV, float time) {
	if (time < 0.0) {
		return vec4(0.0);
	}

	vec2 delta = localUV - IMPACT_CENTER;
	float distanceFromImpact = length(delta);
	vec2 direction = distanceFromImpact > 0.0001 ? delta / distanceFromImpact : vec2(0.0, 1.0);

	float shockRadius = time * 0.00175;
	float shockProgress = clamp(time / 480.0, 0.0, 1.0);
	float shockWidth = mix(0.11, 0.045, shockProgress);
	float shockFade = 1.0 - smoothstep(80.0, 760.0, time);
	float outerBand = smoothstep(max(shockRadius - shockWidth, 0.0), shockRadius, distanceFromImpact);
	float innerBand = 1.0 - smoothstep(shockRadius, shockRadius + shockWidth, distanceFromImpact);
	float shockBand = outerBand * innerBand * shockFade;

	float wakeFade = 1.0 - smoothstep(60.0 * IMPACT_RECOVERY_TIME_SCALE, 700.0 * IMPACT_RECOVERY_TIME_SCALE, time);
	float wakeInner = max(shockRadius - shockWidth * 1.8 - 0.035, 0.0);
	float wakeOuter = shockRadius + shockWidth * 0.35;
	float wakeMask = (1.0 - smoothstep(wakeInner, wakeOuter, distanceFromImpact)) * wakeFade;

	float coreFade = 1.0 - smoothstep(120.0 * IMPACT_RECOVERY_TIME_SCALE, 760.0 * IMPACT_RECOVERY_TIME_SCALE, time);
	float coreRadius = mix(0.22, 0.1, shockProgress);
	float coreMask = (1.0 - smoothstep(coreRadius, coreRadius + 0.08, distanceFromImpact)) * coreFade;

	return vec4(direction, shockBand, max(wakeMask, coreMask));
}

vec4 getNukeShockField(vec2 localUV, float time, vec2 center, float strength) {
	if (time < 0.0 || strength <= 0.001) {
		return vec4(0.0);
	}

	vec2 delta = localUV - center;
	float distanceFromCenter = length(delta);
	vec2 direction = distanceFromCenter > 0.0001 ? delta / distanceFromCenter : vec2(0.0, 1.0);
	float stableSeed = fract(sin(dot(center + vec2(strength * 0.31, strength * 0.73), vec2(127.1, 311.7))) * 43758.5453123);
	float coreRadius = mix(0.09, 0.16, strength) * mix(0.92, 1.28, stableSeed);
	float shockRadius = coreRadius * 0.92 + time * mix(0.00085, 0.00135, strength) * mix(0.92, 1.15, stableSeed);
	float shockProgress = clamp(time / mix(72.0, 120.0, strength), 0.0, 1.0);
	float shockWidth = mix(0.07, 0.032, shockProgress) * mix(0.95, 1.12, stableSeed);
	float shockFade = 1.0 - smoothstep(10.0, mix(82.0, 118.0, strength), time);
	float outerBand = smoothstep(max(shockRadius - shockWidth, 0.0), shockRadius, distanceFromCenter);
	float innerBand = 1.0 - smoothstep(shockRadius, shockRadius + shockWidth, distanceFromCenter);
	float shockBand = outerBand * innerBand * shockFade;

	float wakeFade = 1.0 - smoothstep(8.0 * NUKE_RECOVERY_TIME_SCALE, mix(90.0, 135.0, strength) * NUKE_RECOVERY_TIME_SCALE, time);
	float wakeInner = max(shockRadius - shockWidth * 1.8 - 0.018, 0.0);
	float wakeOuter = shockRadius + shockWidth * 0.28;
	float wakeMask = (1.0 - smoothstep(wakeInner, wakeOuter, distanceFromCenter)) * wakeFade;

	float coreFade = 1.0 - smoothstep(0.0, mix(18.0, 32.0, strength) * NUKE_RECOVERY_TIME_SCALE, time);
	float coreMask = (1.0 - smoothstep(coreRadius, coreRadius + 0.06, distanceFromCenter)) * coreFade;

	return vec4(direction, shockBand, max(wakeMask, coreMask));
}

float getNightVisibility(vec2 movingUV) {
	vec2 fragCoord = floor(movingUV / vec2(0.0625, 0.0625)) * vec2(0.0625, 0.0625) - vec2(offset, 0.0);
	vec2 uv = (2.25 * fragCoord - 1.1);
	vec2 suv = (2.0 * fragCoord - 1.0);

	vec3 light = vec3(sin(phase * PI), 0.0, cos(phase * PI));
	vec3 n = vec3(uv, sqrt(1.0 - clamp(dot(uv, uv), 0.0, 1.0)));
	float brightness = dot(n, light);

	brightness = max(brightness, (abs(phase) - 0.7) * clamp(dot(suv, suv), 0.0, 1.0));

	if (abs(phase) < 0.5) {
		if (phase < 0.0) {
			brightness = phase * 4.0 + 2.0 - uv.x;
		} else {
			brightness = -phase * 4.0 + 2.0 + uv.x;
		}
	}

	brightness = max(brightness, 0.05);
	return mix(0.22, 1.0, clamp(0.85 - brightness, 0.0, 1.0));
}

void main() {
	vec2 localUV = gl_TexCoord[0].xy;
	vec2 movingUV = localUV + vec2(offset, 0.0);
	vec2 wrappedUV = fract(movingUV);
	vec2 patternUV = localUV + vec2(patternOffset, 0.0);
	vec2 impactPixelCoord = floor(patternUV * PIXEL_GRID);
	vec2 impactPixelUV = (impactPixelCoord + 0.5) / PIXEL_GRID;
	vec4 impactField = getImpactField(impactPixelUV, impactTime);

	float alphaMask = 1.0;
	if (useBodyAlphaMask != 0) {
		alphaMask = texture2D(bodyTex, wrappedUV).a;
		if (alphaMask <= 0.001) {
			gl_FragColor = vec4(0.0);
			return;
		}
	}

	float lightningStrength = clamp(cloudLightningStrength, 0.0, 1.0);
	if (lightningStrength <= 0.001 || (atmosphereStyle != 1 && atmosphereStyle != 2)) {
		gl_FragColor = vec4(0.0);
		return;
	}

	float density = clamp(atmosphereAlpha, 0.0, 1.0);
	float tintStrength = clamp(cloudTintStrength, 0.0, 1.0);
	float denseAtmosphereVisibility = mix(1.0, 0.5, smoothstep(0.72, 1.0, density));
	float nightVisibility = getNightVisibility(movingUV);
	float cloudMotionScale = mix(1.0, 1.5, step(0.999, density));
	float motionTime = atmosphereTime * cloudMotionScale;
	float impactSuppression = clamp(max(impactField.w, impactField.z * mix(0.55, 0.95, density)), 0.0, 1.0);
	float nukeClearStrength = 0.0;
	vec2 nukePixelCoord = floor(localUV * PIXEL_GRID);
	vec2 nukePixelUV = (nukePixelCoord + 0.5) / PIXEL_GRID;
	for (int i = 0; i < MAX_NUKE_SHOCKS; i++) {
		if (i < nukeShockCount) {
			float shockStrength = clamp(nukeShockStrength[i], 0.0, 1.0);
			vec4 nukeField = getNukeShockField(nukePixelUV, nukeShockTime[i], vec2(nukeShockCenterX[i], nukeShockCenterY[i]), shockStrength);
			nukeClearStrength = max(nukeClearStrength, clamp(max(nukeField.w, nukeField.z * mix(0.55, 0.95, density)), 0.0, 1.0));
		}
	}
	float nukeSuppression = max(impactSuppression, nukeClearStrength);
	if (nukeSuppression >= 0.999) {
		gl_FragColor = vec4(0.0);
		return;
	}
	vec4 city = texture2D(cityMask, movingUV);
	float maskCoverage = max(max(city.r, city.g), city.b) * city.a;
	if (maskCoverage <= 0.001) {
		gl_FragColor = vec4(0.0);
		return;
	}

	vec2 texelCoord = floor(patternUV * PIXEL_GRID);
	vec2 uv = (texelCoord + 0.5) / PIXEL_GRID;
	vec2 texelFlow = vec2(motionTime * 0.18, -motionTime * 0.11);
	vec2 flowDrift = texelFlow / PIXEL_GRID;
	float lightningAlpha = 0.0;

	if (lightningMode == 1) {
		float atmosphereFlash = clamp(eveFlashStrength, 0.0, 1.0) * (0.05 + lightningStrength * 0.06);
		lightningAlpha += atmosphereFlash * denseAtmosphereVisibility;
	}
	if (atmosphereStyle == 2) {
		float hazeField = fbm(uv * 2.4 + flowDrift * 0.55);
		float hazeSheet = fbm(uv * 4.0 + vec2(-motionTime * 0.012, motionTime * 0.01));
		float turbulence = noise(uv * 10.0 + vec2(motionTime * 0.018, -motionTime * 0.014));
		float hazeMix = smoothstep(0.28, 0.88, mix(hazeField, hazeSheet, 0.4));
		float burstWindow = floor(atmosphereTime * 0.72 + patternOffset * 5.0);
		float burstSeed = hash(vec2(burstWindow, 57.91));
		float burstPhase = fract(atmosphereTime * 0.72 + burstSeed * 0.43);
		float burstGate = step(0.82, burstSeed) * smoothstep(0.24, 0.76, lightningStrength);
		float primaryFlash = smoothstep(0.0, 0.025, burstPhase) * (1.0 - smoothstep(0.025, 0.12, burstPhase));
		float secondaryFlash = smoothstep(0.16, 0.19, burstPhase) * (1.0 - smoothstep(0.19, 0.3, burstPhase));
		float flashPulse = burstGate * (primaryFlash + secondaryFlash * 0.55);
		float lightningPatch = hash(floor(texelCoord / 4.0) + vec2(burstWindow * 1.4, 33.6));
		float lightningMask = smoothstep(0.34, 0.82, hazeMix + turbulence * 0.2)
			* smoothstep(0.68, 0.94, lightningPatch + hazeField * 0.35);
		lightningAlpha += flashPulse * lightningMask * nightVisibility * (0.62 + lightningStrength * 0.38) * denseAtmosphereVisibility;
	} else {
		vec2 cloudBase = (texelCoord + texelFlow) / vec2(7.5, 6.0);
		float largeSwirl = fbm(cloudBase * 0.75);
		float shear = fbm((texelCoord.yx + vec2(-motionTime * 0.12, motionTime * 0.09)) / vec2(8.5, 10.0));
		vec2 cloudUv = cloudBase + vec2(largeSwirl * 0.48, shear * 0.18);
		float cloudCover = clamp(0.22 + density * 0.95, 0.0, 1.0);
		float cloudField = fbm(cloudUv);
		float wisps = fbm(cloudUv * 1.3 + vec2(-motionTime * 0.01, motionTime * 0.007) + vec2(cloudField, largeSwirl));
		float cloudPattern = mix(cloudField, wisps, 0.32);
		float cloudMask = smoothstep(mix(0.54, 0.4, cloudCover), mix(0.76, 0.67, cloudCover), cloudPattern);
		float cloudCoverage = smoothstep(mix(0.46, 0.31, cloudCover), mix(0.68, 0.59, cloudCover), cloudPattern + 0.06);
		float jet = 0.5 + 0.5 * sin((texelCoord.y + largeSwirl * 1.35) * 1.05 + motionTime * 0.45);
		float jetMask = smoothstep(mix(0.8, 0.65, cloudCover), mix(0.96, 0.92, cloudCover), jet)
			* smoothstep(mix(0.5, 0.38, cloudCover), mix(0.84, 0.76, cloudCover), wisps);
		float cloudPresence = max(max(cloudMask, cloudCoverage * (0.58 + cloudCover * 0.18)), jetMask * (0.46 + cloudCover * 0.18));
		float neutralClouds = 1.0 - smoothstep(0.02, 0.18, tintStrength);
		float neutralBoost = neutralClouds * (1.0 - density * 0.45);
		float burstWindow = floor(atmosphereTime * 0.85 + patternOffset * 7.0);
		float burstSeed = hash(vec2(burstWindow, 23.17));
		float burstPhase = fract(atmosphereTime * 0.85 + burstSeed * 0.37);
		float burstGate = step(0.84, burstSeed) * smoothstep(0.28, 0.82, lightningStrength);
		float primaryFlash = smoothstep(0.0, 0.02, burstPhase) * (1.0 - smoothstep(0.02, 0.09, burstPhase));
		float secondaryFlash = smoothstep(0.11, 0.14, burstPhase) * (1.0 - smoothstep(0.14, 0.22, burstPhase));
		float flashPulse = burstGate * (primaryFlash + secondaryFlash * 0.65);
		float lightningPatch = hash(floor(texelCoord / 3.0) + vec2(burstWindow * 1.9, 41.3));
		float lightningMask = smoothstep(0.42, 0.82, cloudPresence) * smoothstep(0.7, 0.92, lightningPatch + cloudPattern * 0.4);
		lightningAlpha += flashPulse * lightningMask * nightVisibility * (0.72 + lightningStrength * 0.28 + neutralBoost * 0.38) * denseAtmosphereVisibility;
	}
	lightningAlpha *= 1.0 - nukeSuppression;

	gl_FragColor = vec4(vec3(1.0), clamp(lightningAlpha * alphaMask * maskCoverage, 0.0, 1.0));
}
