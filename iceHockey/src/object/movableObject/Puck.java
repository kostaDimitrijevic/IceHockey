package object.movableObject;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Point3D;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Puck extends MovableObject {
	
	private static final double SPEED_X_MIN = 150;
	private static final double SPEED_X_MAX = 200;
	private static final double SPEED_Z_MIN = 120;
	private static final double SPEED_Z_MAZ = 120;

	private Shape3D puck;
	private boolean specialPuck = false;

	public Puck ( double radius, double height ) {
		super ( new Translate ( 0, 0, 0 ), Puck.getRandomSpeed ( ) );

		puck = new Cylinder ( radius, height );

		puck.setMaterial ( new PhongMaterial ( Color.GRAY ) );
		super.getChildren ( ).addAll (
				puck
		);
	}

	public Puck (double width, double height, double depth, int option){
		super(new Translate(0,0,0), Puck.getRandomSpeed());

		if(option == 1){
			puck = makeTriangularPuck((float) width, (float) height);

			Timeline timeline = new Timeline(
					new KeyFrame(Duration.ZERO, new KeyValue(puck.rotationAxisProperty(), Rotate.Y_AXIS ), new KeyValue(puck.rotateProperty(), 0)),
					new KeyFrame(Duration.seconds(1), new KeyValue(puck.rotationAxisProperty(), Rotate.Y_AXIS ), new KeyValue(puck.rotateProperty(), 360))
			);

			timeline.setCycleCount(Animation.INDEFINITE);
			timeline.play();
		}
		else{
			puck = new Box(width, height, depth);
			puck.setMaterial ( new PhongMaterial ( Color.GRAY ) );
			super.getChildren ( ).addAll (
					puck
			);
		}
	}

	public MeshView makeTriangularPuck(float a, float h){

		float[] points = {
				-0.5f * a, 0, 0.3f * h,
				0.5f * a, 0, 0.3f * h,
				0, 0, h,
				-0.5f * a, 0.6f * h, 0.3f * h,
				0.5f * a, 0.6f * h, 0.3f * h,
				0, 0.6f * h, h,
		};

		float[] texture = {
			0f, 0f
		};

		int[] faces = {
			0, 0, 1, 0, 2, 0,
			0, 0, 2, 0, 1, 0,
			3, 0, 4, 0, 5, 0,
			3, 0, 5, 0, 4, 0,
			3, 0, 4, 0, 0, 0,
			3, 0, 0, 0, 4, 0,
			4, 0, 1, 0, 0, 0,
			4, 0, 0, 0, 1, 0,
			4, 0, 5, 0, 1, 0,
			4, 0, 1, 0, 5, 0,
			5, 0, 2, 0, 1, 0,
			5, 0, 1, 0, 2, 0,
			3, 0, 5, 0, 0, 0,
			3, 0, 0, 0, 5, 0,
			5, 0, 2, 0, 0, 0,
			5, 0, 0, 0, 2, 0
		};

		TriangleMesh triangleMesh = new TriangleMesh();
		triangleMesh.getPoints().addAll(points);
		triangleMesh.getTexCoords().addAll(texture);
		triangleMesh.getFaces().addAll(faces);

		MeshView meshView = new MeshView(triangleMesh);
		meshView.setMaterial(new PhongMaterial(Color.GRAY));
		super.getChildren().addAll(meshView);

		return meshView;
	}

	
	private static Point3D getRandomSpeed ( ) {
		double x = Math.random ( ) * ( Puck.SPEED_X_MAX - Puck.SPEED_X_MIN ) + SPEED_X_MIN;
		double z = Math.random ( ) * ( Puck.SPEED_Z_MAZ - Puck.SPEED_Z_MIN ) + SPEED_Z_MIN;
		double negativ = Math.random() * 100;
		if(negativ > 75){
			x *= -1;
			z *= -1;
		}
		else if(negativ >= 50 && negativ < 75)
			z *= -1;
		else if (negativ >= 25 && negativ < 50)
			x *= -1;
		return new Point3D ( x, 0, z );
	}

	public void specialPuck(){
		specialPuck = true;
		puck.setMaterial(new PhongMaterial(Color.rgb(252, 3, 165)));
	}

	public boolean isSpecial(){
		return specialPuck;
	}
}
