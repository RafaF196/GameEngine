package loaders;

import animatedModel.AnimatedModel;
import animatedModel.Joint;
import colladaLoader.ColladaLoader;
import dataStructures.AnimatedModelData;
import dataStructures.JointData;
import dataStructures.MeshData;
import dataStructures.SkeletonData;
import models.RawModel;
import renderEngine.Loader;
import textures.ModelTexture;
import textures.TextureData;
import xmlParser.MyFile;

public class AnimatedModelLoader {
	
	public static final int MAX_WEIGHTS = 3;

	public static AnimatedModel loadEntity(MyFile modelFile, MyFile textureFile, Loader loader) {
		AnimatedModelData entityData = ColladaLoader.loadColladaModel(modelFile, MAX_WEIGHTS);
		MeshData mesh = entityData.getMeshData();
		RawModel model = loader.loadToVAO(mesh.getVertices(), mesh.getTextureCoords(), mesh.getNormals(),
				mesh.getJointIds(), mesh.getVertexWeights(), mesh.getIndices());
		
		int textureId = loader.loadTexture(textureFile.getName(), true);
		ModelTexture texture = new ModelTexture(textureId);
		
		SkeletonData skeletonData = entityData.getJointsData();
		Joint headJoint = createJoints(skeletonData.headJoint);
		return new AnimatedModel(model, texture, headJoint, skeletonData.jointCount);
	}

	private static Joint createJoints(JointData data) {
		Joint joint = new Joint(data.index, data.nameId, data.bindLocalTransform);
		for (JointData child : data.children) {
			joint.addChild(createJoints(child));
		}
		return joint;
	}

}
