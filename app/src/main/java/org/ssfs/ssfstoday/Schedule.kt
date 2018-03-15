package org.ssfs.ssfstoday

import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.activity_lunch.*
import kotlinx.android.synthetic.main.activity_schedule.*
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Schedule : AppCompatActivity(), AsyncResponse {

    private var sched: TextView? = null
    private var dayOfWeek: TextView? = null
    private var today: DateInfo? = null
    private var todaysDate: String? = null
    private var currentDay: Int = 0
    var currentDayOfMonth: Int = 0
    private var fullSemesterScehdule: String? = null

    // Needed to detect right and left swipes
    var x1 = 0f
    var x2 = 0f

    internal var asyncTask = GetScheduleFromServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)
        val list_of_items = arrayOf("Athletics", "Schedule", "Lunch", "Library/Beestro", "Wildezine")

        sched = findViewById(R.id.schedule_view) as TextView
        dayOfWeek = findViewById(R.id.date_label) as TextView
        dayOfWeek!!.text = WEEKDAYS[currentDay]

        schedule_spinner.adapter = ArrayAdapter(this, R.layout.schedule_spinner_item, list_of_items)
        schedule_spinner.setSelection(1)
        schedule_spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position == 2) {
                    val myIntent = Intent(this@Schedule,
                            Lunch::class.java)
                    startActivity(myIntent)
                } else if (position == 0) {
                    val myIntent = Intent(this@Schedule,
                            Athletics::class.java)
                    startActivity(myIntent)
                } else if (position == 3) {
                    val myIntent = Intent(this@Schedule,
                            LibraryBeestro::class.java)
                    startActivity(myIntent)
                } else if (position == 4) {
                    val myIntent = Intent(this@Schedule,
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
        getDateInformation()
        asyncTask = GetScheduleFromServer()
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
        fullSemesterScehdule = output
        updateSchedule()
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
            todaysSchedule += line + "\n"

        }

        return if (todaysSchedule == "") {
            "Schedule Not Found"
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
            updateSchedule()
        }

        return false
        //return super.onTouchEvent(event)
    }

    fun updateSchedule() {
        var todaysSchedule = ""
        var temp = fullSemesterScehdule!!.split("\n")
        for (line in temp) {
            var items = line.split(",")
            println(items[0] + ", " + todaysDate + ", " + (items[0] == todaysDate))
            if (items[0] == todaysDate) {
                todaysSchedule += items[1] + "\n" + items[2] + " - " + items[4] + "\n\n"
            }
        }

        dayOfWeek!!.text = WEEKDAYS[currentDay]
        sched!!.text = todaysSchedule

    }


    companion object {
        private val WEBSERVER = "https://grover.ssfs.org/menus/calendar.csv"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }
}
