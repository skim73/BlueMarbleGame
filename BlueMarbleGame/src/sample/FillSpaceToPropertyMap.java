package sample;

import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

/**
 * Literally only for filling in the spaceToProperty HashMap in GameManager with space indices mapping to
 * Properties (which are also instantiated).
 */
class FillSpaceToPropertyMap {
    static final String thatDirectory = "img_properties/";
    public static void fill(HashMap<Integer, Property> spaceToProperty) throws FileNotFoundException {

        spaceToProperty.put(1, new RegularProperty("Taipei",
            new double[]{.50, .50, 1.50, 2.50},
            new double[]{.02, .10, .30, .90, 2.50}, 1));
        spaceToProperty.put(3, new RegularProperty("Hong Kong",
            new double[]{.80, .50, 1.50, 2.50},
            new double[]{.04, .20, .60, 1.80, 4.50}, 3));
        spaceToProperty.put(4, new RegularProperty("Manila",
            new double[]{.80, .50, 1.50, 2.50},
            new double[]{.04, .20, .60, 1.80, 4.50}, 4));
        spaceToProperty.put(5, new SpecialProperty("Jeju Island", 2.00, 3.00, 5));
        spaceToProperty.put(6, new RegularProperty("Singapore",
            new double[]{1.00, .50, 1.50, 2.50},
            new double[]{.06, .30, .90, 2.70, 5.50}, 6));
        spaceToProperty.put(8, new RegularProperty("Cairo",
            new double[]{1.00, .50, 1.50, 2.50},
            new double[]{.06, .30, .90, 2.70, 5.50}, 8));
        spaceToProperty.put(9, new RegularProperty("Istanbul",
            new double[]{1.20, .50, 1.50, 2.50},
            new double[]{.08, .40, 1.00, 3.00, 6.00}, 9));

        spaceToProperty.put(11, new RegularProperty("Athens",
            new double[]{1.40, 1.00, 3.00, 5.00},
            new double[]{.10, .50, 1.50, 4.50, 7.50}, 11));
        spaceToProperty.put(13, new RegularProperty("Copenhagen",
            new double[]{1.60, 1.00, 3.00, 5.00},
            new double[]{.12, .60, 1.80, 5.00, 9.00}, 13));
        spaceToProperty.put(14, new RegularProperty("Stockholm",
            new double[]{1.60, 1.00, 3.00, 5.00},
            new double[]{.12, .60, 1.80, 5.00, 9.00}, 14));
        spaceToProperty.put(15, new SpecialProperty("Concorde", 2.00, 3.00, 15));
        spaceToProperty.put(16, new RegularProperty("Zurich",
            new double[]{1.80, 1.00, 3.00, 5.00},
            new double[]{.14, .70, 2.00, 5.50, 9.50}, 16));
        spaceToProperty.put(18, new RegularProperty("Berlin",
            new double[]{1.80, 1.00, 3.00, 5.00},
            new double[]{.14, .70, 2.00, 5.50, 9.50}, 18));
        spaceToProperty.put(19, new RegularProperty("Montreal",
            new double[]{2.00, 1.00, 3.00, 5.00},
            new double[]{.16, .80, 2.20, 6.00, 10.00}, 19));

        spaceToProperty.put(21, new RegularProperty("Buenos Aires",
            new double[]{2.20, 1.50, 4.50, 7.50},
            new double[]{.18, .90, 2.50, 7.00, 10.50}, 21));
        spaceToProperty.put(23, new RegularProperty("São Paulo",
            new double[]{2.40, 1.50, 4.50, 7.50},
            new double[]{.20, 1.00, 3.00, 7.50, 11.00}, 23));
        spaceToProperty.put(24, new RegularProperty("Sydney",
            new double[]{2.40, 1.50, 4.50, 7.50},
            new double[]{.20, 1.00, 3.00, 7.50, 11.00}, 24));
        spaceToProperty.put(25, new SpecialProperty("Busan", 5.00, 6.00, 25));
        spaceToProperty.put(26, new RegularProperty("Hawaii",
            new double[]{2.60, 1.50, 4.50, 7.50},
            new double[]{.22, 1.10, 3.30, 8.00, 11.50}, 26));
        spaceToProperty.put(27, new RegularProperty("Lisbon",
            new double[]{2.60, 1.50, 4.50, 7.50},
            new double[]{.22, 1.10, 3.30, 8.00, 11.50}, 27));
        spaceToProperty.put(28, new SpecialProperty("Queen Elizabeth 2", 3.00, 2.50, 28));
        spaceToProperty.put(29, new RegularProperty("Madrid",
            new double[]{2.80, 1.50, 4.50, 7.50},
            new double[]{.24, 1.20, 3.60, 8.50, 12.00}, 29));

        spaceToProperty.put(31, new RegularProperty("Tokyo",
            new double[]{3.00, 2.00, 6.00, 10.00},
            new double[]{.26, 1.30, 3.90, 9.00, 12.75}, 31));
        spaceToProperty.put(32, new ColumbiaSpaceShuttle());
        spaceToProperty.put(33, new RegularProperty("Paris",
            new double[]{3.20, 2.00, 6.00, 10.00},
            new double[]{.28, 1.50, 4.50, 10.00, 14.00}, 33));
        spaceToProperty.put(34, new RegularProperty("Rome",
            new double[]{3.20, 2.00, 6.00, 10.00},
            new double[]{.28, 1.50, 4.50, 10.00, 14.00}, 34));
        spaceToProperty.put(36, new RegularProperty("London",
            new double[]{3.50, 2.00, 6.00, 10.00},
            new double[]{.35, 1.75, 5.00, 11.00, 15.00}, 36));
        spaceToProperty.put(37, new RegularProperty("New York",
            new double[]{3.50, 2.00, 6.00, 10.00},
            new double[]{.35, 1.75, 5.00, 11.00, 15.00}, 37));
        spaceToProperty.put(39, new SpecialProperty("Seoul Olympics", 10.00, 20.00, 39));

        for (int index : spaceToProperty.keySet()) {
            spaceToProperty.get(index).setPropertyCard(new Image(new FileInputStream(
                thatDirectory + index + ".png")));
        }
}
    }