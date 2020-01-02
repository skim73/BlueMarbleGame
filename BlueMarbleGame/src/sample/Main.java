package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class Main extends Application {

    /**
     * Main BlueMarbleGame Application
     *
     * @param primaryStage the window shown
     * @throws Exception any Exception thrown
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Blue Marble Game");

        // BorderPane root {LEFT: boardStackPane, CENTER: gameBox}
        // StackPane boardStackPane { Node 0: ImageView of board ; Node 1 : playerGridPane }
        BorderPane root = new BorderPane();
        StackPane boardStackPane = new StackPane();
        GridPane playerGridPane = new GridPane();

        // ***********EDIT APPLICATION SIZE HERE!***********
        primaryStage.setScene(new Scene(root, 1280, 658));
        primaryStage.show();

        // Installs board to the left of the application
        Image board = new Image(new FileInputStream("bluemarbleboard.png"));
        ImageView boardIV = new ImageView(board);
        boardIV.setFitWidth(primaryStage.getScene().getHeight());
        boardIV.setFitHeight(primaryStage.getScene().getHeight());
        boardStackPane.getChildren().add(boardIV);

        /*  Sets up playerGridPane to have a row and column for every space.
            (Row 0 and Column 0 represent the white frame around the board)
            GO SPACE at Column 11, Row 11, CAIRO at Column 3, Row 11, etc.
         */
        playerGridPane.setGridLinesVisible(true);
        playerGridPane.setHgap(1);
        playerGridPane.setVgap(1);

        /*  These are the pixel lengths of the frame, corners, and spaces of the board expressed as fractions of
            the entire board.
         */
        final double FRAME_RATIO = 40/1654.0;
        final double CORNER_RATIO = 222/1654.0;
        final double SPACE_RATIO = 122/1654.0;
        // Row 0 & Column 0 represent the white frame around the board
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(FRAME_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(FRAME_RATIO * boardIV.getFitHeight()));
        // Row 1 & Column 1 are wider, as seen in the board
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(CORNER_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(CORNER_RATIO * boardIV.getFitHeight()));
        // Rows & Columns 2 - 10
        for (int i = 2; i <= 10; i++) {
            playerGridPane.getColumnConstraints().add(new ColumnConstraints(SPACE_RATIO * boardIV.getFitWidth()));
            playerGridPane.getRowConstraints().add(new RowConstraints(SPACE_RATIO * boardIV.getFitHeight()));
        }
        // Row 11 & Column 11 are just as wide as Row 1 & Column 1
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(CORNER_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(CORNER_RATIO * boardIV.getFitHeight()));

        boardStackPane.getChildren().add(playerGridPane);

        // Create gameBox to the right of the board
        // Prompt for number of players on the right of the application
        ComboBox<String> numPlayersComboBox = new ComboBox<>();
        numPlayersComboBox.setPromptText("How many players?");
        numPlayersComboBox.getItems().addAll("2 PLAYERS", "3 PLAYERS", "4 PLAYERS");

        Button playButton = new Button("PLAY!");
        Button howToPlayButton = new Button("How to Play");

        VBox gameBox = new VBox(numPlayersComboBox, playButton);
        gameBox.setSpacing(25);
        gameBox.setAlignment(Pos.CENTER);
        BorderPane.setMargin(gameBox, new Insets(16));

        root.setLeft(boardStackPane);
        root.setCenter(gameBox);

        // create a GameManager object
        GameManager gameManager = new GameManager(gameBox, playerGridPane);


        // When PLAY button is clicked: load Player setup UI
        playButton.setOnAction(actionEvent -> {
                int numPlayers = 0;
                switch (numPlayersComboBox.getValue()) {
                    case "2 PLAYERS":
                        gameManager.players = new ArrayList<>(2);
                        numPlayers = 2;
                        gameBox.getChildren().clear();
                        break;
                    case "3 PLAYERS":
                        gameManager.players = new ArrayList<>(3);
                        numPlayers = 3;
                        gameBox.getChildren().clear();
                        break;
                    case "4 PLAYERS":
                        gameManager.players = new ArrayList<>(4);
                        numPlayers = 4;
                        gameBox.getChildren().clear();
                        break;
                }

                VBox[] playerSetup = new VBox[numPlayers];
                for (int i = 0; i < numPlayers; i++) {
                    gameManager.players.add(new Player(i + 1));
                    Label nameLabel = new Label("Player " + (i + 1) + " Name: ");
                    TextField input = new TextField("Player " + (i + 1));
                    HBox nameInput = new HBox(nameLabel, input);

                    Label chooseLabel = new Label("Choose plane color (Click to change): ");
                    HBox planeColor = new HBox();
                    try {
                        PlaneColorChoices planeColorChoices = new PlaneColorChoices();
                        ImageView planeImageView = new ImageView(planeColorChoices.getIcon());
                        planeImageView.setFitWidth(64);
                        planeImageView.setFitHeight(64);

                        planeImageView.setOnMouseClicked(mouseEvent -> {
                                planeImageView.setImage(planeColorChoices.next());
                        });

                        planeColor.getChildren().addAll(chooseLabel, planeImageView);

                    } catch (FileNotFoundException e) {
                        System.out.println("IMAGE NOT FOUND");
                    }

                    playerSetup[i] = new VBox(nameInput, planeColor);
                    playerSetup[i].setSpacing(15);
                    gameBox.getChildren().add(playerSetup[i]);
                }
                gameBox.setSpacing(30);

                Button LetsPlayButton = new Button("LET'S PLAY!");
                LetsPlayButton.setOnAction(actionEvent1 -> {
                        for (int i = 0; i < gameManager.players.size(); i++) {
                            Player player = gameManager.players.get(i);
                            ImageView playerPlane =
                                ((ImageView) ((HBox) playerSetup[i].getChildren().get(1)).getChildren().get(1));
                            player.setPlane(playerPlane);
                        }
                        gameBox.getChildren().clear();
                        gameManager.beginGame();
                });
                gameBox.getChildren().add(LetsPlayButton);
        });
    }



    /**
     * Private inner class for letting Players selecting their plane icon colors.
     */
    private static class PlaneColorChoices {
        final Image[] planes = {
            new Image(new FileInputStream("img_planes/red.png")),
            new Image(new FileInputStream("img_planes/blue.png")),
            new Image(new FileInputStream("img_planes/yellow.png")),
            new Image(new FileInputStream("img_planes/white.png"))
        };

        private int index = 0;

        private PlaneColorChoices() throws FileNotFoundException {
        }

        Image next() {
            if (++index == 4)
                index = 0;
            return planes[index];
        }

        Image getIcon() {
            return planes[index];
        }
    }

    /**
     * launches Application
     *
     * @param args args
     */
    public static void main(String[] args) {
        launch(args);
    }

}

class MoneyFormat {
    public static String format(double amount) {
        return String.format("$%.2fM", amount);
    }
}