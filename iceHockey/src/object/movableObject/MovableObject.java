package object.movableObject;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.transform.Translate;

public abstract class MovableObject extends Group {
	private Translate position;
	private Point3D   speed;
	private static final int startSpeed = 100;
	private static final double FRICTION = 0.1;
	
	public MovableObject ( Translate position, Point3D speed ) {
		this.position = position;
		this.speed = speed;
		
		super.getTransforms ( ).addAll (
				this.position
		);
	}

	public Point3D getSpeed(){
		return speed;
	}

	public void setSpeed(Point3D speed){
		this.speed = speed;
	}

	public Translate getPosition(){
		return position;
	}

	public void update ( double dt ) {

		double frictionX, frictionZ;
		frictionX = frictionZ = FRICTION;

		if(this.speed.getX() < 0){
			frictionX = -FRICTION;
		}
		if(this.speed.getZ() < 0){
			frictionZ = -FRICTION;
		}
		this.speed = new Point3D(this.speed.getX() - frictionX, this.speed.getY(), this.speed.getZ() - frictionZ);

		double newX = position.getX ( ) + speed.getX ( ) * dt;
		double newY = position.getY ( ) + speed.getY ( ) * dt;
		double newZ = position.getZ ( ) + speed.getZ ( ) * dt;
		
		this.position.setX ( newX );
		this.position.setY ( newY );
		this.position.setZ ( newZ );
	}
	
	public void invertSpeedX ( ) {
		this.speed = new Point3D ( -this.speed.getX ( ), this.speed.getY ( ), this.speed.getZ ( ) );
	}
	
	public void invertSpeedZ ( ) {
		this.speed = new Point3D ( this.speed.getX ( ), this.speed.getY ( ), -this.speed.getZ ( ) );
	}

	public void changeSpeed(int e){

		int startSpeedX, startSpeedZ;

		startSpeedX = startSpeedZ = startSpeed;

		if(this.speed.getX() < 0){
			startSpeedX = -1 * startSpeed;
		}
		if(this.speed.getZ() < 0){
			startSpeedZ = -1 * startSpeed;
		}


		this.speed = new Point3D(startSpeedX + this.speed.getX() * e/100, this.speed.getY(), startSpeedZ + this.speed.getZ() * e/100);
	}
}
