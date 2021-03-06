package org.ssfs.ssfstoday


import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_library_beestro.*
import kotlinx.android.synthetic.main.activity_schedule.*

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
    var currentDayOfMonth: Int = 0
    private var today: DateInfo? = null

    var x1 = 0f
    var x2 = 0f

    private var announce: TextView? = null
    private var dayOfWeek: TextView? = null
    private var libraryHours: TextView? = null
    private var libraryAnnouncements: TextView? = null
    private var beestroHours: TextView? = null
    private var libraryBeestroSchedule: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library_beestro)
        val list_of_items = arrayOf("Athletics", "Schedule", "Lunch", "Library/Beestro", "Wildezine")

        //todaysDate = getTodaysDate()
        announce = findViewById(R.id.announcements) as TextView
        libraryHours = findViewById(R.id.lib_hours) as TextView
        beestroHours = findViewById(R.id.beest_hours) as TextView
        dayOfWeek = findViewById(R.id.date_label) as TextView
        libraryAnnouncements = findViewById(R.id.library_announcements) as TextView
        dayOfWeek!!.text = WEEKDAYS[currentDay]

        beestro_spinner.adapter = ArrayAdapter(this, R.layout.beestro_spinner_item, list_of_items)
        beestro_spinner.setSelection(3)
        beestro_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 2) {
                    val myIntent = Intent(this@LibraryBeestro,
                            Lunch::class.java)
                    startActivity(myIntent)
                } else if (position == 1) {
                    val myIntent = Intent(this@LibraryBeestro,
                            Schedule::class.java)
                    startActivity(myIntent)
                } else if (position == 0) {
                    val myIntent = Intent(this@LibraryBeestro,
                            Athletics::class.java)
                    startActivity(myIntent)
                } else if (position == 4) {
                    val myIntent = Intent(this@LibraryBeestro,
                            Wildezine::class.java)
                    startActivity(myIntent)
                }
            }

        }
        getDateInformation()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    override fun onResume() {
        super.onResume()
        //todaysDate = getTodaysDate()
        getDateInformation()
        asyncTask = GetDataFromServer()
        asyncTask.delegate = this
        asyncTask.execute(WEBSERVER)
    }

    fun getDateInformation() {
        today = DateInfo()
        currentDay = today!!.currentDay
        currentDayOfMonth = today!!.currentDate
        todaysDate = today!!.todaysDate
    }

    override fun processFinish(output: String) {
        libraryBeestroSchedule = output

        updateLibraryBeestroSchedule()
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
        var todaysSchedule = ""

        var line: String? = null;
        while ({ line = r.readLine(); line }() != null) {
            todaysSchedule += line + "\n"
        }

        return if (todaysSchedule == "") {
            "No response from server,No response from server,No response from server," + "No response from server,No response from server"
        } else {
            todaysSchedule
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (event!!.action == MotionEvent.ACTION_DOWN) {

            x1 = event.getX()

        }

        if (event!!.action == MotionEvent.ACTION_UP) {
            x2 = event.getX()


            // if left to right swipe event on screen
            if (x2 > x1 && currentDay > 1)
            {
                currentDay--
                currentDayOfMonth--
            }

            // if right to left swipe event on screen
            if (x1 > x2 && currentDay < 5)
            {
                currentDay++
                currentDayOfMonth++
            }
            todaysDate = today!!.getDateString(currentDayOfMonth)
            updateLibraryBeestroSchedule()
        }

        return false
        //return super.onTouchEvent(event)
    }

    fun updateLibraryBeestroSchedule() {
        // var todaysSchedule = ""
        var temp = libraryBeestroSchedule!!.split("\n")
        for (line in temp) {
            var items = line.split(",")
            if (items[0] == todaysDate) {
                val data = items
                dayOfWeek!!.text = WEEKDAYS[currentDay]
                libraryHours!!.setText(data[1])
                libraryAnnouncements!!.setText(data[2])
                beestroHours!!.setText(data[3])
                announce!!.setText(data[4])
                break
            }
        }
    }

    companion object {

        private val WEBSERVER = "https://grover.ssfs.org/menus/library_beestro.csv"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }

}// Required empty public constructor
