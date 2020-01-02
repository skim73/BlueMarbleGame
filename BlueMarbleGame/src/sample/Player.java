package sample;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.util.Duration;

import java.util.*;

/**
 * Player class represents a player of the board game who has money, list of properties
 */
class Player {
    private String name;
    private ImageView plane;
    private double money;
    private int space;
    private LinkedList<Property> properties;

    final Timeline moveAnimation = new Timeline();

    static final HashMap<Integer, int[]> spaceToGrid = new HashMap<>();

    /**
     * Player constructor.
     * Name is defaulted to "Player (#)".
     * Plane icon will be selected.
     * Player start with $30.00M
     * Player starts at the GO space (0th space from GO)
     * @param playerNum Player 1, 2, 3, or 4
     */
    public Player(int playerNum) {
        this.name = "Player " + playerNum;
        this.plane = null;
        money = 30.00;
        space = 0;
        properties = new LinkedList<>();

        for (int i = 0; i < 10; i++) {
            spaceToGrid.put(i, new int[] {11-i, 11});
            spaceToGrid.put(i+10, new int[] {1, 11-i});
            spaceToGrid.put(i+20, new int[] {i+1, 1});
            spaceToGrid.put(i+30, new int[] {11, i+1});
        }
    }

    /**
     * Moves this Player by the specified number of spaces
     * @param delta number of spaces to move
     * @return the new space index of Player (how many spaces from GO) (*** returned as soon as
     * the moving animation is called ***)
     */
    public int move(int delta) {
        moveAnimation.setCycleCount(delta);
        moveAnimation.getKeyFrames().setAll(new KeyFrame(
            Duration.millis(400),
            actionEvent -> {
                    if (++space == 40)
                        space = 0;
                    GridPane.setConstraints(plane, spaceToGrid.get(space)[0], spaceToGrid.get(space)[1]);
            }
        ));
        moveAnimation.playFromStart();

        return (space + delta) % 40;
    }

    /**
     * Changes Player's money amount
     * @param amount amount changed
     * @throws NotEnoughMoneyException thrown if Player attempts to pay more than their current balance
     */
    public void changeMoney(double amount) {
        if (amount < 0 && money < -amount)
            throw new NotEnoughMoneyException();
        money += amount;
    }

    /**
     * Player purchases a property if they can afford it.
     * @param property the property to purchase
     * @return Player's remaining balance after transaction
     */
    public double purchase(Property property) {
        try {
            changeMoney(-property.price);
            properties.add(property);
            property.owner = this;
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You don't have enough money!");
            alert.setHeaderText("You can't afford to purchase this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
        }
        return money;
    }

    /**
     * If a Player lands on their property, they can construct a building there.
     * @param property the property at which the new building is constructed
     * @param building the building to construct
     * @return Player's remaining balance after transaction
     */
    public double build(Property property, Building building) {
        if (!(property instanceof RegularProperty))
            return money;
        try {
            changeMoney(-1 * ((RegularProperty) property).upgrade(building));
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("You don't have enough money!");
            alert.setHeaderText("You can't afford this building at this property!");
            alert.setContentText("Try again when you do have enough money.");
            alert.showAndWait();
        }
        return money;
    }

    /**
     * Player pays another by the specified amount
     * @param other An opponent to whom this Player pays
     * @param amount The amount paid
     * @return this Player's (the payer's) remaining balance after transaction; otherwise, this Player's debt to
     * the other Player is returned
     */
    public double payOther(Player other, double amount) {
        try {
            changeMoney(-amount);
            other.changeMoney(amount);
        } catch (NotEnoughMoneyException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("You are on debt!");
            alert.setHeaderText("You owe your opponent money!");
            alert.setContentText("You owe " + other.getName() + " " + MoneyFormat.format(amount) + "!" +
                "\nYou must sell some of your properties to " + other.getName() + "!");
            alert.showAndWait();
            return amount - this.money;
        }
        return money;
    }

    /**
     * Take a list of properties from an opponent either because the opponent lacked money or declared bankruptcy
     * upon landing on this Player's property.
     * @param other The opponent selling their properties to this Player
     * @param properties The list of properties sold
     */
    public void takeOver(Player other, List<Property> properties) {
        this.properties.addAll(properties);
        for (Property property : properties) {
            property.owner = this;
            other.properties.remove(property);
        }
    }

    /**
     * Declare bankruptcy and be eliminated.
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
        assert selection.isPresent();
        if (selection.get() == alert.getButtonTypes().get(0)) {
            System.out.println(name + ", you are bankrupt.");
            return true;
        }
        return false;
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

    public double getMoney() {
        return money;
    }

    public int getSpace() {
        return space;
    }

    public LinkedList<Property> getProperties() {
        return properties;
    }
}