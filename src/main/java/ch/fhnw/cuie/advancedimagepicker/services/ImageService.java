package ch.fhnw.cuie.advancedimagepicker.services;

import ch.fhnw.cuie.advancedimagepicker.ImageDataHolder;

import java.util.List;

/**
 * ImageService interface, it declares methods services must implement.
 * 
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public interface ImageService {

    ImageDataHolder getPreviewImage(String searchTerm);
    List<ImageDataHolder> getImages(String searchTerm, int numberOfImages, int pageIndex);

}
