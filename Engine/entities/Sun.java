package entities;

import org.lwjgl.util.vector.Vector3f;

import particles.ParticleSystem;
import particles.ParticleTexture;
import renderEngine.Loader;
 
public class Sun {
	
	private static final float SUN_DISTANCE = 600;

	private ParticleSystem particles;
	private Vector3f sunPos;
	
	public Sun(Loader loader){
		ParticleTexture sunTexture = new ParticleTexture(loader.loadTexture("sunTexture"), 1, true);
		particles = new ParticleSystem(sunTexture, 120, 0.1f, 0.0f, 0.2f, 60);
		particles.setDirection(new Vector3f(0, 1, 0), 1.0f);
		particles.setLifeError(0.0f);
		particles.setSpeedError(0.1f);
		particles.setScaleError(0.05f);
		particles.randomizeRotation();
		sunPos = new Vector3f(0.0f, 100.0f, 0.0f);
	}
	
	public void drawSun(Vector3f camPos, Vector3f lightDir){
		this.sunPos = getWorldPosition(camPos, lightDir);
		particles.generateParticles(this.sunPos);
	}
	
	
	public Vector3f getWorldPosition(Vector3f camPos, Vector3f lightDirection) {
        Vector3f sunPos2 = new Vector3f(lightDirection);
        sunPos2.normalise();
        sunPos2.scale(SUN_DISTANCE);
        return Vector3f.add(camPos, sunPos2, null);
    }
	
}
