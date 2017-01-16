package ch.fhnw.cuie.advancedimagepicker;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 * Utility class providing utility methods for Image operations.
 *
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public class AdvancedImageUtils {

    private AdvancedImageUtils() {
        // utility classes should not have public constructors
    }

    /**
     * Creates an ImageView that shows a cropped version of given image.
     * Cropped to given width and height.
     *
     * @param image  Image to be cropped
     * @param width  Width of cropped image
     * @param height Height of cropped image
     * @return New cropped ImageView
     */
    public static ImageView createCroppedImageView(Image image, double width, double height) {
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        int xOffset = 0;
        int yOffset = 0;

        double resizedWidth = imageHeight / (height / width);
        double resizedHeight = imageWidth / (width / height);
        if (resizedHeight <= imageHeight) {
            resizedWidth = imageWidth;
            yOffset = (int) ((imageHeight - resizedHeight) / 2);
        } else {
            resizedHeight = imageHeight;
            xOffset = (int) ((imageWidth - resizedWidth) / 2);
        }

        // crop to square based on original image size
        WritableImage croppedImage = new WritableImage(image.getPixelReader(), xOffset, yOffset, (int) resizedWidth, (int) resizedHeight);

        // resize
        ImageView resizedCroppedImageView = new ImageView(croppedImage);
        resizedCroppedImageView.setFitWidth(width);
        resizedCroppedImageView.setFitHeight(height);
        return resizedCroppedImageView;
    }
}
