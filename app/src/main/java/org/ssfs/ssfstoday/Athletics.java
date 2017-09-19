package org.ssfs.ssfstoday;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 */
public class Athletics extends Fragment implements AsyncResponse {

    GetScheduleFromServer asyncTask = new GetScheduleFromServer();
    private static final String WEBSERVER = "https://www.ssfs.org/athletics/athletics-today";
    public int day;
    public int currentDay;
    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};

    private TextView games;
    private TextView dayOfWeek;
    private String dailyGames;
    private int dayOfMonth;


    String rawHtml;
    public Athletics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_WEEK);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        currentDay = day - 1;

        asyncTask.delegate = this;
        asyncTask.execute(WEBSERVER);

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_athletics, container, false);
        games = (TextView) view.findViewById(R.id.sports_label);
        dayOfWeek = (TextView) view.findViewById(R.id.date_label);
        dayOfWeek.setText(WEEKDAYS[currentDay]);


        return view;
    }

    public void processFinish(String output){

        games.setText(getGames());
    }

    public class GetScheduleFromServer extends AsyncTask<String, Integer, String> {
        public AsyncResponse delegate = null;
        String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    // Log.v("CatalogClient", server_response);
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            rawHtml = server_response;
            delegate.processFinish(rawHtml);

        }
    }

// Converting InputStream to String

    private String readStream(InputStream in) {
        BufferedReader reader = null;
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    public String getGames() {
        StringBuilder schedule = new StringBuilder();
        Pattern pattern = Pattern.compile("\"fsDay\">" + dayOfMonth + "<(.*?)a>");
        Matcher m = pattern.matcher(rawHtml);
        while (m.find()) {
            String newString = m.group(1);
            Pattern nextPattern = Pattern.compile("href=\"#\">(.*?)</");
            Matcher n = nextPattern.matcher(newString);
            if (n.find()) {
                schedule.append(n.group(1));
                schedule.append("\n");
            }
        }
        return schedule.toString();
    }

}
