package sample;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 * A Golden Key card can have various effects to Players, such as moving their locations, rewarding/subtracting
 * their money, etc.
 */
public class GoldenKey {
    private int id;
    private ImageView imageView;

    public GoldenKey(int id, Image image) {
        this.id = id;
        this.imageView = new ImageView(image);
    }
}
