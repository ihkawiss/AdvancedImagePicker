package ch.fhnw.cuie.advancedimagepicker.services;


import ch.fhnw.cuie.advancedimagepicker.ImageDataHolder;

import java.util.List;

public interface ImageService {

    ImageDataHolder getPreviewImage(String searchTerm);
    List<ImageDataHolder> getImages(String searchTerm);

}
