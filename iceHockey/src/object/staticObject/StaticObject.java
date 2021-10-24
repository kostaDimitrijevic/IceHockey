package object.staticObject;

import javafx.scene.Group;
import object.movableObject.MovableObject;

public abstract class StaticObject extends Group {
	private boolean lastCollisionDetected;
	
	public boolean collision ( MovableObject movableObject ) {
		boolean collisionDetected = false;
		if ( this.getBoundsInParent ( ).intersects ( movableObject.getBoundsInParent ( ) ) ) {
			collisionDetected = true;
		}
		
		boolean returnValue = !this.lastCollisionDetected && collisionDetected;
		this.lastCollisionDetected = collisionDetected;
		
		return returnValue;
	}
}