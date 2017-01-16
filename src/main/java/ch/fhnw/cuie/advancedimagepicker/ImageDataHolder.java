package ch.fhnw.cuie.advancedimagepicker;

import java.io.InputStream;

/**
 * This class is used to represent image data of a specific
 * image received from a ImageService.
 * 
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
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
