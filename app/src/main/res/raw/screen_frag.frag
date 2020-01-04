precision mediump float;

varying vec2 aCoord;

uniform sampler2D vTexture;

void main(){
    gl_FragColor = texture2D(vTexture, aCoord);
}