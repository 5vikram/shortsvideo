attribute vec3 attPosition;
attribute vec2 attUV;

varying vec2 textureCoordinate;
// varying vec3 pos_debug;

uniform float u_width_offset;
uniform float u_height_offset;

uniform float u_orientation;
uniform float u_rotate;
uniform float u_scale;
uniform float u_height_ratio;

uniform vec2 u_uv_scale;
uniform vec2 u_translate;

mat3 compose_rot_mat(float deg){
    float rad = radians(deg);
    mat3 rot = mat3(
        cos(rad), -sin(rad), 0., 
        sin(rad), cos(rad), 0., 
        0., 0., 1.
    );

    return rot;
}

void main() {
    vec3 pos = attPosition;
    pos.y *= u_height_ratio;

    // scale
    pos *= u_scale;

    // rotate
    /// image orientation
    float angle = radians(u_orientation * 90.);
    mat3 rot_orient = compose_rot_mat(angle);

    /// rotation controller
    mat3 rot_rotation = compose_rot_mat(u_rotate);
    mat3 rot = rot_rotation * rot_orient;

    pos = pos * rot;
    
    // calibrate with screen size
    pos.y *= (u_height_offset / u_width_offset);//= screenWidth / screenHeight

    // translate
    pos += vec3(u_translate.x, u_translate.y, 0.0);
    // pos_debug = pos;
    gl_Position = vec4(pos, 1.);
    textureCoordinate = 0.5 + (attUV - 0.5) * u_uv_scale;
}
