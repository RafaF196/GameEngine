package models;

public class RawModel {
	
	private int vaoID;
	private int vertexCount;
	
	private float[] vertices;
	private int[] indices;
	
	public RawModel(int vaoID, int vertexCount, float[] vertices, int[] indices){
		this.vaoID = vaoID;
		this.vertexCount = vertexCount;
		this.vertices = vertices;
		this.indices = indices;
	}

	public int getVaoID() {
		return vaoID;
	}

	public int getVertexCount() {
		return vertexCount;
	}

	public float[] getVertices() {
		return vertices;
	}

	public int[] getIndices() {
		return indices;
	}

}
