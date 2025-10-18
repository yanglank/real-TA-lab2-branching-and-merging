package dogapi;

import java.util.*;

/**
 * This BreedFetcher caches fetch request results to improve performance and
 * lessen the load on the underlying data source. An implementation of BreedFetcher
 * must be provided. The number of calls to the underlying fetcher are recorded.
 *
 * If a call to getSubBreeds produces a BreedNotFoundException, then it is NOT cached
 * in this implementation. The provided tests check for this behaviour.
 *
 * The cache maps the name of a breed to its list of sub breed names.
 */
public class CachingBreedFetcher implements BreedFetcher {
    private int callsMade = 0;
    private final Map<String, List<String>> cache = new HashMap<>();
    private final BreedFetcher fetcher;

    public CachingBreedFetcher(BreedFetcher fetcher) {
        this.fetcher = Objects.requireNonNull(fetcher, "fetcher must not be null");
    }

    @Override
    public List<String> getSubBreeds(String breed) throws BreedNotFoundException {
        String key = (breed == null) ? "" : breed.trim().toLowerCase(Locale.ROOT);

        if (cache.containsKey(key)) {
            return cache.get(key);
        }

        try {
            List<String> subBreeds = fetcher.getSubBreeds(breed);
            callsMade++;
            List<String> safeList = (subBreeds == null)
                    ? List.of()
                    : Collections.unmodifiableList(new ArrayList<>(subBreeds));

            cache.put(key, safeList);
            return safeList;

        } catch (BreedNotFoundException e) {
            callsMade++;
            throw e;
        }
    }

    public int getCallsMade() {
        return callsMade;
    }
}