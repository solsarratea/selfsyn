// taken from the Shadertone example and added rotation
// https://github.com/overtone/shadertone/blob/master/examples/zoomwave.glsl
// licensed as part of Shadertone under these conditions: https://github.com/overtone/shadertone/blob/master/LICENSE

#ifdef GL_ES
precision mediump float;
#endif

float PI = 3.14159;
const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
vec3 hsv2rgb(vec3 c) {
  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec2 multC(vec2 a, vec2 b){
    float re = a.x*b.x - a.y*b.y;
    float im = a.x*b.y + a.y*b.x;
    return vec2(re,im);
}

vec2 rotate(vec2 space, vec2 center, float amount){
    return vec2(cos(amount) * (space.x - center.x) + sin(amount) * (space.y - center.y),
        cos(amount) * (space.y - center.y) - sin(amount) * (space.x - center.x));
}
float reflection(inout vec2 pos, float angle){
    vec2 normal = vec2(cos(angle),sin(angle));
    float d = dot(pos, normal);
    pos -= normal*min(0.,d)*2.;
    return smoothstep(0.1,0.,abs(d));
}
float rand(const in float n){return fract(sin(n) * 1e4);}
float rand(const in vec2 n) { return fract(1e4 * sin(17.0 * n.x + n.y * 0.1) * (0.1 + abs(sin(n.y * 13.0 + n.x))));
}

float noise(float x) {
    float i = floor(x);
    float f = fract(x);
    float u = f * f * (3.0 - 2.0 * f);
    return mix(rand(i), rand(i + 1.0), u);
}
float noise(vec2 x) {
    vec2 i = floor(x);
    vec2 f = fract(x);

    // Four corners in 2D of a tile
    float a = rand(i);
    float b = rand(i + vec2(1.0, 0.0));
    float c = rand(i + vec2(0.0, 1.0));
    float d = rand(i + vec2(1.0, 1.0));

    vec2 u = f * f * (3.0 - 2.0 * f);
    return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}
void main()
{
    vec2  uvN = gl_FragCoord.xy/iResolution.xy;
    vec2  pos = gl_FragCoord.xy/iResolution.xy * 2. -1.;
    vec2  uv = pos;

    float sound   = texture(iChannel0, uvN).x;

    vec4 color;

    color.rb = pos;

    for (float i = 0.0; i < 4.; i++) {
        //Escalo por constate
        pos *= 1.5;
        //Reflejo del plano
        reflection(pos,PI/3.);
        //Rotacion del plano
        pos = rotate(pos,vec2(0.,0.),pos.x+iTime*0.2);
        //Traslaciones
        //pos.x += 0.;
        reflection(pos,-PI/3.);
        //Escalo por valor que depende de la posicion
        pos *= 1./dot(pos,pos);
    }

    color.gb = pos;// noise(pos*4.+.5*iTime);
    color.rgb = hsv2rgb(color.rgb);


    gl_FragColor = color;
}
