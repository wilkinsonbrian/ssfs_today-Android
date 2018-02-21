package org.ssfs.ssfstoday


import android.graphics.drawable.ShapeDrawable
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.lang.reflect.Array
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar


class Lunch : AppCompatActivity(), AsyncResponse {

    internal var asyncTask = GetLunchMenuFromServer()
    private var entree: TextView? = null
    private var veggie: TextView? = null
    private var sides: TextView? = null
    private var deli: TextView? = null
    private var dayOfWeek: TextView? = null
    var day: Int = 0
    var currentDay: Int = 0
    //private ShapeDrawable background;

    private var weeklyMenu: LunchMenu? = null
    private var today: DateInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lunch)

        entree = findViewById(R.id.lunch_entree) as TextView
        veggie = findViewById(R.id.veggie_entree) as TextView
        sides = findViewById(R.id.sides) as TextView
        deli = findViewById(R.id.deli) as TextView
        //background = (ShapeDrawable) view.findViewById(R.id.ba)
        dayOfWeek = findViewById(R.id.date_label) as TextView
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        getDateInformation()

        // Starts the AsyncTask that actually retrieves the lunch data from the server.
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }


    override fun onResume() {
        super.onResume()
        getDateInformation()
        asyncTask = GetLunchMenuFromServer()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    fun getDateInformation() {
        today = DateInfo()
        day = today!!.dayOfWeek
        currentDay = today!!.currentDay
    }

    override fun processFinish(output: String) {
        weeklyMenu = LunchMenu(output)
        updateMenuItems(currentDay)
    }

    fun updateMenuItems(day: Int) {
        /*
        Method called when a new value is chosen from the spinner.  The index (day) of the spinner
        is passed to this method.
         */
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        entree!!.text = weeklyMenu!!.getLunchEntree(day)
        veggie!!.text = weeklyMenu!!.getVegetarianEntree(day)
        sides!!.text = weeklyMenu!!.getSides(day)
        deli!!.text = weeklyMenu!!.getDeli(day)
    }

    inner class GetLunchMenuFromServer : AsyncTask<String, Int, String>() {
        var delegate: AsyncResponse? = null

        override fun doInBackground(vararg params: String): String {

            try {
                return downloadUrl(params[0])
            } catch (e: IOException) {

                return "Unable to retrieve web page. URL may be invalid."
            }

        }


        override fun onPostExecute(result: String) {
            /*
            This method is where the UI is first updated.  The default action is to use the
            information from the current day to populate the initial menu.
             */
            //Lunch.this.weeklyMenu = new LunchMenu(result, day);
            //Lunch.this.updateMenuItems(currentDay);
            delegate!!.processFinish(result)
        }

        @Throws(IOException::class)
        private fun downloadUrl(myurl: String): String {
            var `is`: InputStream? = null

            try {

                /*
                Reads the XML file from server (grover) and returns the raw xml
                 */
                val url = URL(myurl)
                val conn = url.openConnection() as HttpURLConnection
                conn.readTimeout = 10000
                conn.connectTimeout = 15000
                conn.requestMethod = "GET"
                conn.connect()
                `is` = conn.inputStream

                return readIt(`is`)
            } finally {
                if (`is` != null) {
                    `is`.close()
                }
            }
        }
    }

    /*
    Takes the URL data and appends each line of XML one by one to the Stringbuilder.
    The final string returned is the complete XML file with all the tags.
     */
    @Throws(IOException::class, UnsupportedEncodingException::class)
    fun readIt(stream: InputStream?): String {
        val r = BufferedReader(InputStreamReader(stream!!))

        val total = StringBuilder()

        var line: String? = null;
        while ({ line = r.readLine(); line }() != null) {
            total.append(line + "\n")
        }

        return String(total)
    }

    companion object {

        private val WEBSERVER = "https://grover.ssfs.org/menus/word/document.xml"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

}// Required empty public constructor

