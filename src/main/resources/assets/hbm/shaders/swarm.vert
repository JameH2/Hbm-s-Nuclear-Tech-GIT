
#version 130

uniform float iTime;

varying vec3 vPosition;
varying vec4 vColor;

float hash(float x){ return fract(cos(x*124.123)*412.0); }

void main() {
	vPosition = gl_Vertex.xyz;
	vColor = gl_Color;

	float v = float(gl_VertexID);
	float t = v + iTime;
	float r = hash(v);
	float r2 = hash(v + 0.5);
	float r3 = hash(v + 0.75);

	float y = (cos(t) * r + sin(t) * (1.0 - r)) * 0.2 * r2 * r2;
	float o = cos(y * 2.0) * (1.0 - ((r3 - 0.5) * 0.05));

	gl_Position = gl_ModelViewProjectionMatrix * (vec4(gl_Vertex.x * o, y, gl_Vertex.z * o, gl_Vertex.w));
	gl_TexCoord[0] = gl_MultiTexCoord0;
}