package services;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.collections.Collection;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.test.TestInterface;

import javafx.scene.image.Image;

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

		Flickr f = new Flickr(API_KEY, API_SECRET, new REST());
		TestInterface testInterface = f.getTestInterface();
		try {
			Collection rs = (Collection) testInterface.echo(Collections.EMPTY_MAP);
		} catch (FlickrException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SearchParameters s = new SearchParameters();
		s.setText(searchTerm);
		
		try {
			List<Photo> flickrResponse = service.getPhotosInterface().search(s, PER_PAGE_COUNT, 0);
			
			List<Image> results = new ArrayList<>();
			
			for(Photo p : flickrResponse) {
				results.add(new Image(p.getSmallAsInputStream()));
			}
			
			return results;
			
		} catch (FlickrException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
