import javafx.scene.image.Image;

/**
 * Created by Hutschi on 06.01.2017.
 */
public interface AdvancedImagePickerListener {
    void onImageSelected(Image image);
    void onCancelled();
}
