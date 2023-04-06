package renderEngine;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import animatedModel.AnimatedModel;
import entities.AnimatedEntity;
import entities.Camera;
import entities.Entity;
import entities.Light;
import models.TexturedModel;
import normalMappingRenderer.NormalMappingRenderer;
import renderer.AnimatedModelRenderer;
import renderer.AnimatedModelShader;
import shaders.StaticShader;
import shaders.TerrainShader;
import shadows.ShadowMapMasterRenderer;
import skybox.SkyboxRenderer;
import terrains.Terrain;

public class MasterRenderer {

	public static final float FOV = 70;
	public static final float NEAR_PLANE = 0.1f;
	public static final float FAR_PLANE = 1200;
	
	public static float time;

	public static final float DAY_RED = 0.65f;
	public static final float DAY_GREEN = 0.64f;
	public static final float DAY_BLUE = 0.74f;
	
	public static final float NIGHT_RED = 0.04f;
	public static final float NIGHT_GREEN = 0.04f;
	public static final float NIGHT_BLUE = 0.05f;
	
	public static float RED = DAY_RED;
	public static float GREEN = DAY_GREEN;
	public static float BLUE = DAY_BLUE;
	
	public static float fogDensity = 0.000f;
	public static float fogGradient = 2.0f;

	private Matrix4f projectionMatrix;

	private StaticShader shader = new StaticShader();
	private EntityRenderer renderer;

	private TerrainShader terrainShader = new TerrainShader();
	private TerrainRenderer terrainRenderer;
	
	private AnimatedModelShader animatedShader = new AnimatedModelShader();
	private AnimatedModelRenderer animatedRenderer;
	
	private NormalMappingRenderer normalMapRenderer;

	private SkyboxRenderer skyboxRenderer;
	private ShadowMapMasterRenderer shadowMapRenderer;

	private Map<TexturedModel, List<Entity>> entities = new HashMap<TexturedModel, List<Entity>>();
	private Map<TexturedModel, List<Entity>> normalMapEntities = new HashMap<TexturedModel, List<Entity>>();
	private Map<AnimatedModel, List<AnimatedEntity>> animatedEntities = new HashMap<AnimatedModel, List<AnimatedEntity>>();
	private List<Terrain> terrains = new ArrayList<Terrain>();

	public MasterRenderer(Loader loader, Camera camera) {
		enableCulling();
		createProjectionMatrix();
		renderer = new EntityRenderer(shader, projectionMatrix);
		terrainRenderer = new TerrainRenderer(terrainShader, projectionMatrix);
		skyboxRenderer = new SkyboxRenderer(loader, projectionMatrix);
		normalMapRenderer = new NormalMappingRenderer(projectionMatrix);
		animatedRenderer = new AnimatedModelRenderer(animatedShader, projectionMatrix);
		this.shadowMapRenderer = new ShadowMapMasterRenderer(camera);
		time = 14000;
	}

	public Matrix4f getProjectionMatrix() {
		return this.projectionMatrix;
	}

	public void renderScene(List<Entity> entities, List<Entity> normalEntities, List<AnimatedEntity> animEntities, List<Terrain> terrains,
			List<Light> lights, Camera camera, Vector4f clipPlane) {
		for (Terrain terrain : terrains) {
			processTerrain(terrain);
		}
		for (Entity entity : entities) {
			processEntity(entity);
		}
		for(Entity entity : normalEntities){
			processNormalMapEntity(entity);
		}
		for(AnimatedEntity entity : animEntities){
			processAnimatedEntity(entity);
		}
		render(lights, camera, clipPlane);
	}

