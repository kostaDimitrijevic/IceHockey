import javafx.animation.AnimationTimer;
import object.movableObject.MovableObject;
import object.staticObject.Paddle;
import object.staticObject.StaticObject;

import java.util.Arrays;
import java.util.List;

public class Timer extends AnimationTimer {
	private MovableObject       movableObject;
	private List<StaticObject>  staticObjects;
	private long                previous;
	private long				sec;
	
	public Timer ( MovableObject movableObject, StaticObject... staticObjects ) {
		this.movableObject = movableObject;
		this.staticObjects = Arrays.asList ( staticObjects );
	}

	public void setMovableObject(MovableObject m){
		this.movableObject = m;
	}

	@Override public void handle ( long now ) {
		if ( this.previous == 0 ) {
			this.previous = now;
			this.sec = now;
		}
		
		double dt = ( now - this.previous ) / 1e9;
		this.previous = now;
		
		this.movableObject.update ( dt );
		
		this.staticObjects.forEach ( staticObject -> {
			staticObject.collision ( this.movableObject );

			if(staticObject instanceof Paddle && (now - this.sec) / 1e9 >= 1) {
				Paddle paddle = (Paddle)staticObject;
				paddle.decreaseEnergy();
			}
		});
		if((now - this.sec) / 1e9 >= 1)
			this.sec = now;
	}
}
