import javafx.concurrent.Task;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import services.ImageService;
import services.YahooImageService;

import java.util.List;

public class AdvancedImagePicker extends BorderPane {

    public static final int IMAGE_SQUARE_SIZE = 200;
    private final AdvancedImagePickerListener listener;
    private ImageService imageService;
    private String searchTerm;
    private final TilePane imageTilePane;
    private final ScrollPane centerScrollPane;
    private final ImageView loadingImageView;

    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener) {
        super();
        this.listener = listener;
        imageService = new YahooImageService();

        // top bar
        TextField tfSearch = new TextField(searchTerm);
        Button btnSearch = new Button("Bilder anzeigen");
        btnSearch.setOnMouseClicked(event -> showImageSearchResults(tfSearch.getText()));
        BorderPane topBorderPane = new BorderPane();
        topBorderPane.setCenter(tfSearch);
        topBorderPane.setRight(btnSearch);
        setTop(topBorderPane);

        // center
        imageTilePane = new TilePane();
        imageTilePane.prefWidthProperty().bind(widthProperty());
        centerScrollPane = new ScrollPane(imageTilePane);
        centerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setCenter(centerScrollPane);

        // bottom bar
        Button ok = new Button("OK");
        ok.setOnMouseClicked(event -> imageTilePane.getChildren().add(new ImageView("http://qwerdesign.com/index/img/img_l2.jpg")));
        setBottom(ok);

        loadingImageView = new ImageView(AdvancedImageView.LOADING_IMAGE);
        loadingImageView.setFitHeight(IMAGE_SQUARE_SIZE);
        loadingImageView.setFitWidth(IMAGE_SQUARE_SIZE);

        // initial image load
        showImageSearchResults(searchTerm);
    }

    private void showImageSearchResults(String newSearchTerm) {
        searchTerm = newSearchTerm;
        imageTilePane.getChildren().clear();
        setCenter(loadingImageView);

        Task<List<Image>> previewImageLoaderTask = new Task<List<Image>>() {
            @Override
            protected List<Image> call() throws Exception {
                return imageService.getImages(searchTerm);
            }
        };
        previewImageLoaderTask.setOnSucceeded(event -> {
            Object images = event.getSource().getValue();
            if (images instanceof List) {
                imageTilePane.getChildren().clear();
                for (int i = 0; i < 10; i++) {
                    Image image = ((List<Image>) images).get(i);
                    ImageView croppedImage = AdvancedImageUtils.cropImage(image, IMAGE_SQUARE_SIZE, IMAGE_SQUARE_SIZE);
                    croppedImage.setCursor(Cursor.HAND);
                    croppedImage.setOnMouseEntered(event1 -> {
                        ColorAdjust colorAdjust = new ColorAdjust();
                        colorAdjust.setBrightness(0.2);
                        croppedImage.setEffect(colorAdjust);
                    });
                    croppedImage.setOnMouseExited(event1 -> croppedImage.setEffect(null));
                    croppedImage.setOnMouseClicked(event12 -> listener.onImageSelected(image));
                    imageTilePane.getChildren().add(croppedImage);
                    setCenter(centerScrollPane);
                }

            }
        });
        new Thread(previewImageLoaderTask).start();
    }

    public ImageService getImageService() {
        return imageService;
    }

    public void setImageService(ImageService imageService) {
        this.imageService = imageService;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
