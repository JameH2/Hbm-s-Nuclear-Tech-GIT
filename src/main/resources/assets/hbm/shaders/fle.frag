// GLSL 1.20 Shader (Minecraft 1.7.10 Compatible)

// Modify variables that affect how the scene looks.
#define ITERATIONS 28.
#define FREQUENCY_MULTIPLIER 1.18
#define DIFFUSE_STRENGTH 20.
#define SPECULAR_SHININESS 50.
#define DIFFUSE_FRESNEL_BIAS 1.
#define SPECULAR_FRESNEL_BIAS 4.
#define FLUID_STRENGTH 0.5
#define ROTATION 5.
#define LURCHINIESS 1.5
#define SPEED 2.

// Modify lighting colors.
#define AMBIENT_COLOR vec3(1., 0.05, 0.2) * 0.02
#define DIFFUSE_COLOR vec3(1., 0.0, 0.2)
#define DIFFUSE_HIGHLIGHT_COLOR vec3(1., 0.35, 0.2) * 1.
#define SPECULAR_COLOR vec3(1., 0.8, 0.8) * 0.4
#define FRESNEL_COLOR vec3(1., 0.05, 0.2)


// Toggle texture/lighting features.
#define USE_FLUID true
#define USE_DIFFUSE true
#define USE_DIFFUSE_HIGHLIGHTS true
#define USE_SPECULAR_HIGHLIGHTS true
#define USE_FRESNEL true
#define USE_AMBIENT true

uniform float iTime;
uniform vec2 iResolution;
uniform sampler2D iChannel0;
varying vec3 vPosition; // Assuming you have the vertex position passed as vPosition

mat2 RM2D(float a)
{
    return mat2(cos(a), sin(a), -sin(a), cos(a));
}

float aperiodicSin(float x)
{
    float eOver2 = 1.3591409;
    float pi = 3.141592;
    return sin(eOver2 * x + 1.04) * sin(pi * x);
}

// Creates freaky shapes.
float FBM(vec2 uv)
{
    vec2 n, q, u = vec2(uv - 0.5);
    float centeredDot = dot(u, u);
    float frequency = 15. - (0.5 - centeredDot) * 8.0;
    float result = 0.;
    mat2 matrix = RM2D(ROTATION);

    for (float i = 0.; i < ITERATIONS; i++)
    {
        u = matrix * u;
        n = matrix * n;
        q = u * frequency + iTime * SPEED + aperiodicSin(iTime * LURCHINIESS - centeredDot * 1.2) * 0.4 * LURCHINIESS + i + n;
        result += dot(cos(q) / frequency, vec2(2., 2.));
        n -= sin(q);
        frequency *= FREQUENCY_MULTIPLIER;
    }
    return result;
}

float CalculateDiffuseLight(vec3 normal, vec3 lightDirection)
{
    float maxBrightness = 0.3;
    return pow(max(dot(normal, lightDirection), 0.0), DIFFUSE_STRENGTH) * maxBrightness;
}

float CalculateSpecularLight(vec3 normal, vec3 lightDirection, vec3 currentPosition)
{
    vec3 lightSource = vec3(0.9, 0.1, 1.0);
    vec3 reflectedDirection = reflect(-lightDirection, normal);  
    vec3 viewDirection = normalize(lightSource - currentPosition);
    return pow(max(dot(viewDirection, reflectedDirection), 0.0), SPECULAR_SHININESS);
}

void main()
{
    float pi = 3.141592;

    // Calculate UV from the vertex position
    vec2 uv = vec2(atan(vPosition.z, vPosition.x) / (2.0 * pi) + 0.5, vPosition.y * 0.5 + 0.5);

    // Create the noise
    float noise = FBM(uv);
    float originalNoise = noise;
    noise = clamp(noise, 0.0, 1.0);
    
    // Flesh-like color with pulsating effect, now focusing on red tones
    float pulsate = sin(iTime * 0.5) * 0.5 + 0.5;  // Pulse the color between 0 and 1
    vec3 fleshColor = mix(vec3(0.8, 0.3, 0.2), vec3(1.0, 0.5, 0.4), pulsate);  \

    vec3 currentPosition = vec3(uv, 1.0);

    vec3 lightSource = vec3(0.76, 0.7, 0.);
    vec3 lightDirection = normalize(currentPosition - lightSource);
    
    if (USE_FLUID)
    {
        float fluidViscosity = 681.72;
        float fluidNoiseAngle = originalNoise * 13.05 + iTime * 0.78;
        vec2 fluidOffset = vec2(cos(fluidNoiseAngle) + originalNoise * 14.0, sin(fluidNoiseAngle) + iTime * 5.5) / fluidViscosity;
        float fluidNoise = pow(texture2D(iChannel0, uv * 0.12 + fluidOffset).x, 5.5 * FLUID_STRENGTH) * 0.27;
        noise += fluidNoise * smoothstep(0.4, 0.0, noise);
    }
    
    // Normal map calculation from FBM noise
    vec3 normal = normalize(vec3(noise - FBM(uv + vec2(0.001, 0)), noise - FBM(uv + vec2(0, 0.001)), 1.0));

    float brightness = CalculateDiffuseLight(normal, lightDirection);
    
    lightSource = vec3(0.9, 0.1, 1.0);
    vec3 viewDirection = normalize(lightSource - currentPosition);
    vec3 fresnelNormal = normalize(vec3(normal.x, normal.y, 1.0));
    float base = 1.0 - dot(viewDirection, fresnelNormal);
    float exponential = pow(base, 0.2);
    float R = exponential + DIFFUSE_FRESNEL_BIAS * (1.0 - exponential);
    R *= 0.05;
    vec3 fresnel = FRESNEL_COLOR * clamp(R, 0.04, 1.0);


    gl_FragColor = vec4(noise * DIFFUSE_COLOR.r * 0.7, 0, 0, 1.0);  // yeah idk

    base = 1. - clamp(dot(viewDirection, reflect(-lightDirection, normal)), 0., 1.);
	exponential = pow(base, 0.2);
	R = exponential + SPECULAR_FRESNEL_BIAS * (1.0 - exponential);

    if (USE_DIFFUSE)
        noise += brightness;

    if (USE_DIFFUSE_HIGHLIGHTS)
        gl_FragColor += vec4(DIFFUSE_HIGHLIGHT_COLOR * brightness, 0.0);
    if (USE_SPECULAR_HIGHLIGHTS)
        gl_FragColor += vec4(SPECULAR_COLOR * CalculateSpecularLight(normal, lightDirection, currentPosition), 1.0) * R;
    if (USE_FRESNEL)
        gl_FragColor += vec4(fresnel, 1.0);
    if (USE_AMBIENT)
        gl_FragColor += vec4(AMBIENT_COLOR, 1.0);
}
