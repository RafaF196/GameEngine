package main;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import animatedModel.AnimatedModel;
import animation.Animation;
import collisions.CollisionDetector;
import collisions.CollisionMaster;
import collisions.CollisionPacket;
import loaders.AnimationLoader;
import entities.AnimatedEntity;
import entities.Camera;
import entities.Entity;
import entities.Light;
import entities.Player;
import entities.Sun;
import fontMeshCreator.FontType;
import fontMeshCreator.GUIText;
import fontRendering.TextMaster;
import guis.GuiRenderer;
import guis.GuiTexture;
import loaders.AnimatedModelLoader;
import models.RawModel;
import models.TexturedModel;
import normalMappingObjConverter.NormalMappedObjLoader;
import objConverter.OBJFileLoader;
import particles.ParticleMaster;
import particles.ParticleSystem;
import particles.ParticleTexture;
import postProcessing.Fbo;
import postProcessing.PostProcessing;
import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import renderEngine.OBJLoader;
import terrains.Terrain;
import textures.ModelTexture;
import textures.TerrainTexture;
import textures.TerrainTexturePack;
import toolbox.MousePicker;
import water.WaterFrameBuffers;
import water.WaterRenderer;
import water.WaterShader;
import water.WaterTile;
import xmlParser.MyFile;

public class MainGameLoop {

	public static void main(String[] args) {

		DisplayManager.createDisplay();
		Loader loader = new Loader();
		
		TextMaster.init(loader);
		
		AnimatedModel persona = AnimatedModelLoader.loadEntity(new MyFile(new MyFile("res"), "model.dae"),
				new MyFile(new MyFile("res"), "diffuse.png"), loader);
		Animation movingAnimation = AnimationLoader.loadAnimation(new MyFile(new MyFile("res"), "model.dae"));
		
		Player player = new Player(persona, new Vector3f(350, 5, -350), 0, 180, 0, 1.0f);
		
		// *********TERRAIN TEXTURE STUFF**********
		
		TerrainTexture backgroundTexture = new TerrainTexture(loader.loadTexture("grassy2"));
		
		//TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("mud"));
		//TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("grassFlowers"));
		//TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("path"));
		
		TerrainTexture rTexture = new TerrainTexture(loader.loadTexture("sand"));
		TerrainTexture gTexture = new TerrainTexture(loader.loadTexture("mountain"));
		TerrainTexture bTexture = new TerrainTexture(loader.loadTexture("mountaintop"));

		TerrainTexturePack texturePack = new TerrainTexturePack(backgroundTexture, rTexture, gTexture, bTexture);
		TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("blendMap2"));
		//TerrainTexture blendMap = new TerrainTexture(loader.loadTexture("worldterraintexture"));
		
		Terrain terrain = new Terrain(0, -1, loader, texturePack, blendMap, "heightmap");
		List<Terrain> terrains = new ArrayList<Terrain>();
		terrains.add(terrain);
		
		// -----------------------------------------		
		
		Camera camera = new Camera(player, terrain);
		
		MasterRenderer renderer = new MasterRenderer(loader, camera);
		ParticleMaster.init(loader, renderer.getProjectionMatrix());
		
		//ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleAtlas"), 4, true);
		ParticleTexture particleTexture = new ParticleTexture(loader.loadTexture("particleStar"), 1, true);
		
		ParticleSystem system = new ParticleSystem(particleTexture, 100, 20, -0.1f, 0.1f, 2);
		system.randomizeRotation();
		system.setDirection(new Vector3f(0, 1, 0), 1.0f);
		system.setLifeError(0.0f);
		system.setSpeedError(0.1f);
		system.setScaleError(0.1f);
		
		FontType font = new FontType(loader.loadTexture("candara"), "candara");
		GUIText text = new GUIText("This is some text!", 2f, font, new Vector2f(0.01f, 0.01f), 1f, false);
		
		List<Entity> entities = new ArrayList<Entity>();
		List<Entity> normalMapEntities = new ArrayList<Entity>();
		List<AnimatedEntity> animatedEntities = new ArrayList<AnimatedEntity>();

		// *****************************************
		
