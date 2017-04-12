
package com.example.boklista2;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import static com.example.boklista2.R.id.forfattare;


public class MainActivity extends AppCompatActivity {

    private bokAdapter mAdapter;
    private ListView lv;
    private ArrayList<Bok> mBooksList;
    private ProgressBar Bar;
    private static final String LOG_TAG = MainActivity.class.getName();
    final String BOOK_REQUEST_URL = "https://www.googleapis.com/books/v1/volumes?q=";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Bar = (ProgressBar) findViewById(R.id.loading_indicator);
        Bar.setVisibility(View.GONE);
        lv = (ListView) findViewById(R.id.list);

//Kolla internet permission
        final int REQUEST_INTERNET_PERMISSION = 1;
        int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, new String[]{Manifest.permission.INTERNET},
                    REQUEST_INTERNET_PERMISSION);
        } else {

//click listener to Text edit search buttom
            Button sokKnapp = (Button) findViewById(R.id.sokKnapp);
            sokKnapp.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    Bar.setVisibility(View.VISIBLE);
                    BookAsyncTask task = new BookAsyncTask();
                    task.execute();
                }
            });
        }
    }

    private void updateUi() {
        final bokAdapter adapter = new bokAdapter(this, mBooksList);

        lv.setAdapter(adapter);

        lv.setVisibility(View.VISIBLE);

        Bar.setVisibility(View.GONE);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Bok currentBok = adapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri bokUri = Uri.parse(currentBok.getBokLank());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, bokUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

    }

    private class BookAsyncTask extends AsyncTask<URL, Void, ArrayList<Bok>> {

        TextView mSearchEditText = (TextView) findViewById(R.id.editText);
        private String searchInput = mSearchEditText.getText().toString();

        @Override
        protected ArrayList<Bok> doInBackground(URL... urls) {


            if (searchInput.length() == 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        Toast.makeText(MainActivity.this, getResources().getString(R.string.enter_search_keyword), Toast.LENGTH_SHORT).show();
                    }
                });
                return null;
            }

            //OBS! kolla det har om de verkligen stammmer
            searchInput = searchInput.replace(" ", "%20");

            // Create URL object
            URL url = createUrl(BOOK_REQUEST_URL + searchInput + "&maxResults=6");

            //Log.d(LOG_TAG, BOOK_REQUEST_URL + searchInput + "&maxResults=6");
            // Perform HTTP request to the URL and receive a JSON response back
            String jsonResponse = "";
            try {
                jsonResponse = makeHttpRequest(url);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IOException", e);
            }

            // Extract relevant fields from the JSON response and create an ArrayList of Book
            ArrayList<Bok> books = extractBookInfoFromJson(jsonResponse);


            // Return the {@link Event} object as the result fo the {@link BookAsyncTask}
            return books;
        }

        @Override
        protected void onPostExecute(ArrayList<Bok> bookList) {

            TextView mInfoTextView = (TextView) findViewById(R.id.tomView);
            if (bookList == null) {

                mInfoTextView.setVisibility(View.VISIBLE);
                return;
            }


            mInfoTextView.setVisibility(View.GONE);
            mBooksList = bookList;
            updateUi();

        }

        /**
         * Returns new URL object from the given string URL.
         */
        private URL createUrl(String stringUrl) {
            URL url = null;
            try {
                url = new URL(stringUrl);
            } catch (MalformedURLException exception) {
                Log.e(LOG_TAG, "Error with creating URL", exception);
                return null;
            }
            return url;
        }

        private String makeHttpRequest(URL url) throws IOException {
            String jsonResponse = "";
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.setReadTimeout(10000 /* milliseconds */);
                urlConnection.setConnectTimeout(15000 /* milliseconds */);
                urlConnection.connect();
                if (urlConnection.getResponseCode() == 200) {
                    inputStream = urlConnection.getInputStream();
                    jsonResponse = readFromStream(inputStream);
                } else {
                    Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the book JSON results.", e);
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (inputStream != null) {
                    // function must handle java.io.IOException here
                    inputStream.close();
                }
            }
            return jsonResponse;
        }

        private String readFromStream(InputStream inputStream) throws IOException {
            StringBuilder output = new StringBuilder();
            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
                BufferedReader reader = new BufferedReader(inputStreamReader);
                String line = reader.readLine();
                while (line != null) {
                    output.append(line);
                    line = reader.readLine();
                }
            }
            return output.toString();
        }

        private ArrayList<Bok> extractBookInfoFromJson(String bookJSON) {
            if (TextUtils.isEmpty(bookJSON)) {
                return null;
            }

            ArrayList<Bok> books = new ArrayList<Bok>();

            try {
                JSONObject baseJsonResponse = new JSONObject(bookJSON);
                if (baseJsonResponse.getInt("totalItems") == 0) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            Toast.makeText(MainActivity.this, getResources().getString(R.string.result_not_found), Toast.LENGTH_SHORT).show();
                        }
                    });

                    return null;
                }

                JSONArray itemArray = baseJsonResponse.getJSONArray("items");

                // If there are results in the items array
                for (int i = 0; i < itemArray.length(); i++) {
                    // Extract out the cuurent item (which is a book)
                    JSONObject cuurentItem = itemArray.getJSONObject(i);
                    JSONObject bookInfo = cuurentItem.getJSONObject("volumeInfo");

                    // Extract out the title, authors, and description
                    String title = bookInfo.getString("title");

                    String[] authors = new String[]{};
                    JSONArray authorJsonArray = bookInfo.optJSONArray("authors");
                    if (authorJsonArray != null) {
                        ArrayList<String> authorList = new ArrayList<String>();
                        for (int j = 0; j < authorJsonArray.length(); j++) {
                            authorList.add(authorJsonArray.get(j).toString());
                        }
                        authors = authorList.toArray(new String[authorList.size()]);

                    }
                    String description = "";
                    if (bookInfo.optString("description") != null)
                        description = bookInfo.optString("description");

                    String infoLink = "";
                    if (bookInfo.optString("infoLink") != null)
                        infoLink = bookInfo.optString("infoLink");


                    books.add(new Bok(title, authors, description, infoLink));
                    Log.d(LOG_TAG, title);
                    Log.d(LOG_TAG, description);
                    Log.d(LOG_TAG, infoLink);

                }
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the book JSON results", e);
            }

            return books;
        }

    }

}
