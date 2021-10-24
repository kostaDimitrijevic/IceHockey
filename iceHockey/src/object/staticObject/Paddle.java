package object.staticObject;

import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Translate;
import object.movableObject.MovableObject;

public class Paddle extends StaticObject {
	private Translate translate;
	private static final int LOWER_RADIUS	 = 50;
	private static final int UPPER_RADIUS	 = 50;
	private static final int DIVISIONS		 = 100;
	private static final int RING_HEIGHT	 = 20;
	private static final int TYPE_OF_PADDLE  = 1;
	private static final int ENERGY_DROP 	 = 2;
	private int energy;
	private int lifePoints;

	public Paddle ( double width, double height, double depth, int option ) {
		lifePoints = 5;
		energy = 100;
		PhongMaterial material = new PhongMaterial ( Color.GREEN );
		this.translate = new Translate();
		super.getTransforms().addAll(
				this.translate
		);
		switch (option) {
			case 0 : {
				Box box = new Box(width, height, depth);
				box.setMaterial(material);
				super.getChildren().addAll(box);
				break;
			}
			case 1 : makePaddleOptionOne(width, height, depth, material); break;
			case 2 : makePaddleOptionTwo(width, height, depth, material); break;
		}
	}

	public void makePaddleOptionTwo(double width, double height, double depth, PhongMaterial material){
		Cylinder cylinder = new Cylinder(width/4, height/2);
		Sphere sphere = new Sphere(width/4);
		sphere.getTransforms().addAll(
				new Translate(0, -width/4)
		);

		Cylinder base = new Cylinder(depth * 2.5, height/8);
		base.getTransforms().addAll(
				new Translate(0, depth)
		);

		MeshView firstRing = getRing(RING_HEIGHT, DIVISIONS, LOWER_RADIUS, UPPER_RADIUS, false);
		MeshView secondRing = getRing(RING_HEIGHT, DIVISIONS, (int) (LOWER_RADIUS/1.2), (int) (UPPER_RADIUS/1.2), false);
		MeshView onTop = getRing((int)(-RING_HEIGHT*0.04), DIVISIONS, UPPER_RADIUS, (int) (UPPER_RADIUS/1.2), true);

		firstRing.setMaterial(material);
		secondRing.setMaterial(material);
		onTop.setMaterial(material);
		cylinder.setMaterial(material);
		sphere.setMaterial(material);
		base.setMaterial(material);

		super.getChildren().addAll(	sphere, cylinder, base, firstRing, secondRing, onTop);
	}

	public void makePaddleOptionOne(double width, double height, double depth, PhongMaterial material){
		float w = (float) width;
		float h = (float) height;
		float d = (float) depth;

		float[] firstPoints = {
				0.2f * w, 0.2f * h, -0.5f * d,
				0.5f * w, 0.2f * h, -0.5f * d,
				0.2f * w, -0.2f * h, -0.5f * d,
				0.2f * w, 0.2f * h, 0.5f * d,
				0.5f * w, 0.2f * h, 0.5f * d,
				0.2f * w, -0.2f * h, 0.5f * d
		};

		float[] firstTextCoords = {
				0f, 0f
		};

		int[] faces = {
				0,0,1,0,2,0,
				0,0,2,0,1,0,
				5,0,1,0,2,0,
				5,0,2,0,1,0,
				5,0,4,0,1,0,
				5,0,1,0,4,0,
				5,0,4,0,3,0,
				5,0,3,0,4,0,
				4,0,1,0,0,0,
				4,0,0,0,1,0,
				4,0,0,0,3,0,
				4,0,3,0,0,0
		};

		Box base = new Box(0.4 * width, height, depth);
		base.setMaterial ( material );
		base.getTransforms().addAll(
				new Translate(0, -0.3 * height, 0)
		);
		super.getChildren().addAll(base);

		TriangleMesh firstTriangleMesh = new TriangleMesh();
		firstTriangleMesh.getPoints().addAll(firstPoints);
		firstTriangleMesh.getTexCoords().addAll(firstTextCoords);
		firstTriangleMesh.getFaces().addAll(faces);

		MeshView firstMeshView = new MeshView(firstTriangleMesh);
		firstMeshView.setMaterial(material);
		super.getChildren().addAll(firstMeshView);

		float[] secondPoints = {
				-0.2f * w, 0.2f * h, -0.5f * d,
				-0.5f * w, 0.2f * h, -0.5f * d,
				-0.2f * w, -0.2f * h, -0.5f * d,
				-0.2f * w, 0.2f * h, 0.5f * d,
				-0.5f * w, 0.2f * h, 0.5f * d,
				-0.2f * w, -0.2f * h, 0.5f * d
		};

		float[] secondTextCoords = {
				0f, 0f
		};

		int[] secondFaces = {
				0,0,1,0,2,0,
				0,0,2,0,1,0,
				5,0,1,0,2,0,
				5,0,2,0,1,0,
				5,0,4,0,1,0,
				5,0,1,0,4,0,
				5,0,4,0,3,0,
				5,0,3,0,4,0,
				4,0,1,0,0,0,
				4,0,0,0,1,0,
				4,0,0,0,3,0,
				4,0,3,0,0,0
		};

		TriangleMesh secondTriangleMesh = new TriangleMesh();
		secondTriangleMesh.getPoints().addAll(secondPoints);
		secondTriangleMesh.getTexCoords().addAll(secondTextCoords);
		secondTriangleMesh.getFaces().addAll(secondFaces);

		MeshView secondMeshView = new MeshView(secondTriangleMesh);
		secondMeshView.setMaterial(material);
		super.getChildren().addAll(secondMeshView);

	}