		ModelTexture fernTextureAtlas = new ModelTexture(loader.loadTexture("fern"));
		fernTextureAtlas.setNumberOfRows(2);

		TexturedModel fern = new TexturedModel(OBJFileLoader.loadOBJ("fern", loader), fernTextureAtlas);
		fern.getTexture().setHasTransparency(true);

		TexturedModel bobble = new TexturedModel(OBJFileLoader.loadOBJ("pine", loader), new ModelTexture(loader.loadTexture("pine")));
		bobble.getTexture().setHasTransparency(true);

		TexturedModel lamp = new TexturedModel(OBJLoader.loadObjModel("lamp", loader),
				new ModelTexture(loader.loadTexture("lamp")));
		lamp.getTexture().setUseFakeLighting(true);
		
		TexturedModel lantern = new TexturedModel(OBJLoader.loadObjModel("lantern", loader),
				new ModelTexture(loader.loadTexture("lantern")));
		lantern.getTexture().setExtraInfoMap(loader.loadTexture("lanternS"));
		
		TexturedModel cherryModel = new TexturedModel(OBJLoader.loadObjModel("cherry", loader),
				new ModelTexture(loader.loadTexture("cherry")));
		cherryModel.getTexture().setHasTransparency(true);
		cherryModel.getTexture().setShineDamper(10);
		cherryModel.getTexture().setReflectivity(0.5f);
		cherryModel.getTexture().setExtraInfoMap(loader.loadTexture("cherryS"));
		
		TexturedModel pAvatar = new TexturedModel(OBJLoader.loadObjModel("person", loader),
				new ModelTexture(loader.loadTexture("playerTexture")));		
		
		//******************NORMAL MAP MODELS************************
		
