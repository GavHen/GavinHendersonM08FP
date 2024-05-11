package BrickBreaker;
	
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.application.Platform;

public class Main extends Application {

    private boolean gameStarted = false;
    private double deltaX = 2;
    private double deltaY = -2;
    private Button gameOverButton;
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Create root node (AnchorPane)
        AnchorPane root = new AnchorPane();
        
        // Set background color
        root.setStyle("-fx-background-color: #FDF5E6;"); // Set the background color to light gray

        // Create Circle (ball)
        Circle circle = new Circle(300, 300, 5, Color.GREY);

        // Create Rectangle (paddle)
        Rectangle paddle = new Rectangle(256, 344, 88, 10);
        paddle.setFill(Color.MEDIUMSEAGREEN);


        // Create Start Button
        Button startButton = new Button("START");
        startButton.setLayoutX(188);
        startButton.setLayoutY(145);
        startButton.setPrefSize(225, 111);
        startButton.setOnAction(event -> startGame(primaryStage, startButton, circle, paddle));

        // Add components to the root AnchorPane
        root.getChildren().addAll(circle, paddle, startButton);

        // Create scene
        Scene scene = new Scene(root, 600, 400);

        // Set scene and show stage
        primaryStage.setTitle("Brick Breaker");
        primaryStage.setScene(scene);
        primaryStage.show();
        
        // Create Game Over Button
        gameOverButton = new Button("Game Over! Play again?");
        gameOverButton.setLayoutX(200);
        gameOverButton.setLayoutY(270);
        gameOverButton.setPrefSize(200, 50);
        gameOverButton.setOnAction(event -> primaryStage.close()); // Close the primary stage when the button is clicked
        gameOverButton.setVisible(false); // Make the button invisible initially
        
