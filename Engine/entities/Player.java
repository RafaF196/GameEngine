package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector3f;

import animatedModel.AnimatedModel;
import renderEngine.DisplayManager;
import terrains.Terrain;

public class Player extends AnimatedEntity {

	public static final float GRAVITY = -98f;
	private static final float JUMP_POWER = 100;
	
	private float runSpeed = 40;
	private float currentSpeed = 0;
	private float upwardsSpeed = 0;
	
	private Vector3f objectivePosition = this.getPosition();
	private Vector3f velocity = new Vector3f();
	
	private boolean isInAir = false;
	private boolean isMoving = false;
	private boolean colliding = false;

	public Player(AnimatedModel model, Vector3f position, float rotX, float rotY, float rotZ,
			float scale) {
		super(model, position, rotX, rotY, rotZ, scale);
	}

	public void gravityForce(Terrain terrain, Vector3f colPos) {
		upwardsSpeed += GRAVITY * DisplayManager.getFrameTimeSeconds();
		super.increasePosition(0, upwardsSpeed * DisplayManager.getFrameTimeSeconds(), 0);
		this.velocity.y = upwardsSpeed * DisplayManager.getFrameTimeSeconds();
		float terrainHeight = terrain.getHeightOfTerrain(getPosition().x, getPosition().z);
		if (super.getPosition().y < terrainHeight) {
			upwardsSpeed = 0;
			isInAir = false;
			super.getPosition().y = terrainHeight;
		} 
		try {
			if (colPos != null) {
				//System.out.println(colPos.y);
			}
			if (super.getPosition().y < colPos.y) {
				upwardsSpeed = 0;
				isInAir = false;
				//super.getPosition().y = colPos.y - super.getPosition().y > 5000*DisplayManager.getFrameTimeSeconds() ?
					//	super.getPosition().y : colPos.y;
				super.getPosition().y = colPos.y;
			}
		} catch (NullPointerException e) { } 
	}
	
	public void moveTo() {
		Vector3f cPos = this.getPosition();
		Vector3f oPos = this.objectivePosition;
		float distx = oPos.x - cPos.x;
		float disty = oPos.y - cPos.y;
		float distz = oPos.z - cPos.z;
		float total = (Math.abs(distx) + /*Math.abs(disty) +*/ Math.abs(distz));
		float normx = total == 0 ? 0 : distx / total;
		float normy = total == 0 ? 0 : disty / total;
		float normz = total == 0 ? 0 : distz / total;
		float distance = runSpeed * DisplayManager.getFrameTimeSeconds();
		float dx = distance * normx;
		float dy = distance * normy;
		float dz = distance * normz;
		float angle = (float) Math.atan2(dx,dz);
		if (angle < 0) { angle += Math.PI*2; }
		if (distance < total) {
			if (!this.colliding) super.setRotY((float) Math.toDegrees(angle));
			this.velocity = new Vector3f(dx, 0, dz);
			this.isMoving = true;
			super.increasePosition(dx, 0, dz);
		}
		else {
			this.isMoving = false;
			this.colliding = false;
			this.velocity.x = 0;
			this.velocity.z = 0;
		}
	}

	public void jump() {
		if (!isInAir) {
			this.upwardsSpeed = JUMP_POWER;
			isInAir = true;
		}
	}

	public float getUpwardsSpeed() {
		return upwardsSpeed;
	}

	public void setUpwardsSpeed(float upwardsSpeed) {
		this.upwardsSpeed = upwardsSpeed;
	}

	public float getRunSpeed() {
		return runSpeed;
	}

	public void increaseRunSpeed(float runSpeed) {
		if (this.runSpeed + runSpeed >= 1) {
			this.runSpeed += runSpeed;
		} else { this.runSpeed = 1; }
	}

	public float getCurrentSpeed() {
		return currentSpeed;
	}

	public Vector3f getObjectivePosition() {
		return objectivePosition;
	}

	public void setObjectivePosition(Vector3f objectivePosition, Boolean isColliding) {
		this.objectivePosition = objectivePosition;
		this.colliding = isColliding;
	}
	
	public Vector3f getVelocity() {
		return velocity;
	}
	
	public void update(){
		super.update(this.isMoving, this.runSpeed/40);
	}

}
