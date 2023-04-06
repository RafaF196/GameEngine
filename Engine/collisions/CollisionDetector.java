package collisions;

import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

public class CollisionDetector {
	
	private Vector3f ellipsoidPositionIR;
	private Vector3f ellipsoidPositionIE;
	
	private Vector3f triangleP1R;
	private Vector3f triangleP2R;
	private Vector3f triangleP3R;

	private Vector3f triangleP1E;
	private Vector3f triangleP2E;
	private Vector3f triangleP3E;
	
	private Vector3f P2P1E; // vector p2->p1
	private Vector3f P2P3E; // vector p2->p3
	
	private Vector3f planeIntersectionPointR;
	private Vector3f planeIntersectionPointE;
	
	private Vector3f planeNormal;
	private Vector3f planeUnitNormal;
	
	private float planeConstantD;
	private float planeConstantP;
	
	private float distance;
	
	private float t0;
	private float t1;
	
	private Vector3f velocityR;
	private Vector3f velocityE;
	
	private Matrix3f toESpaceMatrix = new Matrix3f();
	private Matrix3f fromESpaceMatrix = new Matrix3f();
	
	private CollisionPacket collisionPacketVertex;
	private CollisionPacket collisionPacketEdge;
	private CollisionPacket collisionPacketSurface;
	
	public CollisionDetector(Vector3f position, Vector3f velocity, Vector3f P1, Vector3f P2, Vector3f P3,
			float ellipsoidMaxX, float ellipsoidMaxY, float ellipsoidMaxZ) {
		
		this.ellipsoidPositionIR = position;
		this.velocityR = velocity;
		this.triangleP1R = P1;
		this.triangleP2R = P2;
		this.triangleP3R = P3;
		
		this.toESpaceMatrix.m00 = (1/ellipsoidMaxX);
		this.toESpaceMatrix.m11 = (1/ellipsoidMaxY);
		this.toESpaceMatrix.m22 = (1/ellipsoidMaxZ);
		
		this.fromESpaceMatrix = Matrix3f.invert(toESpaceMatrix, fromESpaceMatrix);
		
	}
	
	public CollisionDetector(Vector3f position, Vector3f velocity, Vector4f P1, Vector4f P2, Vector4f P3,
			float ellipsoidMaxX, float ellipsoidMaxY, float ellipsoidMaxZ) {
		
		this.ellipsoidPositionIR = position;
		this.velocityR = velocity;
		this.triangleP1R = new Vector3f(P1.x, P1.y, P1.z);
		this.triangleP2R = new Vector3f(P2.x, P2.y, P2.z);
		this.triangleP3R = new Vector3f(P3.x, P3.y, P3.z);
		
		this.toESpaceMatrix.m00 = (1/ellipsoidMaxX);
		this.toESpaceMatrix.m11 = (1/ellipsoidMaxY);
		this.toESpaceMatrix.m22 = (1/ellipsoidMaxZ);
		
	}
	
	public void sendValuesToESpace() {
		triangleP1E = Matrix3f.transform(toESpaceMatrix, triangleP1R, triangleP1E);
		triangleP2E = Matrix3f.transform(toESpaceMatrix, triangleP2R, triangleP2E);
		triangleP3E = Matrix3f.transform(toESpaceMatrix, triangleP3R, triangleP3E);
		velocityE = Matrix3f.transform(toESpaceMatrix, velocityR, velocityE);
		ellipsoidPositionIE = Matrix3f.transform(toESpaceMatrix, ellipsoidPositionIR, ellipsoidPositionIE);
	}
	
	public void sendValuesToPSpace() {
		planeIntersectionPointR = Matrix3f.transform(fromESpaceMatrix, planeIntersectionPointE, planeIntersectionPointR);
	}
	
	public Boolean checkDistance() {
		if (Math.abs(this.ellipsoidPositionIR.x - this.triangleP1R.x) < 100) return true;
		if (Math.abs(this.ellipsoidPositionIR.x - this.triangleP2R.x) < 100) return true;
		if (Math.abs(this.ellipsoidPositionIR.x - this.triangleP3R.x) < 100) return true;
		if (Math.abs(this.ellipsoidPositionIR.z - this.triangleP1R.z) < 100) return true;
		if (Math.abs(this.ellipsoidPositionIR.z - this.triangleP2R.z) < 100) return true;
		if (Math.abs(this.ellipsoidPositionIR.z - this.triangleP3R.z) < 100) return true;
		return false;
	}
	
