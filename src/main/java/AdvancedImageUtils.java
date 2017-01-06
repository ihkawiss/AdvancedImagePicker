import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;

/**
 * Created by Hutschi on 06.01.2017.
 */
public class AdvancedImageUtils {

    public static ImageView cropImage(Image image, double width, double height) {
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
