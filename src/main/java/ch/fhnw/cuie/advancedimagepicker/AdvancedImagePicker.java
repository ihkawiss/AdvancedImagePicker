package ch.fhnw.cuie.advancedimagepicker;

import ch.fhnw.cuie.advancedimagepicker.services.FlickrImageService;
import ch.fhnw.cuie.advancedimagepicker.services.ImageService;
import ch.fhnw.cuie.advancedimagepicker.services.ImageServiceListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdvancedImagePicker extends BorderPane implements ImageServiceListener {

    public static final int IMAGE_SQUARE_PREFERRED_SIZE = 200;
    private final AdvancedImagePickerListener listener;
    private ImageService imageService;
    private String searchTerm;
    private final TilePane imageTilePane;
    private final ScrollPane centerScrollPane;
    private final ImageView loadingImageView;
    private final AtomicInteger numberOfRunningImageLoaderTasks = new AtomicInteger(0);

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
                        ((ImageView) imageTile).setFitWidth(imageSquareSize);
                        ((ImageView) imageTile).setFitHeight(imageSquareSize);
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
        imageTilePane.getChildren().add(loadingImageView);
        setCenter(centerScrollPane);
        startImageSearch(searchTerm);
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

    private void startImageSearch(String searchTerm) {
        numberOfRunningImageLoaderTasks.set(0);
        startImageSearch(searchTerm, 5, 10);
    }


    public void startImageSearch(String searchTerm, int imagesPerPage, int numberOfPages) {
        Task<Void> imageSearchTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for (int i = 0; i < numberOfPages; i++) {
                    List<ImageDataHolder> imageDataHolders = imageService.getImages(searchTerm, imagesPerPage, i);
                    onNewImageResults(imageDataHolders);
                }
                return null;
            }
        };
        //imageSearchTask.setOnSucceeded(event -> listener.onFinished());
        new Thread(imageSearchTask).start();
    }

    @Override
    public void onNewImageResults(final List<ImageDataHolder> imageDataHolders) {

        Task<List<ImageView>> previewImageLoaderTask = new Task<List<ImageView>>() {
            @Override
            protected List<ImageView> call() throws Exception {
                int imageSquareSize = calculateImageSquareSize();
                numberOfRunningImageLoaderTasks.incrementAndGet();
                List<ImageView> imageViews = new ArrayList<>();
                for (ImageDataHolder imageDataHolder : imageDataHolders) {
                    Image image = new Image(imageDataHolder.getThumbnailUrl());
                    ImageView croppedImage = AdvancedImageUtils.cropImage(image, imageSquareSize, imageSquareSize);
                    croppedImage.setFitHeight(imageSquareSize);
                    croppedImage.setFitWidth(imageSquareSize);
                    croppedImage.setPreserveRatio(true);
                    croppedImage.setSmooth(true);
                    croppedImage.setCursor(Cursor.HAND);
                    croppedImage.setOnMouseEntered(event1 -> {
                        ColorAdjust colorAdjust = new ColorAdjust();
                        colorAdjust.setBrightness(0.2);
                        croppedImage.setEffect(colorAdjust);
                    });
                    croppedImage.setOnMouseExited(event1 -> croppedImage.setEffect(null));
                    croppedImage.setOnMouseClicked(event1 -> listener.onImageSelected(imageDataHolder));
                    imageViews.add(croppedImage);
                }
                return imageViews;
            };
        };
        previewImageLoaderTask.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                int imageSquareSize = calculateImageSquareSize();
                List<ImageView> imageViews = (List<ImageView>) event.getSource().getValue();
                ObservableList<Node> tiles = imageTilePane.getChildren();
                for (ImageView imageView : imageViews) {
                    imageView.setFitHeight(imageSquareSize);
                    imageView.setFitHeight(imageSquareSize);
                    tiles.add(tiles.size()-1,imageView);
                }

                int i = numberOfRunningImageLoaderTasks.decrementAndGet();
                System.out.println(i);
                if (i == 0) {
                    onFinished();
                }
            }
        });
        new Thread(previewImageLoaderTask).start();
    }

    @Override
    public void onFinished() {
        if (imageTilePane.getChildren().contains(loadingImageView)) {
            imageTilePane.getChildren().remove(loadingImageView);
        }
    }







    private void showImageSearchResultsBackup(String newSearchTerm) {
        searchTerm = newSearchTerm;
        imageTilePane.getChildren().clear();
        setCenter(loadingImageView);

        /*
        Task<Void> previewImageLoaderTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                List<ImageDataHolder> images = imageService.getImages(searchTerm);
                imageTilePane.getChildren().clear();
                int imageSquareSize = calculateImageSquareSize();
                for (int i = 0; i < 50; i++) {
                    ImageDataHolder imageDataHolder = images.get(i);
                    Image image = new Image(imageDataHolder.getThumbnailUrl());
                    ImageView croppedImage = AdvancedImageUtils.cropImage(image, imageSquareSize, imageSquareSize);
                    croppedImage.setCursor(Cursor.HAND);
                    croppedImage.setOnMouseEntered(event1 -> {
                        ColorAdjust colorAdjust = new ColorAdjust();
                        colorAdjust.setBrightness(0.2);
                        croppedImage.setEffect(colorAdjust);
                    });
                    croppedImage.setOnMouseExited(event1 -> croppedImage.setEffect(null));
                    croppedImage.setOnMouseClicked(event1 -> listener.onImageSelected(imageDataHolder));
                    imageTilePane.getChildren().add(croppedImage);
                }
                return null;
            }
        };
        previewImageLoaderTask.setOnSucceeded(event -> {
            setCenter(centerScrollPane);
        });
        new Thread(previewImageLoaderTask).start();*/
    }
}