	public void render(List<Light> lights, Camera camera, Vector4f clipPlane) {
		prepare();
		shader.start();
		shader.loadClipPlane(clipPlane);
		shader.loadSkyColour(RED, GREEN, BLUE);
		shader.loadLights(lights);
		shader.loadViewMatrix(camera);
		renderer.render(entities, shadowMapRenderer.getToShadowMapSpaceMatrix());
		shader.stop();
		normalMapRenderer.render(normalMapEntities, clipPlane, lights, camera, shadowMapRenderer.getToShadowMapSpaceMatrix());
		animatedShader.start();
		animatedShader.loadClipPlane(clipPlane);
		animatedShader.loadSkyColour(RED, GREEN, BLUE);
		animatedShader.loadLights(lights);
		animatedShader.loadViewMatrix(camera);
		animatedShader.stop();
		animatedRenderer.render(animatedEntities, shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainShader.start();
		terrainShader.loadClipPlane(clipPlane);
		terrainShader.loadSkyColour(RED, GREEN, BLUE);
		terrainShader.loadLights(lights);
		terrainShader.loadViewMatrix(camera);
		terrainRenderer.render(terrains, shadowMapRenderer.getToShadowMapSpaceMatrix());
		terrainShader.stop();
		skyboxRenderer.render(camera, RED, GREEN, BLUE, time);
		terrains.clear();
		entities.clear();
		normalMapEntities.clear();
		animatedEntities.clear();
	}

	public static void enableCulling() {
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glCullFace(GL11.GL_BACK);
	}

	public static void disableCulling() {
		GL11.glDisable(GL11.GL_CULL_FACE);
	}

	public void processTerrain(Terrain terrain) {
		terrains.add(terrain);
	}

	public void processEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = entities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			entities.put(entityModel, newBatch);
		}
	}
	
	public void processNormalMapEntity(Entity entity) {
		TexturedModel entityModel = entity.getModel();
		List<Entity> batch = normalMapEntities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<Entity> newBatch = new ArrayList<Entity>();
			newBatch.add(entity);
			normalMapEntities.put(entityModel, newBatch);
		}
	}
	
	public void processAnimatedEntity(AnimatedEntity entity) {
		AnimatedModel entityModel = entity.getModel();
		List<AnimatedEntity> batch = animatedEntities.get(entityModel);
		if (batch != null) {
			batch.add(entity);
		} else {
			List<AnimatedEntity> newBatch = new ArrayList<AnimatedEntity>();
			newBatch.add(entity);
			animatedEntities.put(entityModel, newBatch);
		}
	}
	
	public void renderShadowMap(List<Entity> entityList, List<Entity> normalEntities, List<Terrain> terrains, List<AnimatedEntity> animEntities,
			Light sun){
		for (Terrain terrain : terrains) {
			processTerrain(terrain);
		}
		for (Entity entity : entityList){
			processEntity(entity);
		}
		for (Entity entity : normalEntities){
			processNormalMapEntity(entity);
		}
		for (AnimatedEntity entity : animEntities){
			processAnimatedEntity(entity);
		}
		shadowMapRenderer.render(entities, normalMapEntities, terrains, animatedEntities, sun);
		entities.clear();
	}
	
	public int getShadowMapTexture(){
		return shadowMapRenderer.getShadowMap();
	}

	public void cleanUp() {
		shader.cleanUp();
		terrainShader.cleanUp();
		animatedShader.cleanUp();
		normalMapRenderer.cleanUp();
		shadowMapRenderer.cleanUp();
	}

	public void prepare() {
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		GL11.glClearColor(RED, GREEN, BLUE, 1);
		GL13.glActiveTexture(GL13.GL_TEXTURE5);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, getShadowMapTexture());
	}

    private void createProjectionMatrix(){
    	projectionMatrix = new Matrix4f();
		float aspectRatio = (float) Display.getWidth() / (float) Display.getHeight();
		float y_scale = (float) ((1f / Math.tan(Math.toRadians(FOV / 2f))));
		float x_scale = y_scale / aspectRatio;
		float frustum_length = FAR_PLANE - NEAR_PLANE;

		projectionMatrix.m00 = x_scale;
		projectionMatrix.m11 = y_scale;
		projectionMatrix.m22 = -((FAR_PLANE + NEAR_PLANE) / frustum_length);
		projectionMatrix.m23 = -1;
		projectionMatrix.m32 = -((2 * NEAR_PLANE * FAR_PLANE) / frustum_length);
		projectionMatrix.m33 = 0;
    }

	public static void update(Light sun) {
		time += DisplayManager.getFrameTimeSeconds() * 100;
		time %= 24000;
		
		if (time >= 19000) time = 6000;
		
		float r = 1000000;
		float angle = (float) ((time-6000)/(24000/(2*Math.PI)));
		float x = (float) (r*Math.cos(angle));
		float y = (float) (r*Math.sin(angle));
		
		sun.setPosition(new Vector3f(x, y, -300000.0f));
		
		float blendFactor;		
		if (time >= 0 && time < 6000) {
			blendFactor = 0;
		} else if (time >= 5000 && time < 7000) {
			blendFactor = (time - 5000)/(7000 - 5000);
		} else if (time >= 7000 && time < 17000) {
			blendFactor = 1;
		} else if (time >= 17000 && time < 19000) {
			blendFactor = (19000 - time)/(19000 - 17000);
		} else {
			blendFactor = 0;
		}
		
		sun.setAttenuation(new Vector3f(1.0f, (1-blendFactor)/130000 + fogDensity/2000, 0.0f));
		
		RED = DAY_RED * blendFactor + NIGHT_RED * (1 - blendFactor);
		GREEN = DAY_GREEN * blendFactor + NIGHT_GREEN * (1 - blendFactor);
		BLUE = DAY_BLUE * blendFactor + NIGHT_BLUE * (1 - blendFactor);
	}

}
