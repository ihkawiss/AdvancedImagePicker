import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import services.FlickrImageService;
import services.ImageService;

import java.util.List;

public class AdvancedImagePicker extends BorderPane {

    public static final int IMAGE_SQUARE_PREFERRED_SIZE = 350;
    private final AdvancedImagePickerListener listener;
    private ImageService imageService;
    private String searchTerm;
    private final TilePane imageTilePane;
    private final ScrollPane centerScrollPane;
    private final ImageView loadingImageView;

    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener) {
        super();
        this.listener = listener;
        imageService = new FlickrImageService();

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
        imageTilePane.prefWidthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int imageSquareSize = calculateImageSquareSize();
                for (Node imageTile : imageTilePane.getChildren()) {
                    if (imageTile instanceof ImageView) {
                        ((ImageView)imageTile).setFitWidth(imageSquareSize);
                        ((ImageView)imageTile).setFitHeight(imageSquareSize);
                    }
                }

            }
        });
        imageTilePane.setStyle("--fx-border-width: 0; -fx-padding: 0;");
        centerScrollPane = new ScrollPane(imageTilePane);
        centerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScrollPane.setStyle("-fx-border-width: 0; -fx-padding: 0;  ");
        setCenter(centerScrollPane);

        // bottom bar
        Button ok = new Button("OK");
        ok.setOnMouseClicked(event -> imageTilePane.getChildren().add(new ImageView("http://qwerdesign.com/index/img/img_l2.jpg")));
        setBottom(ok);

        loadingImageView = new ImageView(AdvancedImageView.LOADING_IMAGE);
        loadingImageView.setFitHeight(IMAGE_SQUARE_PREFERRED_SIZE);
        loadingImageView.setFitWidth(IMAGE_SQUARE_PREFERRED_SIZE);

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
                int imageSquareSize = calculateImageSquareSize();
                    for (int i = 0; i < 100; i++) {
                        Image image = ((List<Image>) images).get(i);
                        ImageView croppedImage = AdvancedImageUtils.cropImage(image, imageSquareSize, imageSquareSize);
                        croppedImage.setCursor(Cursor.HAND);
                        croppedImage.setOnMouseEntered(event1 -> {
                            ColorAdjust colorAdjust = new ColorAdjust();
                            colorAdjust.setBrightness(0.2);
                            croppedImage.setEffect(colorAdjust);
                        });
                        croppedImage.setOnMouseExited(event1 -> croppedImage.setEffect(null));
                        croppedImage.setOnMouseClicked(event1 -> listener.onImageSelected(image));
                        imageTilePane.getChildren().add(croppedImage);
                        setCenter(centerScrollPane);
                    }
            }
        });
        new Thread(previewImageLoaderTask).start();
    }

    private int calculateImageSquareSize() {
        double tilePaneWidth = imageTilePane.getPrefWidth();
        int numberOfColumns = (int) (tilePaneWidth / IMAGE_SQUARE_PREFERRED_SIZE);
        return (int) (tilePaneWidth / numberOfColumns);
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
