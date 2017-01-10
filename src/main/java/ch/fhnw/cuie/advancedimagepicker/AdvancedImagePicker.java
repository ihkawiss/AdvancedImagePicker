package ch.fhnw.cuie.advancedimagepicker;

import ch.fhnw.cuie.advancedimagepicker.services.FlickrImageService;
import ch.fhnw.cuie.advancedimagepicker.services.ImageService;
import com.sun.javafx.scene.control.skin.ScrollBarSkin;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AdvancedImagePicker extends BorderPane {

    private static final int DEFAULT_PREF_TILE_SIZE = 350;

    private AdvancedImagePickerListener listener;
    private String searchTerm;
    private ImageService imageService;

    private TilePane imageTilePane;
    private ImageView loadingImageView;
    private int prefTileSize = DEFAULT_PREF_TILE_SIZE;
    private int imageTileGapSize = 20;
    private boolean initialLoadFinished = false;

    private AtomicInteger numberOfRemainingCallbacks = new AtomicInteger(0);
    private int numberOfImagesPerSearchRequest;
    private int numberOfPagesOnInitialSearchRequest;
    private int numberOfPagesOnLazyLoading;
    private int currentImageSearchPage = 0;
    private ScrollPane centerScrollPane;


    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener) {
        super();
        this.listener = listener;
        imageService = new FlickrImageService();

        numberOfImagesPerSearchRequest = 1;
        numberOfPagesOnInitialSearchRequest = 50;
        numberOfPagesOnLazyLoading = 10;

        loadingImageView = new ImageView(AdvancedImageView.LOADING_IMAGE);
        loadingImageView.setFitHeight(prefTileSize);
        loadingImageView.setFitWidth(prefTileSize);

        init(searchTerm);
    }

    private void init(final String searchTerm) {
        // top bar
        initTopBar(searchTerm);

        // center
        initCenterPane(searchTerm);

        // initial image load
        showImageSearchResults(searchTerm);
    }

    private void initTopBar(String searchTerm) {
        TextField tfSearch = new TextField(searchTerm);
        Button btnSearch = new Button("Bilder anzeigen");
        tfSearch.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                showImageSearchResults(tfSearch.getText());
            }
        });
        btnSearch.setOnMouseClicked(event -> showImageSearchResults(tfSearch.getText()));
        BorderPane topBorderPane = new BorderPane();
        topBorderPane.setCenter(tfSearch);
        topBorderPane.setRight(btnSearch);
        setTop(topBorderPane);
    }

    private void initCenterPane(final String searchTerm) {
        // tilePane containing the image results
        imageTilePane = new TilePane();
        imageTilePane.setStyle("--fx-border-width: 0; -fx-padding: 0;");
        imageTilePane.setHgap(imageTileGapSize);
        imageTilePane.setVgap(imageTileGapSize);
        imageTilePane.prefWidthProperty().bind(widthProperty());
        imageTilePane.prefWidthProperty().addListener((a, b, c) -> resizeImageTiles());

        // scrollPane containing the tilePane to support scrolling
        centerScrollPane = new ScrollPane(imageTilePane);
        centerScrollPane.setStyle("-fx-border-width: 0; -fx-padding: 0;  ");
        centerScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        centerScrollPane.vvalueProperty().addListener((observable, oldValue, newValue) -> {
            if (initialLoadFinished && newValue.intValue() == 1) {
                startImageSearch(searchTerm, numberOfImagesPerSearchRequest, numberOfPagesOnLazyLoading);
                // jump back to position 0.999 so user can scroll to position 1 again to trigger lazy loading
                centerScrollPane.setVvalue(0.999);
            }
        });
        setCenter(centerScrollPane);
    }

    public void showImageSearchResults(String searchTerm) {
        this.searchTerm = searchTerm;
        imageTilePane.getChildren().clear();
        startImageSearch(searchTerm, numberOfImagesPerSearchRequest, numberOfPagesOnInitialSearchRequest);
    }

    public void startImageSearch(String searchTerm, int imagesPerPage, int numberOfPages) {
        if (!imageTilePane.getChildren().contains(loadingImageView)) {
            imageTilePane.getChildren().add(loadingImageView);
            numberOfRemainingCallbacks.set(numberOfPages);
            Task<Void> imageSearchTask = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    for (int i = 0; i < numberOfPages; i++) {
                        currentImageSearchPage++;
                        List<ImageDataHolder> imageDataHolders = imageService.getImages(searchTerm, imagesPerPage, currentImageSearchPage);
                        onNewImageResults(imageDataHolders);
                    }
                    return null;
                }
            };
            //imageSearchTask.setOnSucceeded(event -> listener.onFinished());
            new Thread(imageSearchTask).start();
        }
    }

    protected void onNewImageResults(final List<ImageDataHolder> imageDataHolders) {
        // prepare image tiles for search results
        Task<List<ImageView>> imageTileTask = new Task<List<ImageView>>() {
            @Override
            protected List<ImageView> call() throws Exception {
                List<ImageView> imageViews = new ArrayList<>();
                for (ImageDataHolder imageDataHolder : imageDataHolders) {
                    // create Image with image data
                    Image image = new Image(imageDataHolder.getThumbnailInputStream());

                    // create styled image tile
                    ImageView tile = createImageTile(image);

                    // add on click listener
                    tile.setOnMouseClicked(event -> listener.onImageSelected(imageDataHolder));

                    // add to list
                    imageViews.add(tile);
                }
                return imageViews;
            }

            ;
        };

        // add image tiles to tilePane
        imageTileTask.setOnSucceeded(event -> {
            int tileSize = calculateTileSize();
            ObservableList<Node> tiles = imageTilePane.getChildren();

            List imageViews = (List) event.getSource().getValue();
            for (Object imageViewObject : imageViews) {
                ImageView imageView = (ImageView) imageViewObject;
                imageView.setFitWidth(tileSize);
                imageView.setFitHeight(tileSize);

                // add as second last tile to keep loadingImageTile as last tile
                tiles.add(tiles.size() - 1, imageView);
            }

            if (numberOfRemainingCallbacks.decrementAndGet() == 0) {
                onFinished();
            }
        });
        new Thread(imageTileTask).start();
    }

    protected ImageView createImageTile(Image image) {
        // use util method to crop image and create ImageView with prefTileSize (will be resized later)
        ImageView tile = AdvancedImageUtils.createCroppedImageView(image, prefTileSize, prefTileSize);

        // set hand cursor on tiles to signalize clickable tile
        tile.setCursor(Cursor.HAND);
        tile.setOnMouseExited(event -> tile.setEffect(null));

        // add highlight hover effect
        tile.setOnMouseEntered(event -> {
            ColorAdjust colorAdjust = new ColorAdjust();
            colorAdjust.setBrightness(0.2);
            tile.setEffect(colorAdjust);
        });
        return tile;
    }

    protected void onFinished() {
        ObservableList<Node> tiles = imageTilePane.getChildren();
        if (tiles.contains(loadingImageView)) {
            tiles.remove(loadingImageView);
        }

        // set flag
        initialLoadFinished = true;

        if (centerScrollPane.getVvalue() == 1) {
            // jump back to position 0.999 so user can scroll to position 1 again to trigger lazy loading
            centerScrollPane.setVvalue(0.999);
        }

        // resize tiles in case window has been resized
        resizeImageTiles();
    }

    protected void resizeImageTiles() {
        int tileSize = calculateTileSize();
        imageTilePane.getChildren().stream().filter(imageTile -> imageTile instanceof ImageView).forEach(imageTile -> {
            ((ImageView) imageTile).setFitWidth(tileSize);
            ((ImageView) imageTile).setFitHeight(tileSize);
        });
    }

    protected int calculateTileSize() {
        // subtract scrollBar width to prevent tiles from getting overlapped by scrollBar
        double tilesArea = imageTilePane.getPrefWidth() - ScrollBarSkin.DEFAULT_WIDTH;

        // calculate number of tile columns
        int numberOfColumns = (int) Math.round(tilesArea / prefTileSize);

        // fill tilesArea width if only one tile per row
        if (numberOfColumns < 2) {
            return (int) tilesArea;
        }

        // calculate number of gaps between tiles columns
        int numberOfGaps = numberOfColumns - 1;

        // calculate total width of all gaps in one column together
        int totalGapsWidth = numberOfGaps * imageTileGapSize;

        // calculate total width of all tiles in one column together
        double totalTilesWidth = tilesArea - totalGapsWidth;

        // calculate and return size for a tile in a row with multiple columns
        return (int) (totalTilesWidth / numberOfColumns);
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
