package water;

import java.util.List;

import models.RawModel;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import renderEngine.DisplayManager;
import renderEngine.Loader;
import renderEngine.MasterRenderer;
import toolbox.Maths;
import entities.Camera;
import entities.Light;

public class WaterRenderer {
	
	private static final String DUDV_MAP = "waterDUDV";
	private static final String NORMAL_MAP = "normal";
	private static final float WAVE_SPEED = 0.03f;

	private RawModel quad;
	private WaterShader shader;
	private WaterFrameBuffers fbos;
	
	private float moveFactor = 0;
	
	private int dudvTexture;
	private int normalMap;

	public WaterRenderer(Loader loader, WaterShader shader, Matrix4f projectionMatrix, WaterFrameBuffers fbos) {
		this.shader = shader;
		this.fbos = fbos;
		dudvTexture = loader.loadTexture(DUDV_MAP);
		normalMap = loader.loadTexture(NORMAL_MAP);
		shader.start();
		shader.connectTextureUnits();
		shader.loadProjectionMatrix(projectionMatrix);
		shader.stop();
		setUpVAO(loader);
	}

	public void render(List<WaterTile> water, Camera camera, Light sun) {
		prepareRender(camera, sun);	
		for (WaterTile tile : water) {
			Matrix4f modelMatrix = Maths.createTransformationMatrix(
					new Vector3f(tile.getX(), tile.getHeight(), tile.getZ()), 0, 0, 0, WaterTile.TILE_SIZE);
			shader.loadModelMatrix(modelMatrix);
			GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, quad.getVertexCount());
		}
		unbind();
	}
	
	private void prepareRender(Camera camera, Light sun){
		shader.start();
		shader.loadViewMatrix(camera);
		moveFactor += WAVE_SPEED * DisplayManager.getFrameTimeSeconds();
		moveFactor %= 1;
		shader.loadMoveFactor(moveFactor);
		shader.loadLight(sun);
		shader.loadSkyColour(MasterRenderer.RED, MasterRenderer.GREEN, MasterRenderer.BLUE);
		shader.loadFogProperties(MasterRenderer.fogDensity, MasterRenderer.fogGradient);
		GL30.glBindVertexArray(quad.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL13.glActiveTexture(GL13.GL_TEXTURE0);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getReflectionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE1);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionTexture());
		GL13.glActiveTexture(GL13.GL_TEXTURE2);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, dudvTexture);
		GL13.glActiveTexture(GL13.GL_TEXTURE3);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, normalMap);
		GL13.glActiveTexture(GL13.GL_TEXTURE4);
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, fbos.getRefractionDepthTexture());
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
	}
	
	private void unbind(){
		GL20.glDisableVertexAttribArray(0);
		GL30.glBindVertexArray(0);
		GL11.glDisable(GL11.GL_BLEND);
		shader.stop();
	}

	private void setUpVAO(Loader loader) {
		// Just x and z vertex positions here, y is set to 0 in v.shader
		
		int grid = 50;
		int triangles = grid*grid*2;
		float[] vertices = new float[triangles * 3 * 2];
		
		int vertexPointer = 0;
		for (int i = 0; i < grid; i++) {
			for (int j = 0; j < grid; j++) {
				vertices[12 * vertexPointer + 0] = (((float) i / grid)*2)-1;
				vertices[12 * vertexPointer + 1] = (((float) j / grid)*2)-1;
				vertices[12 * vertexPointer + 2] = (((float) i / grid)*2)-1;
				vertices[12 * vertexPointer + 3] = (((float) (j+1) / grid)*2)-1;
				vertices[12 * vertexPointer + 4] = (((float) (i+1) / grid)*2)-1;
				vertices[12 * vertexPointer + 5] = (((float) j / grid)*2)-1;
				vertices[12 * vertexPointer + 6] = (((float) (i+1) / grid)*2)-1;
				vertices[12 * vertexPointer + 7] = (((float) j / grid)*2)-1;
				vertices[12 * vertexPointer + 8] = (((float) i / grid)*2)-1;
				vertices[12 * vertexPointer + 9] = (((float) (j+1) / grid)*2)-1;
				vertices[12 * vertexPointer +10] = (((float) (i+1) / grid)*2)-1;
				vertices[12 * vertexPointer +11] = (((float) (j+1) / grid)*2)-1;
				vertexPointer++;
			}
		}
		
		quad = loader.loadToVAO(vertices, 2);
	}

}
