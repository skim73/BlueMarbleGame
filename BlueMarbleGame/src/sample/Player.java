package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
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
        money = 30.00;
        moneyText = new Label(MoneyFormat.format(money));
        moneyText.setFont(new Font("Arial Black", 16));
        moneyText.setTextFill(Color.FORESTGREEN);
        space = 0;
        properties = new ArrayList<>(29);
        propertiesComboBox = new ComboBox<>();
        propertiesComboBox.setPromptText("(Properties list)");


        for (int i = 0; i < 10; i++) {
            spaceToGrid[i] = new int[]{11 - i, 11};
            spaceToGrid[i + 10] = new int[]{1, 11 - i};
            spaceToGrid[i + 20] = new int[]{i + 1, 1};
            spaceToGrid[i + 30] = new int[]{11, i + 1};
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
                Duration.millis(400),
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
     * @param destination space index of destination
     * @param fromSpaceStation whether this Player is moving from Space Station or not
     * @return destination
     */
    public int directlyMove(int destination, boolean fromSpaceStation) {
        if (destination < space) {
            destination += 40;
        }
        moveAnimation.setRate(4.5);
        int rtn = move(destination - space, false);
        if (fromSpaceStation)
            spaceStation = false;

        GameManager.popup.setOnHidden(windowEvent -> {});
        return rtn;
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
        if (money <= 6.00)
            moneyText.setTextFill(Color.CRIMSON);
        else {
            moneyText.setTextFill(Color.FORESTGREEN);
        }
    }

    /**
     * Player purchases a property if they can afford it.
     *
     * @param property the property to purchase
     * @return Player's remaining balance after transaction
     */
    public double purchase(Property property) {
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
            alert.setHeaderText(name + ", you can't afford to purchase this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
        }
        return money;
    }

    /**
     * If a Player lands on their property, they can construct buildings there.
     *
     * Positive # in buildMatrix -> build that many
     * Negative # in buildMatrix -> sell that many
     *
     * @param property the property at which the new building is constructed
     * @param buildMatrix {# HOUSES to build/sell, # OFFICE BUILDINGS to build/sell, # HOTELS to build/sell}
     * @return Player's remaining balance after transaction
     */
    public double build(Property property, int[] buildMatrix) {
        double originalPrice = property.price;
        try {
            ((RegularProperty) property).construct(buildMatrix);
            changeMoney(originalPrice - property.price);
            propertiesComboBox.getItems().set(propertiesComboBox.getItems().indexOf(property), property);
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You don't have enough money!");
            alert.setHeaderText(name + ", you can't afford to construct these buildings at this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
        }
        return money;
    }

    /**
     * Player pays another by the specified amount
     *
     * @param other  An opponent to whom this Player pays
     * @param amount The amount paid
     * @return this Player's (the payer's) remaining balance after transaction; if this Player is on debt, the
     * amount that this Player is short is returned (expressed as -$#.##M).
     */
    public double payOther(Player other, double amount) {
        try {
            changeMoney(-amount);
            if (other != null) {
                other.changeMoney(amount);
            }
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("You are on debt!");
            alert.setHeaderText(name + ", you can't pay " + MoneyFormat.format(amount) + " to " +
                other.getName() + "!");
            alert.setContentText("You owe " + other.getName() + " " + MoneyFormat.format(amount) + "!" +
                "\nYou must sell some of your properties to " + other.getName() + "!");
            alert.showAndWait();
            money -= amount;
            openDebtWindow();
        }
        return money;
    }

    /**
     * Sell properties and get half the total price of properties
     * @param properties list of properties to sell
     * @return Player's money after selling properties
     */
    public double sell(List<Property> properties) {
        double soldPrice = 0;
        for (Property property : properties) {
            property.owner = null;
            soldPrice += property.price;
            if (property instanceof RegularProperty)
                ((RegularProperty) property).deconstruct();
            properties.remove(property);
            propertiesComboBox.getItems().remove(property);
        }
        money += soldPrice;
        return money;
    }

    /**
     * Sell property and get half its total price (overloaded version taking in just 1 Property)
     * @param property property to sell
     * @return Player's money after selling property
     */
    public double sell(Property property) {
        ArrayList<Property> sold = new ArrayList<>(1);
        sold.add(property);
        return sell(sold);
    }

    /**
     * Sets up and shows the window where the Player can handle their debt to another
     */
    public void openDebtWindow() {
        Stage debtWindow = new Stage();

        debtWindow.setOnCloseRequest(windowEvent -> {
            if (money < 0) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("You still have debt to pay off.");
                alert.setHeaderText("You haven't finished paying all your debts off.");
                alert.setContentText("You cannot exit until you have finished paying all your debts.");
                alert.showAndWait();
            } else {
                debtWindow.close();
            }
        });
        debtWindow.showAndWait();
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
        for (Property property : properties) {
            property.owner = this;
            other.properties.remove(property);
            other.propertiesComboBox.getItems().remove(property);
        }
        Collections.sort(this.properties);
        Collections.sort(propertiesComboBox.getItems());
    }

    /**
     * Declare bankruptcy and be eliminated.
     *
     * @return TRUE if confirmed bankruptcy; FALSE if cancelled.
     */
    public boolean declareBankruptcy() {
        Alert alert = new Alert(Alert.AlertType.WARNING,
            "You will be eliminated from the game, and all your properties will belong to whom you owe.",
            new ButtonType("yes. T-T", ButtonBar.ButtonData.YES),
            new ButtonType("NO! >:C", ButtonBar.ButtonData.NO));
        alert.setTitle("DECLARE BANKRUPTCY");
        alert.setHeaderText("Are you sure you want to declare bankruptcy?");

        Optional<ButtonType> selection = alert.showAndWait();
        if (selection.isEmpty() || selection.get() == alert.getButtonTypes().get(1))
        System.out.println(name + ", you are bankrupt.");
        return true;
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