	public MeshView getRing(float ring_height, int divisions, int lower_radius, int upper_radius, boolean onTop){
		float[] points = new float[divisions * 6];
		double startAngle = 90;
		double offset = 360. / divisions;

		int pIndex = 0;
		float lowerY = 0;
		if(onTop){
			lowerY = ring_height;
		}
		float upperY = ring_height;
		for(int i = 0; i < divisions; i ++){
			float lowerX = (float) (lower_radius * Math.cos(startAngle * Math.PI/ 180));
			float lowerZ = (float) (lower_radius * Math.sin(startAngle * Math.PI/ 180));

			float upperX = (float) (upper_radius * Math.cos(startAngle * Math.PI / 180));
			float upperZ = (float) (upper_radius * Math.sin(startAngle * Math.PI / 180));

			startAngle += offset;

			points[pIndex++] = lowerX;
			points[pIndex++] = lowerY;
			points[pIndex++] = lowerZ;

			points[pIndex++] = upperX;
			points[pIndex++] = upperY;
			points[pIndex++] = upperZ;
		}


		float[] textCoords = {
				0f, 0f
		};

		int[] faces = new int[divisions * 24];

		int index = 0;
		int lowerF = -2;

		for (int i = 0; i < divisions; i++){

			lowerF = lowerF + 2;
			int upperF = lowerF + 1;

			int lowerS = lowerF + 2;
			if(lowerS > divisions * 2 - 1){
				lowerS = 2;
			}
			int upperS = lowerF + 3;
			if(upperS > divisions * 2 - 2){
				upperS = 3;
			}

			faces[index++] = upperF;
			faces[index++] = 0;
			faces[index++] = lowerF;
			faces[index++] = 0;
			faces[index++] = lowerS;
			faces[index++] = 0;

			faces[index++] = upperF;
			faces[index++] = 0;
			faces[index++] = lowerS;
			faces[index++] = 0;
			faces[index++] = lowerF;
			faces[index++] = 0;

			faces[index++] = upperF;
			faces[index++] = 0;
			faces[index++] = lowerS;
			faces[index++] = 0;
			faces[index++] = upperS;
			faces[index++] = 0;

			faces[index++] = upperF;
			faces[index++] = 0;
			faces[index++] = upperS;
			faces[index++] = 0;
			faces[index++] = lowerS;
			faces[index++] = 0;

		}
		TriangleMesh light = new TriangleMesh();
		light.getPoints().addAll(points);
		light.getTexCoords().addAll(textCoords);
		light.getFaces().addAll(faces);

		MeshView meshView = new MeshView(light);
		meshView.setMaterial( new PhongMaterial(Color.RED));

		return meshView;
	}

	@Override public boolean collision ( MovableObject movableObject ) {
		boolean collisionDetected = super.collision ( movableObject );
		
		if ( collisionDetected && TYPE_OF_PADDLE == 0) {
			movableObject.invertSpeedX ( );
			movableObject.changeSpeed(this.energy);
			this.boostEnergy();
		}
		else if(collisionDetected && TYPE_OF_PADDLE == 1){
			Point3D speed = movableObject.getSpeed();
			Translate position = movableObject.getPosition();
			Point3D n = new Point3D(position.getX() - translate.getX(), 0, position.getZ() - translate.getZ()).normalize();
			movableObject.setSpeed(n.multiply(-speed.magnitude()));
			movableObject.changeSpeed(this.energy);
			this.boostEnergy();
		}

		
		return collisionDetected;
	}
	
	public void move ( double dx, double dy, double dz ) {
		this.translate.setX ( this.translate.getX ( ) + dx );
		this.translate.setY ( this.translate.getY ( ) + dy );
		this.translate.setZ ( this.translate.getZ ( ) + dz );
	}

	public void decreaseEnergy(){
		this.energy -= ENERGY_DROP;
	}
	public void boostEnergy(){
		this.energy += 5;

		if(this.energy >= 100) this.energy = 100;
	}
	public double getZ ( ) {
		return this.translate.getZ ( );
	}

	public int decreaseLife(int points){
		return lifePoints -= points;
	}

	public int getLifePoints(){
		return lifePoints;
	}

	public void setEnergy(int e){
		energy = e;
	}

	public int getCurrentEnergy(){
		return energy;
	}
}
