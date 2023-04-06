package shadows;

import java.util.List;
import java.util.Map;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;

import animatedModel.AnimatedModel;
import entities.AnimatedEntity;
import entities.Entity;
import models.RawModel;
import models.TexturedModel;
import renderEngine.MasterRenderer;
import shaders.TerrainShader;
import terrains.Terrain;
import textures.TerrainTexturePack;
import toolbox.Maths;

public class ShadowMapEntityRenderer {

	private Matrix4f projectionViewMatrix;
	private ShadowShader shader;
	private TerrainShadowShader terShader;
	private AnimatedShadowShader aniShader;

	protected ShadowMapEntityRenderer(ShadowShader shader, TerrainShadowShader terShader, AnimatedShadowShader aniShader, 
			Matrix4f projectionViewMatrix) {
		this.shader = shader;
		this.aniShader = aniShader;
		this.terShader = terShader;
		this.projectionViewMatrix = projectionViewMatrix;
	}

	protected void render(Map<TexturedModel, List<Entity>> entities, Map<TexturedModel, List<Entity>> normalEntities,
			List<Terrain> terrains, Map<AnimatedModel, List<AnimatedEntity>> animatedEntites) {
		
		shader.start();
		
		for (TexturedModel model : entities.keySet()) {
			RawModel rawModel = model.getRawModel();
			bindModel(rawModel);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.disableCulling();
			}
			for (Entity entity : entities.get(model)) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.enableCulling();
			}
			unBindModel();
		}
		
		for (TexturedModel model : normalEntities.keySet()) {
			RawModel rawModel = model.getRawModel();
			bindModel(rawModel);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.disableCulling();
			}
			for (Entity entity : normalEntities.get(model)) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.enableCulling();
			}
			unBindModel();
		}
		
		shader.stop();
		
		aniShader.start();
		
		for (AnimatedModel model : animatedEntites.keySet()) {
			RawModel rawModel = model.getModel();
			bindAniModel(rawModel);
			GL13.glActiveTexture(GL13.GL_TEXTURE0);
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, model.getTexture().getID());
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.disableCulling();
			}
			for (AnimatedEntity entity : animatedEntites.get(model)) {
				prepareInstance(entity);
				GL11.glDrawElements(GL11.GL_TRIANGLES, rawModel.getVertexCount(), GL11.GL_UNSIGNED_INT, 0);
			}
			if (model.getTexture().isHasTransparency()){
				MasterRenderer.enableCulling();
			}
			unBindAniModel();
		}
		
		aniShader.stop();
		
		terShader.start();
		
		for (Terrain terrain : terrains) {
			RawModel rawModel = terrain.getModel();
			bindModel(rawModel);
			loadModelMatrix(terrain);
			GL11.glDrawElements(GL11.GL_TRIANGLES, terrain.getModel().getVertexCount(),
					GL11.GL_UNSIGNED_INT, 0);
			unBindModel();
		}
		
		terShader.stop();
		
	}

	private void bindModel(RawModel rawModel) {
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
	}
	
	private void unBindModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
	}
	
	private void bindAniModel(RawModel rawModel) {
		GL30.glBindVertexArray(rawModel.getVaoID());
		GL20.glEnableVertexAttribArray(0);
		GL20.glEnableVertexAttribArray(1);
		GL20.glEnableVertexAttribArray(3);
		GL20.glEnableVertexAttribArray(4);
	}
	
	private void unBindAniModel() {
		GL20.glDisableVertexAttribArray(0);
		GL20.glDisableVertexAttribArray(1);
		GL20.glDisableVertexAttribArray(3);
		GL20.glDisableVertexAttribArray(4);
		GL30.glBindVertexArray(0);
	}

	private void prepareInstance(Entity entity) {
		Matrix4f modelMatrix = Maths.createTransformationMatrix(entity.getPosition(),
				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		shader.loadMvpMatrix(mvpMatrix);
	}
	
	private void prepareInstance(AnimatedEntity entity) {
		Matrix4f modelMatrix = Maths.createTransformationMatrix(entity.getPosition(),
				entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, modelMatrix, null);
		aniShader.loadMvpMatrix(mvpMatrix);
		aniShader.loadJointTransforms(entity.getJointTransforms());
	}
	
	private void loadModelMatrix(Terrain terrain) {
		Matrix4f transformationMatrix = Maths.createTransformationMatrix(
				new Vector3f(terrain.getX(), 0, terrain.getZ()), 0, 0, 0, 1);
		Matrix4f mvpMatrix = Matrix4f.mul(projectionViewMatrix, transformationMatrix, null);
		terShader.loadMvpMatrix(mvpMatrix);
	}

}
