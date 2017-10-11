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
    private static final String WEBSERVER = "https://grover.ssfs.org/menus/athletics_schedule.csv";
    public int day;
    public int currentDay;
    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};

    private TextView games;
    private TextView dayOfWeek;
    private DateInfo today;
    private String todaysDate;



    String rawHtml;
    public Athletics() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getDateInformation();
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

    @Override
    public void onResume() {
        super.onResume();
        getDateInformation();
        asyncTask = new GetScheduleFromServer();
        asyncTask.delegate = this;
        asyncTask.execute(WEBSERVER);
    }

    public void getDateInformation() {
        today = new DateInfo();
        currentDay = today.getCurrentDay();
        todaysDate = today.getTodaysDate();
        Log.v("Date: ", todaysDate);
    }

    public void processFinish(String output){
        dayOfWeek.setText(WEEKDAYS[currentDay]);
        games.setText(output);
    }



    public class GetScheduleFromServer extends AsyncTask<String, Integer, String> {
        public AsyncResponse delegate = null;

        @Override
        protected String doInBackground(String... params) {

            try {
                return downloadUrl(params[0]);
            } catch (IOException e) {
                return "Unable to retrieve web page. URL may be invalid.";
            }
        }


        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }

        private String downloadUrl(String myurl) throws  IOException {
            InputStream is = null;

            try {

                /*
                Reads the XML file from server (grover) and returns the raw xml
                 */
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000 /* milliseconds */);
                conn.setConnectTimeout(15000 /* milliseconds */);
                conn.setRequestMethod("GET");
                conn.connect();
                is = conn.getInputStream();

                return readIt(is);
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }
    /*
    Takes the URL data and appends each line of XML one by one to the Stringbuilder.
    The final string returned is the complete XML file with all the tags.
     */
    public String readIt(InputStream stream) throws IOException, UnsupportedEncodingException {
        BufferedReader r = new BufferedReader(new InputStreamReader(stream));
        String todaysSchedule = "";
        String line;
        while ((line = r.readLine()) != null) {
            String[] items = line.split(",");
            if (items[0].equals(todaysDate)) {
                todaysSchedule += items[1] + ": " + items[3] + " vs. " + items[5] + " (" + items[4] + ")" + "\n\n";
            }
        }

        if (todaysSchedule.equals("")) {
            return "No Games Today";
        } else {
            return todaysSchedule;
        }

    }

}
