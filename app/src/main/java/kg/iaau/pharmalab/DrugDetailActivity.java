package kg.iaau.pharmalab;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import com.squareup.picasso.Picasso;

public class DrugDetailActivity extends Activity {

    private static final String TAG = DrugDetailActivity.class.getSimpleName();
    private static String URL = "https://densool.com/api/v1/drugs/";
    private ProgressDialog pDialog;

    ExpandableListView lvExp;
    TextView header, manufacturer, pharmGroup;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG, "Drug Detail Started");
        setContentView(R.layout.activity_drug_detail);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeAsUpIndicator(R.drawable.back2);
        getActionBar().setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        header = (TextView) findViewById(R.id.header);
        manufacturer = (TextView) findViewById(R.id.manufacturer);
        pharmGroup = (TextView) findViewById(R.id.pharm_group);
        image = (ImageView) findViewById(R.id.photo);
        lvExp = (ExpandableListView) findViewById(R.id.lvExp);
        fillContent();

        //close other expended groups while expanding another one
        lvExp.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            int previousGroup = -1;
            @Override
            public void onGroupExpand(int groupPosition) {
                if(groupPosition != previousGroup)
                    lvExp.collapseGroup(previousGroup);
                previousGroup = groupPosition;
                setListViewHeight(lvExp, groupPosition);
            }
        });
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

    private void setListViewHeight(ExpandableListView listView, int group) {
        ExpandableListAdapter listAdapter = listView.getExpandableListAdapter();
        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(),
                View.MeasureSpec.EXACTLY);
        for (int i = 0; i < listAdapter.getGroupCount(); i++) {
            View groupItem = listAdapter.getGroupView(i, false, null, listView);
            groupItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

            totalHeight += groupItem.getMeasuredHeight();

            if (((listView.isGroupExpanded(i)) && (i != group))
                    || ((!listView.isGroupExpanded(i)) && (i == group))) {
                for (int j = 0; j < listAdapter.getChildrenCount(i); j++) {
                    View listItem = listAdapter.getChildView(i, j, false, null,
                            listView);
                    listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);

                    totalHeight += listItem.getMeasuredHeight();

                }
            }
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        int height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getGroupCount() - 1));
        if (height < 10)
            height = 200;
        params.height = height;
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    private void fillContent(){
        new GetDrugInfo().execute(getIntent().getStringExtra("id"));
    }



    private class GetDrugInfo extends AsyncTask<String, Void, Void> {
        HashMap<String,String> drugInfo = new HashMap<>();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(DrugDetailActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        HashMap<String,String> m;
        ArrayList<HashMap<String,String>> groupData = new ArrayList<>();
        ArrayList<HashMap<String, String>> childDataItem;
        ArrayList<ArrayList<HashMap<String,String>>> childData=  new ArrayList<>();

        @Override
        protected Void doInBackground(String... arg0) {
            HttpHandler httpHandler = new HttpHandler();
            String jsonStr = httpHandler.makeServiceCall(URL.concat(arg0[0]+'/'));
            if (jsonStr != null) {
                try {
                    JSONObject jo = new JSONObject(jsonStr);
                    String header = jo.getString("name").concat(", "+jo.getString("form"));
                    String manufacturer = jo.getString("manufacturer").concat(", "
                            +jo.getString("country"));
                    String image = jo.getString("img");
                    String pharmaGroup = jo.getString("pharm_group");
                    drugInfo.put("header", header);
                    drugInfo.put("manufacturer", manufacturer);
                    drugInfo.put("image",image);
                    drugInfo.put("pharm_group",pharmaGroup);

                    JSONArray descriptions = jo.getJSONArray("descriptions");
                    for(int i=0; i < descriptions.length(); i++){
                        JSONObject joDesc = descriptions.getJSONObject(i);
                        String title = joDesc.getString("name");
                        m = new HashMap<>();
                        m.put("title", title);
                        groupData.add(m);

                        String text = joDesc.getString("text");
                        text = text.replaceAll("\\<.*?>","");
                        m = new HashMap<>();
                        m.put("text",text);
                        childDataItem = new ArrayList<>();
                        childDataItem.add(m);
                        childData.add(childDataItem);
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
            Picasso.with(getBaseContext()).load(drugInfo.get("image")).into(image);
            header.setText(drugInfo.get("header"));
            manufacturer.setText(drugInfo.get("manufacturer"));
            pharmGroup.setText(drugInfo.get("pharm_group"));

            ExpandableListAdapter adapter = new SimpleExpandableListAdapter( DrugDetailActivity.this,
                    groupData, R.layout.drug_info_item,
                    new String[]{"title"}, new int[]{R.id.lblListItem},
                    childData, R.layout.drug_info_item,
                    new String[]{"text"}, new int[]{R.id.lblListItem});

            lvExp.setAdapter(adapter);
            setListViewHeight(lvExp, lvExp.getAdapter().getCount() );
        }
    }

}
