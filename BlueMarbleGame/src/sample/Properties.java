package sample;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

/**
 * Property abstract class represents a property in the board game that a Player could purchase
 * SUBCLASSES: Regular properties (can have buildings), Special properties (don't have buildings), and
 * Columbia Space Shuttle (a special property with another unique form of rent)
 */
abstract class Property implements Comparable<Property> {
    protected final String name;
    protected double price;
    protected double rent;
    protected Player owner;
    protected Image propertyCard = null;  // later assigned with setPropertyCard(Image)

    final int space;

    /**
     * Constructor of a Property; owner is defaulted to NULL (the banker)
     *
     * @param name  name of Property (constant)
     * @param price price of Property (changes with more buildings for RegularProperties)
     * @param rent  rent of Property (changes with more buildings for RegularProperties)
     */
    public Property(String name, double price, double rent, int space) {
        this.name = name;
        this.price = price;
        this.rent = rent;
        owner = null;
        this.space = space;
    }

    public void setPropertyCard(Image image) {
        propertyCard = image;
    }

    @Override
    public int compareTo(Property o) {
        return this.space - o.space;
    }

    @Override
    public String toString() {
        return space + ". " + name + "\n(" + MoneyFormat.format(price) + ")";
    }
}


/**
 * RegularProperties can have buildings that raise rents
 * BUILDINGS array consists of three integers: {# of HOUSES, # of OFFICE_BUILDINGS, # of HOTELS}.
 * RegularProperties can have up to 2 of each building types, for a maximum of 6 buildings in total.
 */
class RegularProperty extends Property {
    private int[] buildings;
    private double[] prices;
    private double[] rents;
    HBox buildingPics;

    final Image[][] buildingImages = new Image[3][3];

    /**
     * @param name   Name of property
     * @param prices {base price, HOUSE price, OFFICE_BUILDING price, HOTEL price}
     * @param rents  {base rent, HOUSE1 rent, HOUSE2 rent, OFFICE_BUILDING rent, HOTEL rent}
     */
    public RegularProperty(String name, double[] prices, double[] rents, int space) throws FileNotFoundException {
        super(name, prices[0], rents[0], space);
        buildings = new int[]{0, 0, 0};
        this.prices = prices;
        this.rents = rents;

        buildingImages[0][1] = new Image(new FileInputStream("img_buildings/house.png"));
        buildingImages[0][2] = new Image(new FileInputStream("img_buildings/house2.png"));
        buildingImages[1][1] = new Image(new FileInputStream("img_buildings/officebuilding.png"));
        buildingImages[1][2] = new Image(new FileInputStream("img_buildings/officebuilding2.png"));
        buildingImages[2][1] = new Image(new FileInputStream("img_buildings/hotel.png"));
        buildingImages[2][2] = new Image(new FileInputStream("img_buildings/hotel2.png"));

        buildingPics = new HBox();
        for (int i = 0; i < 3; i++) {
            ImageView buildingIcon = new ImageView();
            buildingIcon.setFitHeight(16);
            buildingIcon.setFitWidth(16);
            buildingPics.getChildren().add(buildingIcon);
        }

        if (space < 10) {
            GridPane.setConstraints(buildingPics,
                Player.spaceToGrid[space][0], 12);
            GridPane.setValignment(buildingPics, VPos.TOP);
        } else if (space < 20) {
            GridPane.setConstraints(buildingPics,
                2, Player.spaceToGrid[space][1]);
            GridPane.setHalignment(buildingPics, HPos.RIGHT);
            buildingPics.setRotate(90);
        } else if (space < 30) {
            GridPane.setConstraints(buildingPics,
                Player.spaceToGrid[space][0], 2);
            GridPane.setValignment(buildingPics, VPos.BOTTOM);
            buildingPics.setRotate(180);
        } else {
            GridPane.setConstraints(buildingPics,
                12, Player.spaceToGrid[space][1]);
            GridPane.setHalignment(buildingPics, HPos.LEFT);
            buildingPics.setRotate(270);
        }
    }

    /**
     * Specifies the number of each building type the Player wants at this Property
     *
     * @param request {# HOUSES to have, # OFFICE BUILDINGS to have, # HOTELS to have}
     */
    public void construct(int[] request) {
        price = prices[0];
        rent = 0;
        if (Arrays.equals(request, new int[]{0, 0, 0})) {
            rent = rents[0];
        } else {
            if (request[0] == 1) {
                rent = rents[1];
                price += prices[1];
            } else if (request[0] == 2) {
                rent = rents[2];
                price += prices[1] * 2;
            }
            rent += rents[3] * request[1] + rents[4] * request[2];
            price += prices[2] * request[1] + prices[3] * request[2];
        }

        buildings = request;


        for (int i = 0; i < 3; i++) {
            ((ImageView) buildingPics.getChildren().get(i)).setImage(buildingImages[i][buildings[i]]);
        }
    }

    /**
     * Removes all buildings at this property
     */
    public void deconstruct() {
        buildings = new int[]{0, 0, 0};
        price = prices[0];
        rent = rents[0];
        ((ImageView) buildingPics.getChildren().get(0)).setImage(null);
        ((ImageView) buildingPics.getChildren().get(1)).setImage(null);
        ((ImageView) buildingPics.getChildren().get(2)).setImage(null);
    }


    public int[] getBuildings() {
        return buildings;
    }

    public double[] getPrices() {
        return prices;
    }

    @Override
    public String toString() {
        return space + ". " + name + " " + Arrays.toString(buildings) + "\n(" + MoneyFormat.format(price) + ")";
    }
}

/**
 * SpecialProperties don't have buildings; thus their rents are constant
 */
class SpecialProperty extends Property {

    public SpecialProperty(String name, double price, double rent, int space) {
        super(name, price, rent, space);
    }
}

/**
 * The Player who owns ColumbiaSpaceShuttle receives a $2.00M entry fee from others who enter
 * SPACE STATION in their next turn
 */
class ColumbiaSpaceShuttle extends SpecialProperty {
    static final double ENTRY_FEE = 2.00;

    public ColumbiaSpaceShuttle() {
        super("Columbia", 4.50, 4.00, 32);
    }
}