        // Add event handler to the "Game Over" button
        gameOverButton.setOnAction(event -> {
            primaryStage.close(); // Close the current window
            // Launch a new instance of the application
            Platform.runLater(() -> {
                try {
                    new Main().start(new Stage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        });

        // Add the button to the root AnchorPane
        root.getChildren().add(gameOverButton);
        
        
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    private void startGame(Stage primaryStage, Button startButton, Circle circle, Rectangle paddle) {
        final AnimationTimer[] gameLoop = {null}; // Declare gameLoop variable as final

        if (!gameStarted) {
            gameStarted = true;

            // Remove the "START" button from the root AnchorPane
            ((AnchorPane) startButton.getParent()).getChildren().remove(startButton);

            // Get the root AnchorPane from the scene
            AnchorPane root = (AnchorPane) primaryStage.getScene().getRoot();

            // Initialize the game loop
            gameLoop[0] = new AnimationTimer() {
                @Override
                public void handle(long now) {
                    // Update ball position
                    circle.setCenterX(circle.getCenterX() + deltaX);
                    circle.setCenterY(circle.getCenterY() + deltaY);

                    // Check collision with game boundaries
                    if (circle.getCenterX() <= circle.getRadius() || circle.getCenterX() >= 600 - circle.getRadius()) {
                        // Reverse ball direction on collision with horizontal boundaries (walls)
                        deltaX *= -1;
                    }
                    if (circle.getCenterY() >= 400 - circle.getRadius()) {
                        // Stop the game if the ball hits the bottom
                        stopGame(gameLoop[0]);
                        gameOverButton.setVisible(true); // Show game over button
                        return; // Exit the method immediately after stopping the game
                    }

                 // Check collision with paddle
                    if (circle.getBoundsInParent().intersects(paddle.getBoundsInParent())) {
                        // Calculate the point of contact on the paddle
                        double intersectX = circle.getCenterX();
                        double paddleLeftX = paddle.getX();
                        double paddleWidth = paddle.getWidth();
                        double relativeIntersectX = intersectX - paddleLeftX;
                        
                        // Calculate the percentage of the paddle width where the collision occurred
                        double relativeCollisionX = relativeIntersectX / paddleWidth;
                        
                        // Adjust the ball direction based on the collision point
                        if (relativeCollisionX < 0.2) {
                            // Leftmost part of paddle
                            deltaY *= -1; // Reverse vertical direction
                            deltaX = -2.5; // Adjust horizontal direction
                        } else if (relativeCollisionX < 0.4) {
                            // Left part of paddle
                            deltaY *= -1; // Reverse vertical direction
                            deltaX = -1.5; // Adjust horizontal direction
                        } else if (relativeCollisionX < 0.6) {
                            // Middle part of paddle
                            deltaY *= -1.05; // Reverse vertical direction
                            deltaX *= 0.8; // Adjust horizontal direction
                        } else if (relativeCollisionX < 0.8) {
                            // Right part of paddle
                            deltaY *= -1; // Reverse vertical direction
                            deltaX = 1.5; // Adjust horizontal direction
                        } else {
                            // Rightmost part of paddle
                            deltaY *= -1; // Reverse vertical direction
                            deltaX = 2.5; // Adjust horizontal direction
                        }
                    }

                    // Check collision with top zone
                    if (circle.getCenterY() <= circle.getRadius()) {
                        // Reverse ball direction on collision with top zone
                        deltaY *= -1;
                    }
                    // Check collision with bricks
                    for (Node brick : root.getChildren()) {
                        if (brick instanceof Rectangle && brick != paddle && circle.getBoundsInParent().intersects(brick.getBoundsInParent())) {
                            // Determine the direction of the collision
                            double brickCenterX = brick.getBoundsInParent().getMinX() + brick.getBoundsInParent().getWidth() / 2;
                            double brickCenterY = brick.getBoundsInParent().getMinY() + brick.getBoundsInParent().getHeight() / 2;

                            double ballCenterX = circle.getCenterX();
                            double ballCenterY = circle.getCenterY();

                            boolean collisionFromLeft = ballCenterX < brickCenterX;
                            boolean collisionFromTop = ballCenterY < brickCenterY;
                            boolean collisionFromBottom = ballCenterY > brickCenterY;

                            // Adjust ball direction based on collision
                            if ((collisionFromLeft && deltaX > 0) || (!collisionFromLeft && deltaX < 0)) {
                                deltaX *= -1; // Reverse horizontal direction
                            }
                            if ((collisionFromTop && deltaY > 0) || (!collisionFromTop && deltaY < 0)) {
                                deltaY *= -1; // Reverse vertical direction
                            }
                            if ((collisionFromBottom && deltaY < 0) || (!collisionFromBottom && deltaY > 0)) {
                                deltaY *= -1; // Reverse vertical direction
                            }
                            // Remove the brick from the scene
                            root.getChildren().remove(brick);
                            break; // Exit the loop after removing one brick
                        }
                    }
                }
            };
            gameLoop[0].start(); // Start the game loop

            // Add mouse event handler for paddle movement
            ((AnchorPane) paddle.getParent()).setOnMouseMoved(event -> {
                double mouseX = event.getX();
                double paddleWidth = paddle.getWidth();
                double sceneWidth = primaryStage.getScene().getWidth();

                // Set the position of the paddle based on mouse movement
                paddle.setX(mouseX - paddleWidth / 2);

                // Ensure paddle stays within the game boundaries
                if (paddle.getX() < 0) {
                    paddle.setX(0);
                } else if (paddle.getX() > sceneWidth - paddleWidth) {
                    paddle.setX(sceneWidth - paddleWidth);
                }
            });
            createBricks(primaryStage.getScene());
        }
    }

    // Method to stop the game loop
    private void stopGame(AnimationTimer gameLoop) {
        gameLoop.stop();
    }
    private int createBricks(Scene scene) {
        int numBricks = 0; // Initialize the counter for bricks
        
        double width = 545; // Boundaries the bricks are in
        double height = 200;
        int spaceCheck = 1;
        
        // Create a new AnchorPane to add bricks to
        AnchorPane root = (AnchorPane) scene.getRoot();
        
        for (double i = height; i > 0; i -= 50) {	// Y spacing influence
            for (double j = width; j > 0; j -= 30) { // X spacing influence
                if (spaceCheck % 3 == 0) { // Spacing of the bricks
                    Rectangle brick = new Rectangle(j, i, 50, 30); // Size of the bricks
                    brick.setFill(Color.FIREBRICK);
                    root.getChildren().add(brick); // Add brick to the root AnchorPane
                    numBricks++; // Increment the counter for bricks
                }
                spaceCheck++;
            }
        }
        return numBricks; // Return the total number of bricks created
    }
}