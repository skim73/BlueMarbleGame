package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

import static sample.FillSpaceToPropertyMap.fill;

/**
 * GameManager class managing number of players, turns, and the display on the right of the
 * game board
 */
class GameManager {
    ArrayList<Player> players;
    int turn;
    VBox gameBox;
    GridPane playerGridPane;

    Label playerTurnLabel;

    boolean rollingPhase;

    ImageView die1, die2;
    Button rollButton;
    boolean gotDouble;
    HashMap<Integer, Image> dice;

    final Timeline rollingDiceAnimation = new Timeline();
    final Random random = new Random();

    double welfare;
    Label welfareText;

    boolean freeSpaceStation;
    boolean worldTour;

    static Stage popup;
    final HashMap<Integer, Property> spaceToProperty;
    final LinkedList<GoldenKey> goldenKeys;

    public GameManager(VBox vBox, GridPane playerGridPane) throws FileNotFoundException {
        gameBox = vBox;
        this.playerGridPane = playerGridPane;

        playerTurnLabel = new Label();
        playerTurnLabel.setFont(new Font("Arial Black", 30));

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
            int moveSpaces = rollDice();
            try {
                players.get(turn).move(moveSpaces, gotDouble);
                rollingPhase = false;
            } catch (DesertedIslandException e) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("You're still stuck in the Deserted Island");
                alert.setHeaderText("You failed to escape the Deserted Island!");
                alert.setContentText("You are stuck here for the next " +
                    players.get(turn).getTurnsLeftOnDesertedIsland() + " turn(s).");
                alert.show();
                alert.setOnHidden(dialogEvent -> playerTurnLabel.setText(nextTurn().getName() + "'s turn."));
            }

        });

        welfareText = new Label(MoneyFormat.format(welfare));
        GridPane.setConstraints(welfareText, 1, 0);
        GridPane.setHalignment(welfareText, HPos.CENTER);
        GridPane.setValignment(welfareText, VPos.CENTER);
        playerGridPane.getChildren().add(welfareText);

        goldenKeys = new LinkedList<>();
        for (int i = 0; i < 29; i++) {
            goldenKeys.add(new GoldenKey(new Image(new FileInputStream(
                "img_goldenkeys/goldenkey" + i + ".jpg")), i));
        }
        Collections.shuffle(goldenKeys);

        popup = new Stage();

        spaceToProperty = new HashMap<>();
        fill(spaceToProperty);
    }

    /**
     * Places Players' planes on board, sets up dice, and begins the game.
     * The first Player to go is chosen randomly.
     */
    public void beginGame() {
        turn = random.nextInt(players.size());
        for (int i = turn + players.size() - 1; i >= turn; i--) {
            ImageView plane = players.get(i % players.size()).getPlane();
            plane.setFitHeight(32);
            plane.setFitWidth(32);
            GridPane.setConstraints(plane, 11, 11);

            playerGridPane.getChildren().add(plane);
            GridPane.setHalignment(plane, HPos.CENTER);
        }

        rollingPhase = true;
        rollButton = new Button("ROLL");
        rollButton.setOnAction(actionEvent -> {
            if (players.get(turn).isSpaceStation()) {
                moveFromSpaceStation();
            }
            if (players.get(turn).getTurnsLeftOnDesertedIsland() > 0 &&
                players.get(turn).hasEscapeDesertedIsland()) {

                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Escape Deserted Island?");
                alert.setHeaderText("You have \"ESCAPE DESERTED ISLAND\" Golden Key.");
                alert.setContentText("Will you use the \"ESCAPE DESERTED ISLAND\" Golden Key?");
                Optional<ButtonType> option = alert.showAndWait();
                if (option.isPresent() && option.get() == alert.getButtonTypes().get(0)) {
                    players.get(turn).useEscapeDesertedIsland();
                }
            }
            if (rollingPhase) {
                rollingDiceAnimation.playFromStart();
            }
        });

        for (Player player : players) {
            player.moveAnimation.setOnFinished(actionEvent -> {
                player.moveAnimation.setRate(1);
                showPopup(players.get(turn).getSpace());
            });
        }

        popup.setOnHidden(windowEvent -> {
            playerTurnLabel.setText(nextTurn().getName() + "'s turn.");
        });

        GridPane.setHalignment(rollButton, HPos.CENTER);
        GridPane.setValignment(rollButton, VPos.CENTER);
        GridPane.setConstraints(rollButton, 6, 6);

        playerGridPane.getChildren().addAll(die1, die2, rollButton);
        playerTurnLabel.setText(nextTurn().getName() + "'s turn.");


        gameBox.getChildren().add(playerTurnLabel);
        for (Player player : players) {
            Label name = new Label(String.format("%-24s", player.getName()));
            HBox hBox = new HBox(name, player.getMoneyText(), player.getPropertiesComboBox());
            hBox.setSpacing(64);
            hBox.setBorder(new Border(new BorderStroke(
                player.getPlayerColor().equals(Color.WHITE) ? Color.BLACK : player.getPlayerColor(),
                BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderWidths.DEFAULT)));
            gameBox.getChildren().add(hBox);
        }

        for (Property property : spaceToProperty.values()) {
            if (property instanceof RegularProperty) {
                GridPane.setConstraints(((RegularProperty) property).buildingPics,
                    Player.spaceToGrid[property.space][0], Player.spaceToGrid[property.space][1]);
                playerGridPane.getChildren().add(((RegularProperty) property).buildingPics);
            }
        }

    }

    /**
     * Private method called in the case where the Player freely chooses their destination from
     * Space Station
     */
    private void moveFromSpaceStation() {
        rollingPhase = false;
        Stage popup = new Stage();
        Label text = new Label("You are on Space Station." +
            "\nTo which space will you go?");
        ComboBox<String> comboBox = new ComboBox<>();
        try {
            Scanner filescan = new Scanner(new File("SpaceIndices.txt"));
            while (filescan.hasNext()) {
                comboBox.getItems().add(filescan.nextLine());
            }
            filescan.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Button moveButton = new Button("MOVE!");
        VBox popupVBox = new VBox(text, comboBox, moveButton);
        popupVBox.setAlignment(Pos.CENTER);
        popup.setScene(new Scene(popupVBox, 300, 200));
        moveButton.setOnAction(actionEvent1 -> {
            if (comboBox.getValue().equals("30: Space Station")) {
                Label uhNoLabel = new Label("No. You're not coming back here.");
                uhNoLabel.setTextFill(Color.RED);
                popupVBox.getChildren().add(uhNoLabel);
                return;
            }
            String dest = comboBox.getValue();
            int destination = Integer.parseInt(dest.substring(0, dest.indexOf(":")));
            players.get(turn).directlyMove(destination, true);
            popup.close();
        });
        popup.showAndWait();
    }

    /**
     * Switches turn to the next Player; if double, the same Player goes again
     *
     * @return the next Player
     */
    public Player nextTurn() {
        if (gotDouble) {
            gotDouble = false;
        } else if (++turn == players.size()) {
            turn = 0;
        }
        rollingPhase = true;
        players.get(turn).getPlane().toFront();


        return players.get(turn);
    }

    /**
     * After playing the animation of rolling the dice, set each die to a random value and return the sum
     * of the two dice's values.
     *
     * @return total value of dice
     */
    public int rollDice() {
        int die1Value = random.nextInt(6) + 1, die2Value = random.nextInt(6) + 1;
        gotDouble = die1Value == die2Value;
        die1.setImage(dice.get(die1Value));
        die2.setImage(dice.get(die2Value));
        return die1Value + die2Value;
    }

    public void showPopup(int space) {
        if (worldTour) {
            worldTour = false;
            playerTurnLabel.setText(nextTurn().getName() + "'s turn.");
            return;
        }
        BorderPane popupBorderPane = new BorderPane();
        ImageView imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setFitHeight(375);
        Button b0 = new Button("OK");
        b0.setOnAction(actionEvent -> popup.close());
        Label topLabel = new Label();
        topLabel.setFont(new Font("Arial Black", 20));
        topLabel.setAlignment(Pos.CENTER);
        VBox rightVBox = new VBox(b0);
        rightVBox.setSpacing(16);
        rightVBox.setAlignment(Pos.CENTER);
        HBox rightHBox = new HBox(rightVBox);
        rightHBox.setSpacing(16);
        rightHBox.getChildren().add(0, new Rectangle(32, 32));
        rightHBox.getChildren().add(new Rectangle(32, 32));
        ((Rectangle)rightHBox.getChildren().get(0)).setFill(Color.GHOSTWHITE);
        ((Rectangle)rightHBox.getChildren().get(2)).setFill(Color.GHOSTWHITE);

        popup.setOnHidden(windowEvent -> {
            playerTurnLabel.setText(nextTurn().getName() + "'s turn.");
        });


        popupBorderPane.setLeft(imageView);
        popupBorderPane.setTop(topLabel);
        popupBorderPane.setRight(rightHBox);

        try {
            switch (space) {
                case 0:
                    imageView.setImage(new Image(new FileInputStream("img_properties/0.png")));
                    topLabel.setText("GO space: You got $2.00M paycheck!");
                    b0.setText("PAY DAY!");
                    b0.setOnAction(actionEvent -> popup.close());
                    break;
                case 10:
                    imageView.setImage(new Image(new FileInputStream("img_properties/10.png")));
                    topLabel.setText("You are stuck in the Deserted Island for 3 turns.");
                    b0.setText("SOS!");
                    b0.setOnAction(actionEvent -> {
                        gotDouble = false;
                        players.get(turn).landOnDesertedIsland();
                        popup.close();
                    });
                    break;
                case 20:
                    imageView.setImage(new Image(new FileInputStream("img_properties/20.png")));
                    topLabel.setText("Welfare Zone has " + MoneyFormat.format(welfare) + " for you!");
                    b0.setText("Thank you, welfare!");
                    b0.setOnAction(actionEvent -> {
                        players.get(turn).changeMoney(welfare);
                        popup.close();
                    });
                    welfare = 0;
                    welfareText.setText(MoneyFormat.format(welfare));
                    break;
                case 30:
                    if (freeSpaceStation) {
                        players.get(turn).enterSpaceStation();
                        GridPane.setConstraints(players.get(turn).getPlane(), 9, 3);
                        freeSpaceStation = false;
                        playerTurnLabel.setText(nextTurn().getName() + "'s turn.");
                        return;
                    }
                    imageView.setImage(new Image(new FileInputStream("img_properties/30.png")));
                    topLabel.setText("Would you like to enter Space Station?");
                    b0.setText("YES (Pay $2.00M to " + (spaceToProperty.get(32).owner == null ?
                        "BANKER" : spaceToProperty.get(32).owner.getName()) + ")");
                    b0.setOnAction(actionEvent -> {
                        players.get(turn).payOther(spaceToProperty.get(32).owner, ColumbiaSpaceShuttle.ENTRY_FEE);
                        players.get(turn).enterSpaceStation();
                        GridPane.setConstraints(players.get(turn).getPlane(), 9, 3);
                        popup.close();
                    });
                    Button b1 = new Button("No Thanks.");
                    b1.setOnAction(actionEvent -> popup.close());
                    rightVBox.getChildren().add(b1);
                    break;
                case 38:
                    imageView.setImage(new Image(new FileInputStream("img_properties/38.png")));
                    topLabel.setText("Please donate $1.50M to Welfare Zone. Thank you for\n" +
                        "your contributions!");
                    b0.setText("Donate $1.50M");
                    b0.setOnAction(actionEvent -> {
                        players.get(turn).payOther(null, 1.50);
                        welfare += 1.50;
                        welfareText.setText(MoneyFormat.format(welfare));
                        popup.close();
                    });
                    break;
                case 2:
                case 7:
                case 12:
                case 17:
                case 22:
                case 35:
                    GoldenKey goldenKey = drawGoldenKeyCard();
                    imageView.setImage(goldenKey.card);
                    topLabel.setText("GOLDEN KEY CARD");
                    b0.setOnAction(actionEvent -> {
                        switch (goldenKey.id) {
                            case 0:
                                for (Player opponent : players) {
                                    if (opponent == players.get(turn))
                                        continue;
                                    opponent.payOther(players.get(turn), .05);
                                }
                                break;
                            case 1:
                            case 8:
                                players.get(turn).changeMoney(-.50);
                                break;
                            case 3:
                                players.get(turn).changeMoney(2.00);
                                break;
                            case 4:
                                players.get(turn).receiveEscapeDesertedIsland();
                                break;
                            case 5:
                                players.get(turn).landOnDesertedIsland();
                                gotDouble = false;
                                break;
                            case 6:
                                players.get(turn).directlyMove(25, false);
                                break;
                            case 7:
                                players.get(turn).directlyMove(5, false);
                                break;
                            case 9:
                                players.get(turn).changeMoney(-1.00);
                                break;
                            case 10:
                                players.get(turn).changeMoney(.50);
                                break;
                            case 11:
                                players.get(turn).move(-2, false);
                                GameManager.popup.setOnHidden(windowEvent -> {});
                                break;
                            case 12:
                                players.get(turn).move(-3, false);
                                GameManager.popup.setOnHidden(windowEvent -> {});
                                break;
                            case 13:
                                players.get(turn).directlyMove(0, false);
                                break;
                            case 14:
                            case 15:
                                players.get(turn).receiveComplimentaryTicket();
                                break;
                            case 16:
                                if (spaceToProperty.get(15).owner != null)
                                    players.get(turn).payOther(spaceToProperty.get(15).owner, 3.00);
                                players.get(turn).directlyMove(1, false);
                                break;
                            case 17:
                                double total = 0;
                                for (Property property : players.get(turn).getProperties()) {
                                    if (property instanceof RegularProperty) {
                                        total += .10 * ((RegularProperty) property).getBuildings()[0] +
                                                 .30 * ((RegularProperty) property).getBuildings()[1] +
                                                 .50 * ((RegularProperty) property).getBuildings()[2];
                                    }
                                }
                                players.get(turn).changeMoney(-total);
                                break;
                            case 19:
                                worldTour = true;
                                players.get(turn).moveAnimation.setRate(8);
                                players.get(turn).move(40, false);
                                players.get(turn).changeMoney(welfare);
                                welfare = 0;
                                return;
                            case 20:
                                double total1 = 0;
                                for (Property property : players.get(turn).getProperties()) {
                                    if (property instanceof RegularProperty) {
                                        total1 += .30 * ((RegularProperty) property).getBuildings()[0] +
                                            .60 * ((RegularProperty) property).getBuildings()[1] +
                                            1.00 * ((RegularProperty) property).getBuildings()[2];
                                    }
                                }
                                players.get(turn).changeMoney(-total1);
                                break;
                            case 21:
                                players.get(turn).changeMoney(3.00);
                                break;
                            case 22:
                            case 23:
                                if (players.get(turn).getProperties().isEmpty())
                                    break;
                                Property mostExpensive = players.get(turn).getProperties().get(0);
                                for (int i = 1; i < players.get(turn).getProperties().size(); i++) {
                                    if (mostExpensive.price < players.get(turn).getProperties().get(i).price)
                                        mostExpensive = players.get(turn).getProperties().get(i);
                                }
                                players.get(turn).sell(mostExpensive);
                                break;
                            case 24:
                                players.get(turn).directlyMove(39, false);
                                break;
                            case 25:
                                double total2 = 0;
                                for (Property property : players.get(turn).getProperties()) {
                                    if (property instanceof RegularProperty) {
                                        total2 += 1.50 * ((RegularProperty) property).getBuildings()[2] +
                                            1.00 * ((RegularProperty) property).getBuildings()[1] +
                                            .50 * ((RegularProperty) property).getBuildings()[0];
                                    }
                                }
                                players.get(turn).changeMoney(-total2);
                                break;
                            case 26:
                                players.get(turn).directlyMove(20, false);
                                players.get(turn).changeMoney(welfare);
                                welfare = 0;
                                break;
                            case 27:
                            case 28:
                                freeSpaceStation = true;
                                players.get(turn).directlyMove(30, false);
                                players.get(turn).enterSpaceStation();
                                GridPane.setConstraints(players.get(turn).getPlane(), 9, 3);
                                break;
                        }
                        popup.close();
                    });
                    break;
                default:
                    Property property = spaceToProperty.get(space);
                    imageView.setImage(property.propertyCard);
                    topLabel.setText((property.owner == null) ? "UNOWNED PROPERTY" :
                        "OWNED BY: " + property.owner.getName());

                    Label message = new Label();
                    rightVBox.getChildren().add(0, message);
                    if (property.owner == null) {
                        message.setText("Would you like to purchase this property?");
                        b0.setText("YES (for " + MoneyFormat.format(property.price) + ")");
                        b0.setOnAction(actionEvent -> {
                            players.get(turn).purchase(property);
                            popup.close();
                        });
                        Button b2 = new Button("NO");
                        b2.setOnAction(actionEvent -> popup.close());
                        rightVBox.getChildren().add(b2);
                    } else if (property.owner != players.get(turn)) {
                        message.setText("You owe rent to " + property.owner.getName() + "!");
                        b0.setText("Pay rent (" + MoneyFormat.format(property.rent) + ")");
                        b0.setOnAction(actionEvent -> {
                            players.get(turn).payOther(property.owner, property.rent);
                            popup.close();
                        });
                        if (players.get(turn).getComplimentaryTickets() > 0) {
                            Button b6 = new Button("...or use Complimentary Ticket!");
                            b6.setOnAction(actionEvent -> {
                                players.get(turn).useComplimentaryTicket();
                                popup.close();
                            });
                            rightVBox.getChildren().add(b6);
                        }
                    } else {
                        if (property instanceof RegularProperty) {
                            message.setText("Set the number of buildings at this property.\n" +
                                "Construct/sell each building type");

                            int[] queries = ((RegularProperty) property).getBuildings().clone();
                            Label[] queriesLabels = {new Label("0"), new Label("0"), new Label("0")};
                            for (int i = 0; i < 3; i++) {
                                queriesLabels[i].setFont(new Font("Arial Black", 26));
                                queriesLabels[i].setText(queries[i] + "");
                            }

                            Button minusButtonHouse = new Button("-");
                            Button plusButtonHouse = new Button("+");
                            Button minusButtonOfficeBuilding = new Button("-");
                            Button plusButtonOfficeBuilding = new Button("+");
                            Button minusButtonHotel = new Button("-");
                            Button plusButtonHotel = new Button("+");

                            minusButtonHouse.setOnAction(actionEvent -> {
                                if (--queries[0] == 0)
                                    minusButtonHouse.setVisible(false);
                                plusButtonHouse.setVisible(true);
                                queriesLabels[0].setText(queries[0] + "");
                            });
                            plusButtonHouse.setOnAction(actionEvent -> {
                                if (++queries[0] == 2)
                                    plusButtonHouse.setVisible(false);
                                minusButtonHouse.setVisible(true);
                                queriesLabels[0].setText(queries[0] + "");
                            });
                            minusButtonOfficeBuilding.setOnAction(actionEvent -> {
                                if (--queries[1] == 0)
                                    minusButtonOfficeBuilding.setVisible(false);
                                plusButtonOfficeBuilding.setVisible(true);
                                queriesLabels[1].setText(queries[1] + "");
                            });
                            plusButtonOfficeBuilding.setOnAction(actionEvent -> {
                                if (++queries[1] == 2)
                                    plusButtonOfficeBuilding.setVisible(false);
                                minusButtonOfficeBuilding.setVisible(true);
                                queriesLabels[1].setText(queries[1] + "");
                            });
                            minusButtonHotel.setOnAction(actionEvent -> {
                                if (--queries[2] == 0)
                                    minusButtonHotel.setVisible(false);
                                plusButtonHotel.setVisible(true);
                                queriesLabels[2].setText(queries[2] + "");
                            });
                            plusButtonHotel.setOnAction(actionEvent -> {
                                if (++queries[2] == 2)
                                    plusButtonHotel.setVisible(false);
                                minusButtonHotel.setVisible(true);
                                queriesLabels[2].setText(queries[2] + "");
                            });


                            HBox houseQuery = new HBox(minusButtonHouse, queriesLabels[0], plusButtonHouse);
                            HBox officeBuildingQuery = new HBox(minusButtonOfficeBuilding, queriesLabels[1],
                                plusButtonOfficeBuilding);
                            HBox hotelQuery = new HBox(minusButtonHotel, queriesLabels[2], plusButtonHotel);
                            houseQuery.setAlignment(Pos.CENTER);
                            houseQuery.setSpacing(20);
                            officeBuildingQuery.setAlignment(Pos.CENTER);
                            officeBuildingQuery.setSpacing(20);
                            hotelQuery.setAlignment(Pos.CENTER);
                            hotelQuery.setSpacing(20);

                            b0.setText("Construct/Sell");
                            b0.setOnAction(actionEvent -> {
                                players.get(turn).build(property, queries);
                                popup.close();
                            });

                            if (((RegularProperty) property).getBuildings()[0] == 0)
                                houseQuery.getChildren().get(0).setVisible(false);
                            else if (((RegularProperty) property).getBuildings()[0] == 2)
                                houseQuery.getChildren().get(2).setVisible(false);

                            if (((RegularProperty) property).getBuildings()[1] == 0)
                                officeBuildingQuery.getChildren().get(0).setVisible(false);
                            else if (((RegularProperty) property).getBuildings()[1] == 2)
                                officeBuildingQuery.getChildren().get(2).setVisible(false);

                            if (((RegularProperty) property).getBuildings()[2] == 0)
                                hotelQuery.getChildren().get(0).setVisible(false);
                            else if (((RegularProperty) property).getBuildings()[2] == 2)
                                hotelQuery.getChildren().get(2).setVisible(false);

                            Button b3 = new Button("No thanks.");
                            b3.setOnAction(actionEvent -> popup.close());

                            rightVBox.getChildren().setAll(message, houseQuery, officeBuildingQuery,
                                hotelQuery, b0, b3);

                        } else {
                            message.setText("You own this property.");
                            b0.setOnAction(actionEvent -> popup.close());
                        }
                    }

            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        popup.setScene(new Scene(popupBorderPane));
        popup.show();
    }


    /**
     * Draws the top card of the stack of Golden Keys and returns it to the bottom.
     *
     * @return the Golden Key card drawn
     */
    public GoldenKey drawGoldenKeyCard() {
        GoldenKey drawn = goldenKeys.removeFirst();
        goldenKeys.addLast(drawn);
        return drawn;
    }


    /**
     * Remove Player from the game; if 1 Player is remaining, Game Over.
     *
     * @param loser Player eliminated
     * @param shark Player to whom the eliminated owes debt; NULL if owed to the Banker.
     */
    public void eliminate(Player loser, Player shark) {
        if (shark == null) {
            for (Property property : loser.getProperties()) {
                property.owner = null;
                if (property instanceof RegularProperty) {
                    ((RegularProperty) property).deconstruct();
                }
            }
        } else {
            shark.takeOver(loser, loser.getProperties());
            shark.changeMoney(loser.getMoney());
        }
        gameBox.getChildren().remove(players.indexOf(loser) + 1);
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