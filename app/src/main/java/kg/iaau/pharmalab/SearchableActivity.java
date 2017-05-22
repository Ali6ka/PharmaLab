package kg.iaau.pharmalab;

import android.app.Activity;
import  android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;

public class SearchableActivity extends Activity {

    private static final String TAG = SearchableActivity.class.getSimpleName();
    private static String URL = "https://densool.com/api/v1/drugs/?search=";

    private ListView listView;
    private ProgressDialog pDialog;
    ArrayList<HashMap<String, String>> resultList;
    Intent intent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_searchable);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.back2);
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        resultList = new ArrayList<>();
        listView = (ListView) findViewById(R.id.listView);
        intent = new Intent(SearchableActivity.this, DrugDetailActivity.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent.putExtra("id", resultList.get((int)id).get("id"));
                startActivity(intent);
            }
        });
        handleIntent(getIntent());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    MySuggestionProvider.AUTHORITY, MySuggestionProvider.MODE);
            suggestions.saveRecentQuery(query, null);
            search(query);
        }
    }

    private void search(String query) {
        new GetResults().execute(query);
    }

    private class GetResults extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(SearchableActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.makeServiceCall(URL.concat(arg0[0]));
            if (jsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(jsonStr);
                    JSONArray drugs = jsonObj.getJSONArray("results");
                    // looping through All drugs
                    for (int i = 0; i < drugs.length(); i++) {
                        JSONObject c = drugs.getJSONObject(i);
                        String id = c.getString("id");
                        String name = c.getString("name");
                        // hash map for single drug
                        HashMap<String, String> drug = new HashMap<>();
                        drug.put("id", id);
                        drug.put("name", name);
                        // adding drug to contact list
                        resultList.add(drug);
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            ListAdapter adapter = new SimpleAdapter(
                    SearchableActivity.this, resultList,
                    R.layout.list_item, new String[]{"name", "id"}, new int[]{R.id.name,
                    R.id.id});
            listView.setAdapter(adapter);
        }
    }
}