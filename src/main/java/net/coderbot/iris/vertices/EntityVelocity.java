package net.coderbot.iris.vertices;

public class EntityVelocity {
	private float posX, posY, posZ;
	private float velX, velY, velZ;

	public static EntityVelocity[][] createMatrix() {
		EntityVelocity[][] matrix = new EntityVelocity[6][4];
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < 4; j++) {
				matrix[i][j] = new EntityVelocity();
			}
		}
		return matrix;
	}

	public EntityVelocity() {

	}

	public void setPos(float x, float y, float z) {
		this.velX = x - posX;
		this.velY = y - posY;
		this.velZ = z - posZ;

		this.posX = x;
		this.posY = y;
		this.posZ = z;
	}

	public float getVelX() {
		return velX;
	}

	public float getVelY() {
		return velY;
	}

	public float getVelZ() {
		return velZ;
	}
}