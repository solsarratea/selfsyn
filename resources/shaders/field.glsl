#ifdef GL_ES
precision mediump float;
#endif
#define PI 3.141592
#define TAU (2.*PI)
#define time iTime

#define PI 3.141592
#define ITER 64.
#define MAT_TUN 0.
#define MAT_SPHE 1.

vec4 bands = texture(iChannel0, gl_FragCoord.xy);
uniform float iOvertoneVolume;
uniform float iBeat;

float smin( float a, float b, float k ){float res = exp( -k*a ) + exp( -k*b );return -log( res )/k;}

vec3 a = vec3(0.5,0.5,0.25);
vec3 b = vec3(0.5,0.5,0.4);
vec3 c = vec3(.2,.3,1.);
vec3 d = vec3(.1,0.3,0.62);
vec3 palette (float t) { return a+b*cos(2.*PI*(c*t+d));}

const vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
vec3 hsv2rgb(vec3 c) {
  vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
  return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec2 multC(vec2 a, vec2 b){ float re = a.x*b.x - a.y*b.y; float im = a.x*b.y + a.y*b.y;return vec2(re,im);
}

void  reflection(inout vec2 pos, float angle){ vec2 normal = vec2(cos(angle),sin(angle)); float d = dot(pos, normal); pos -= normal*min(0.,d)*2.;}
   // return smoothstep(0.1,0.,abs(d));}

float beat(float bpm, float q, float intensity){ float bps = 60./bpm; float bpmVis = tan((iTime*PI/(2. * q))/bps);
 return min(abs(bpmVis),1.) * intensity; }

mat2 rot (float a) { float c=cos(a),s=sin(a); return mat2(c,-s,s,c); }
mat2 rot (float a,float b) { float c=cos(a),s=sin(b); return mat2(c,-s,s,c);}
float hash21(vec2 x)  {return fract(sin(dot(x,vec2(12.35,18.5)))*1245.5);}
void gammaC (inout vec3 color, float g){ pow(color.rgb, vec3(1./g));}
vec3 opRept(vec3 p, vec3 c) {return mod(p, c) - 0.5 * c;}
vec2 moda (vec2 p, float per){ float a = atan(p.y,p.x);  float l = length(p);
  a= mod(a-per/2., per) -per/2.; return vec2 (cos(a), sin(a))*l;
}

vec2 mo (vec2 p, vec2 d)
{
    p.x = abs(p.x)-d.x;
    p.y = abs(p.y)-d.y;
    if (p.y>p.x) p.xy = p.yx;
    return p;
}

float stmin(float a, float b, float k ,float n)
{
    float st = k/n;
    float u = b-k;
    return min(min(a,b), 0.5 * (u+a+abs(mod(u-a+st, 2.*st)-st)));
}

float cyl(vec2 p, float r)
{return length(p)-r;}

float box(vec3 p, vec3 c)
{
    p.xz *= rot(iTime);
    return length(max(abs(p)-c,0.));
}

float sphe(vec3 p, float r)
{return length(p)-r;}

float prim1(vec3 p)
{
    float per = 5.;
    p.xy = mo(p.xy, vec2(10.));
   p.xz = moda(p.xz, 2.*PI/5.);
    p.x -= 2.;
    p.xz *= rot(p.y*0.8);
    return stmin(cyl(p.xz, 0.3), box(vec3(p.x,mod(p.y-per/2.,per)-per/2., p.z),vec3(1.)), 0.8, 5.);
}

vec2 mat_min(vec2 a, vec2 b)
{
    if (a.x < b.x) return a;
    else return b;
}

vec2 tunnel (vec3 p)
{
        p.xy *= rot(p.z*0.2+iTime*0.5);
    float s = prim1(p);
    for (int i = 0; i<8; i++)
    {
        p.xy *= rot(PI/4.);
        p.z -= 8.;
        s = min(s, prim1(p));
    }
    return vec2(s, MAT_TUN);
}

vec2 prim2 (vec3 p)
{
    p.z += cos(p.x + iTime);
    p.x += sin(p.y+iTime*2.);
    p.y -= sin(p.z*2.+iTime*3.);
    return vec2(sphe(p,5.), MAT_SPHE);
}
vec3 opRep(vec3 p, vec3 c) {	return mod(p, c) - 0.5 * c; }


float g = 0.;

vec2 scene2(vec3 p)
{
    vec2 d = mat_min(tunnel(p), prim2(p));
     // glow from lsdlive, originally from balkhan : https://www.shadertoy.com/view/4t2yW1
    g += 0.01/(0.01+d.x*d.x);
    return d;
}

float scene(vec3 pos){
  vec3 p = pos;
  p = opRep(p,vec3(0.,0.,10));
      for (int i = 0; i<1; i++)
    {
   p.xy = mo(p.xy, vec2(20.,3.));
   reflection(p.zy, time*0.2);
   p.xz = moda(p.xz, 2.*PI/2.+sin(time));
   p.x -= 5.;
    p.xz *= rot(p.y*0.8);
    }
    float b = cyl(p.xz,2.);//-sin(time+pos.x)+cos(time+pos.y);

    return b;
}

vec3 estimateNormal(vec3 p) {
    vec2 e = vec2 (0.11, 0.);
    vec3 n = scene(p)- vec3(scene(p-e.xyy),scene(p-e.yxy), scene(p-e.yyx));
    return normalize(n);
}

vec3 transparent (vec3 rayOrigin, vec3 camOrigin){
    vec3 color;float layers = 0.; float aD;
     float thD = .035 + smoothstep(-0.2, 0.2, sin(time*0.75 - 3.14159*0.4))*0.025;
    float max_distance = 50.;
    float totalDist = 0.;
    const int max_pasos = 56;

    for (int i = 0; i < max_pasos; i++) {
        vec3 posOnRay = totalDist*rayOrigin + camOrigin;
        float dist = scene(posOnRay);

    if (color.x > .9 ||totalDist > max_distance || layers > 15.){break;}
        aD = (thD-abs(dist)*14./16.)/thD;

        if(aD>0.) {
            color += aD*aD*(10. - 2.*aD)/(1. + totalDist*totalDist*.25)*.2;
            layers++;
        }

        totalDist += max(abs(dist)*.8, thD*1.5);
    }

    return max(color, vec3(0.));
}

float trace (vec3 ro, vec3 co){
    float tD = 0.;
    for (int i = 0; i < 40; i++) {
        float dist = scene(tD*ro+ co);
        tD += dist;
        if (dist < 0.001){break;}
        if(tD > 80.){return 0.;}

    }

    return tD;
}
vec3 luz(vec3 po, vec3 co){

    vec3 fuenteDeLuz = vec3(10.,20.,-10.);
    vec3 normal = estimateNormal(po);

    vec3 l = normalize(fuenteDeLuz - po);
    float dif = clamp(dot(l, normal),0.,1.);
    vec3 colorDif = vec3(0.2,0.3,0.5)+hsv2rgb(po*0.1)*0.1*dif;

    vec3 lReflejada =normalize(reflect(-l,normal));
    vec3 camDir = normalize(co - po);
    float specular = pow(clamp(dot(lReflejada, camDir),0.,1.),220.2+iOvertoneVolume);
    specular = min(specular,dif);
    vec3 colorSpec = vec3(0.85)*specular;

    vec3 colorAmb = palette(length(po)+time)*0.05;


    return  colorDif + colorAmb.rgb + colorSpec;
}

float fishL (vec2 pos,float a) {return (a-dot(pos.xy, pos.xy)*.5)*.5;}
float quadL (vec2 pos, float a){return (a-max(abs(pos.x),abs(pos.y)));}

void main(void)
{  vec4 color;
   vec2 uv = vec2(gl_FragCoord.xy / iResolution.xy) * 2.0 -1.; uv /= vec2(iResolution.y / iResolution.x, 1);
   uv *= iBeat ;
  float sound = iOvertoneVolume;
  float lens =fishL(uv,1.3+iOvertoneVolume); vec3 pos = vec3(uv,lens);
  //lens = length(uv*beat(128.,2.5,1));
  vec3 ro = normalize(pos); vec3 co = vec3(0., 0.,-10.);



 float tD = 0.;
    for (int i = 0; i < 80; i++) {
        float dist = scene(tD*ro+ co);
        tD += dist;
        if (dist < 0.001){break;}
    }
    if (tD < 80){
      color.rgb = luz(tD*ro+co,co).rgb*iBeat;
    }

  //color.rgb *=  transparent(ro,co).r;

  gammaC(color.rgb, 1.2);

  gl_FragColor = color;
}
