package ch.fhnw.cuie.advancedimagepicker;

/**
 * @author Hoang Tran
 */
public class ImageDataHolder {
    private final String thumbnailInputStream;
    private final String imageInputStream;

    public ImageDataHolder(String thumbnailInputStream, String imageInputStream) {
        this.thumbnailInputStream = thumbnailInputStream;
        this.imageInputStream = imageInputStream;
    }

    public String getThumbnailInputStream() {
        return thumbnailInputStream;
    }

    public String getImageInputStream() {
        return imageInputStream;
    }
}
