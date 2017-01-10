package ch.fhnw.cuie.advancedimagepicker;

import ch.fhnw.cuie.advancedimagepicker.services.FlickrImageService;
import ch.fhnw.cuie.advancedimagepicker.services.ImageService;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.awt.*;

/**
 * Created by Hutschi on 04.01.2017.
 */
public class AdvancedImageView extends ImageView implements AdvancedImagePickerListener {
    private static final String DEFAULT_LOADING_GIF_URL = "http://www.personaltraining-mallorca.com/wp-pt/wp-content/plugins/video-player/images/loading.gif";
    protected final static Image LOADING_IMAGE = new Image(DEFAULT_LOADING_GIF_URL);

    private String searchTerm;
    private ImageService imageService;
    private Stage pickerDialog;

    public AdvancedImageView(String searchTerm) {
        super(LOADING_IMAGE);
        setPreserveRatio(true);
        this.searchTerm = searchTerm;
        imageService = new FlickrImageService();
        loadPreviewImage();
    }

    /**
     * Loads preview image in a Thread to prevent UI from getting blocked.
     */
    public void loadPreviewImage() {
        setImage(LOADING_IMAGE);
        Task<ImageDataHolder> previewImageLoaderTask = new Task<ImageDataHolder>() {
            @Override
            protected ImageDataHolder call() throws Exception {
                return imageService.getPreviewImage(searchTerm);
            }
        };
        previewImageLoaderTask.setOnSucceeded(event -> {
            Object previewImageDataHolder = event.getSource().getValue();
            if (previewImageDataHolder instanceof ImageDataHolder) {
                Image previewImage = new Image(((ImageDataHolder) previewImageDataHolder).getImageInputStream());
                ImageView croppedImageView = AdvancedImageUtils.createCroppedImageView(previewImage, getFitWidth(), getFitHeight());
                setImage(croppedImageView.getImage());
                onPreviewImageLoaded();
            } else {
                System.err.println("Preview image is not an instance of ImageDataHolder: " + previewImageDataHolder.getClass().toString());
            }
        });
        new Thread(previewImageLoaderTask).start();
    }

    protected void onPreviewImageLoaded() {
        setCursor(Cursor.HAND);
        setOnMouseClicked(event -> {
            onClick();
        });
    }

    protected void onClick() {
        AdvancedImagePicker advancedImagePicker = new AdvancedImagePicker(searchTerm, this);
        pickerDialog = new Stage();
        Parent root = new BorderPane(advancedImagePicker);
        pickerDialog.setScene(new Scene(root));
        pickerDialog.setTitle("AdvancedImagePicker");
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double dialogWidth = screenSize.getHeight();
        double dialogHeight = dialogWidth * 3 / 4;
        pickerDialog.setWidth(dialogWidth);
        pickerDialog.setHeight(dialogHeight);
        pickerDialog.initModality(Modality.WINDOW_MODAL);
        pickerDialog.initOwner(getScene().getWindow());
        pickerDialog.show();
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public ImageService getImageService() {
        return imageService;
    }

    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    public void onImageSelected(ImageDataHolder imageDataHolder) {
        Image image = new Image(imageDataHolder.getImageInputStream());
        ImageView croppedImageView = AdvancedImageUtils.createCroppedImageView(image, getFitWidth(), getFitHeight());
        setImage(croppedImageView.getImage());
        if (pickerDialog.isShowing()) {
            pickerDialog.close();
        }
    }
}
