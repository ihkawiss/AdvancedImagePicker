package services;

import javafx.scene.image.Image;
import java.util.List;

public interface ImageService {

    Image getPreviewImage(String searchTerm);
    List<Image> getImages(String searchTerm);

}
