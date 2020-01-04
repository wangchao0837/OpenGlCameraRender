attribute vec4 vPosition;
attribute vec2 vCoord;
varying vec2 aCoord;


uniform mat4 vMatrix;

void main(){
    gl_Position = vMatrix * vPosition;
    aCoord = vCoord;
}