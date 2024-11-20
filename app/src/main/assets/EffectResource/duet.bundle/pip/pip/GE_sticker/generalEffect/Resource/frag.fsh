precision highp float; 

varying vec2 textureCoordinate; 
// varying vec3 pos_debug;

uniform sampler2D defaultBGTexture;
uniform sampler2D userInputTexture;

uniform int u_switch;

void main() {
    vec4 outColor;

    // show default background
    if (u_switch == 0){
        outColor = texture2D(defaultBGTexture, textureCoordinate);
        
    }
    // show user input background
    else{
        if(textureCoordinate.x < 0.0 || 
           textureCoordinate.y < 0.0 ||
           textureCoordinate.x > 1.0 || 
           textureCoordinate.y > 1.0){
            outColor = vec4(1.0, 1.0, 1.0, 1.0);
        }else{
            outColor = texture2D(userInputTexture, textureCoordinate);
        }
    }

    gl_FragColor = outColor;
    //gl_FragColor = vec4(pos_debug * 0.5 + 0.5, 1.0);
}
