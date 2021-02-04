#ifdef GL_ES
precision mediump float;
#endif

float smoothbump(float center, float width, float x) {
    float w2 = width/4.00;
    float cp = center+w2;
    float cm = center-w2;
    float c = smoothstep(cm, center, x) * (1.0-smoothstep(center, cp, x));
    return c;
}

vec3 hsv2rgb(float h, float s, float v) {
    return mix(vec3(1.), clamp((abs(fract(h+vec3(3., 2., 1.)/3.)*6.-3.)-1.), 0., 1.), s)*v;
}

vec3 cosPalette(float t){
    vec3 a = vec3(0.2,0.5,0.25);
    vec3 b = vec3(0.3,0.4,0.4);
    vec3 c = vec3(.2,1.,3.);
    vec3 d = vec3(0.,0.3,0.6);

    return a + b*cos( 6.28318*(c*t+d));
}
vec2 multC(vec2 a, vec2 b){
    float re = a.x*b.x - a.y*b.y;
    float im = a.x*b.y + a.y*b.x;
    return vec2(re,im);
}


void main()
{
    vec2  uvN = gl_FragCoord.xy/iResolution.xy;
    vec2  pos = gl_FragCoord.xy/iResolution.xy * 2. -1.;
    vec2  uv = pos;

    float sound   = texture(iChannel0, vec2(0.75, 0.75)).x;

    vec3 color;
    //pos -= vec2(0.7,0.1);

    pos *= (2. +4.* sound);
    float dist = 0.;
    const int max_pasos = 10;

    for (int i = 0; i < max_pasos; i++) {
        pos = multC(pos,pos);
        vec2 c = vec2( sin(iTime*0.2),cos(iTime*0.2));
        pos -= c;


        float magnitud = length(pos);
        if ( magnitud > 100. ) break;
        dist += 1./float(max_pasos);

    }
    color -= 1./abs(dot(pos,pos));

    color.bgr = cosPalette(dist+sound)*(1.-step(0.2,sound));
    float am = sin(iTime + atan(pos.x,pos.y));

    if (am > 0.5){
         color = texture2D(iChannel1,
                     uvN + vec2(0.001,0.009)* sin(am+dot(uv,uv))
                ).ggg;

    }
    //  color = mix(color,newcolor,0.4);
    gl_FragColor = vec4(color,1.);
}
