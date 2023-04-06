package shadows;

import org.lwjgl.util.vector.Matrix4f;

import shaders.ShaderProgram;

public class AnimatedShadowShader extends ShaderProgram {
	
	private static final String VERTEX_FILE = "/shadows/animatedShadowVertexShader.txt";
	private static final String FRAGMENT_FILE = "/shadows/animatedShadowFragmentShader.txt";
	
	private static final int MAX_JOINTS = 50; // max number of joints in a skeleton
	
	private int location_mvpMatrix;
	private int location_jointTransforms[];

	protected AnimatedShadowShader() {
		super(VERTEX_FILE, FRAGMENT_FILE);
	}

	@Override
	protected void getAllUniformLocations() {
		location_mvpMatrix = super.getUniformLocation("mvpMatrix");
		location_jointTransforms = new int[MAX_JOINTS];
		for(int i=0;i<MAX_JOINTS;i++){
			location_jointTransforms[i] = super.getUniformLocation("jointTransforms[" + i + "]");
		}
	}
	
	protected void loadMvpMatrix(Matrix4f mvpMatrix){
		super.loadMatrix(location_mvpMatrix, mvpMatrix);
	}
	
	public void loadJointTransforms(Matrix4f[] jointTrans){
		for (int i=0;i<MAX_JOINTS;i++) {
			if (i < jointTrans.length) {
				super.loadMatrix(location_jointTransforms[i], jointTrans[i]);
			}
		}
	}

	@Override
	protected void bindAttributes() {
		super.bindAttribute(0, "in_position");
		super.bindAttribute(1, "in_textureCoords");
		super.bindAttribute(3, "in_jointIndices");
		super.bindAttribute(4, "in_weights");
	}

}