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

/**
 * Picker View to provide image results based on specified search termn.
 *
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public class AdvancedImagePicker extends BorderPane {

    private static final int DEFAULT_PREF_TILE_SIZE = 350;
    private static final int DEFAULT_NOF_IMG_PER_SEARCH = 1;
    private static final int DEFAULT_NOF_IMG_ON_INITIAL_SEARCH = 50;
    private static final int DEFAULT_NOF_PAGES_ON_LAZY_LOADING = 10;
    private static final int DEFAULT_IMG_TILE_GAP_SIZE = 20;
    private static final double LAZY_LOADING_CONFORM_SCROLLBAR_POSITION = 0.999;
    private static final String SEARCH_BUTTON_TEXT = "Bilder anzeigen";

    private AdvancedImagePickerListener listener;
    private String searchTerm;
    private ImageService imageService;

    private TilePane imageTilePane;
    private ImageView loadingImageView;
    private int prefTileSize = DEFAULT_PREF_TILE_SIZE;

    private int imageTileGapSize = DEFAULT_IMG_TILE_GAP_SIZE;
    private boolean initialLoadFinished = false;

    private AtomicInteger numberOfRemainingCallbacks = new AtomicInteger(0);
    private int numberOfImagesPerSearchRequest;
    private int numberOfPagesOnInitialSearchRequest;
    private int numberOfPagesOnLazyLoading;
    private int currentImageSearchPage = 0;
    private ScrollPane centerScrollPane;

    /**
     * @see #AdvancedImagePicker(String, AdvancedImagePickerListener, ImageService, int, int, int)
     */
    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener) {
        this(searchTerm, listener, new FlickrImageService());
    }

    /**
     * @see #AdvancedImagePicker(String, AdvancedImagePickerListener, ImageService, int, int, int)
     */
    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener, ImageService imageService) {
        this(searchTerm, listener, imageService, DEFAULT_NOF_IMG_PER_SEARCH, DEFAULT_NOF_IMG_ON_INITIAL_SEARCH, DEFAULT_NOF_PAGES_ON_LAZY_LOADING);
    }

    /**
     * Constructor for {@link AdvancedImagePicker}.
     *
     * @param searchTerm                          Search term
     * @param listener                            {@link AdvancedImagePickerListener} to get called
     * @param imageService                        {@link ImageService} to be used
     * @param numberOfImagesPerSearchRequest      Number of Images per search request
     * @param numberOfPagesOnInitialSearchRequest Number of pages on initial search request
     * @param numberOfPagesOnLazyLoading          Number of pages on lazy loading
     */
    public AdvancedImagePicker(String searchTerm, AdvancedImagePickerListener listener, ImageService imageService, int numberOfImagesPerSearchRequest, int numberOfPagesOnInitialSearchRequest, int numberOfPagesOnLazyLoading) {
        super();
        this.listener = listener;
        this.imageService = imageService;

        this.numberOfImagesPerSearchRequest = numberOfImagesPerSearchRequest;
        this.numberOfPagesOnInitialSearchRequest = numberOfPagesOnInitialSearchRequest;
        this.numberOfPagesOnLazyLoading = numberOfPagesOnLazyLoading;

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
        Button btnSearch = new Button(SEARCH_BUTTON_TEXT);
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
                centerScrollPane.setVvalue(LAZY_LOADING_CONFORM_SCROLLBAR_POSITION);
            }
        });
        setCenter(centerScrollPane);
    }

    /**
     * Starts new image search by clearing current image search results
     * and calling {@link #startImageSearch(String, int, int)} with default values.
     *
     * @param searchTerm Search term of image search
     */
    public void showImageSearchResults(String searchTerm) {
        this.searchTerm = searchTerm;
        imageTilePane.getChildren().clear();
        startImageSearch(searchTerm, numberOfImagesPerSearchRequest, numberOfPagesOnInitialSearchRequest);
    }

    /**
     * Starts image search to given search term.
     * {@link #onNewImageResults(List)} will be called with a list of number of images equals to given imagesPerPage
     * and this will be repeated until counter reaches given numberOfPages.
     * New Images will be added to current image search results.
     *
     * @param searchTerm    Search term of image search
     * @param imagesPerPage Number of images per page to be loaded
     * @param numberOfPages Number of pages to be loaded
     */
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
            //imageSearchTask.setOnSucceeded(event -> listener.onImageSearchFinished());
            new Thread(imageSearchTask).start();
        }
    }

    /**
     * Called when new images are ready to be shown.
     *
     * @param imageDataHolders List of new images to be shown
     */
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
                onImageSearchFinished();
            }
        });
        new Thread(imageTileTask).start();
    }

    /**
     * Creates an ImageView tile from given Image.
     *
     * @param image Image to create an image tile from
     * @return Created image tile
     */
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

    /**
     * Callback method called when all images of the requested image search are loaded and shown.
     */
    protected void onImageSearchFinished() {
        ObservableList<Node> tiles = imageTilePane.getChildren();
        if (tiles.contains(loadingImageView)) {
            tiles.remove(loadingImageView);
        }

        // set flag
        initialLoadFinished = true;

        if (centerScrollPane.getVvalue() == 1) {
            // jump back to position 0.999 so user can scroll to position 1 again to trigger lazy loading
            centerScrollPane.setVvalue(LAZY_LOADING_CONFORM_SCROLLBAR_POSITION);
        }

        // resize tiles in case window has been resized
        resizeImageTiles();
    }

    /**
     * Resize all image tiles to optimal size based on size of this View and set preferred tile size.
     *
     * @see #setPrefTileSize(int)
     */
    protected void resizeImageTiles() {
        int tileSize = calculateTileSize();
        imageTilePane.getChildren().stream().filter(imageTile -> imageTile instanceof ImageView).forEach(imageTile -> {
            ((ImageView) imageTile).setFitWidth(tileSize);
            ((ImageView) imageTile).setFitHeight(tileSize);
        });
    }

    /**
     * Calculates the size for an image tile based on set preferred tile size ({@link #setPrefTileSize(int)}).
     *
     * @return Calculated image tile size
     * @see #setPrefTileSize(int)
     */
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

    public int getPrefTileSize() {
        return prefTileSize;
    }

    public void setPrefTileSize(int prefTileSize) {
        this.prefTileSize = prefTileSize;

        // resize tiles based on new preferred tile size
        resizeImageTiles();
    }

    public int getNumberOfImagesPerSearchRequest() {
        return numberOfImagesPerSearchRequest;
    }

    public void setNumberOfImagesPerSearchRequest(int numberOfImagesPerSearchRequest) {
        this.numberOfImagesPerSearchRequest = numberOfImagesPerSearchRequest;
    }

    public int getNumberOfPagesOnInitialSearchRequest() {
        return numberOfPagesOnInitialSearchRequest;
    }

    public void setNumberOfPagesOnInitialSearchRequest(int numberOfPagesOnInitialSearchRequest) {
        this.numberOfPagesOnInitialSearchRequest = numberOfPagesOnInitialSearchRequest;
    }

    public int getNumberOfPagesOnLazyLoading() {
        return numberOfPagesOnLazyLoading;
    }

    public void setNumberOfPagesOnLazyLoading(int numberOfPagesOnLazyLoading) {
        this.numberOfPagesOnLazyLoading = numberOfPagesOnLazyLoading;
    }

    public int getImageTileGapSize() {
        return imageTileGapSize;
    }

    public void setImageTileGapSize(int imageTileGapSize) {
        this.imageTileGapSize = imageTileGapSize;

        // change gap size
        imageTilePane.setHgap(imageTileGapSize);
        imageTilePane.setVgap(imageTileGapSize);

        // resize tiles with new gap size
        resizeImageTiles();
    }
}