		TexturedModel barrelModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("barrel", loader),
				new ModelTexture(loader.loadTexture("barrel")));
		barrelModel.getTexture().setNormalMap(loader.loadTexture("barrelNormal"));
		barrelModel.getTexture().setShineDamper(10);
		barrelModel.getTexture().setReflectivity(0.5f);
		
		TexturedModel crateModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("crate", loader),
				new ModelTexture(loader.loadTexture("crate")));
		crateModel.getTexture().setNormalMap(loader.loadTexture("crateNormal"));
		crateModel.getTexture().setShineDamper(10);
		crateModel.getTexture().setReflectivity(0.5f);
		
		TexturedModel boulderModel = new TexturedModel(NormalMappedObjLoader.loadOBJ("boulder", loader),
				new ModelTexture(loader.loadTexture("boulder")));
		boulderModel.getTexture().setNormalMap(loader.loadTexture("boulderNormal"));
		boulderModel.getTexture().setShineDamper(10);
		boulderModel.getTexture().setReflectivity(0.5f);
		
		//******************ANIMATED MODELS************************
		
		//AnimatedEntity personaje = new AnimatedEntity(persona, new Vector3f(360, 10, -360), 0, 0, 0, 2f);
		//animatedEntities.add(personaje);
		
		player.doAnimation(movingAnimation, 0.3f, 1.0f);
		
		//************ENTITIES*******************
		
		animatedEntities.add(player);
		
		/*
		Entity entity = new Entity(barrelModel, new Vector3f(75, 10, -75), 0, 0, 0, 1f);
		Entity entity2 = new Entity(boulderModel, new Vector3f(85, 10, -75), 0, 0, 0, 1f);
		Entity entity3 = new Entity(crateModel, new Vector3f(65, 10, -75), 0, 0, 0, 0.04f);
		normalMapEntities.add(entity);
		normalMapEntities.add(entity2);
		normalMapEntities.add(entity3);
		*/
		
		Entity entity3 = new Entity(crateModel, new Vector3f(165, 25, -275), 0, 0, 0, 0.2f);
		Entity entity3b = new Entity(crateModel, new Vector3f(255, 5, -255), 0, 0, 45, 0.3f);
		normalMapEntities.add(entity3);
		normalMapEntities.add(entity3b);
		
		Random random = new Random(5666778);
		for (int i = 0; i < 50; i++) {
			if (i % 3 == 0) {
				float x = random.nextFloat() * 800;
				float z = random.nextFloat() * -800;
				float y = terrain.getHeightOfTerrain(x, z);
				if (y > -5) {
					entities.add(new Entity(bobble, 3, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, 1.9f));
				}
			}
			if (i % 2 == 0) {
				float x = random.nextFloat() * 800;
				float z = random.nextFloat() * -800;
				float y = terrain.getHeightOfTerrain(x, z);
				if (y > -5) {
					entities.add(new Entity(fern, 1, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, random.nextFloat() * 0.6f + 0.8f));
				}
			}
			if (i % 10 == 0) {
				float x = random.nextFloat() * 800;
				float z = random.nextFloat() * -800;
				float y = terrain.getHeightOfTerrain(x, z);
				if (y > -5) {
					entities.add(new Entity(cherryModel, 1, new Vector3f(x, y, z), 0,
							random.nextFloat() * 360, 0, random.nextFloat() * 2f + 5f));
				}
			}
		}
		
		float x = random.nextFloat() * 800;
		float z = random.nextFloat() * -800;
		float y = terrain.getHeightOfTerrain(x, z);
		entities.add(new Entity(lantern, new Vector3f(x, y, z), 0, 0, 0, 2));
		
		entities.add(new Entity(pAvatar, new Vector3f(360, 21, -360), 0, 0, 0, 0.9f));
		
		//*******************OTHER SETUP***************
		
		List<Light> lights = new ArrayList<Light>();
		
		// Must be the first light
		Light sun = new Light(new Vector3f(1000000, 1500000, -1000000), new Vector3f(1.3f, 1.3f, 1.3f));
		lights.add(sun);

		List<GuiTexture> guiTextures = new ArrayList<GuiTexture>();
		GuiRenderer guiRenderer = new GuiRenderer(loader);
		MousePicker picker = new MousePicker(camera, renderer.getProjectionMatrix(), terrain);
	
		//**********Water Renderer Set-up************************
		
		WaterFrameBuffers buffers = new WaterFrameBuffers();
		WaterShader waterShader = new WaterShader();
		WaterRenderer waterRenderer = new WaterRenderer(loader, waterShader, renderer.getProjectionMatrix(), buffers);
		List<WaterTile> waters = new ArrayList<WaterTile>();
		WaterTile water = new WaterTile(200, -200, -5);
		WaterTile water2 = new WaterTile(200, -600, -5);
		WaterTile water3 = new WaterTile(600, -200, -5);
		WaterTile water4 = new WaterTile(600, -600, -5);
		//waters.add(water);
		waters.add(water2);
		//waters.add(water3);
		waters.add(water4);
		
		Fbo multisampleFbo = new Fbo(Display.getWidth(), Display.getHeight());
		Fbo outputFbo = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		Fbo outputFbo2 = new Fbo(Display.getWidth(), Display.getHeight(), Fbo.DEPTH_TEXTURE);
		PostProcessing.init(loader);
		
		//****************Game Loop Below*********************
		
		int counter = 10; // 30 for FPS
		Boolean keyPressed = false;
		
		Sun sunparticles = new Sun(loader);
		
		CollisionMaster collisionMaster = new CollisionMaster(player.getPosition(), player.getVelocity(),
				new Vector3f(3, 9.1f, 3), entities, normalMapEntities, null); //animatedEntites solo tiene a player de momento
		CollisionPacket collisionPacket = null;
		
		while (!Display.isCloseRequested()) {
			
			MasterRenderer.update(lights.get(0));
			sunparticles.drawSun(camera.getPosition(), lights.get(0).getPosition());
			
			try {
				player.gravityForce(terrain, collisionPacket.getCollisionPosition());
			} catch (NullPointerException e) {
				player.gravityForce(terrain, null);
			}
			
			collisionMaster.playerUpdate(player.getPosition(), player.getVelocity());
			collisionPacket = collisionMaster.checkCollisions();
			collisionMaster.collisionReaction(player, collisionPacket);
			
			player.moveTo();
	        player.update();
	        
	        if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
	        	if (!keyPressed) player.increaseRunSpeed(10f);
	            keyPressed = true;
	        } else if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
	        	if (!keyPressed) player.increaseRunSpeed(-10f);
	        	keyPressed = true;
	        } else {
	        	keyPressed = false;
	        }
	        
	        if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
	        	MasterRenderer.fogDensity += 0.0005;
	        } else if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
	        	MasterRenderer.fogDensity = MasterRenderer.fogDensity > 0.0f ? MasterRenderer.fogDensity -= 0.0005 : 0.0f;
	        }
	        
	        if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
	        	player.jump();
	        }
	        
			camera.move();
			picker.update();
			
			Vector3f pointerLocation = picker.getCurrentTerrainPoint();

			/*float delta = DisplayManager.getFrameTimeSeconds();
			float fps = (float) (delta == 0.0 ? 0.0 : (1.0/delta));
	        DecimalFormat decimalFormat = new DecimalFormat("00");
	        String numberAsString = decimalFormat.format(fps);
			
	        if (counter == 30) {
				text.remove();
				text = new GUIText("FPS: " + numberAsString, 2f, font, new Vector2f(0.01f, 0.01f), 1f, false);
				text.setColour(1, 0, 0);
				counter = 1;
	        } else { counter++; }*/
	        
	        float timedisplay = MasterRenderer.time;
	        DecimalFormat decimalFormat = new DecimalFormat("00000");
	        String numberAsString = decimalFormat.format(timedisplay);
			
	        if (counter == 10) {
				text.remove();
				text = new GUIText("Time: " + numberAsString, 2f, font, new Vector2f(0.01f, 0.01f), 1f, false);
				text.setColour(1, 0, 0);
				counter = 1;
	        } else { counter++; }
	        
	        if(Mouse.isButtonDown(0)){
		        if (pointerLocation != null) {
		        	player.setObjectivePosition(picker.getCurrentTerrainPoint(), false);
		        	system.generateParticles(pointerLocation);
		        }
	        }
	        
	        // -----------------------------------------------------------------------------------
			
			ParticleMaster.update(camera);
			//renderer.renderShadowMap(entities, normalMapEntities, terrains, animatedEntities, sun);
			
			GL11.glEnable(GL30.GL_CLIP_DISTANCE0);
			
			//render reflection texture
			buffers.bindReflectionFrameBuffer();
			float distance = 2 * (camera.getPosition().y - water.getHeight());
			camera.getPosition().y -= distance;
			camera.invertPitch();
			renderer.renderScene(entities, normalMapEntities, animatedEntities, terrains, lights, camera, new Vector4f(0, 1, 0, -water.getHeight()+1));
			camera.getPosition().y += distance;
			camera.invertPitch();
			
			//render refraction texture
			buffers.bindRefractionFrameBuffer();
			renderer.renderScene(entities, normalMapEntities, animatedEntities, terrains, lights, camera, new Vector4f(0, -1, 0, water.getHeight()));
			
			//render to screen
			GL11.glDisable(GL30.GL_CLIP_DISTANCE0);
			buffers.unbindCurrentFrameBuffer();
			
			multisampleFbo.bindFrameBuffer();
			
			renderer.renderScene(entities, normalMapEntities, animatedEntities, terrains, lights, camera, new Vector4f(0, -1, 0, 100000));	
			waterRenderer.render(waters, camera, sun);
			ParticleMaster.renderParticles(camera);
			
			multisampleFbo.unbindFrameBuffer();
			multisampleFbo.resolveToScreen();
			
			//multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT0, outputFbo);
			//multisampleFbo.resolveToFbo(GL30.GL_COLOR_ATTACHMENT1, outputFbo2);
			//PostProcessing.doPostProcessing(outputFbo.getColourTexture(), outputFbo2.getColourTexture());
			
			guiRenderer.render(guiTextures);
			TextMaster.render();
			
			DisplayManager.updateDisplay();
		}

		//*********Clean Up Below**************
		
		PostProcessing.cleanUp();
		outputFbo.cleanUp();
		outputFbo2.cleanUp();
		multisampleFbo.cleanUp();

		ParticleMaster.cleanUp();
		TextMaster.cleanUp();
		buffers.cleanUp();
		waterShader.cleanUp();
		guiRenderer.cleanUp();
		renderer.cleanUp();
		loader.cleanUp();
		DisplayManager.closeDisplay();

	}


}
