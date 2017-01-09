package ch.fhnw.cuie.advancedimagepicker.services;

/**
 * @author Hoang Tran
 */
public class AbstractImageService {
    private ImageServiceListener listener;

    public AbstractImageService(ImageServiceListener listener) {
        this.listener = listener;
    }
}
