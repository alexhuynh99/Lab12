package edu.illinois.cs.cs125.lab12;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Main class for our UI design lab.
 */
public final class MainActivity extends AppCompatActivity {
    /** Default logging tag for messages from the main activity. */
    private static final String TAG = "Lab12:Main";

    /** Request queue for our API requests. */
    private static RequestQueue requestQueue;

    /** This does stuff. */
    private TextView calories;

    /** This does stuff. */
    private TextView protein;

    /** This does stuff. */
    private TextView carbs;

    /** This does stuff. */
    private TextView fiber;

    /** This does stuff. */
    private TextView sugars;

    /** This does stuff. */
    private TextView fat;

    /** This does stuff. */
    private TextView sodium;

    /** Also does stuff. */
    private EditText inquiry;

    /** The API key that doesn't want to work?? */
    private String apiKey = "d0RWUmW3rtef51gTWysU7WxhphQpM76NXdcu6Krp";

    /**
     * Run when this activity comes to the foreground.
     *
     * @param savedInstanceState unused
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Set up the queue for our API requests
        requestQueue = Volley.newRequestQueue(this);

        setContentView(R.layout.activity_main);
        calories = findViewById(R.id.calories);
        protein = findViewById(R.id.protein);
        carbs = findViewById(R.id.carbs);
        fiber = findViewById(R.id.fiber);
        sugars = findViewById(R.id.sugar);
        fat = findViewById(R.id.fat);
        sodium = findViewById(R.id.sodium);
        inquiry = findViewById(R.id.inquiry);

        // Attach the handler to our UI button
        final Button startAPICall = findViewById(R.id.search);
        startAPICall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                Log.d(TAG, "Start API button clicked");
                startAPICall();
            }
        });
    }

    /**
     * Run when this activity is no longer visible.
     */
    @Override
    protected void onPause() {
        super.onPause();
    }

    /**
     * Make a call to the weather API.
     */
    void startAPICall() {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://api.nal.usda.gov/ndb/search/?format=json&q="
                            + inquiry.getText()
                            + "&sort=n&max=1&offset=0&api_key="
                            + apiKey,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                Log.d(TAG, response.toString(2));
                                setFacts(getID(response));
                                Log.d(TAG, response.toString());
                            } catch (JSONException ignored) { }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(final VolleyError error) {
                            Log.e(TAG, error.toString());
                        }
                    });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the ndbno id of the food item.
     * @param json does stuff.
     * @return a string.
     */
    public String getID(final JSONObject json) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject result = parser.parse(json.toString()).getAsJsonObject();
            return result.get("list").getAsJsonObject().get("item").getAsJsonArray().get(0).getAsJsonObject().get("ndbno").getAsString();
            //return "spoon";
        } catch (Exception e) {
            Log.w(TAG, "Error in getDescription!", e);
            return "";
        }
    }

    /**
     * Finds the facts
     * @param id the ndbno tag for the food item
     * @return the nutrition facts
     */
    void setFacts(final String id) {
        try {
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                    Request.Method.GET,
                    "https://api.nal.usda.gov/ndb/nutrients/?format=json&api_key="
                    + apiKey
                    + "&nutrients=208&nutrients=203&nutrients=205&nutrients=291&nutrients=269&nutrie" +
                            "nts=204&nutrients=307&ndbno="
                    + id,
                    null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(final JSONObject response) {
                            try {
                                Log.d(TAG, response.toString(2));
                                calories.setText(getCalories(response, "Calories"));
                                protein.setText(getCalories(response, "Protein"));
                                carbs.setText(getCalories(response, "Carbohydrates"));
                                fiber.setText(getCalories(response, "Fiber"));
                                sugars.setText(getCalories(response, "Sugar"));
                                fat.setText(getCalories(response, "Fat"));
                                sodium.setText(getCalories(response, "Sodium"));
                                Log.d(TAG, response.toString());
                            } catch (JSONException ignored) { }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(final VolleyError error) {
                    Log.e(TAG, error.toString());
                }
            });
            requestQueue.add(jsonObjectRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the specific fact
     * @param json does stuff.
     *             @param nutrient the nutrient desire
     * @return a string.
     */
    public String getCalories(final JSONObject json, final String nutrient) {
        try {
            JsonParser parser = new JsonParser();
            JsonObject result = parser.parse(json.toString()).getAsJsonObject();

            int position = 0;
            if (nutrient.equals("Calories")) { position = 4; }
            if (nutrient.equals("Protein")) { position = 0; }
            if (nutrient.equals("Carbohydrates")) { position = 3; }
            if (nutrient.equals("Fiber")) { position = 5; }
            if (nutrient.equals("Sugar")) { position = 1; }
            if (nutrient.equals("Fat")) { position = 2; }
            if (nutrient.equals("Sodium")) { position = 6; }
            String value = result.get("report").getAsJsonObject().get("foods").getAsJsonArray()
                    .get(0).getAsJsonObject()
                    .get("nutrients").getAsJsonArray().get(position)
                    .getAsJsonObject().get("value").getAsString();

            if (nutrient.equals("Calories")) { return nutrient + ": " + value; }
            if (nutrient.equals("Sodium")) { return nutrient + ": " + value + "mg"; }
            return nutrient + ": " + value + "g";
        } catch (Exception e) {
            Log.w(TAG, "Error in getCalories!", e);
            return "";
        }
    }
}
