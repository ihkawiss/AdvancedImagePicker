package ch.fhnw.cuie.advancedimagepicker.services;

import ch.fhnw.cuie.advancedimagepicker.ImageDataHolder;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FlickrImageService implements ImageService{

	private final String API_KEY = "44b9f70edc1ac4b26247794e6196bc16";
	private final String API_SECRET = "6c77bfcd85dfeb59";
	private final int PER_PAGE_COUNT = 1;
	
	private Flickr service;

	public FlickrImageService() {
		service = new Flickr(API_KEY, API_SECRET, new REST());
	}
	
	@Override
	public ImageDataHolder getPreviewImage(String searchTerm) {
		return getImages(searchTerm, 1, 0).get(0);
	}

	@Override
	public List<ImageDataHolder> getImages(String searchTerm) {
		return getImages(searchTerm, 50, 0);
	}

	private List<ImageDataHolder> getImages(String searchTerm, int imageCount, int pageNum) {
		ArrayList<ImageDataHolder> images = new ArrayList<>();

		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setAccuracy(1);
		searchParameters.setText(searchTerm);

		try {
			PhotoList<Photo> list = service.getPhotosInterface().search(searchParameters, imageCount, pageNum);
			if (list.isEmpty()) {
				System.out.println("empty");
			}

			Iterator itr = list.iterator();
			while (itr.hasNext()) {
				Photo photo = (Photo) itr.next();
				ImageDataHolder image = new ImageDataHolder(photo.getMediumUrl(), photo.getLargeUrl());
				images.add(image);
			}
		} catch (FlickrException e) {
			e.printStackTrace();
		}
		return images;
	}
}