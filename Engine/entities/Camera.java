package entities;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import terrains.Terrain;
import toolbox.MousePicker;

public class Camera {
	
	private float distanceFromPlayer = 30;
	public float actualDistanceFromPlayer = distanceFromPlayer;
	private float angleAroundPlayer = 0;
	
	private Vector3f position = new Vector3f(0, 0, 0);
	private float pitch = 30;
	private float yaw = 0;
	private float roll;
	
	private Player player;
	private Terrain terrain;
	
	public Camera(Player player, Terrain terrain){
		this.player = player;
		this.terrain = terrain;
		this.angleAroundPlayer = player.getRotY();
	}
	
	public void move(){
		this.actualDistanceFromPlayer = distanceFromPlayer;
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (/*player.getRotY() + */angleAroundPlayer);
		yaw %= 360;
		isCameraUnderGround(this.position);
	}
	
	public void moveAfter(){
		calculateZoom();
		calculatePitch();
		calculateAngleAroundPlayer();
		float horizontalDistance = calculateHorizontalDistance();
		float verticalDistance = calculateVerticalDistance();
		calculateCameraPosition(horizontalDistance, verticalDistance);
		this.yaw = 180 - (/*player.getRotY() + */angleAroundPlayer);
		yaw %= 360;
		isCameraUnderGround(this.position);
	}
	
	public void invertPitch(){
		this.pitch = -pitch;
	}

	public Vector3f getPosition() {
		return position;
	}

	public float getPitch() {
		return pitch;
	}

	public float getYaw() {
		return yaw;
	}

	public float getRoll() {
		return roll;
	}
	
	private void calculateCameraPosition(float horizDistance, float verticDistance){
		float theta = /*player.getRotY() +*/ angleAroundPlayer;
		float offsetX = (float) (horizDistance * Math.sin(Math.toRadians(theta)));
		float offsetZ = (float) (horizDistance * Math.cos(Math.toRadians(theta)));
		position.x = player.getPosition().x - offsetX;
		position.z = player.getPosition().z - offsetZ;
		position.y = player.getPosition().y + verticDistance + 4;
	}
	
	private float calculateHorizontalDistance(){
		return (float) (actualDistanceFromPlayer * Math.cos(Math.toRadians(pitch+4)));
	}
	
	private float calculateVerticalDistance(){
		return (float) (actualDistanceFromPlayer * Math.sin(Math.toRadians(pitch+4)));
	}
	
	private void calculateZoom(){
		float zoomLevel = Mouse.getDWheel() * 0.02f;
		distanceFromPlayer -= zoomLevel;
		if(distanceFromPlayer < 10){
			distanceFromPlayer = 10;
		} else if (distanceFromPlayer > 60){
			distanceFromPlayer = 60;
		}
		if (actualDistanceFromPlayer > distanceFromPlayer) {
			actualDistanceFromPlayer = distanceFromPlayer;
		}
	}
	
	public void isCameraUnderGround(Vector3f cameraPos){
		if (isUnderGround(cameraPos)){
			this.actualDistanceFromPlayer -= 0.2;
			moveAfter();
		} else {
			this.actualDistanceFromPlayer -= 0.1; //que no se vea por debajo
		}
	}
	
	public boolean isUnderGround(Vector3f testPoint) {
		Terrain terrain = getTerrain(testPoint.getX(), testPoint.getZ());
		float height = 0;
		if (terrain != null) {
			height = terrain.getHeightOfTerrain(testPoint.getX(), testPoint.getZ()) + 0.4f;
		}
		if (testPoint.y < height) {
			return true;
		} else {
			return false;
		}
	}
	
	private Terrain getTerrain(float worldX, float worldZ) {
		return terrain;
	}
	
	private void calculatePitch(){
		if(Mouse.isButtonDown(1)){
			float pitchChange = Mouse.getDY() * 0.2f;
			pitch -= pitchChange;
		}
	}
	
	private void calculateAngleAroundPlayer(){
		if(Mouse.isButtonDown(1)){
			float angleChange = Mouse.getDX() * 0.2f;
			angleAroundPlayer -= angleChange;
		}
	}

}
