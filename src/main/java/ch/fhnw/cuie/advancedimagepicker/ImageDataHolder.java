package ch.fhnw.cuie.advancedimagepicker;

/**
 * @author Hoang Tran
 */
public class ImageDataHolder {
    private final String thumbnailUrl;
    private final String imageUrl;

    public ImageDataHolder(String thumbnailUrl, String imageUrl) {
        this.thumbnailUrl = thumbnailUrl;
        this.imageUrl = imageUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
