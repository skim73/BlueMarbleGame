package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;

/**
 * Player class represents a player of the board game who has money, list of properties
 */
class Player {
    private String name;
    private ImageView plane;
    private Color playerColor;
    private double money;
    private Label moneyText;
    private int space;
    private ArrayList<Property> properties;
    private ComboBox<Property> propertiesComboBox;

    private int turnsLeftOnDesertedIsland;
    private int complimentaryTickets;
    private boolean escapeDesertedIsland;
    private boolean spaceStation;   // on GridPane Col 9, Row 3

    final Timeline moveAnimation = new Timeline();

    static final int[][] spaceToGrid = new int[40][2];

    /**
     * Player constructor.
     * Name is defaulted to "Player (#)".
     * Plane icon will be selected.
     * Player start with $30.00M
     * Player starts at the GO space (0th space from GO)
     *
     * @param playerNum Player 1, 2, 3, or 4
     */
    public Player(int playerNum) {
        this.name = "Player " + playerNum;
        this.plane = null;
        money = Math.random() < .5 ? 30.00 : 6.00;
        moneyText = new Label(MoneyFormat.format(money));
        moneyText.setFont(new Font("Arial Black", 16));
        moneyText.setTextFill(Color.FORESTGREEN);
        space = 0;
        properties = new ArrayList<>(29);
        propertiesComboBox = new ComboBox<>();
        propertiesComboBox.setPromptText("(Properties list)");

        spaceToGrid[0] = new int[]{13, 13};
        spaceToGrid[10] = new int[]{1, 13};
        spaceToGrid[20] = new int[]{1, 1};
        spaceToGrid[30] = new int[]{13, 1};
        for (int i = 1; i < 10; i++) {
            spaceToGrid[i] = new int[]{12 - i, 13};
            spaceToGrid[i + 10] = new int[]{1, 12 - i};
            spaceToGrid[i + 20] = new int[]{i + 2, 1};
            spaceToGrid[i + 30] = new int[]{13, i + 2};
        }
    }

    /**
     * Moves this Player by the specified number of spaces
     *
     * @param delta     number of spaces to move; if traveling via Space Station, the space index [0,39] of
     *                  the Player's destination
     * @param gotDouble whether a double was rolled or not
     * @return the new space index of Player (how many spaces from GO) (*** returned as soon as
     * the moving animation is called ***)
     * @throws DesertedIslandException thrown if Player is stuck in Deserted Island
     */
    public int move(int delta, boolean gotDouble) {
        if (gotDouble)
            turnsLeftOnDesertedIsland = 0;
        if (turnsLeftOnDesertedIsland > 0) {
            turnsLeftOnDesertedIsland--;
            throw new DesertedIslandException("SOS");
        }
        moveAnimation.setCycleCount(Math.abs(delta));
        if (delta > 0) {
            moveAnimation.getKeyFrames().setAll(new KeyFrame(
                Duration.millis(40),
                actionEvent -> {
                    if (++space == 40) {
                        space = 0;
                        System.out.print("PAY DAY! ");
                        changeMoney(2.00);
                    }
                    GridPane.setConstraints(plane, spaceToGrid[space][0], spaceToGrid[space][1]);
                }
            ));
        } else {
            // ONLY FROM "Moving Day" GOLDEN KEY CARDS
            moveAnimation.getKeyFrames().setAll(new KeyFrame(
                Duration.millis(300),
                actionEvent -> {
                    if (--space == -1) {
                        space = 39;
                    }
                    GridPane.setConstraints(plane, spaceToGrid[space][0], spaceToGrid[space][1]);
                }
            ));
        }
        moveAnimation.playFromStart();

        return (space + delta) % 40;
    }

    /**
     * Moves this Player to the specified space index
     *
     * @param destination      space index of destination
     * @param fromSpaceStation whether this Player is moving from Space Station or not
     */
    public void directlyMove(int destination, boolean fromSpaceStation) {
        if (destination < space) {
            destination += 40;
        }
        moveAnimation.setRate(4.5);
        move(destination - space, false);
        if (fromSpaceStation)
            spaceStation = false;

        GameManager.popup.setOnHidden(windowEvent -> {
        });
    }