	public void constructCollisionValues() {
		
		P2P1E = Vector3f.sub(triangleP1E, triangleP2E, P2P1E);
		P2P3E = Vector3f.sub(triangleP3E, triangleP2E, P2P3E);
		
		planeNormal = Vector3f.cross(P2P3E, P2P1E, planeNormal);
		planeUnitNormal = planeNormal.normalise(planeUnitNormal);
	
		planeConstantD = (planeNormal.x*triangleP2E.x*-1) + (planeNormal.y*triangleP2E.y*-1) + (planeNormal.z*triangleP2E.z*-1);
		planeConstantP = (float) (planeConstantD/(Math.sqrt((planeNormal.x*planeNormal.x) + (planeNormal.y*planeNormal.y) + (planeNormal.z*planeNormal.z))));
		
		distance = Vector3f.dot(planeUnitNormal, ellipsoidPositionIE) + planeConstantP;
		
		if (Vector3f.dot(planeUnitNormal, velocityE) == 0.0){
			
			t0 = 0;
			t1 = 1; 
			
		} else {
		
			try {
				t0 = (1-distance)/(Vector3f.dot(planeUnitNormal, velocityE));
			} catch (IllegalArgumentException e) {
				t0 = 0; t1 = 1;
			}
			try {
				t1 = (-1-distance)/(Vector3f.dot(planeUnitNormal, velocityE));
			} catch (IllegalArgumentException e) {
				t0 = 0; t1 = 1;
			}
			
		}
		
		if (t1 < t0) {
			float temp = t1;
			t1 = t0;
			t0 = temp;
		}
		
	}
	
