package entities;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import animatedModel.AnimatedModel;
import animation.Animation;

public class AnimatedEntity {

	private AnimatedModel model;
	private Vector3f position;
	private float rotX, rotY, rotZ;
	private float scale;
	
	private int textureIndex = 0;

	public AnimatedEntity(AnimatedModel model, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}
	
	public AnimatedEntity(AnimatedModel model, int index, Vector3f position, float rotX, float rotY, float rotZ, float scale) {
		this.textureIndex = index;
		this.model = model;
		this.position = position;
		this.rotX = rotX;
		this.rotY = rotY;
		this.rotZ = rotZ;
		this.scale = scale;
	}
	
	public float getTextureXOffset(){
		int column = textureIndex%model.getTexture().getNumberOfRows();
		return (float)column/(float)model.getTexture().getNumberOfRows();
	}
	
	public float getTextureYOffset(){
		int row = textureIndex/model.getTexture().getNumberOfRows();
		return (float)row/(float)model.getTexture().getNumberOfRows();
	}

	public void increasePosition(float dx, float dy, float dz) {
		this.position.x += dx;
		this.position.y += dy;
		this.position.z += dz;
	}

	public void increaseRotation(float dx, float dy, float dz) {
		this.rotX += dx;
		if (this.rotX > 360) this.rotX -= 360;
		if (this.rotX < 0) this.rotX += 360;
		this.rotY += dy;
		if (this.rotY > 360) this.rotY -= 360;
		if (this.rotY < 0) this.rotY += 360;
		this.rotZ += dz;
		if (this.rotZ > 360) this.rotZ -= 360;
		if (this.rotZ < 0) this.rotZ += 360;
	}

	public AnimatedModel getModel() {
		return model;
	}

	public void setModel(AnimatedModel model) {
		this.model = model;
	}

	public Vector3f getPosition() {
		return position;
	}

	public void setPosition(Vector3f position) {
		this.position = position;
	}

	public float getRotX() {
		return rotX;
	}

	public void setRotX(float rotX) {
		this.rotX = rotX;
	}

	public float getRotY() {
		return rotY;
	}

	public void setRotY(float rotY) {
		this.rotY = rotY;
	}

	public float getRotZ() {
		return rotZ;
	}

	public void setRotZ(float rotZ) {
		this.rotZ = rotZ;
	}

	public float getScale() {
		return scale;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}
	
	public Matrix4f[] getJointTransforms(){
		return model.getJointTransforms();
	}
	
	public void doAnimation(Animation animation, float startTime, float speed){
		model.doAnimation(animation, startTime, speed);
	}
	
	public void update(Boolean cA, float f){
		model.update(cA, f);
	}

}
