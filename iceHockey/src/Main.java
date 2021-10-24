import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.scene.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import object.movableObject.MovableObject;
import object.movableObject.Puck;
import object.staticObject.Paddle;
import object.staticObject.Wall;

public class Main extends Application implements Wall.GameStopper, EventHandler<KeyEvent> {
	
	private static final double WIDTH            = 800;
	private static final double HEIGHT           = 800;
	private static final double NEAR_CLIP        = 0.1;
	private static final double FAR_CLIP         = 5000;
	private static final double CAMERA_Z         = -2500;
	private static final double PUCK_RADIUS      = 20;
	private static final double PUCK_HEIGHT      = 40;
	private static final double LONG_WALL_WIDTH  = 1000;
	private static final double SHORT_WALL_WIDTH = 500;
	private static final double WALL_HEIGHT      = PUCK_HEIGHT;
	private static final double WALL_DEPTH       = 20;
	private static final double PADDLE_WIDTH     = 100;
	private static final double PADDLE_HEIGHT    = 100;
	private static final double PADDLE_DEPTH     = 20;
	private static final double STEP             = 10;
	private static final double CAMERA_STEP      = 50;
	private static final int CONE_LOWER_RADIUS   = 100;
	private static final int CONE_UPPER_RADIUS   = 50;
	private static final int CONE_CIRCLE_POINTS = 100;
	private static final int CONE_HEIGHT         = 120;
	private              Timer  timer;
	private              Paddle leftPaddle, rightPaddle;
	private PerspectiveCamera camera;
	private PerspectiveCamera birdViewCamera;
	private PerspectiveCamera playerCamera;
	private final Translate scrollT = new Translate(0, 0, CAMERA_Z);
	private final Rotate orbital = new Rotate(0, Rotate.Y_AXIS);
	private final Rotate birdView = new Rotate(-90, Rotate.X_AXIS);
	private MovableObject puck;
	private int puckOption, paddleOption;
	private Group playRoot, root, infoRoot;
	private Scene scene;
	private SubScene playScene;
	private SubScene infoScene;
	private double currMouseX;
	private double currMouseY;
	private Rotate cameraRotationByX = new Rotate(0, Rotate.X_AXIS);
	private Rotate cameraRotationByY = new Rotate(0, Rotate.Y_AXIS);
	private int player = 0;
	private static Text time = new Text("Time: 60");
	private static Text leftPlayerLives = new Text("Life: 5");
	private static Text rightPlayerLives = new Text("Life: 5");
	private static Text leftPlayerEnergy = new Text("Energy: 100");
	private static Text rightPlayerEnergy = new Text("Energy: 100");
	private static Stage primaryStage;
	private int numberOfGoals = 0;
	private static final int turnToSpecial = (int)(Math.random() * 3 + 1);

	public static void main ( String[] arguments ) {
		launch ( arguments );
	}

	public AnimationTimer t = new AnimationTimer() {
		private double prev ;
		private int timer = 60;
		@Override
		public void handle(long now) {
			if(this.prev == 0){
				this.prev = now;
			}

			if((now - this.prev)/1e9 >= 1){
				this.prev = now;
				--timer;
				time.setText("Time: " + timer);
				if(timer == 0){
					stopGame(3);
					this.stop();
				}
				leftPlayerEnergy.setText("Energy: " + leftPaddle.getCurrentEnergy());
				rightPlayerEnergy.setText("Energy: " + rightPaddle.getCurrentEnergy());
			}

		}
	};

