package sample;

import javafx.scene.image.Image;

/**
 * A Golden Key card can have various effects to Players, such as moving their locations, rewarding/subtracting
 * their money, etc.
 */
public class GoldenKey {
    final Image card;
    final int id;

    public GoldenKey(Image image, int id) {
        card = image;
        this.id = id;
    }
}
