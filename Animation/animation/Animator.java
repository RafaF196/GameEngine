package animation;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.util.vector.Matrix4f;

import animatedModel.AnimatedModel;
import animatedModel.Joint;
import renderEngine.DisplayManager;


public class Animator {

	private final AnimatedModel entity;

	private Animation currentAnimation;
	private float animationStartTime = 0;
	private float animationTime = 0;
	private float animationSpeed = 1;

	public Animator(AnimatedModel entity) {
		this.entity = entity;
	}

	public void doAnimation(Animation animation, float startTime, float speed) {
		this.animationStartTime = startTime;
		this.currentAnimation = animation;
		this.animationSpeed = speed;
	}

	public void update(Boolean continueAnimation, float f) {
		this.animationSpeed = f;
		if (currentAnimation == null) {
			return;
		}
		if (continueAnimation) {
			increaseAnimationTime();
		} else {
			animationTime = animationStartTime;
		}
		Map<String, Matrix4f> currentPose = calculateCurrentAnimationPose();
		applyPoseToJoints(currentPose, entity.getRootJoint(), new Matrix4f());
	}

	private void increaseAnimationTime() {
		animationTime += DisplayManager.getFrameTimeSeconds() * this.animationSpeed;
		if (animationTime > currentAnimation.getLength()) {
			this.animationTime %= currentAnimation.getLength();
		}
	}

	private Map<String, Matrix4f> calculateCurrentAnimationPose() {
		KeyFrame[] frames = getPreviousAndNextFrames();
		float progression = calculateProgression(frames[0], frames[1]);
		return interpolatePoses(frames[0], frames[1], progression);
	}

	private void applyPoseToJoints(Map<String, Matrix4f> currentPose, Joint joint, Matrix4f parentTransform) {
		Matrix4f currentLocalTransform = currentPose.get(joint.name);
		Matrix4f currentTransform = Matrix4f.mul(parentTransform, currentLocalTransform, null);
		for (Joint childJoint : joint.children) {
			applyPoseToJoints(currentPose, childJoint, currentTransform);
		}
		Matrix4f.mul(currentTransform, joint.getInverseBindTransform(), currentTransform);
		joint.setAnimationTransform(currentTransform);
	}

	private KeyFrame[] getPreviousAndNextFrames() {
		KeyFrame[] allFrames = currentAnimation.getKeyFrames();
		KeyFrame previousFrame = allFrames[0];
		KeyFrame nextFrame = allFrames[0];
		for (int i = 1; i < allFrames.length; i++) {
			nextFrame = allFrames[i];
			if (nextFrame.getTimeStamp() > animationTime) {
				break;
			}
			previousFrame = allFrames[i];
		}
		return new KeyFrame[] { previousFrame, nextFrame };
	}

	private float calculateProgression(KeyFrame previousFrame, KeyFrame nextFrame) {
		float totalTime = nextFrame.getTimeStamp() - previousFrame.getTimeStamp();
		float currentTime = animationTime - previousFrame.getTimeStamp();
		return currentTime / totalTime;
	}

	private Map<String, Matrix4f> interpolatePoses(KeyFrame previousFrame, KeyFrame nextFrame, float progression) {
		Map<String, Matrix4f> currentPose = new HashMap<String, Matrix4f>();
		for (String jointName : previousFrame.getJointKeyFrames().keySet()) {
			JointTransform previousTransform = previousFrame.getJointKeyFrames().get(jointName);
			JointTransform nextTransform = nextFrame.getJointKeyFrames().get(jointName);
			JointTransform currentTransform = JointTransform.interpolate(previousTransform, nextTransform, progression);
			currentPose.put(jointName, currentTransform.getLocalTransform());
		}
		return currentPose;
	}

	public float getAnimationSpeed() {
		return animationSpeed;
	}

	public void setAnimationSpeed(float animationSpeed) {
		this.animationSpeed = animationSpeed;
	}

}
