package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * GameManager class managing number of players, turns, and the display on the right of the
 * game board
 */
class GameManager {
    ArrayList<Player> players;
    int turn;
    VBox gameBox;
    GridPane playerGridPane;

    boolean rollingPhase;

    ImageView die1, die2;
    Button rollButton;
    boolean gotDouble;
    HashMap<Integer, Image> dice;

    final Timeline rollingDiceAnimation = new Timeline();


    final Random random = new Random();

    public GameManager(VBox vBox, GridPane playerGridPane) throws FileNotFoundException {
        gameBox = vBox;
        this.playerGridPane = playerGridPane;

        dice = new HashMap<>();
        for (int i = 1; i <= 6; i++) {
            dice.put(i, new Image(new FileInputStream("img_dice/" + i + ".png")));
        }

        die1 = new ImageView(dice.get(random.nextInt(6) + 1));
        die1.setFitWidth(64);
        die1.setFitHeight(64);
        GridPane.setHalignment(die1, HPos.CENTER);
        GridPane.setValignment(die1, VPos.CENTER);
        GridPane.setConstraints(die1, 5, 5);
        die1.setFitWidth(playerGridPane.getColumnConstraints().get(5).getPrefWidth());
        die1.setFitHeight(playerGridPane.getRowConstraints().get(5).getPrefHeight());

        die2 = new ImageView(dice.get(random.nextInt(6) + 1));
        die2.setFitWidth(64);
        die2.setFitHeight(64);
        GridPane.setHalignment(die2, HPos.CENTER);
        GridPane.setValignment(die2, VPos.CENTER);
        GridPane.setConstraints(die2, 7, 5);
        die2.setFitWidth(playerGridPane.getColumnConstraints().get(7).getPrefWidth());
        die2.setFitHeight(playerGridPane.getRowConstraints().get(5).getPrefHeight());

        rollingDiceAnimation.setCycleCount(32);
        rollingDiceAnimation.getKeyFrames().add(new KeyFrame(
            Duration.millis(16),
            actionEvent -> {
                    die1.setImage(dice.get(random.nextInt(6) + 1));
                    die2.setImage(dice.get(random.nextInt(6) + 1));
            }
            ));

        rollingDiceAnimation.setOnFinished(onFinishedEvent -> {
                int dest = players.get(turn).move(rollDice());
//                rollingPhase = false;
                System.out.println(dest);

                players.get(turn).moveAnimation.setOnFinished(actionEvent -> {
                        // sdfjs
                });
        });


    }

    /**
     * Places Players' planes on board, sets up dice, and begins the game.
     * The first Player to go is chosen randomly.
     */
    public void beginGame() {
        for (int i = players.size() - 1; i >= 0; i--) {
            ImageView plane = players.get(i).getPlane();
            plane.setFitHeight(32);
            plane.setFitWidth(32);
            GridPane.setConstraints(plane, 11, 11);

            playerGridPane.getChildren().add(plane);
            GridPane.setHalignment(plane, HPos.CENTER);

            players.get(i).getPlane().setOnMouseClicked(mouseEvent -> {});
        }

        rollingPhase = true;
        rollButton = new Button("ROLL");
        rollButton.setOnAction(actionEvent -> {
                if (rollingPhase) {
                    rollingDiceAnimation.playFromStart();
                }
        });
        GridPane.setHalignment(rollButton, HPos.CENTER);
        GridPane.setValignment(rollButton, VPos.CENTER);
        GridPane.setConstraints(rollButton, 6, 6);

        playerGridPane.getChildren().addAll(die1, die2, rollButton);

        turn = random.nextInt(players.size());
    }

    /**
     * Switches turn to the next Player
     * @return the next Player
     */
    public Player nextTurn() {
        if (gotDouble) {
            gotDouble = false;
        }
        else if (++turn == players.size()) {
            turn = 0;
        }
        rollingPhase = true;
        return players.get(turn);
    }

    /**
     * After playing the animation of rolling the dice, set each die to a random value and return the sum
     * of the two dice's values.
     * @return total value of dice
     */
    public int rollDice() {
        int die1Value = random.nextInt(6) + 1, die2Value = random.nextInt(6) + 1;
        gotDouble = die1Value == die2Value;
        die1.setImage(dice.get(die1Value));
        die2.setImage(dice.get(die2Value));
        return die1Value + die2Value;
    }



    /**
     * Remove Player from the game; if 1 Player is remaining, Game Over.
     * @param loser Player eliminated
     * @param shark Player to whom the eliminated owes debt; NULL if owed to the Banker.
     */
    public void eliminate(Player loser, Player shark) {
        if (shark == null) {
            for (Property property : loser.getProperties()) {
                property.owner = null;
                if (property instanceof RegularProperty) {
                    property.rent = ((RegularProperty) property).getRents()[0];
                    ((RegularProperty) property).deconstruct();
                }
            }
        } else {
            shark.takeOver(loser, loser.getProperties());
            shark.changeMoney(loser.getMoney());
        }
        players.remove(loser);

        if (players.size() == 1)
            gameOver();
    }

    /**
     * Closing the game by declaring the last Player remaining as the winner.
     */
    public void gameOver() {
        gameBox.getChildren().clear();

        /* CONGRATULATIONS, (Winner). \n YOU ARE THE WINNER! */
    }
}