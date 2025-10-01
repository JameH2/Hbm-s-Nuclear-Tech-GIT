#version 120


uniform float time;        // animation time
uniform float divergence;  // 1 = no divergence, >1 = diverging
uniform float colorR;      // red channel multiplier
uniform float colorG;      // green channel multiplier
uniform float colorB;      // blue channel multiplier
uniform float interp; 

vec2 cpow(vec2 z, float n){
    float r = length(z);
    float a = atan(z.y, z.x);
    return pow(r, n) * vec2(cos(a * n), sin(a * n));
}

vec2 rot(vec2 uv, float t){
    return mat2(cos(t), sin(t), -sin(t), cos(t)) * uv;
}

float mandelbrot(vec2 z, float k, vec2 uv, int iter, const int MAXITER, float R_out){
    while(length(z) < min(R_out, 15.0) && iter < MAXITER) {
        z = cpow(z, k) + uv;
        iter += 1;
    }
    return (float(iter) + 1. - log(log(length(z))) / log(2.)) / (float(MAXITER) + 1. - log(log(length(z))) / log(2.));
}


void main() {
    // UV coordinates
    vec2 uv = gl_TexCoord[0].xy * 2.0 - 1.0;
    uv *= 2.0;
    int iter = 0;
    const int MAXITER = 50;
    vec2 z = vec2(0.0);

    // rotation speed
    float speed = 0.5 * time;


    const float KMAX = 7.0;
    float m = floor(interp);     
    float t = fract(interp);     
    m = clamp(m, 1.0, KMAX*2.0 - 1.0);
    t = clamp(t, 0.0, 1.0);
    float k = m + t;



    float R_out = divergence;
    float R_in = pow(k, (1.0 / (1.0 - k))) - pow(k, (k / (1.0 - k)));


    uv = rot(uv, speed);

    float color = mandelbrot(z, k, uv, iter, MAXITER, R_out);
    uv *= R_out / R_in;
    color = max(color, mandelbrot(z, k, -uv, iter, MAXITER, R_out));
    uv *= R_out / R_in;
    color = max(color, mandelbrot(z, k, uv, iter, MAXITER, R_out));

    color = pow(color, 0.9);

    color = pow(color, 0.6);
    float glow = 1 + 0.1 * color;
    color *= glow;
    color = clamp(color, 0.0, 1.0);

    float alpha = color; 
 
    // final color
    gl_FragColor = vec4(color * colorR, color * colorG, color * colorB, alpha);

}
