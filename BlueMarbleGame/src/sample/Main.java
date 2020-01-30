package sample;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
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

        AudioClip startup = new AudioClip(Paths.get("sounds/logo.wav").toUri().toString());
        startup.play(.5);

        // Installs board to the left of the application
        Image board = new Image(new FileInputStream("bluemarbleboard.png"));
        ImageView boardIV = new ImageView(board);
        boardIV.setFitWidth(primaryStage.getScene().getHeight());
        boardIV.setFitHeight(primaryStage.getScene().getHeight());
        boardStackPane.getChildren().add(boardIV);

        /*  Sets up playerGridPane to have a row and column for every space.
            (Row 0 and Column 0 represent the white frame around the board)
            GO SPACE at Column 13, Row 13, CAIRO at Column 3, Row 13, etc.

            The four final values below are the pixel lengths of the frame, corners, and spaces of the board
            expressed as fractions of the entire board.
         */

        final double FRAME_RATIO = 40 / 1654.0;
        final double CORNER_RATIO = 178 / 1654.0;
        final double SPACE_RATIO = 124.01 / 1654.0;
        final double BUILDING_AREA_RATIO = 44 / 1654.0;
        // Row 0 & Column 0 represent the white frame around the board
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(FRAME_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(FRAME_RATIO * boardIV.getFitHeight()));
        // Row 1 & Column 1 are wider, as seen in the board
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(CORNER_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(CORNER_RATIO * boardIV.getFitHeight()));
        // Row 2 & Column 2 represent the little rectangles on top of Property spaces for buildings
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(BUILDING_AREA_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(BUILDING_AREA_RATIO * boardIV.getFitHeight()));
        // Rows & Columns 3 - 11
        for (int i = 3; i <= 11; i++) {
            playerGridPane.getColumnConstraints().add(new ColumnConstraints(SPACE_RATIO * boardIV.getFitWidth()));
            playerGridPane.getRowConstraints().add(new RowConstraints(SPACE_RATIO * boardIV.getFitHeight()));
        }
        // Row 12 & Column 12 same as Row 2 & Column 2
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(BUILDING_AREA_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(BUILDING_AREA_RATIO * boardIV.getFitHeight()));
        // Row 13 & Column 13 same as Row 1 & Column 1
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(CORNER_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(CORNER_RATIO * boardIV.getFitHeight()));
        // Row 14 & Column 14 same as Row 0 & Column 0
        playerGridPane.getColumnConstraints().add(new ColumnConstraints(FRAME_RATIO * boardIV.getFitWidth()));
        playerGridPane.getRowConstraints().add(new RowConstraints(FRAME_RATIO * boardIV.getFitHeight()));

        boardStackPane.getChildren().add(playerGridPane);

        // Create gameBox to the right of the board
        // Prompt for number of players on the right of the application
        ComboBox<String> numPlayersComboBox = new ComboBox<>();
        numPlayersComboBox.setPromptText("How many players?");
        numPlayersComboBox.getItems().addAll("2 PLAYERS", "3 PLAYERS", "4 PLAYERS");
        numPlayersComboBox.setOnMouseEntered(mouseEvent -> AudioClips.buttonAudioClips[4].play(.5));
        numPlayersComboBox.setOnHidden(event -> AudioClips.buttonAudioClips[1].play(.5));

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
        playButton.setOnMouseEntered(mouseEvent -> AudioClips.buttonAudioClips[3].play(.5));
        playButton.setOnAction(actionEvent -> {
            try {
                int numPlayers = Integer.parseInt(numPlayersComboBox.getValue().substring(0, 1));
                AudioClips.buttonAudioClips[0].play(.5);
                gameManager.players = new ArrayList<>(numPlayers);
                gameBox.getChildren().clear();

                VBox[] playerSetup = new VBox[numPlayers];
                for (int i = 0; i < numPlayers; i++) {
                    gameManager.players.add(new Player(i + 1));
                    Label nameLabel = new Label("Player " + (i + 1) + " Name: ");
                    TextField input = new TextField("Player " + (i + 1));
                    HBox nameInput = new HBox(nameLabel, input);
                    final ComboBox<String> planeColorChoices = new ComboBox<>();
                    planeColorChoices.getItems().addAll("RED", "BLUE", "YELLOW", "WHITE");
                    planeColorChoices.setOnHidden(event -> AudioClips.buttonAudioClips[1].play(.5));
                    planeColorChoices.setOnMouseEntered(mouseEvent -> AudioClips.buttonAudioClips[4].play(.5));

                    Label chooseLabel = new Label("Choose plane color:  ");
                    HBox planeColor = new HBox();

                    planeColor.getChildren().addAll(chooseLabel, planeColorChoices);


                    playerSetup[i] = new VBox(nameInput, planeColor);
                    playerSetup[i].setSpacing(15);
                    gameBox.getChildren().add(playerSetup[i]);
                }
                gameBox.setSpacing(30);

                Button LetsPlayButton = new Button("LET'S PLAY!");
                HashSet<String> colors = new HashSet<>();
                LetsPlayButton.setOnMouseEntered(mouseEvent -> AudioClips.buttonAudioClips[3].play(.5));
                LetsPlayButton.setOnAction(actionEvent1 -> {
                    colors.clear();
                    for (int i = 0; i < gameManager.players.size(); i++) {
                        Player player = gameManager.players.get(i);
                        player.setName(
                            ((TextField) (((HBox) playerSetup[i].getChildren().get(0)).getChildren().get(1)))
                                .getText());
                        String colorChoice = ((ComboBox<String>)
                            ((HBox) playerSetup[i].getChildren().get(1)).getChildren().get(1)).getValue();
                        if (!colors.add(colorChoice)) {
                            Label repeatedColorLabel = new Label("A plane color cannot be repeated. " +
                                "Please select again.");
                            AudioClips.buttonAudioClips[6].play(.5);
                            repeatedColorLabel.setTextFill(Color.RED);
                            gameBox.getChildren().add(repeatedColorLabel);
                            return;
                        }
                        AudioClips.buttonAudioClips[0].play(.5);
                        try {
                            switch (colorChoice) {
                                case "RED":
                                    player.setPlane(new ImageView(new Image(
                                        new FileInputStream("img_planes/red.png"))));
                                    player.setPlayerColor(Color.RED);
                                    break;
                                case "BLUE":
                                    player.setPlane(new ImageView(new Image(
                                        new FileInputStream("img_planes/blue.png"))));
                                    player.setPlayerColor(Color.DODGERBLUE);
                                    break;
                                case "YELLOW":
                                    player.setPlane(new ImageView(new Image(
                                        new FileInputStream("img_planes/yellow.png"))));
                                    player.setPlayerColor(Color.GOLDENROD);
                                    break;
                                case "WHITE":
                                    player.setPlane(new ImageView(new Image(
                                        new FileInputStream("img_planes/white.png"))));
                                    player.setPlayerColor(Color.BLACK);
                                    break;
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    gameBox.getChildren().clear();
                    try {
                        gameManager.beginGame();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
                gameBox.getChildren().add(LetsPlayButton);
            } catch (Exception noSelectionIGuess) {
                AudioClips.buttonAudioClips[6].play(.5);
            }
        });
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