package ch.fhnw.cuie.advancedimagepicker;

/**
 * Interface for Listener that gets notified if an image has been selected with the {@link AdvancedImagePicker}.
 *
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public interface AdvancedImagePickerListener {

    /**
     * Callback method to be called when an image has been selected.
     *
     * @param image Selected image
     */
    void onImageSelected(ImageDataHolder image);
}