    public void landOnDesertedIsland() {
        space = 10;
        GridPane.setConstraints(plane, spaceToGrid[10][0], spaceToGrid[10][1]);
        this.turnsLeftOnDesertedIsland = 3;
    }

    public void useEscapeDesertedIsland() {
        escapeDesertedIsland = false;
        turnsLeftOnDesertedIsland = 0;
    }

    public void enterSpaceStation() {
        this.spaceStation = true;
    }

    /**
     * Changes Player's money amount
     *
     * @param amount amount changed
     * @throws NotEnoughMoneyException thrown if Player attempts to pay more than their current balance
     */
    public void changeMoney(double amount) {
        if (amount < 0 && money < -amount)
            throw new NotEnoughMoneyException();
        money += amount;
        moneyText.setText(MoneyFormat.format(money));
        if (money < 6.00)
            moneyText.setTextFill(Color.CRIMSON);
        else {
            moneyText.setTextFill(Color.FORESTGREEN);
        }
    }

    /**
     * Player purchases a property if they can afford it.
     *
     * @param property the property to purchase
     */
    public void purchase(Property property) {
        try {
            changeMoney(-property.price);
            properties.add(property);
            propertiesComboBox.getItems().add(property);
            Collections.sort(properties);
            Collections.sort(propertiesComboBox.getItems());
            property.owner = this;
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You don't have enough money!");
            alert.setHeaderText(name + ", you can't afford this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
        }
    }

    /**
     * If a Player lands on their property, they can construct/sell buildings there.
     * <p>
     *
     * @param property    the property at which the new building is constructed
     * @param buildMatrix {# HOUSES to build/sell, # OFFICE BUILDINGS to build/sell, # HOTELS to build/sell}
     * @return false if NotEnoughMoneyException was caught during this method; true otherwise
     */
    public boolean build(RegularProperty property, int[] buildMatrix) {
        try {
            double finalPrice = property.getPrices()[0] +
                property.getPrices()[1] * buildMatrix[0] +
                property.getPrices()[2] * buildMatrix[1] +
                property.getPrices()[3] * buildMatrix[2];
            changeMoney(property.price - finalPrice);
            property.construct(buildMatrix);
            propertiesComboBox.getItems().set(propertiesComboBox.getItems().indexOf(property), property);
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You don't have enough money!");
            alert.setHeaderText(name + ", you can't afford to construct these buildings at this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
            return false;
        }
        return true;
    }

    /**
     * Player pays another by the specified amount
     *
     * @param other  An opponent to whom this Player pays
     * @param amount The amount paid
     */
    public void payOther(Player other, double amount) throws BankruptcyException {
        try {
            changeMoney(-amount);
            if (other != null) {
                other.changeMoney(amount);
            }
        } catch (NotEnoughMoneyException e) {
            if (openDebtWindow(other, amount - money) > 0) {
                throw new BankruptcyException(other);
            } else {
                changeMoney(-amount);
            }
        }
    }

    /**
     * Sells property to the specified player
     * @param sold      Property being sold
     * @param receiver  to whom the Property is being sold (if null, Banker)
     */
    public void sell(Property sold, Player receiver) {
        if (sold == null)
            return;
        sold.owner = receiver;
        properties.remove(sold);
        propertiesComboBox.getItems().remove(sold);
        if (receiver != null) {
            receiver.properties.add(sold);
            receiver.propertiesComboBox.getItems().add(sold);
            Collections.sort(receiver.properties);
            Collections.sort(receiver.propertiesComboBox.getItems());
        } else {
            if (sold instanceof RegularProperty)
                ((RegularProperty) sold).deconstruct();
        }
        Collections.sort(properties);
        Collections.sort(propertiesComboBox.getItems());
    }


    /**
     * Sets up and shows the window where the Player can handle their debt to another
     *
     * @param other the Player to whom the debtor (this Player) owes money
     * @param debt the amount of debt this Player owes to other
     * @return false if Player has successfully paid off all their debt; true if Player declared bankruptcy
     */
    public double openDebtWindow(Player other, double debt) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("You are on debt!");
        alert.setHeaderText(name + ", you can't pay your bills to " +
            (other == null ? "BANKER" : other.getName()) + "!");
        alert.setContentText("You are " + MoneyFormat.format(debt) + " short!" +
            "\nYou must sell some of your properties to " + (other == null ? "BANKER" : other.getName()) + "!");
        alert.showAndWait();

        final double[] debtArray = {debt};
        Stage debtWindow = new Stage();
        BorderPane pane = new BorderPane();
        Label promptLabel = new Label("You must pay off your debt to " +
            (other == null ? "BANKER" : other.getName()) + "\nbefore closing this window.");
        Label warningLabel = new Label("If you close this window without paying all your debt, YOU ARE " +
            "DECLARING BANKRUPTCY.");
        warningLabel.setTextFill(Color.DEEPPINK);
        Label debtText = new Label("CURRENT DEBT: " + MoneyFormat.format(debt));
        debtText.setTextFill(Color.CRIMSON);
        debtText.setFont(new Font("Arial Black", 20));
        VBox topLabels = new VBox(promptLabel, warningLabel, debtText);
        topLabels.setAlignment(Pos.CENTER);
        topLabels.setSpacing(32);
        pane.setTop(topLabels);

        Label yourPropertiesLabel = new Label("Select properties to sell to " +
            (other == null ? "BANKER" : other.getName()));
        ListView<Property> properties = new ListView<>();
        properties.getItems().addAll(this.properties);

        Button sellButton = new Button("SELL");


        sellButton.setOnAction(actionEvent -> {
            Property sold = properties.getSelectionModel().getSelectedItem();
            if (sold == null)
                return;
            if (sold instanceof RegularProperty &&
                !Arrays.equals(((RegularProperty) sold).getBuildings(), new int[] {0,0,0})) {

                Stage sellBuildingStage = new Stage();
                Label prompt = new Label("Do you want to sell buildings to the BANKER first?");
                int[] queries = new int[3];
                Label[] queriesLabels = {new Label("0"), new Label("0"), new Label("0")};
                for (int i = 0; i < 3; i++) {
                    queriesLabels[i].setFont(new Font("Arial Black", 26));
                    queriesLabels[i].setText(queries[i] + "");
                }
                Button b0 = new Button("Sell buildings");

                Button minusButtonHouse = new Button("-");
                Button plusButtonHouse = new Button("+");
                Button minusButtonOfficeBuilding = new Button("-");
                Button plusButtonOfficeBuilding = new Button("+");
                Button minusButtonHotel = new Button("-");
                Button plusButtonHotel = new Button("+");

                minusButtonHouse.setOnAction(actionEvent1 -> {
                    if (--queries[0] == 0)
                        minusButtonHouse.setVisible(false);
                    plusButtonHouse.setVisible(true);
                    queriesLabels[0].setText(queries[0] + "");
                });
                plusButtonHouse.setOnAction(actionEvent1 -> {
                    if (++queries[0] == ((RegularProperty) sold).getBuildings()[0])
                        plusButtonHouse.setVisible(false);
                    minusButtonHouse.setVisible(true);
                    queriesLabels[0].setText(queries[0] + "");
                });
                minusButtonOfficeBuilding.setOnAction(actionEvent1 -> {
                    if (--queries[1] == 0)
                        minusButtonOfficeBuilding.setVisible(false);
                    plusButtonOfficeBuilding.setVisible(true);
                    queriesLabels[1].setText(queries[1] + "");
                });
                plusButtonOfficeBuilding.setOnAction(actionEvent1 -> {
                    if (++queries[1] == ((RegularProperty) sold).getBuildings()[1])
                        plusButtonOfficeBuilding.setVisible(false);
                    minusButtonOfficeBuilding.setVisible(true);
                    queriesLabels[1].setText(queries[1] + "");
                });
                minusButtonHotel.setOnAction(actionEvent1 -> {
                    if (--queries[2] == 0)
                        minusButtonHotel.setVisible(false);
                    plusButtonHotel.setVisible(true);
                    queriesLabels[2].setText(queries[2] + "");
                });
                plusButtonHotel.setOnAction(actionEvent1 -> {
                    if (++queries[2] == ((RegularProperty) sold).getBuildings()[2])
                        plusButtonHotel.setVisible(false);
                    minusButtonHotel.setVisible(true);
                    queriesLabels[2].setText(queries[2] + "");
                });

                Label house = new Label("HOUSES: ");
                house.setTextFill(Color.RED);
                Label officeBuilding = new Label("OFFICE\nBUILDINGS:");
                officeBuilding.setTextFill(Color.LIMEGREEN);
                Label hotel = new Label("HOTELS: ");
                hotel.setTextFill(Color.DODGERBLUE);

                HBox houseQuery = new HBox(house, minusButtonHouse, queriesLabels[0], plusButtonHouse);
                HBox officeBuildingQuery = new HBox(officeBuilding, minusButtonOfficeBuilding,
                    queriesLabels[1], plusButtonOfficeBuilding);
                HBox hotelQuery = new HBox(hotel, minusButtonHotel, queriesLabels[2], plusButtonHotel);
                houseQuery.setAlignment(Pos.CENTER);
                houseQuery.setSpacing(20);
                officeBuildingQuery.setAlignment(Pos.CENTER);
                officeBuildingQuery.setSpacing(20);
                hotelQuery.setAlignment(Pos.CENTER);
                hotelQuery.setSpacing(20);

                if (((RegularProperty) sold).getBuildings()[0] == 0) {
                    houseQuery.getChildren().remove(minusButtonHouse);
                    houseQuery.getChildren().remove(plusButtonHouse);
                }
                if (((RegularProperty) sold).getBuildings()[1] == 0) {
                    officeBuildingQuery.getChildren().remove(minusButtonOfficeBuilding);
                    officeBuildingQuery.getChildren().remove(plusButtonOfficeBuilding);
                }
                if (((RegularProperty) sold).getBuildings()[2] == 0) {
                    hotelQuery.getChildren().remove(minusButtonHotel);
                    hotelQuery.getChildren().remove(plusButtonHotel);
                }

                b0.setOnAction(actionEvent1 -> {
                    double originalMoney = money;
                    int[] minus = ((RegularProperty) sold).getBuildings();
                    for (int i = 0; i < 3; i++)
                        minus[i] -= queries[i];
                    build((RegularProperty) sold, minus);
                    debtArray[0] -= (money - originalMoney);
                    if (debtArray[0] <= 0) {
                        debtText.setText("Your debts have been covered.\nYou may safely close this window.");
                        debtText.setTextFill(Color.FORESTGREEN);
                    } else {
                        debtText.setText("CURRENT DEBT: " + MoneyFormat.format(debtArray[0]));
                    }
                });

                Button doneButton = new Button("Done");
                doneButton.setOnAction(actionEvent1 -> {
                    sellBuildingStage.close();
                });
                Button justSellAll = new Button("Sell property with all these buildings");
                justSellAll.setOnAction(actionEvent1 -> {
                    Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
                    alert1.setTitle("Will you sell this property with all these buildings?");
                    alert1.setHeaderText("Are you sure you want to sell this property with all these buildings?");
                    alert1.setContentText("Once you select OK, this property will be \npermanently sold for " +
                        MoneyFormat.format(sold.price) + ".");
                    Optional<ButtonType> option = alert1.showAndWait();
                    if (option.isPresent() && option.get() == alert1.getButtonTypes().get(0)) {
                        sell(sold, other);
                        debtArray[0] -= sold.price;
                        properties.getItems().remove(sold);

                        if (debtArray[0] <= 0) {
                            debtText.setText("Your debts have been covered.\nYou may safely close this window.");
                            debtText.setTextFill(Color.FORESTGREEN);
                        } else {
                            debtText.setText("CURRENT DEBT: " + MoneyFormat.format(debtArray[0]));
                        }
                    }
                });

                VBox vBox = new VBox(prompt, houseQuery, officeBuildingQuery, hotelQuery, b0);
                vBox.setSpacing(16);

                sellBuildingStage.setScene(new Scene(vBox, 500, 375));

                sellBuildingStage.showAndWait();
            }
            Alert alert1 = new Alert(Alert.AlertType.CONFIRMATION);
            alert1.setTitle("Will you sell this property?");
            alert1.setHeaderText("Are you sure you want to sell this property?");
            alert1.setContentText("Once you select OK, this property will be \npermanently sold for " +
                MoneyFormat.format(sold.price) + ".");
            Optional<ButtonType> option = alert1.showAndWait();
            if (option.isPresent() && option.get() == alert1.getButtonTypes().get(0)) {
                sell(sold, other);
                debtArray[0] -= sold.price;
                properties.getItems().remove(sold);

                if (debtArray[0] <= 0) {
                    debtText.setText("Your debts have been covered.\nYou may safely close this window.");
                    debtText.setTextFill(Color.FORESTGREEN);
                } else {
                    debtText.setText("CURRENT DEBT: " + MoneyFormat.format(debtArray[0]));
                }
            }
        });

        VBox centerBox = new VBox(yourPropertiesLabel, sellButton, properties);
        centerBox.setSpacing(16);
        centerBox.setAlignment(Pos.CENTER);

        pane.setCenter(centerBox);

        debtWindow.setOnCloseRequest(windowEvent -> {
            if (debtArray[0] > 0) {
                if (!declareBankruptcy()) {
                    windowEvent.consume();
                } else {
                    debtWindow.close();
                }
            } else {
                changeMoney(-debtArray[0]);
                debtWindow.close();
            }
        });

        pane.setBottom(new Rectangle(32, 32));
        ((Rectangle)pane.getBottom()).setFill(Color.GHOSTWHITE);
        debtWindow.setScene(new Scene(pane, 500, 375));
        debtWindow.showAndWait();
        return debtArray[0];
    }

    /**
     * Take a list of properties from an opponent either because the opponent lacked money or declared bankruptcy
     * upon landing on this Player's property.
     *
     * @param other      The opponent selling their properties to this Player
     * @param properties The list of properties sold
     */
    public void takeOver(Player other, List<Property> properties) {
        this.properties.addAll(properties);
        propertiesComboBox.getItems().addAll(properties);
        for (int i = properties.size() - 1; i >= 0; i--) {
            properties.get(i).owner = this;
            other.propertiesComboBox.getItems().remove(properties.get(i));
            other.properties.remove(properties.get(i));
        }
        Collections.sort(this.properties);
        Collections.sort(propertiesComboBox.getItems());
    }

    /**
     * Declare bankruptcy and be eliminated.
     *
     * @return TRUE if declared bankruptcy; FALSE if cancelled.
     */
    public boolean declareBankruptcy() {
        Alert alert = new Alert(Alert.AlertType.WARNING,
            "You will be eliminated from the game, and all your properties will belong to whom you owe.",
            new ButtonType("yes. T-T", ButtonBar.ButtonData.YES),
            new ButtonType("NO! >:C", ButtonBar.ButtonData.NO));
        alert.setTitle("DECLARE BANKRUPTCY");
        alert.setHeaderText("ARE YOU SURE YOU WANT TO DECLARE BANKRUPTCY?");

        Optional<ButtonType> selection = alert.showAndWait();
        if (selection.isPresent() && selection.get() == alert.getButtonTypes().get(0)) {
            System.out.println(name + " IS BANKRUPT.");
            return true;
        }
        return false;
    }

    public void receiveEscapeDesertedIsland() {
        this.escapeDesertedIsland = true;
    }

    public int getComplimentaryTickets() {
        return complimentaryTickets;
    }

    public void receiveComplimentaryTicket() {
        this.complimentaryTickets++;
    }

    public void useComplimentaryTicket() {
        this.complimentaryTickets--;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageView getPlane() {
        return plane;
    }

    public void setPlane(ImageView plane) {
        this.plane = plane;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public void setPlayerColor(Color playerColor) {
        this.playerColor = playerColor;
    }

    public double getMoney() {
        return money;
    }

    public int getSpace() {
        return space;
    }

    public ArrayList<Property> getProperties() {
        return properties;
    }

    public ComboBox<Property> getPropertiesComboBox() {
        return propertiesComboBox;
    }

    public Label getMoneyText() {
        return moneyText;
    }

    public int getTurnsLeftOnDesertedIsland() {
        return turnsLeftOnDesertedIsland;
    }

    public boolean hasEscapeDesertedIsland() {
        return escapeDesertedIsland;
    }

    public boolean isSpaceStation() {
        return spaceStation;
    }
}