package services;

import javafx.scene.image.Image;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of {@link ImageService} for Yahoo.
 *
 * @author Hoang Tran
 */
public class YahooImageService implements ImageService {

    private static final String IMAGE_SEARCH_URL = "https://de.images.search.yahoo.com/search/images?p=";
    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.76 Mobile Safari/537.36";
    private static final String IMAGE_CONTAINER_SELECTOR = "#results";

    public Image getPreviewImage(String searchTerm) {
        List<Image> result = getImages(searchTerm, 1);
        return result.isEmpty() ? null : result.get(0);
    }

    public List<Image> getImages(String searchTerm) {
        return getImages(searchTerm, 0);
    }

    /**
     * Get image result of given search term.
     * Number of results is limited to numbers of images provided by yahoo and/or given limit.
     *
     * @param searchTerm Search term to find image results to
     * @param limit      0 for no limit, otherwise positive number
     * @return List of Image elements
     */
    private List<Image> getImages(String searchTerm, int limit) {
        List<Image> results = new ArrayList<>();

        // adjust search term to be usable in yahoo search url
        searchTerm = searchTerm.trim().replace(" ", "+");

        if (!searchTerm.isEmpty()) {
            try {
                // get image search result html
                Document doc = Jsoup.connect(IMAGE_SEARCH_URL + searchTerm).userAgent(USER_AGENT).get();

                // get image container element
                Elements imageContainers = doc.select(IMAGE_CONTAINER_SELECTOR);
                if (imageContainers.size() > 0) {
                    Element imageContainer = imageContainers.get(0);

                    // iterate over img elements
                    int counter = 0;
                    for (Element imgElement : imageContainer.select("img")) {
                        String url = imgElement.attr("src");
                        results.add(new Image(url));
                        counter++;

                        if (limit > 0 && counter >= limit) {
                            // stopped if reached limit
                            return results;
                        }
                    }
                }

            } catch (IOException e) {
                System.err.println("Could not get images: " + e.getMessage());
            }
        }

        return results;
    }
}
