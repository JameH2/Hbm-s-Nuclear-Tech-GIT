#version 120

uniform float phase;
uniform int nukeShockCount;
uniform float nukeShockTime[4];
uniform float nukeShockCenterX[4];
uniform float nukeShockCenterY[4];
uniform float nukeShockStrength[4];

const float FLASH_GRID = 16.0;
const float FLASH_SUBGRID = FLASH_GRID * 4.0;
const float RING_GRID = 64.0;
const int MAX_NUKE_SHOCKS = 4;
const float PI = 3.1415926538;

float stableHash(vec2 p) {
	return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453123);
}

float getFlashFade(float time, float strength) {
	return 1.0 - smoothstep(0.0, mix(4.0, 6.0, strength), time);
}

float getExplosionScale(float strength) {
	return mix(0.9, 2.3, smoothstep(0.35, 1.0, clamp(strength, 0.0, 1.0)));
}

float getFlashCellMask(vec2 subPixelUV, vec2 center, float strength) {
	float explosionScale = getExplosionScale(strength);
	vec2 subDelta = abs((subPixelUV - center) * FLASH_SUBGRID) / explosionScale;
	float withinBlock = 1.0 - step(2.0, max(subDelta.x, subDelta.y));
	float isCorner = step(1.0, subDelta.x) * step(1.0, subDelta.y);
	return withinBlock * (1.0 - isCorner);
}

float getFlashMask(vec2 subPixelUV, float time, vec2 center, float strength) {
	if (time < 0.0 || strength <= 0.001) {
		return 0.0;
	}

	float flashFade = getFlashFade(time, strength);
	float explosionScale = getExplosionScale(strength);
	float distanceFromCenter = length(subPixelUV - center);
	float flashRadius = mix(0.010, 0.014, strength) * explosionScale;
	float flashEdge = mix(0.014, 0.022, strength);
	return (1.0 - smoothstep(flashRadius, flashRadius + flashEdge, distanceFromCenter)) * flashFade * getFlashCellMask(subPixelUV, center, strength);
}

float getAfterglowMask(vec2 subPixelUV, float time, vec2 center, float strength) {
	if (time < 0.0 || strength <= 0.001) {
		return 0.0;
	}

	float warmRise = smoothstep(1.0, 4.0, time);
	float warmFade = 1.0 - smoothstep(8.0, mix(18.0, 30.0, strength), time);
	float explosionScale = getExplosionScale(strength);
	float distanceFromCenter = length(subPixelUV - center);
	float glowRadius = mix(0.018, 0.026, strength) * explosionScale;
	float glowEdge = mix(0.020, 0.032, strength);
	float glowMask = 1.0 - smoothstep(glowRadius, glowRadius + glowEdge, distanceFromCenter);
	return glowMask * warmRise * warmFade * getFlashCellMask(subPixelUV, center, strength);
}

float getDaySideMask(vec2 localUV) {
	vec2 uv = 2.25 * localUV - 1.1;
	vec3 light = vec3(sin(phase * PI), 0.0, cos(phase * PI));
	vec3 n = vec3(uv, sqrt(1.0 - clamp(dot(uv, uv), 0.0, 1.0)));
	float brightness = dot(n, light);

	if (abs(phase) < 0.5) {
		if (phase < 0.0) {
			brightness = phase * 4.0 + 2.0 - uv.x;
		} else {
			brightness = -phase * 4.0 + 2.0 + uv.x;
		}
	}

	return clamp(brightness, 0.0, 1.0);
}

float getRingMask(vec2 pixelUV, float time, vec2 center, float strength) {
	if (time < 0.0 || strength <= 0.001) {
		return 0.0;
	}

	float stableSeed = stableHash(center + vec2(strength * 0.31, strength * 0.73));
	float startRadius = mix(0.022, 0.038, strength) * mix(0.94, 1.16, stableSeed);
	float ringRadius = startRadius + time * mix(0.00085, 0.00135, strength) * mix(0.92, 1.15, stableSeed);
	float ringProgress = clamp(time / mix(72.0, 120.0, strength), 0.0, 1.0);
	float ringWidth = mix(0.044, 0.017, ringProgress) * mix(0.95, 1.1, stableSeed);
	float ringReveal = smoothstep(1.5, 4.5, time);
	float ringFade = 1.0 - smoothstep(8.0, mix(68.0, 104.0, strength), time);
	float flashFade = getFlashFade(time, strength);
	float ringOpacity = mix(0.3, 0.78, smoothstep(0.0, 12.0, time));
	float distanceFromCenter = length(pixelUV - center);
	float outerBand = smoothstep(max(ringRadius - ringWidth, 0.0), ringRadius, distanceFromCenter);
	float innerBand = 1.0 - smoothstep(ringRadius, ringRadius + ringWidth, distanceFromCenter);
	float ringMask = outerBand * innerBand;
	return ringMask * ringReveal * ringFade * ringOpacity * getDaySideMask(pixelUV) * (1.0 - flashFade * 0.9);
}

void main() {
	vec2 localUV = gl_TexCoord[0].xy;
	vec2 flashPixelCoord = floor(localUV * FLASH_SUBGRID);
	vec2 flashPixelUV = (flashPixelCoord + 0.5) / FLASH_SUBGRID;
	vec2 ringPixelCoord = floor(localUV * RING_GRID);
	vec2 ringPixelUV = (ringPixelCoord + 0.5) / RING_GRID;
	float flashAlpha = 0.0;
	float afterglowAlpha = 0.0;
	float ringAlpha = 0.0;

	for (int i = 0; i < MAX_NUKE_SHOCKS; i++) {
		if (i < nukeShockCount) {
			float shockStrength = clamp(nukeShockStrength[i], 0.0, 1.0);
			vec2 shockCenter = vec2(nukeShockCenterX[i], nukeShockCenterY[i]);
			float shockTime = nukeShockTime[i];
			flashAlpha = max(flashAlpha, getFlashMask(flashPixelUV, shockTime, shockCenter, shockStrength));
			afterglowAlpha = max(afterglowAlpha, getAfterglowMask(flashPixelUV, shockTime, shockCenter, shockStrength));
			ringAlpha = max(ringAlpha, getRingMask(ringPixelUV, shockTime, shockCenter, shockStrength));
		}
	}

	float ringContribution = ringAlpha * 0.42;
	float finalAlpha = clamp(max(max(flashAlpha, afterglowAlpha), ringContribution), 0.0, 1.0);
	vec3 ringColor = vec3(0.72, 0.64, 0.56);
	vec3 flashColor = vec3(1.0);
	vec3 afterglowWarm = vec3(1.0, 0.72, 0.38);
	vec3 afterglowAsh = vec3(0.62, 0.62, 0.6);
	float afterglowToAsh = clamp(afterglowAlpha > 0.0 ? smoothstep(0.18, 0.75, afterglowAlpha) : 0.0, 0.0, 1.0);
	vec3 afterglowColor = mix(afterglowAsh, afterglowWarm, afterglowToAsh);

	vec3 accumColor = ringColor * ringContribution;
	accumColor += afterglowColor * afterglowAlpha * 0.9;
	accumColor += flashColor * flashAlpha;
	vec3 finalColor = finalAlpha > 0.001 ? clamp(accumColor / max(finalAlpha, 0.001), 0.0, 1.0) : vec3(0.0);

	gl_FragColor = vec4(finalColor, finalAlpha);
}
