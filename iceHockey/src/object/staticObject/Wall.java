package object.staticObject;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.util.Duration;
import object.movableObject.MovableObject;

public class Wall extends StaticObject {
	private Type        type;
	private GameStopper gameStopper;
	private Box wall;
	private PhongMaterial material;

	public Wall ( double width, double height, double depth, Type type, Color color, GameStopper gameStopper ) {
		wall = new Box ( width, height, depth );
		material = new PhongMaterial ( color );

		if(type != Type.LEFTGOAL && type != Type.RIGHTGOAL){
			material.setDiffuseMap( new Image("wall.png"));
			wall.setMaterial ( material );
		}
		else
			wall.setMaterial ( material );

		super.getChildren ( ).addAll ( wall );
		
		this.type = type;
		this.gameStopper = gameStopper;
	}
	
	public Wall ( double width, double height, double depth, Type type, Color color ) {
		this ( width, height, depth, type, color, null );
	}
	
	@Override public boolean collision ( MovableObject movableObject ) {
		boolean collisionDetected = super.collision ( movableObject );
		
		if ( collisionDetected ) {
			Timeline movementChange;
			Timeline colorChanger;
			double z = wall.getTranslateZ();
			switch ( type ) {
				case UPPER:{
					movementChange = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(wall.translateZProperty(),   20)),
							new KeyFrame(Duration.seconds(1), new KeyValue(wall.translateZProperty(), 0))
					);
					movementChange.play();
					movableObject.invertSpeedZ ( );
					break;
				}
				case LOWER:{
					movementChange = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(wall.translateZProperty(),  - 20)),
							new KeyFrame(Duration.seconds(1), new KeyValue(wall.translateZProperty(), 0))
					);
					movementChange.play();
					movableObject.invertSpeedZ ( );
					break;
				}
				case LEFT:{
					movementChange = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(wall.translateZProperty(), - 20)),
							new KeyFrame(Duration.seconds(1), new KeyValue(wall.translateZProperty(), 0))
					);
					movementChange.play();
					movableObject.invertSpeedX ( );
					break;
				}
				case RIGHT:
				{
					movementChange = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(wall.translateZProperty(),  20)),
							new KeyFrame(Duration.seconds(1), new KeyValue(wall.translateZProperty(), 0))
					);
					movementChange.play();
					movableObject.invertSpeedX ( );
					break;
				}
				case LEFTGOAL:{
					colorChanger = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(material.diffuseColorProperty(), Color.RED)),
							new KeyFrame(Duration.seconds(2), new KeyValue(material.diffuseColorProperty(), Color.BLUE))
					);
					colorChanger.play();
					this.gameStopper.goal(1);
					break;
				}
				case RIGHTGOAL: {
					colorChanger = new Timeline(
							new KeyFrame(Duration.ZERO, new KeyValue(material.diffuseColorProperty(), Color.RED)),
							new KeyFrame(Duration.seconds(2), new KeyValue(material.diffuseColorProperty(), Color.BLUE))
					);
					colorChanger.play();
					this.gameStopper.goal(2);
					break;
				}
			}
		}
		
		return collisionDetected;
	}

	public enum Type {
		UPPER, LOWER, LEFTGOAL, RIGHTGOAL, LEFT, RIGHT
	}
	
	
	public static interface GameStopper {
		void stopGame ( int playerLost);
		void goal(int player);
	}
}