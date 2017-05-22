package kg.iaau.pharmalab;

import android.content.SearchRecentSuggestionsProvider;

/**
 * Created by ali on 5/22/17.
 */

public class MySuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "kg.iaau.pharmalab.MySuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public MySuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
