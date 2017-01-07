import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import services.ImageService;
import services.YahooImageService;

/**
 * Created by Hutschi on 04.01.2017.
 */
public class AdvancedImageView extends ImageView implements AdvancedImagePickerListener {
    private static final String DEFAULT_LOADING_GIF_URL = "http://www.personaltraining-mallorca.com/wp-pt/wp-content/plugins/video-player/images/loading.gif";
    protected final static Image LOADING_IMAGE = new Image(DEFAULT_LOADING_GIF_URL);

    private String searchTerm;
    private AdvancedImagePickerListener listener;
    private ImageService imageService;
    private Stage pickerDialog;

    public AdvancedImageView(String searchTerm) {
        super(LOADING_IMAGE);
        this.searchTerm = searchTerm;
        imageService = new YahooImageService();
        loadPreviewImage();
        listener = this;
    }

    /**
     * Loads preview image in a Thread to prevent UI from getting blocked.
     */
    private void loadPreviewImage() {
        Task<Image> previewImageLoaderTask = new Task<Image>() {
            @Override
            protected Image call() throws Exception {
                return imageService.getPreviewImage(searchTerm);
            }
        };
        previewImageLoaderTask.setOnSucceeded(event -> {
            Object previewImage = event.getSource().getValue();
            if (previewImage instanceof Image) {
                ImageView imageView = AdvancedImageUtils.cropImage((Image) previewImage, getFitWidth(), getFitHeight());
                setImage(imageView.getImage());
                onPreviewImageLoaded();
            } else {
                System.err.println("Preview image is not an instance of Image: " + previewImage.getClass().toString());
            }
        });
        new Thread(previewImageLoaderTask).start();
    }

    private void onPreviewImageLoaded() {
        setCursor(Cursor.HAND);
        setOnMouseClicked(event -> {
            showAdvancedImagePickerDialog();
        });
    }

    private void showAdvancedImagePickerDialog() {
        AdvancedImagePicker advancedImagePicker = new AdvancedImagePicker(searchTerm, listener);
        pickerDialog = new Stage();
        Parent root = new BorderPane(advancedImagePicker);
        pickerDialog.setScene(new Scene(root));
        pickerDialog.setTitle("AdvancedImagePicker");
        pickerDialog.setWidth(1020);
        pickerDialog.setHeight(620);
        pickerDialog.initModality(Modality.WINDOW_MODAL);
        pickerDialog.initOwner(getScene().getWindow());
        pickerDialog.show();

        pickerDialog.setOnCloseRequest(event -> listener.onCancelled());
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public AdvancedImagePickerListener getListener() {
        return listener;
    }

    public void setListener(AdvancedImagePickerListener listener) {
        this.listener = listener;
    }

    public ImageService getImageService() {
        return imageService;
    }

    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    @Override
    public void onImageSelected(Image image) {
        ImageView imageView = AdvancedImageUtils.cropImage(image, getFitWidth(), getFitHeight());
        setImage(imageView.getImage());
        if (pickerDialog.isShowing()) {
            pickerDialog.close();
        }
    }

    @Override
    public void onCancelled() {

    }
}
