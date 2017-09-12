package referenceobjects.stores;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import referenceobjects.Rating;

public class RatingStore {
	private static RatingStore instance;
	
	private Map<String, Rating> ratingMap = new HashMap<>();
	
	private RatingStore() {};
	
	public static synchronized RatingStore getInstance() {
		if (instance == null) {
			instance = new RatingStore();
		}
		return instance;
	}
	
	public void addRating(Rating rat) {
		ratingMap.put(rat.getRating(), rat);
	}
	
	public Rating getRating(String rat) {
		if (!ratingMap.containsKey(rat)) {
			Rating newRat = new Rating(rat);
			ratingMap.put(rat,  newRat);
		}
		return ratingMap.get(rat);
	}
	
	public int getSize() {
		return ratingMap.size();
	}
	
	public Collection<Rating> getAllRatings() {
		return ratingMap.values();
	}

}
