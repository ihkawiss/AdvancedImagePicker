package ch.fhnw.cuie.advancedimagepicker.services;

import ch.fhnw.cuie.advancedimagepicker.ImageDataHolder;

import java.util.List;

/**
 * @author Hoang Tran
 */
public interface ImageServiceListener {
    void onNewImageResults(List<ImageDataHolder> imageDataHolders);
    void onFinished();
}