	private void createCameras ( ) {
		this.birdViewCamera = new PerspectiveCamera(true);
		this.birdViewCamera.setNearClip ( NEAR_CLIP );
		this.birdViewCamera.setFarClip ( FAR_CLIP );
		this.birdViewCamera.getTransforms().addAll(
				birdView,
				new Translate(0,0,CAMERA_Z)
		);
		this.camera = new PerspectiveCamera ( true );
		this.camera.setNearClip ( NEAR_CLIP );
		this.camera.setFarClip ( FAR_CLIP );
		this.camera.getTransforms ( ).addAll (
				cameraRotationByY,
				cameraRotationByX,
				orbital,
				new Rotate ( -45, Rotate.X_AXIS ),
				scrollT
		);
		this.playScene.addEventHandler(ScrollEvent.ANY, event -> {
			double deltaY = event.getDeltaY() > 0 ? 1 : -1;
			this.scrollT.setZ(this.scrollT.getZ() + CAMERA_STEP * deltaY);
		});

		this.playScene.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_PRESSED)){
				this.currMouseX = event.getX();
				this.currMouseY = event.getY();
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_DRAGGED)){
				double dx = event.getX() - this.currMouseX;
				double dy = event.getY() - this.currMouseY;

				this.currMouseX = event.getX();
				this.currMouseY = event.getY();

				cameraRotationByX.setAngle(cameraRotationByX.getAngle() - dy * 0.05);
				cameraRotationByY.setAngle(cameraRotationByY.getAngle() - dx * 0.05);
			}
		});
	}

	private void createPlayersCamera(int player){
		playerCamera = new PerspectiveCamera(true);
		playerCamera.setNearClip(NEAR_CLIP);
		playerCamera.setFarClip(FAR_CLIP);
		Rotate rotate;
		if(player == 3) {
			 rotate = new Rotate(-90, Rotate.Y_AXIS);
		}
		else{
			 rotate = new Rotate(90, Rotate.Y_AXIS);
		}
		playerCamera.getTransforms().addAll(
				rotate,
				new Translate(0, -350, CAMERA_Z)
		);
	}

	private Scene getScene ( int paddleOption, int puckOption) {
		t.start();
		this.playRoot = new Group ( );
		this.root = new Group();
		
		this.playScene = new SubScene (playRoot, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED );

		this.playScene.setFill(Color.BLACK);

		this.puck = this.makePuck(puckOption);
		playRoot.getChildren ( ).addAll ( puck );

		Group field = new Group();

		Circle center = new Circle(110);
		center.setFill(null);
		center.setStroke(Color.RED);
		center.setStrokeWidth(10);
		center.getTransforms().addAll(
				new Translate(0, WALL_HEIGHT/2),
				new Rotate(90, Rotate.X_AXIS)
		);

		Line centerLine = new Line(0, - SHORT_WALL_WIDTH / 2, 0, SHORT_WALL_WIDTH/2);
		centerLine.setStroke(Color.RED);
		centerLine.setFill(null);
		centerLine.setStrokeWidth(10);
		centerLine.getTransforms().addAll(
				new Translate(0, WALL_HEIGHT/2),
				new Rotate(90, Rotate.X_AXIS)
		);

		Arc leftGoalArc = new Arc(-LONG_WALL_WIDTH/2, 0, SHORT_WALL_WIDTH * 0.25, SHORT_WALL_WIDTH * 0.25, -90, 180);
		leftGoalArc.setFill(null);
		leftGoalArc.setStroke(Color.RED);
		leftGoalArc.setStrokeWidth(10);
		leftGoalArc.getTransforms().addAll(
				new Translate(WALL_DEPTH/2, WALL_HEIGHT/2),
				new Rotate(90, Rotate.X_AXIS)
		);

		Arc rightGoalArc = new Arc(LONG_WALL_WIDTH/2, 0, SHORT_WALL_WIDTH * 0.25, SHORT_WALL_WIDTH * 0.25, 90, 180);
		rightGoalArc.setFill(null);
		rightGoalArc.setStroke(Color.RED);
		rightGoalArc.setStrokeWidth(10);
		rightGoalArc.getTransforms().addAll(
				new Translate(-WALL_DEPTH/2, WALL_HEIGHT/2),
				new Rotate(90, Rotate.X_AXIS)
		);

		Box fieldColor = new Box(LONG_WALL_WIDTH, 1, SHORT_WALL_WIDTH);
		fieldColor.setMaterial(new PhongMaterial(Color.LIGHTSKYBLUE));
		fieldColor.getTransforms().addAll(
				new Translate(0, WALL_HEIGHT/ 1.15)
		);

		field.getChildren().addAll(center, centerLine, leftGoalArc, rightGoalArc, fieldColor);

		Wall upperWall = new Wall ( LONG_WALL_WIDTH, WALL_HEIGHT, WALL_DEPTH, Wall.Type.UPPER, Color.GRAY);
		Wall lowerWall = new Wall ( LONG_WALL_WIDTH, WALL_HEIGHT, WALL_DEPTH, Wall.Type.LOWER, Color.GRAY );

		Wall leftDownSmallWall  = new Wall ( SHORT_WALL_WIDTH * 0.25, WALL_HEIGHT, WALL_DEPTH, Wall.Type.LEFT, Color.GRAY );
		Wall leftUpperSmallWall = new Wall ( SHORT_WALL_WIDTH * 0.25, WALL_HEIGHT, WALL_DEPTH, Wall.Type.LEFT, Color.GRAY);
		Wall leftGoal = new Wall ( SHORT_WALL_WIDTH * 0.5, WALL_HEIGHT, WALL_DEPTH, Wall.Type.LEFTGOAL, Color.BLUE, this );

		Wall rightDownSmallWall  = new Wall ( SHORT_WALL_WIDTH * 0.25, WALL_HEIGHT, WALL_DEPTH, Wall.Type.RIGHT, Color.GRAY );
		Wall rightUpperSmallWall = new Wall ( SHORT_WALL_WIDTH * 0.25, WALL_HEIGHT, WALL_DEPTH, Wall.Type.RIGHT, Color.GRAY );
		Wall rightGoal = new Wall ( SHORT_WALL_WIDTH * 0.5, WALL_HEIGHT, WALL_DEPTH, Wall.Type.RIGHTGOAL, Color.BLUE, this );
		
		upperWall.getTransforms ( ).addAll (
				new Translate ( 0, 0, SHORT_WALL_WIDTH / 2 + WALL_DEPTH / 2 )
		);
		lowerWall.getTransforms ( ).addAll (
				new Translate ( 0, 0, -( SHORT_WALL_WIDTH / 2 + WALL_DEPTH / 2 ) )
		);
		leftDownSmallWall.getTransforms ( ).addAll (
				new Translate ( -( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2 ), 0, -SHORT_WALL_WIDTH * 0.5 + (SHORT_WALL_WIDTH * 0.25 / 2)),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		leftUpperSmallWall.getTransforms ( ).addAll (
				new Translate ( -( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2 ), 0, SHORT_WALL_WIDTH * 0.5  - (SHORT_WALL_WIDTH * 0.25 / 2)),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		leftGoal.getTransforms ( ).addAll (
				new Translate ( -( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2 ), 0, 0),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		rightUpperSmallWall.getTransforms ( ).addAll (
				new Translate ( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2, 0, SHORT_WALL_WIDTH * 0.5 - (SHORT_WALL_WIDTH * 0.25 / 2) ),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		rightDownSmallWall.getTransforms ( ).addAll (
				new Translate ( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2, 0, - SHORT_WALL_WIDTH * 0.5 + (SHORT_WALL_WIDTH * 0.25 / 2) ),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		rightGoal.getTransforms ( ).addAll (
				new Translate ( LONG_WALL_WIDTH / 2 - WALL_DEPTH / 2, 0, 0 ),
				new Rotate ( 90, Rotate.Y_AXIS )
		);

		playRoot.getChildren ( ).addAll ( upperWall, lowerWall, leftDownSmallWall, leftGoal, leftUpperSmallWall, rightUpperSmallWall,
				rightDownSmallWall, rightGoal, field);

		MeshView leftReflectorLight = createReflectorLight();
		MeshView rightReflectorLight = createReflectorLight();

		PointLight reflectorLeft = new PointLight();
		PointLight reflectorRight = new PointLight();

		reflectorLeft.getTransforms().addAll(
				new Translate(-LONG_WALL_WIDTH/2, - 300, 0 ),
				new Rotate(120, Rotate.Z_AXIS)
		);

		reflectorRight.getTransforms().addAll(
				new Translate(LONG_WALL_WIDTH/2, - 300, 0 ),
				new Rotate(120, Rotate.Z_AXIS)
		);
		leftReflectorLight.getTransforms().addAll(
				new Translate(-LONG_WALL_WIDTH/2, - 300, 0 ),
				new Rotate(120, Rotate.Z_AXIS),
				new Translate(0,-CONE_HEIGHT/2,0)
		);

		rightReflectorLight.getTransforms().addAll(
				new Translate(LONG_WALL_WIDTH/2, - 300, 0 ),
				new Rotate(-120, Rotate.Z_AXIS),
				new Translate(0,-CONE_HEIGHT/2,0)
		);

		playRoot.getChildren().addAll(leftReflectorLight, rightReflectorLight, reflectorLeft, reflectorRight);

		this.leftPaddle = new Paddle ( PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_DEPTH, paddleOption );
		this.rightPaddle = new Paddle ( PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_DEPTH, paddleOption );
		this.leftPaddle.getTransforms ( ).addAll (
				new Translate ( -LONG_WALL_WIDTH / 2 * 0.8, -10, 0 ),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		this.rightPaddle.getTransforms ( ).addAll (
				new Translate ( LONG_WALL_WIDTH / 2 * 0.8, -10, 0 ),
				new Rotate ( 90, Rotate.Y_AXIS )
		);
		
		playRoot.getChildren ( ).addAll ( this.leftPaddle, this.rightPaddle);
		
		this.createCameras ( );
		this.playScene.setCamera ( this.camera );
		
		this.timer = new Timer ( puck, upperWall, lowerWall, leftGoal, leftUpperSmallWall, leftDownSmallWall, rightGoal, rightUpperSmallWall, rightDownSmallWall, this.rightPaddle, this.leftPaddle );
		this.timer.start ( );

		this.scene = new Scene(root, WIDTH, HEIGHT, true);

		this.scene.addEventHandler ( KeyEvent.KEY_PRESSED, this );

		root.getChildren().addAll(playScene, this.setInfoScene());

		return this.scene;
	}

	public Puck makePuck(int puckOption){

		if(puckOption == 0) return new Puck(PUCK_RADIUS, PUCK_HEIGHT - 20);

		return new Puck(PUCK_HEIGHT, PUCK_HEIGHT, PUCK_HEIGHT , puckOption);
	}

	public SubScene setInfoScene(){

		this.infoRoot = new Group();

		this.infoScene = new SubScene(this.infoRoot, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED);

		time.setFill(Color.WHITE);
		time.setFont(Font.font(20));
		time.getTransforms().addAll(
				new Translate(WIDTH/2 - 10, 50)
		);

		leftPlayerLives.setFill(Color.WHITE);
		leftPlayerLives.setFont(Font.font(20));
		leftPlayerLives.getTransforms().addAll(
				new Translate(30, 100)
		);

		rightPlayerLives.setFill(Color.WHITE);
		rightPlayerLives.setFont(Font.font(20));
		rightPlayerLives.getTransforms().addAll(
				new Translate(WIDTH - 150, 100)
		);

		leftPlayerEnergy.setFill(Color.WHITE);
		leftPlayerEnergy.setFont(Font.font(20));
		leftPlayerEnergy.getTransforms().addAll(
				new Translate(30, 150)
		);

		rightPlayerEnergy.setFill(Color.WHITE);
		rightPlayerEnergy.setFont(Font.font(20));
		rightPlayerEnergy.getTransforms().addAll(
				new Translate(WIDTH - 150, 150)
		);

		this.infoRoot.getChildren().addAll(time, leftPlayerLives, rightPlayerLives, leftPlayerEnergy, rightPlayerEnergy);

		return this.infoScene;
	}

	public Scene setSecondScene(){
		this.root = new Group();

		Group puckGroup = new Group();

		Puck puckOne = new Puck(PUCK_RADIUS, PUCK_HEIGHT - 20);
		Puck puckTwo = new Puck(PUCK_HEIGHT, PUCK_HEIGHT, 0, 1);
		Puck puckThree = new Puck(PUCK_RADIUS, PUCK_RADIUS, PUCK_RADIUS, 2);

		puckOne.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : puckOne.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : puckOne.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GRAY));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				puckOption = 0;
				primaryStage.setScene ( this.getScene(paddleOption, 0));
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		puckTwo.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : puckTwo.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : puckTwo.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GRAY));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				puckOption = 1;
				primaryStage.setScene ( this.getScene(paddleOption, 1));
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		puckThree.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : puckThree.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : puckThree.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GRAY));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				puckOption = 2;
				primaryStage.setScene ( this.getScene(paddleOption, 2));
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		puckOne.getTransforms().addAll(
				new Translate(WIDTH/2 - 100, HEIGHT/2)
		);

		puckTwo.getTransforms().addAll(
				new Translate(WIDTH/2, HEIGHT/2 - 10)
		);

		puckThree.getTransforms().addAll(
				new Translate(WIDTH/2 + 100, HEIGHT/2)
		);
		puckGroup.getChildren().addAll(puckOne, puckTwo, puckThree);

		this.playScene = new SubScene(puckGroup, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED);
		this.playScene.setFill(Color.BLACK);

		this.scene = new Scene(root, WIDTH, HEIGHT, true);
		this.root.getChildren().addAll(playScene);

		return this.scene;
	}

	public Scene setFirstScene(){

		this.root = new Group();

		Group paddleGroup = new Group();

		Paddle paddleOne = new Paddle(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_DEPTH, 0);
		Paddle paddleTwo = new Paddle(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_DEPTH, 1);
		Paddle paddleThree = new Paddle(PADDLE_WIDTH, PADDLE_HEIGHT, PADDLE_DEPTH, 2);

		paddleOne.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : paddleOne.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : paddleOne.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GREEN));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				paddleOption = 0;
				primaryStage.setScene ( this.setSecondScene());
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		paddleTwo.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : paddleTwo.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : paddleTwo.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GREEN));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				paddleOption = 1;
				primaryStage.setScene ( this.setSecondScene());
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		paddleThree.addEventHandler(MouseEvent.ANY, event -> {
			if(event.getEventType().equals(MouseEvent.MOUSE_ENTERED)){
				for(Node child : paddleThree.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.RED));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_EXITED)){
				for(Node child : paddleThree.getChildren()){
					Shape3D part = (Shape3D) child;
					part.setMaterial(new PhongMaterial(Color.GREEN));
				}
			}
			else if(event.getEventType().equals(MouseEvent.MOUSE_CLICKED)){
				paddleOption = 2;
				primaryStage.setScene ( this.setSecondScene());
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );
			}
		});

		paddleOne.getTransforms().addAll(
			new Translate(WIDTH/2 - PADDLE_WIDTH - 30, HEIGHT/2)
		);

		paddleTwo.getTransforms().addAll(
				new Translate(WIDTH/2, HEIGHT/2 + 30)
		);

		paddleThree.getTransforms().addAll(
				new Translate(WIDTH/2 + PADDLE_WIDTH + 30, HEIGHT/2 + 20)
		);
		paddleGroup.getChildren().addAll(paddleOne, paddleTwo, paddleThree);

		this.playScene = new SubScene(paddleGroup, WIDTH, HEIGHT, true, SceneAntialiasing.DISABLED);
		this.playScene.setFill(Color.BLACK);

		this.scene = new Scene(root, WIDTH, HEIGHT, true);
		this.root.getChildren().addAll(playScene);

		return this.scene;
	}
	
	@Override public void start ( Stage primary ) throws Exception {
		primaryStage = primary;
		primaryStage.setScene ( this.setFirstScene());
		primaryStage.setTitle ( "Vazdusni hokej" );
		primaryStage.show ( );
	}

	public MeshView createReflectorLight(){
		float[] points = new float[CONE_CIRCLE_POINTS * 6 + 3];
		points[0] =  points[2] = 0;
		points[1] = CONE_HEIGHT;

		double startAngle = 90;
		double offset = 360. / CONE_CIRCLE_POINTS;

		int pIndex = 3;
		float lowerY = 0;
		float upperY = CONE_HEIGHT;
		for(int i = 0; i < CONE_CIRCLE_POINTS; i ++){
			float lowerX = (float) (CONE_LOWER_RADIUS * Math.cos(startAngle * Math.PI/ 180));
			float lowerZ = (float) (CONE_LOWER_RADIUS * Math.sin(startAngle * Math.PI/ 180));

			float upperX = (float) (CONE_UPPER_RADIUS * Math.cos(startAngle * Math.PI / 180));
			float upperZ = (float) (CONE_UPPER_RADIUS * Math.sin(startAngle * Math.PI / 180));

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

		int[] faces = new int[CONE_CIRCLE_POINTS * 36];

		int index = 0;
		int lowerF = -1;

		int upperF = 0;
		for(int i = 0; i < CONE_CIRCLE_POINTS; i++){
			upperF = upperF + 2;
			int upperS = upperF + 2;

			if(upperS > CONE_CIRCLE_POINTS * 2){
				upperS = 2;
			}

			faces[index++] = 0;
			faces[index++] = 0;
			faces[index++] = upperF;
			faces[index++] = 0;
			faces[index++] = upperS;
			faces[index++] = 0;

			faces[index++] = 0;
			faces[index++] = 0;
			faces[index++] = upperS;
			faces[index++] = 0;
			faces[index++] = upperF;
			faces[index++] = 0;
		}

		for (int i = 0; i < CONE_CIRCLE_POINTS; i++){

			lowerF = lowerF + 2;
			upperF = lowerF + 1;

			int lowerS = lowerF + 2;
			if(lowerS > CONE_CIRCLE_POINTS * 2){
				lowerS = 3;
			}
			int upperS = lowerF + 3;
			if(upperS > CONE_CIRCLE_POINTS * 2){
				upperS = 4;
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
		meshView.setMaterial( new PhongMaterial(Color.GRAY));
		return meshView;
	}


	@Override public void stopGame ( int ev) {
		leftPaddle.setEnergy(100);
		rightPaddle.setEnergy(100);
		switch (ev) {
			case 0: {
				this.timer.stop ( );
				playRoot.getChildren().remove(puck);
				puck = makePuck(puckOption);
				if(numberOfGoals == turnToSpecial){
					((Puck)puck).specialPuck();
				}
				this.timer.setMovableObject(puck);
				playRoot.getChildren().addAll(puck);
				this.timer.start();
				break;
			}
			case 1:{
				this.timer.stop ( );
				playRoot.getChildren().remove(puck);

				this.root = new Group();
				this.scene = new Scene(this.root, WIDTH, HEIGHT);

				Text win = new Text("RIGHT PLAYER WINS!");
				win.getTransforms().addAll(
						new Translate(WIDTH/2 - win.getText().length() - 10, HEIGHT/2)
				);
				this.root.getChildren().addAll(win);

				primaryStage.setScene ( this.scene);
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );

				break;
			}
			case 2:{
				this.timer.stop ( );
				playRoot.getChildren().remove(puck);
				Text win = new Text("LEFT PLAYER WINS!");
				win.getTransforms().addAll(
						new Translate(WIDTH/2 - win.getText().length() - 10, HEIGHT/2)
				);
				this.root = new Group();
				this.root.getChildren().addAll(win);

				primaryStage.setScene ( this.scene);
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );

				break;
			}
			case 3:{
				this.timer.stop ( );
				playRoot.getChildren().remove(puck);

				Text timeOut = new Text("TIME IS OUT!");
				Text winner;

				if(leftPaddle.getLifePoints() > rightPaddle.getLifePoints())
					winner = new Text("LEFT PLAYER WINS!");
				else if (leftPaddle.getLifePoints() == rightPaddle.getLifePoints())
					winner = new Text("DRAW!");
				else
					winner = new Text("RIGHT PLAYER WINS!");
				winner.getTransforms().addAll(
						new Translate(WIDTH/2 - winner.getText().length(), HEIGHT/2 + 100)
				);

				timeOut.getTransforms().addAll(
						new Translate(WIDTH/2 - timeOut.getText().length(), HEIGHT/2)
				);

				this.root = new Group();
				this.root.getChildren().addAll(timeOut, winner);
				this.scene = new Scene(this.root, WIDTH, HEIGHT, true);

				primaryStage.setScene ( this.scene);
				primaryStage.setTitle ( "Vazdusni hokej" );
				primaryStage.show ( );


				break;
			}
		}

	}
	
	@Override public void handle ( KeyEvent event ) {
		if ( event.getCode ( ).equals ( KeyCode.W ) ) {
			double newZ = this.leftPaddle.getZ ( ) + STEP;
			
			if ( ( newZ + PADDLE_WIDTH / 2 ) <= SHORT_WALL_WIDTH / 2 ) {
				this.leftPaddle.move ( 0, 0, STEP );
			}
		} else if ( event.getCode ( ).equals ( KeyCode.S ) ) {
			double newZ = this.leftPaddle.getZ ( ) - STEP;
			
			if ( ( newZ - PADDLE_WIDTH / 2 ) >= -SHORT_WALL_WIDTH / 2 ) {
				this.leftPaddle.move ( 0, 0, -STEP );
			}
		} else if ( event.getCode ( ).equals ( KeyCode.UP ) ) {
			double newZ = this.rightPaddle.getZ ( ) + STEP;
			
			if ( ( newZ + PADDLE_WIDTH / 2 ) <= SHORT_WALL_WIDTH / 2 ) {
				this.rightPaddle.move ( 0, 0, STEP );
				if(player == 3){
					playerCamera.setTranslateZ( playerCamera.getTranslateZ() + STEP);
				}
				else if(player == 4){
					playerCamera.setTranslateZ( playerCamera.getTranslateZ() + STEP);
				}
			}


		} else if ( event.getCode ( ).equals ( KeyCode.DOWN ) ) {
			double newZ = this.rightPaddle.getZ ( ) - STEP;
			
			if ( ( newZ - PADDLE_WIDTH / 2 ) >= -SHORT_WALL_WIDTH / 2 ) {
				this.rightPaddle.move ( 0, 0, -STEP );
				if(player == 3){
					playerCamera.setTranslateZ( playerCamera.getTranslateZ() - STEP);
				}
				else if(player == 4){
					playerCamera.setTranslateZ( playerCamera.getTranslateZ() - STEP);
				}
			}
		} else if(event.getCode().equals(KeyCode.LEFT)) {
			orbital.setAngle(orbital.getAngle() + 10);

		}
		else if(event.getCode().equals(KeyCode.RIGHT)) {
			orbital.setAngle(orbital.getAngle() - 10);

		}
		else if(event.getCode().equals(KeyCode.DIGIT2)) {
			this.playScene.setCamera(birdViewCamera);
		}
		else if(event.getCode().equals(KeyCode.DIGIT1)){
			this.playScene.setCamera(camera);
		}
		else if(event.getCode().equals(KeyCode.DIGIT3)){
			player = 3;
			createPlayersCamera(3);
			this.playScene.setCamera(playerCamera);
		}
		else if(event.getCode().equals(KeyCode.DIGIT4)){
			player = 4;
			createPlayersCamera(4);
			this.playScene.setCamera(playerCamera);
		}
	}

	@Override
	public void goal(int player){
		int life;
		numberOfGoals++;
		switch (player){
			case 1 : {
				if(((Puck)puck).isSpecial()){
					life = leftPaddle.decreaseLife(2);
				}
				else{
					life = leftPaddle.decreaseLife(1);
				}
				if(life == 0) this.stopGame(1);
				break;
			}
			case 2 : {
				if(((Puck)puck).isSpecial()){
					life = rightPaddle.decreaseLife(2);
				}
				else{
					life = rightPaddle.decreaseLife(1);
				}
				if(life == 0) this.stopGame(2);
				break;
			}
		}

		rightPlayerLives.setText("Life: " + this.rightPaddle.getLifePoints());
		leftPlayerLives.setText("Life: " + this.leftPaddle.getLifePoints());

		this.stopGame(0);
	}
}
