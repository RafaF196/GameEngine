package collisions;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import entities.AnimatedEntity;
import entities.Entity;
import entities.Player;
import toolbox.Maths;

public class CollisionMaster {
	
	private CollisionPacket packet = new CollisionPacket(new Vector3f(0,0,0), 0.0f, 0);
	private CollisionDetector detector;
	
	private Vector3f playerPos;
	private Vector3f playerVel;
	private Vector3f playerSize;
	
	private List<Entity> entities = new ArrayList<Entity>();
	private List<Entity> normalMapEntities  = new ArrayList<Entity>();
	private List<AnimatedEntity> animatedEntities  = new ArrayList<AnimatedEntity>();
	
	public CollisionMaster(Vector3f playerPosition, Vector3f playerVelocity, Vector3f playerSize,
			List<Entity> entities, List<Entity> normalEntities, List<AnimatedEntity> animEntities) {
		this.playerPos = playerPosition;
		this.playerVel = playerVelocity;
		this.playerSize = playerSize;
		this.entities = entities;
		this.normalMapEntities = normalEntities;
		this.animatedEntities = animEntities;
	}
	
	public static int getMaxValue(int[] numbers){
		int maxValue = numbers[0];
		for(int i=1;i<numbers.length;i++){
			if(numbers[i] > maxValue){
				maxValue = numbers[i];
			}
		}
		return maxValue;
	}
	
	public CollisionPacket checkCollisions() {
		
		Matrix4f transformationMatrix;
		float[] pos;
		int[] ind;
		Vector3f P1, P2, P3;
		int i1, i2, i3;
		
		try {
			
			for (Entity entity : entities) {
				
				transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
						entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
				pos = entity.getModel().getRawModel().getVertices();
				ind = entity.getModel().getRawModel().getIndices();
				
				for (int i = 0; i < ind.length; i += 3) {
					
					i1 = ind[i];
					i2 = ind[i+1];
					i3 = ind[i+2];
					
					P1 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i1], pos[3*i1 +1], pos[3*i1 +2]));
					P2 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i2], pos[3*i2 +1], pos[3*i2 +2]));
					P3 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i3], pos[3*i3 +1], pos[3*i3 +2]));
					
					detector = new CollisionDetector(playerPos, playerVel, P1, P2, P3, playerSize.x, playerSize.y, playerSize.z);
					packet = detector.detectCollision();
					try {
						if (packet.getType() > 0){
							return packet;
						}
					} catch (NullPointerException e) { }
					
				}
				
			}
		
		} catch (NullPointerException e) { }
		
		try {
			
			for (Entity entity : normalMapEntities) {
				
				transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
						entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
				pos = entity.getModel().getRawModel().getVertices();
				ind = entity.getModel().getRawModel().getIndices();
				
				for (int i = 0; i < ind.length; i += 3) {
					
					i1 = ind[i];
					i2 = ind[i+1];
					i3 = ind[i+2];
					
					P1 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i1], pos[3*i1 +1], pos[3*i1 +2]));
					P2 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i2], pos[3*i2 +1], pos[3*i2 +2]));
					P3 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i3], pos[3*i3 +1], pos[3*i3 +2]));
					
					detector = new CollisionDetector(playerPos, playerVel, P1, P2, P3, playerSize.x, playerSize.y, playerSize.z);
					packet = detector.detectCollision();
					try {
						if (packet.getType() > 0){
							return packet;
						}
					} catch (NullPointerException e) { }
					
				}
				
			}
		
		} catch (NullPointerException e) { }
		
		try {
			
			for (AnimatedEntity entity : animatedEntities) {
				
				transformationMatrix = Maths.createTransformationMatrix(entity.getPosition(),
						entity.getRotX(), entity.getRotY(), entity.getRotZ(), entity.getScale());
				pos = entity.getModel().getModel().getVertices();
				ind = entity.getModel().getModel().getIndices();
				
				for (int i = 0; i < ind.length; i += 3) {
					
					i1 = ind[i];
					i2 = ind[i+1];
					i3 = ind[i+2];
					
					P1 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i1], pos[3*i1 +1], pos[3*i1 +2]));
					P2 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i2], pos[3*i2 +1], pos[3*i2 +2]));
					P3 = Maths.transform(transformationMatrix, new Vector3f(pos[3*i3], pos[3*i3 +1], pos[3*i3 +2]));
					
					detector = new CollisionDetector(playerPos, playerVel, P1, P2, P3, playerSize.x, playerSize.y, playerSize.z);
					packet = detector.detectCollision();
					try {
						if (packet.getType() > 0){
							return packet;
						}
					} catch (NullPointerException e) { }
					
				}
				
			}
		
		} catch (NullPointerException e) { }
		
		return null;
		
	}
	
	public void collisionReaction(Player player, CollisionPacket collisionPacket){
		
		try {
			if (collisionPacket.getType() > 0) {
				
				System.out.println("Collision");
				//Vector3f newObjective = new Vector3f(collisionPacket.getCollisionPosition().x, player.getPosition().y,
					//	collisionPacket.getCollisionPosition().z);
				//player.setObjectivePosition(newObjective);
				//player.setObjectivePosition(collisionPacket.getCollisionPosition());
				Vector3f dif = new Vector3f();
				Vector3f newobj = new Vector3f();
				dif = Vector3f.sub(player.getPosition(), collisionPacket.getCollisionPosition(), dif);
				dif = new Vector3f(dif.x/2.0f, dif.y/2.0f, dif.z/2.0f);
				newobj = Vector3f.add(player.getPosition(), dif, newobj);
				player.setObjectivePosition(newobj, true);
				player.setUpwardsSpeed(15f);
				
				//System.out.println(dif);
				
				/*
				 * 
				 * VELOCIDAD VERTICAL NO ESTA BIEN CALCULADA EN TODO MOMENTO?
				 * SOBRE TODO CUANDO SE QUEDA QUIETO Y POR ESO NO DETECTA COLISIONES CAYENDO
				 * 
				 */
			}
		} catch (NullPointerException e) { }
		
	}
	
	public void playerUpdate(Vector3f playerPosition, Vector3f playerVelocity) {
		this.playerPos = new Vector3f(playerPosition.x, playerPosition.y, playerPosition.z);
		this.playerVel = playerVelocity;
	}
	
}
