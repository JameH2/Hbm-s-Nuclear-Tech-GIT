#version 120

uniform float phase;
uniform float offset;

uniform sampler2D lights;
uniform sampler2D cityMask;
uniform int blackouts;

#define PI 3.1415926538

vec2 quantize(vec2 inp, vec2 period) {
	return floor(inp / period) * period;
}

float hash(float x){ return fract(cos(x*124.123)*412.0); }

void main() {
	vec2 fragCoord = quantize(gl_TexCoord[0].xy + vec2(offset, 0), vec2(0.0625, 0.0625)) - vec2(offset, 0);
	vec2 uv = (2.25 * fragCoord - 1.1);
	vec2 suv = (2.0 * fragCoord - 1.0);

	vec3 light = vec3(sin(phase * PI), 0.0, cos(phase * PI));

	vec3 n = vec3(uv, sqrt(1.0 - clamp(dot(uv, uv), 0.0, 1.0)));
	float brightness = dot(n, light);

	// when nearly new moon, ring glow
	brightness = max(brightness, (abs(phase) - 0.7) * clamp(dot(suv, suv), 0.0, 1.0));

	// become full square when nearing full illumination
	if (abs(phase) < 0.5) {
		if (phase < 0.0) {
			brightness = phase * 4.0 + 2.0 - uv.x;
		} else {
			brightness = -phase * 4.0 + 2.0 + uv.x;
		}
	}

	// minimum brightness
	brightness = max(brightness, 0.05);

	// Apply night lights and mask out cities
	gl_FragColor = texture2D(lights, gl_TexCoord[0].xy);
	gl_FragColor = gl_FragColor * texture2D(cityMask, gl_TexCoord[0].xy) * (0.8 - brightness);

	for (int i = 0; i < blackouts; i++) {
		float bx = hash(i * 100.0 + 1.0);
		float by = hash(i * 100.0 + 2.0);

		if (gl_TexCoord[0].x > bx - 0.15 && gl_TexCoord[0].x < bx + 0.15 && gl_TexCoord[0].y > by - 0.15 && gl_TexCoord[0].y < by + 0.15) {
			gl_FragColor = vec4(0.0);
		}
	}

	gl_FragColor.a = 1.0 - brightness;
}
