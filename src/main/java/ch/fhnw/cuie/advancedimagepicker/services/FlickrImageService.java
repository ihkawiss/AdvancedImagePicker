package ch.fhnw.cuie.advancedimagepicker.services;

import ch.fhnw.cuie.advancedimagepicker.ImageDataHolder;
import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.PhotoList;
import com.flickr4java.flickr.photos.SearchParameters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * This is the concrete Flickr ImageService.
 * 
 * @author Hoang Tran <hoang.tran@students.fhnw.ch>
 * @author Kevin Kirn <kevin.kirn@students.fhnw.ch>
 */
public class FlickrImageService implements ImageService {

	private static final String API_KEY = "44b9f70edc1ac4b26247794e6196bc16";
	private static final String API_SECRET = "6c77bfcd85dfeb59";
	private static final String DEFAULT_SEARCH_MEDIA_TYPE = "photos";
	private static final int DEFAULT_SEARCH_ACCURACY = 1;
	private static final int DEFAULT_SEARCH_SORTING = SearchParameters.RELEVANCE;

	private Flickr flickr;

	public FlickrImageService() {
		this(API_KEY, API_SECRET);
	}

	public FlickrImageService(String apiKey, String apiSecret) {
		flickr = new Flickr(apiKey, apiSecret, new REST());
	}

	@Override
	public ImageDataHolder getPreviewImage(String searchTerm) {
		return getImages(searchTerm, 1, 0).get(0);
	}

	@Override
	public List<ImageDataHolder> getImages(String searchTerm, int numberOfImages, int pageIndex) {

		// prepare search parameters
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setText(searchTerm);
		searchParameters.setAccuracy(DEFAULT_SEARCH_ACCURACY);
		searchParameters.setSort(DEFAULT_SEARCH_SORTING);

		// try to define media type
		try {
			searchParameters.setMedia(DEFAULT_SEARCH_MEDIA_TYPE);
		} catch (FlickrException e) {
			System.out.println("Failed to set media to photos: " + e.getMessage());
		}

		// prepare result holder
		ArrayList<ImageDataHolder> images = new ArrayList<>();
		
		try {
			
			// pass search request to api
			PhotoList<Photo> photoList = flickr.getPhotosInterface().search(searchParameters, numberOfImages, pageIndex);

			if (photoList.isEmpty()) {
				System.out.println("Returning empty list. No Image Results found for: " + searchTerm);
			}

			// put received photos into holder
			for (Photo photo : photoList) {
				ImageDataHolder imageDataHolder = null;

				try {
					imageDataHolder = new ImageDataHolder(photo.getMediumAsStream(), photo.getLargeAsStream());
				} catch (IOException e) {
					e.printStackTrace();
				}

				images.add(imageDataHolder);
			}
			
		} catch (FlickrException e) {
			System.err.println("Returning empty list. Image Search for \"" + searchTerm + "\" failed: " + e.getMessage());
		}

		return images;
	}
}
