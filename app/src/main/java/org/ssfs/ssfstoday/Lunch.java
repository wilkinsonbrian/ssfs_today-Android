package org.ssfs.ssfstoday;


import android.graphics.drawable.ShapeDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;


/**
 * A simple {@link Fragment} subclass.
 */
public class Lunch extends Fragment implements AsyncResponse{

    GetLunchMenuFromServer asyncTask = new GetLunchMenuFromServer();
    private TextView entree;
    private TextView veggie;
    private TextView sides;
    private TextView deli;
    private TextView dayOfWeek;
    public int day;
    public int currentDay;
    //private ShapeDrawable background;

    private LunchMenu weeklyMenu;

    private static final String WEBSERVER = "https://grover.ssfs.org/menus/word/document.xml";
    private static final String[] WEEKDAYS = {"Sunday", "Monday", "Tuesday",
            "Wednesday", "Thursday", "Friday", "Saturday"};


    public Lunch() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Calendar calendar = Calendar.getInstance();
        day = calendar.get(Calendar.DAY_OF_WEEK);
        currentDay = day - 1;

        // Starts the AsyncTask that actually retrieves the lunch data from the server.
        asyncTask.delegate = this;
        asyncTask.execute(WEBSERVER);
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_lunch, container, false);
        entree = (TextView) view.findViewById(R.id.lunch_entree);
        veggie = (TextView) view.findViewById(R.id.veggie_entree);
        sides = (TextView) view.findViewById(R.id.sides);
        deli = (TextView) view.findViewById(R.id.deli);
        //background = (ShapeDrawable) view.findViewById(R.id.ba)
        dayOfWeek = (TextView) view.findViewById(R.id.date_label);
        dayOfWeek.setText(WEEKDAYS[currentDay]);
        return view;


    }

    public void processFinish(String output){
        weeklyMenu = new LunchMenu(output);
        updateMenuItems(currentDay);
    }

    public void updateMenuItems(int day) {
        /*
        Method called when a new value is chosen from the spinner.  The index (day) of the spinner
        is passed to this method.
         */
        entree.setText(weeklyMenu.getLunchEntree(day));
        veggie.setText(weeklyMenu.getVegetarianEntree(day));
        sides.setText(weeklyMenu.getSides(day));
        deli.setText(weeklyMenu.getDeli(day));
    }

    public class GetLunchMenuFromServer extends AsyncTask<String, Integer, String> {
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
            /*
            This method is where the UI is first updated.  The default action is to use the
            information from the current day to populate the initial menu.
             */
            //Lunch.this.weeklyMenu = new LunchMenu(result, day);
            //Lunch.this.updateMenuItems(currentDay);
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

        StringBuilder total = new StringBuilder();
        String line;
        while ((line = r.readLine()) != null) {
            total.append(line + "\n");
        }

        return new String(total);
    }

}