	public CollisionPacket detectCollision(){
		
		if (!checkDistance()) return null;
		
		sendValuesToESpace();
		constructCollisionValues();
		
		//System.out.println("V: " + this.velocityR);
		
		if (Vector3f.dot(planeUnitNormal, velocityE) == 0) {
			if (Math.abs(distance) > 1){
				return null;
			}
		}
		
		else if (((t0 < 0) || (t0 > 1)) && ((t1 < 0) || (t1 > 1))) {
			return null;
		}
		
		planeIntersectionPointE = Vector3f.sub(ellipsoidPositionIE, (Vector3f) planeUnitNormal, planeIntersectionPointE);
		Vector3f velocityExt0 = new Vector3f(velocityE.x*t0, velocityE.y*t0, velocityE.z*t0);
		planeIntersectionPointE = Vector3f.add(planeIntersectionPointE, velocityExt0, planeIntersectionPointE);
		
		if (checkPositionWithTriangle(planeIntersectionPointE, triangleP1E, triangleP2E, triangleP3E)) {
			System.out.println(t0 + " " + t1);
			sendValuesToPSpace();
			collisionPacketSurface = new CollisionPacket(planeIntersectionPointR, distance, 1);
			return collisionPacketSurface;
		} 
		
		else {
			/*
			Vector3f ellipsoidVertexDistance = new Vector3f();
			Vector3f vertexEllipsoidDistance = new Vector3f();
			
			float vertexTime1, vertexTime2;
			float smallestSolutionVertex = 10005;
			
			Vector3f collisionPoint = new Vector3f();
			
			float a = (float) velocityE.lengthSquared();
			
			//triangleP1E
			
			ellipsoidVertexDistance = Vector3f.sub(ellipsoidPositionIE, triangleP1E, ellipsoidVertexDistance);
			
			float b = 2 * Vector3f.dot(velocityE, ellipsoidVertexDistance);
			
			vertexEllipsoidDistance = Vector3f.sub(triangleP1E, ellipsoidPositionIE, vertexEllipsoidDistance);
			
			float c = vertexEllipsoidDistance.lengthSquared() -1;
			
			if (((b*b) - (4*a*c)) >= 0){
				vertexTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				vertexTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if ((vertexTime1 < vertexTime2) && (vertexTime1 <= t1) && (vertexTime1 >= t0) &&
						((vertexTime1 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime1;
					collisionPoint = triangleP1E;
				} else if ((vertexTime2 < vertexTime1) && (vertexTime2 <= t1) && (vertexTime2 >= t0) &&
						((vertexTime2 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime2;
					collisionPoint = triangleP1E;
				}
			}
			
			//triangleP2E
			
			ellipsoidVertexDistance = Vector3f.sub(ellipsoidPositionIE, triangleP2E, ellipsoidVertexDistance);
			
			b = 2 * Vector3f.dot(velocityE, ellipsoidVertexDistance);
			
			vertexEllipsoidDistance = Vector3f.sub(triangleP2E, ellipsoidPositionIE, vertexEllipsoidDistance);
			
			c = vertexEllipsoidDistance.lengthSquared() -1;
			
			if (((b*b) - (4*a*c)) >= 0) {
				vertexTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				vertexTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if ((vertexTime1 < vertexTime2) && (vertexTime1 <= t1) && (vertexTime1 >= t0) &&
						((vertexTime1 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime1;
					collisionPoint = triangleP2E;
				} else if ((vertexTime2 < vertexTime1) && (vertexTime2 <= t1) && (vertexTime2 >= t0) &&
						((vertexTime2 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime2;
					collisionPoint = triangleP2E;
				}
			}
			
			//triangleP3E
			
			ellipsoidVertexDistance = Vector3f.sub(ellipsoidPositionIE, triangleP3E, ellipsoidVertexDistance);
			
			b = 2 * Vector3f.dot(velocityE, ellipsoidVertexDistance);
			
			vertexEllipsoidDistance = Vector3f.sub(triangleP3E, ellipsoidPositionIE, vertexEllipsoidDistance);
			
			c = vertexEllipsoidDistance.lengthSquared() -1;
			
			if (((b*b) - (4*a*c)) >= 0) {
				vertexTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				vertexTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if ((vertexTime1 < vertexTime2) && (vertexTime1 <= t1) && (vertexTime1 >= t0) &&
						((vertexTime1 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime1;
					collisionPoint = triangleP3E;
				} else if ((vertexTime2 < vertexTime1) && (vertexTime2 <= t1) && (vertexTime2 >= t0) &&
						((vertexTime2 < smallestSolutionVertex) || (smallestSolutionVertex == 10005))) {
					smallestSolutionVertex = vertexTime2;
					collisionPoint = triangleP3E;
				}
			}
			
			if (smallestSolutionVertex != 10005) {
				float vertexDistance = smallestSolutionVertex * velocityE.length();
				System.out.println(t0 + " " + t1 + " " + distance);
				collisionPacketVertex = new CollisionPacket(collisionPoint, vertexDistance, 3);
			}
			*/
			
			Vector3f edge = new Vector3f();
			Vector3f baseToVertex = new Vector3f();
			Vector3f edgeIntersectionPoint = new Vector3f();
			
			float intersectionDistance;
			Vector3f fromEdgePoint = new Vector3f();
			Vector3f smallestEdge = new Vector3f();
			
			float edgeTime1, edgeTime2;
			float smallestSolutionEdge = 10005;
			float smallerSolutionEdge;
			float smallestF = 0;
			
			//edge = triangleP2E->triangleP1E
			
			float smallerF = -1;
			edge = Vector3f.sub(triangleP2E, triangleP1E, edge);
			baseToVertex = Vector3f.sub(triangleP1E, ellipsoidPositionIE, baseToVertex);
			
			float a = edge.lengthSquared() * -1 * velocityE.lengthSquared() + (Vector3f.dot(edge, velocityE) * Vector3f.dot(edge, velocityE));
			float b = edge.lengthSquared() * 2 * Vector3f.dot(velocityE, baseToVertex) - (2* Vector3f.dot(edge,velocityE) * Vector3f.dot(edge, baseToVertex));
			float c = edge.lengthSquared() * (1 - baseToVertex.lengthSquared()) + (Vector3f.dot(edge, baseToVertex) * Vector3f.dot(edge, baseToVertex));
			
			if (((b*b) - (4*a*c)) >= 0) {
				edgeTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				edgeTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if (edgeTime1 <= edgeTime2) {
					smallerSolutionEdge = edgeTime1;
				} else {
					smallerSolutionEdge = edgeTime2;
				}
				
				if (smallerSolutionEdge >= 0 && smallerSolutionEdge <= 1) {
					smallerF = ((Vector3f.dot(edge, velocityE) * smallerSolutionEdge) - Vector3f.dot(edge, baseToVertex)) / edge.lengthSquared();
				}
				
				if ((smallerF >= 0 && smallerF <= 1) && (smallerSolutionEdge < smallestSolutionEdge)) {
					smallestF = smallerF;
					smallestSolutionEdge = smallerSolutionEdge;
					fromEdgePoint = triangleP1E;
					smallestEdge = edge;
				}
				
			}
			
			//edge = triangleP3E->triangleP2E
			
			smallerF = -1;
			edge = Vector3f.sub(triangleP3E, triangleP2E, edge);
			baseToVertex = Vector3f.sub(triangleP2E, ellipsoidPositionIE, baseToVertex);
			
			a = edge.lengthSquared() * -1 * velocityE.lengthSquared() + (Vector3f.dot(edge, velocityE) * Vector3f.dot(edge, velocityE));
			b = edge.lengthSquared() * 2 * Vector3f.dot(velocityE, baseToVertex) - (2* Vector3f.dot(edge,velocityE) * Vector3f.dot(edge, baseToVertex));
			c = edge.lengthSquared() * (1 - baseToVertex.lengthSquared()) + (Vector3f.dot(edge, baseToVertex) * Vector3f.dot(edge, baseToVertex));
			
			if (((b*b) - (4*a*c)) >= 0) {
				edgeTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				edgeTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if (edgeTime1 <= edgeTime2) {
					smallerSolutionEdge = edgeTime1;
				} else {
					smallerSolutionEdge = edgeTime2;
				}
				
				if (smallerSolutionEdge >= 0 && smallerSolutionEdge <= 1) {
					smallerF = ((Vector3f.dot(edge, velocityE) * smallerSolutionEdge) - Vector3f.dot(edge, baseToVertex)) / edge.lengthSquared();
				}
				
				if ((smallerF >= 0 && smallerF <= 1) && (smallerSolutionEdge < smallestSolutionEdge)) {
					smallestF = smallerF;
					smallestSolutionEdge = smallerSolutionEdge;
					fromEdgePoint = triangleP2E;
					smallestEdge = edge;
				}
				
			}
			
			//edge = triangleP1E->triangleP3E
			
			smallerF = -1;
			edge = Vector3f.sub(triangleP1E, triangleP3E, edge);
			baseToVertex = Vector3f.sub(triangleP3E, ellipsoidPositionIE, baseToVertex);
			
			a = edge.lengthSquared() * -1 * velocityE.lengthSquared() + (Vector3f.dot(edge, velocityE) * Vector3f.dot(edge, velocityE));
			b = edge.lengthSquared() * 2 * Vector3f.dot(velocityE, baseToVertex) - (2* Vector3f.dot(edge,velocityE) * Vector3f.dot(edge, baseToVertex));
			c = edge.lengthSquared() * (1 - baseToVertex.lengthSquared()) + (Vector3f.dot(edge, baseToVertex) * Vector3f.dot(edge, baseToVertex));
			
			if (((b*b) - (4*a*c)) >= 0) {
				edgeTime1 = (-b + (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				edgeTime2 = (-b - (float) Math.sqrt((double) ((b*b)-(4*a*c))))/(2*a);
				
				if (edgeTime1 <= edgeTime2) {
					smallerSolutionEdge = edgeTime1;
				} else {
					smallerSolutionEdge = edgeTime2;
				}
				
				if (smallerSolutionEdge >= 0 && smallerSolutionEdge <= 1) {
					smallerF = ((Vector3f.dot(edge, velocityE) * smallerSolutionEdge) - Vector3f.dot(edge, baseToVertex)) / edge.lengthSquared();
				}
				
				if ((smallerF >= 0 && smallerF <= 1) && (smallerSolutionEdge < smallestSolutionEdge)) {
					smallestF = smallerF;
					smallestSolutionEdge = smallerSolutionEdge;
					fromEdgePoint = triangleP3E;
					smallestEdge = edge;
				}
				
			}
			
			if (smallestSolutionEdge != 10005) {
				edgeIntersectionPoint = Vector3f.add(fromEdgePoint, (Vector3f) edge.scale(smallestF), edgeIntersectionPoint);
				intersectionDistance = smallestSolutionEdge * velocityE.length();
				edgeIntersectionPoint = Matrix3f.transform(fromESpaceMatrix, edgeIntersectionPoint, edgeIntersectionPoint);
				collisionPacketEdge = new CollisionPacket(edgeIntersectionPoint, intersectionDistance, 2);
			}

		} // end of the edge/vertex portion of this method
		
		if ((collisionPacketEdge != null) || (collisionPacketVertex != null)) {
			
			try {
				if (collisionPacketEdge.getCollisionDistance() < collisionPacketVertex.getCollisionDistance()) {
					return collisionPacketEdge;
				}
			} catch (NullPointerException e) {}
			
			try {
				if (collisionPacketEdge.getCollisionDistance() > collisionPacketVertex.getCollisionDistance()) {
					return collisionPacketVertex;
				}
			} catch (NullPointerException e) {}
			
			if (collisionPacketEdge != null) {
				try {
					return collisionPacketEdge;
				} catch (NullPointerException e) {}
			}
			
			if (collisionPacketVertex != null) {
				try {
					return collisionPacketVertex;
				} catch (NullPointerException e) {}
			}
			
		} 
		
		return null;
		
	}
	
	public boolean checkPositionWithTriangle(Vector3f position, Vector3f P1, Vector3f P2, Vector3f P3){
		
		float angles = 0;
		
		Vector3f v1 = new Vector3f();
		Vector3f.sub(position, P1, v1);
		Vector3f v2 = new Vector3f();
		Vector3f.sub(position, P2, v2);
		Vector3f v3 = new Vector3f();
		Vector3f.sub(position, P3, v3);
		v1.normalise();
		v2.normalise();
		v3.normalise();
		
		angles += Math.acos(Vector3f.dot(v1, v2));
		angles += Math.acos(Vector3f.dot(v1, v3));
		angles += Math.acos(Vector3f.dot(v2, v3));
		
		return (Math.abs(angles - 2*Math.PI) <= 0.05);
		
	}
	
}
