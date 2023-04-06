package animatedModel;

import org.lwjgl.util.vector.Matrix4f;

import animation.Animation;
import animation.Animator;
import models.RawModel;
import textures.ModelTexture;


public class AnimatedModel {

	private final RawModel model;
	private final ModelTexture texture;

	private final Joint rootJoint;
	private final int jointCount;

	private final Animator animator;

	public AnimatedModel(RawModel model, ModelTexture texture, Joint rootJoint, int jointCount) {
		this.model = model;
		this.texture = texture;
		this.rootJoint = rootJoint;
		this.jointCount = jointCount;
		this.animator = new Animator(this);
		rootJoint.calcInverseBindTransform(new Matrix4f());
	}

	public RawModel getModel() {
		return model;
	}

	public ModelTexture getTexture() {
		return texture;
	}

	public Joint getRootJoint() {
		return rootJoint;
	}

	public void doAnimation(Animation animation, float startTime, float speed) {
		animator.doAnimation(animation, startTime, speed);
	}

	public void update(Boolean cA, float f) {
		animator.update(cA, f);
	}

	public Matrix4f[] getJointTransforms() {
		Matrix4f[] jointMatrices = new Matrix4f[jointCount];
		addJointsToArray(rootJoint, jointMatrices);
		return jointMatrices;
	}

	private void addJointsToArray(Joint headJoint, Matrix4f[] jointMatrices) {
		jointMatrices[headJoint.index] = headJoint.getAnimatedTransform();
		for (Joint childJoint : headJoint.children) {
			addJointsToArray(childJoint, jointMatrices);
		}
	}

}
