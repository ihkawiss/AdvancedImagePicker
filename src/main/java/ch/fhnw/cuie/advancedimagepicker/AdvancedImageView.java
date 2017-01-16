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
 * JavaFX CUSTOM CONTROL AdvancedImageView
 * 
 * This class represents the actual custom control. It is based on a JavaFX
 * ImageView and enhanced by additional features such as: - image can be loaded
 * by an ImageService (interface) - extension with own service is supported by
 * design - integrated picker to replace loaded image - search functionality to
 * filter loaded images - enhanced performance by threading - easy to use due to
 * small api
 * 
 * This control was developed during the module cuie at the FHNW.
 * 
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public class AdvancedImageView extends ImageView implements AdvancedImagePickerListener {

	// prepare loading animation
	private static final String DEFAULT_LOADING_GIF_URL = "http://goo.gl/ys7ob7";
	protected static final Image LOADING_IMAGE = new Image(DEFAULT_LOADING_GIF_URL);

	private String searchTerm;
	private Stage pickerDialog;
	private ImageService imageService;

	// initialize loading animation on new objects
	public AdvancedImageView(String searchTerm) {
		super(LOADING_IMAGE);
		setPreserveRatio(true);
		this.searchTerm = searchTerm;
		imageService = new FlickrImageService();
		loadPreviewImage();
	}

	// Loads preview image in a thread to prevent UI from getting blocked.
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
				System.err.println("Preview image is not an instance of ImageDataHolder: "
						+ previewImageDataHolder.getClass().toString());
			}
		});

		new Thread(previewImageLoaderTask).start();
	}

	// bind event on loaded images
	protected void onPreviewImageLoaded() {
		setCursor(Cursor.HAND);
		setOnMouseClicked(event -> {
			onClick();
		});
	}

	// define onClick for all ImageViews
	protected void onClick() {
		AdvancedImagePicker advancedImagePicker = new AdvancedImagePicker(searchTerm, this);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		Parent root = new BorderPane(advancedImagePicker);
		
		double dialogWidth = screenSize.getHeight();
		double dialogHeight = dialogWidth * 3 / 4;
		
		pickerDialog = new Stage();
		pickerDialog.setScene(new Scene(root));
		pickerDialog.setTitle("AdvancedImagePicker");
		pickerDialog.setWidth(dialogWidth);
		pickerDialog.setHeight(dialogHeight);
		pickerDialog.initModality(Modality.WINDOW_MODAL);
		pickerDialog.initOwner(getScene().getWindow());
		pickerDialog.show();
	}

	// Getter & Setters
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
	// Handle process after image was selected in picker view
	public void onImageSelected(ImageDataHolder imageDataHolder) {
		Image image = new Image(imageDataHolder.getImageInputStream());
		ImageView croppedImageView = AdvancedImageUtils.createCroppedImageView(image, getFitWidth(), getFitHeight());
		setImage(croppedImageView.getImage());
		if (pickerDialog.isShowing()) {
			pickerDialog.close();
		}
	}
}
