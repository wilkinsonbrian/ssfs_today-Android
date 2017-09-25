package org.ssfs.ssfstoday;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.ssfs.ssfstoday.R;
import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;

/**
 * A simple {@link Fragment} subclass.
 */
public class LibraryBeestro extends Fragment implements AsyncResponse {

    private static final String WEBSERVER = "https://grover.ssfs.org/menus/library_beestro.csv";
    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};
    GetDataFromServer asyncTask = new GetDataFromServer();

    private String todaysDate;
    private int currentDay;
    private TextView announce;
    private TextView dayOfWeek;
    private TextView libraryHours;
    private TextView beestroHours;


    public LibraryBeestro() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        todaysDate = getTodaysDate();
        asyncTask.delegate = this;
        asyncTask.execute(WEBSERVER);
    }
    @Override
    public void onResume() {
        super.onResume();
        asyncTask = new GetDataFromServer();
        asyncTask.delegate = this;
        asyncTask.execute(WEBSERVER);
    }


    private String getTodaysDate() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        currentDay = dayOfWeek - 1;
        return Integer.toString(month + 1) + "/" + Integer.toString(day) +"/" + Integer.toString(year);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_library_beestro, container, false);

        announce = (TextView) view.findViewById(R.id.announcements);
        libraryHours = (TextView) view.findViewById(R.id.lib_hours);
        beestroHours = (TextView) view.findViewById(R.id.beest_hours);
        dayOfWeek = (TextView) view.findViewById(R.id.date_label);
        dayOfWeek.setText(WEEKDAYS[currentDay]);
        return view;

    }

    public void processFinish(String output){
        String[] data = output.split(",");
        libraryHours.setText(data[1]);
        beestroHours.setText(data[2]);
        announce.setText(data[3]);
    }

    public class GetDataFromServer extends AsyncTask<String, Integer, String> {
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

        String line;
        while ((line = r.readLine()) != null) {
            String[] items = line.split(",");
            if (items[0].equals(todaysDate)) {
                return line;
            }
        }

        return "No data found";
    }

}
