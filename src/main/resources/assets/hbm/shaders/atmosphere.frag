#version 120

uniform float offset;

uniform sampler2D bodyTex;
uniform int useBodyAlphaMask;
uniform float atmosphereColorR;
uniform float atmosphereColorG;
uniform float atmosphereColorB;
uniform float cloudColorR;
uniform float cloudColorG;
uniform float cloudColorB;
uniform float cloudTintStrength;
uniform float cloudStormDarkness;
uniform float atmosphereAlpha;
uniform float atmosphereTime;
uniform float patternOffset;
uniform float impactTime;
uniform int nukeShockCount;
uniform float nukeShockTime[4];
uniform float nukeShockCenterX[4];
uniform float nukeShockCenterY[4];
uniform float nukeShockStrength[4];
uniform int atmosphereStyle;

const float PIXEL_GRID = 16.0;
const int MAX_NUKE_SHOCKS = 4;
const vec2 IMPACT_CENTER = vec2(0.25, 0.7);
const float IMPACT_RECOVERY_TIME_SCALE = 1.5;
const float NUKE_RECOVERY_TIME_SCALE = 1.5;

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

void main() {
	vec2 localUV = gl_TexCoord[0].xy;
	vec2 movingUV = localUV + vec2(offset, 0);
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

	float density = clamp(atmosphereAlpha, 0.0, 1.0);
	vec3 baseColor = vec3(atmosphereColorR, atmosphereColorG, atmosphereColorB);
	vec3 cloudTint = vec3(cloudColorR, cloudColorG, cloudColorB);
	float tintStrength = clamp(cloudTintStrength, 0.0, 1.0);
	float stormDarkness = clamp(cloudStormDarkness, 0.0, 1.0);
	float cloudMotionScale = mix(1.0, 1.5, step(0.999, density));
	float motionTime = atmosphereTime * cloudMotionScale;
	float impactClearStrength = clamp(max(impactField.w, impactField.z * mix(0.55, 0.95, density)), 0.0, 1.0);
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
	float suppressionStrength = max(impactClearStrength, nukeClearStrength);
	if (suppressionStrength >= 0.999) {
		gl_FragColor = vec4(0.0);
		return;
	}
	vec2 texelCoord = floor(patternUV * PIXEL_GRID);
	vec2 uv = (texelCoord + 0.5) / PIXEL_GRID;
	vec2 texelFlow = vec2(motionTime * 0.18, -motionTime * 0.11);
	vec2 flowDrift = texelFlow / PIXEL_GRID;
	vec3 layeredColor = baseColor;
	float alphaBoost = 0.9;
	float overlayAlpha = atmosphereAlpha * alphaBoost;

	if (atmosphereStyle == 3) {
		float bandWarp = fbm(uv * vec2(2.5, 5.5) + vec2(atmosphereTime * 0.005, -atmosphereTime * 0.0025));
		float fineWarp = fbm(uv * vec2(5.0, 11.0) + vec2(-atmosphereTime * 0.007, atmosphereTime * 0.0035));
		float bands = 0.5 + 0.5 * sin((uv.y * PIXEL_GRID + bandWarp * 3.5) * 1.8 + atmosphereTime * 0.22);
		float thinBands = 0.5 + 0.5 * sin((uv.y * PIXEL_GRID + fineWarp * 5.0) * 4.6 - atmosphereTime * 0.11);
		float storm = smoothstep(0.64, 0.9, fbm(uv * vec2(6.0, 3.0) + vec2(atmosphereTime * 0.0035, -atmosphereTime * 0.006)));

		vec3 bandDark = baseColor * 0.74;
		vec3 bandLight = min(baseColor * 1.28 + vec3(0.08), vec3(1.0));
		layeredColor = mix(bandDark, bandLight, bands);
		layeredColor = mix(layeredColor, bandLight, thinBands * 0.25);
		layeredColor = mix(layeredColor, bandDark * 0.9, storm * 0.18);
		alphaBoost = 0.96 + storm * 0.04;
		overlayAlpha = atmosphereAlpha * alphaBoost;
	} else if (atmosphereStyle == 2) {
		float hazeField = fbm(uv * 2.4 + flowDrift * 0.55);
		float hazeSheet = fbm(uv * 4.0 + vec2(-motionTime * 0.012, motionTime * 0.01));
		float turbulence = noise(uv * 10.0 + vec2(motionTime * 0.018, -motionTime * 0.014));
		float hazeMix = smoothstep(0.28, 0.88, mix(hazeField, hazeSheet, 0.4));
		hazeMix *= 1.0 - impactClearStrength;

		vec3 deepHaze = baseColor * mix(0.68, 0.55, density);
		vec3 brightHaze = min(baseColor * (1.08 + density * 0.18) + vec3(0.06), vec3(1.0));
		layeredColor = mix(deepHaze, brightHaze, hazeMix * 0.42 + turbulence * 0.16);

		alphaBoost = 0.94 + hazeMix * 0.06;
		overlayAlpha = atmosphereAlpha * alphaBoost;
	} else if (atmosphereStyle == 1) {
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
		float impactClear = 1.0 - impactClearStrength;
		cloudMask *= impactClear;
		cloudCoverage *= impactClear;
		jetMask *= impactClear;
		float turbulence = noise((texelCoord + vec2(motionTime * 0.16, -motionTime * 0.12)) / 2.75);
		float cloudPresence = max(max(cloudMask, cloudCoverage * (0.58 + cloudCover * 0.18)), jetMask * (0.46 + cloudCover * 0.18));
		float neutralClouds = 1.0 - smoothstep(0.02, 0.18, tintStrength);
		float neutralBoost = neutralClouds * (1.0 - density * 0.45);
		float stormMask = smoothstep(0.24, 0.78, cloudPresence);
		float stormShade = mix(1.0, 0.22, stormDarkness);

		vec3 shadowColor = baseColor * mix(0.72, 0.55, density);
		vec3 cloudColor = min(cloudTint * (1.18 + density * 0.18) + vec3(0.04 + density * 0.04), vec3(1.0));
		cloudColor = mix(cloudColor, vec3(1.0), neutralClouds * 0.38);
		cloudColor *= mix(1.0, stormShade, 0.85);
		vec3 stormCloudColor = mix(cloudColor, vec3(0.22, 0.22, 0.24), 0.52 + stormDarkness * 0.28);
		vec3 airColor = mix(shadowColor, baseColor, 0.35 + turbulence * 0.3);
		layeredColor = mix(airColor, cloudColor, clamp(cloudMask * (0.96 + density * 0.3 + neutralBoost * 0.42), 0.0, 1.0));
		layeredColor = mix(layeredColor, cloudColor, clamp(cloudCoverage * (0.48 + density * 0.2 + neutralBoost * 0.3), 0.0, 1.0));
		layeredColor = mix(layeredColor, cloudColor, clamp(jetMask * (0.5 + density * 0.24 + neutralBoost * 0.16), 0.0, 1.0));
		layeredColor = mix(layeredColor, stormCloudColor, stormMask * stormDarkness * (0.84 + stormDarkness * 0.16));

		alphaBoost = 0.98 + cloudPresence * 0.64;
		overlayAlpha = max(atmosphereAlpha * alphaBoost, cloudPresence * (0.28 + density * 1.1 + stormDarkness * 0.22 + neutralBoost * 0.34));
	} else {
		float shimmer = noise(uv * 8.0 + vec2(atmosphereTime * 0.015, -atmosphereTime * 0.011));
		vec3 tintLow = baseColor * 0.82;
		vec3 tintHigh = min(baseColor * 1.05 + vec3(0.02), vec3(1.0));
		layeredColor = mix(tintLow, tintHigh, shimmer * 0.35);
		alphaBoost = 0.88;
		overlayAlpha = atmosphereAlpha * alphaBoost;
	}

	overlayAlpha *= 1.0 - suppressionStrength;
	float finalAlpha = clamp(overlayAlpha * alphaMask, 0.0, 1.0);

	gl_FragColor = vec4(layeredColor, finalAlpha);
}
