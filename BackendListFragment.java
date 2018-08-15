package oishii.oishiiproject;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class BackendListFragment extends Fragment {
    private ListView mListView;
    private LocationTracker locationTracker;
    private static final String TAG = BackendListFragment.class.getSimpleName();

    public BackendListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // return inflater.inflate(R.layout.fragment_backend_list, container, false);
        View view = inflater.inflate(R.layout.fragment_backend_list, container, false);
        mListView = (ListView) view.findViewById(R.id.backend_list);
        getNearbyRestaurantThroughBackend();
        return view;

    }

    public void getNearbyRestaurantThroughBackend() {
        //String urlSearch = "http://10.0.2.2:8080/Oishii/search?lat=37.386051&lon=-122.083855";
        locationTracker = new LocationTracker(getActivity());
        locationTracker.getLocation();
        String urlSearch = "http://10.0.2.2:8080/Oishii/search?lat=" +
                Double.toString(locationTracker.getLatitude()) +
                "&lon=" + Double.toString(locationTracker.getLongitude()) + "&user_id=1111";

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest2 = new StringRequest(Request.Method.GET,
                urlSearch, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                new GetRestaurantsFromBackendAsyncTask(response).execute();
                Log.d(TAG, response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest2);
    }

    // Create a method that is able to parse the JSON string and generate a restuarants list, add the following method
    private class GetRestaurantsFromBackendAsyncTask extends
            AsyncTask<Void, Void, Void> {
        private String response;
        private List<Restaurant> restaurantList;

        public GetRestaurantsFromBackendAsyncTask(String response) {
            this.response = response;
            restaurantList = new ArrayList<Restaurant>();
        }

        // Create restaurants data
        @Override
        protected Void doInBackground(Void... params) {
            try {
                JSONArray reader = new JSONArray(response);
                for (int index = 0; index < reader.length(); index++) {
                    JSONObject item = reader.getJSONObject(index);
                    Restaurant restaurant = new Restaurant();

                    restaurant.setName(item.getString("name"));
                    restaurant.setAddress(item.getString("address"));
                    restaurant.setLat(item.getDouble("latitude"));
                    restaurant.setLng(item.getDouble("longitude"));
                    restaurant.setStars(item.getDouble("rating"));
                    restaurant.setUrl(item.getString("image_url"));
                    // restaurant.setFavorate(item.getBoolean("favorite"));
                    restaurant.setItem_id(item.getString("item_id"));
                    JSONArray category = item.getJSONArray("categories");
                    List<String> cat = new ArrayList<String>();
                    for (int j = 0; j < category.length(); j++) {
                        cat.add(category.get(j).toString());
                    }
                    restaurant.setCategories(cat);

                    restaurantList.add(restaurant);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            RestaurantBackendAdapter adapter = new RestaurantBackendAdapter(getContext(),
                    restaurantList);
            mListView.setAdapter(adapter);
        }
    }
}

