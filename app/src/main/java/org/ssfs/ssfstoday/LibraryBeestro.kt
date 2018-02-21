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

import org.ssfs.ssfstoday.R
import org.w3c.dom.Text

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar



class LibraryBeestro : AppCompatActivity(), AsyncResponse {
    internal var asyncTask = GetDataFromServer()

    private var todaysDate: String? = null
    private var currentDay: Int = 0
    private var announce: TextView? = null
    private var dayOfWeek: TextView? = null
    private var libraryHours: TextView? = null
    private var libraryAnnouncements: TextView? = null
    private var beestroHours: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_beestro)

        todaysDate = getTodaysDate()
        announce = findViewById(R.id.announcements) as TextView
        libraryHours = findViewById(R.id.lib_hours) as TextView
        beestroHours = findViewById(R.id.beest_hours) as TextView
        dayOfWeek = findViewById(R.id.date_label) as TextView
        libraryAnnouncements = findViewById(R.id.library_announcements) as TextView
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    override fun onResume() {
        super.onResume()
        todaysDate = getTodaysDate()
        asyncTask = GetDataFromServer()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    private fun getTodaysDate(): String {
        val calendar = Calendar.getInstance()
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val month = calendar.get(Calendar.MONTH)
        val year = calendar.get(Calendar.YEAR)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        currentDay = dayOfWeek - 1
        return Integer.toString(month + 1) + "/" + Integer.toString(day) + "/" + Integer.toString(year)
    }


    override fun processFinish(output: String) {
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        val data = output.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
        libraryHours!!.setText(data[1])
        libraryAnnouncements!!.setText(data[2])
        beestroHours!!.setText(data[3])
        announce!!.setText(data[4])
    }

    inner class GetDataFromServer : AsyncTask<String, Int, String>() {
        var delegate: AsyncResponse? = null

        override fun doInBackground(vararg params: String): String {

            try {
                return downloadUrl(params[0])
            } catch (e: IOException) {
                // Will generate an array when split in the processFinish method.
                return "No response from server,No response from server,No response from server," + "No response from server,No response from server"
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

        var line: String? = null;
        while ({ line = r.readLine(); line }() != null) {
            val items = line!!.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            if (items[0] == todaysDate) {
                return line!!
            }
        }

        return "No data found"
    }

    companion object {

        private val WEBSERVER = "https://grover.ssfs.org/menus/library_beestro.csv"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

}// Required empty public constructor
