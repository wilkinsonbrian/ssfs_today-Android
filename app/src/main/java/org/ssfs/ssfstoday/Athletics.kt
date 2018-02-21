package org.ssfs.ssfstoday


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
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.Calendar
import java.util.regex.Matcher
import java.util.regex.Pattern



class Athletics : AppCompatActivity(), AsyncResponse {

    internal var asyncTask = GetScheduleFromServer()
    var day: Int = 0
    var currentDay: Int = 0

    private var games: TextView? = null
    private var dayOfWeek: TextView? = null
    private var today: DateInfo? = null
    private var todaysDate: String? = null


    internal var rawHtml: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_athletics)
        games = findViewById(R.id.sports_label) as TextView
        dayOfWeek = findViewById(R.id.date_label) as TextView
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        getDateInformation()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)

    }


    override fun onResume() {
        super.onResume()
        getDateInformation()
        asyncTask = GetScheduleFromServer()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    fun getDateInformation() {
        today = DateInfo()
        currentDay = today!!.currentDay
        todaysDate = today!!.todaysDate
        Log.v("Date: ", todaysDate)
    }

    override fun processFinish(output: String) {
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        games!!.text = output
    }


    inner class GetScheduleFromServer : AsyncTask<String, Int, String>() {
        var delegate: AsyncResponse? = null

        override fun doInBackground(vararg params: String): String {

            try {
                return downloadUrl(params[0])
            } catch (e: IOException) {
                return "Unable to retrieve web page. URL may be invalid."
            }

        }


        override fun onPostExecute(result: String) {
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
        var todaysSchedule = ""

        var line: String? = null;
        while ({ line = r.readLine(); line }() != null) {
            val items = line!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (items[0] == todaysDate) {
                todaysSchedule += items[1] + ": " + items[3] + " vs. " + items[5] + " (" + items[4] + ")" + "\n\n"
            }
        }

        return if (todaysSchedule == "") {
            "No Games Today"
        } else {
            todaysSchedule
        }

    }

    companion object {
        private val WEBSERVER = "https://grover.ssfs.org/menus/athletics_schedule.csv"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

}// Required empty public constructor
