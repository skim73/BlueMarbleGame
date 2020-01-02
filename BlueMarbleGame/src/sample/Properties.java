package sample;

/**
 * Property abstract class represents a property in the board game that a Player could purchase
 * SUBCLASSES: Regular properties (can have buildings), Special properties (don't have buildings), and
 * Columbia Space Shuttle (a special property with another unique form of rent)
 */
abstract class Property {
    protected final String name;
    protected final double price;
    protected double rent;
    protected Player owner;

    public Property(String name, double price, double rent) {
        this.name = name;
        this.price = price;
        this.rent = rent;
        owner = null;
    }
}

/**
 * Building enum represents the different types of buildings a RegularProperty can have
 */
enum Building {
    NONE, HOUSE1, HOUSE2, OFFICE_BUILDING, HOTEL
}

/**
 * RegularProperties can have buildings that raise rents
 */
class RegularProperty extends Property {
    private Building building;
    private double[] prices;
    private double[] rents;

    /**
     * @param name Name of property
     * @param prices {base price, HOUSE1 price, HOUSE2 price, OFFICE_BUILDING price, HOTEL price}
     * @param rents {base rent, HOUSE1 rent, HOUSE2 rent, OFFICE_BUILDING rent, HOTEL rent}
     */
    public RegularProperty(String name, double[] prices, double[] rents) {
        super(name, prices[0], rents[0]);
        this.prices = prices;
        this.rents = rents;
    }

    /**
     * upgrades the building of this property
     * @param building the new building to be built
     * @return price for this new building
     */
    public double upgrade(Building building) {
        double pay = 0;
        switch (building) {
            case HOUSE1:
                rent = rents[1];
                pay = prices[1];
                break;
            case HOUSE2:
                rent = rents[2];
                pay = prices[2];
                break;
            case OFFICE_BUILDING:
                rent = rents[3];
                pay = prices[3];
                break;
            case HOTEL:
                rent = rents[4];
                pay = prices[4];
                break;
        }
        this.building = building;
        return pay;
    }

    /**
     * Remove the building on this Property. (This is only to be used by the GameManager eliminating a Player
     * owing debt to the Banker.)
     */
    void deconstruct() {
        building = Building.NONE;
    }

    public Building getBuilding() {
        return building;
    }

    public double[] getPrices() {
        return prices;
    }

    public double[] getRents() {
        return rents;
    }
}

/**
 * SpecialProperties don't have buildings; thus their rents are constant
 */
class SpecialProperty extends Property {

    public SpecialProperty(String name, double price, double rent) {
        super(name, price, rent);
    }
}

/**
 * The Player who owns ColumbiaSpaceShuttle receives a $2.00M entry fee from others who enter
 * SPACE STATION in their next turns
 */
class ColumbiaSpaceShuttle extends SpecialProperty {
    final double ENTRY_FEE = 2.00;

    public ColumbiaSpaceShuttle(String name, double price, double rent) {
        super(name, price, rent);
    }
}