package ch.fhnw.cuie.advancedimagepicker;

import java.io.InputStream;

/**
 * @author Hoang Tran
 */
public class ImageDataHolder {
    private final InputStream thumbnailInputStream;
    private final InputStream imageInputStream;

    public ImageDataHolder(InputStream thumbnailInputStream, InputStream imageInputStream) {
        this.thumbnailInputStream = thumbnailInputStream;
        this.imageInputStream = imageInputStream;
    }

    public InputStream getThumbnailInputStream() {
        return thumbnailInputStream;
    }

    public InputStream getImageInputStream() {
        return imageInputStream;
    }
}
