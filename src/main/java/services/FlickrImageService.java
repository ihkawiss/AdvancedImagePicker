package services;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;
import javafx.scene.image.Image;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlickrImageService implements ImageService{

	private final String API_KEY = "44b9f70edc1ac4b26247794e6196bc16";
	private final String API_SECRET = "6c77bfcd85dfeb59";
	private final int PER_PAGE_COUNT = 1;
	
	private Flickr service;
	
	public void FlickrImageImageSe() {
		service = new Flickr(API_KEY, API_SECRET, new REST());
	}
	
	@SuppressWarnings("restriction")
	@Override
	public Image getPreviewImage(String searchTerm) {
		
		return getImages(searchTerm).get(0);
	}

	@SuppressWarnings("restriction")
	@Override
	public List<Image> getImages(String searchTerm) {
		ArrayList<Image> images = new ArrayList<>();

		Flickr flickr = new Flickr(API_KEY, API_SECRET, new REST());
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setAccuracy(1);
		searchParameters.setText(searchTerm);

		try {
			PhotoList<Photo> list = flickr.getPhotosInterface().search(searchParameters, 100, 0);
			if (list.isEmpty()) {
				System.out.println("empty");
			}

			Iterator itr = list.iterator();
			while (itr.hasNext()) {
				Photo photo = (Photo) itr.next();
				Image image = new Image(photo.getSquareLargeUrl());
				images.add(image);
			}
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return images;
	}

}
