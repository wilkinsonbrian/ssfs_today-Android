package org.ssfs.ssfstoday

import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

class Schedule : AppCompatActivity(), AsyncResponse {

    private var sched: TextView? = null
    private var dayOfWeek: TextView? = null
    private var today: DateInfo? = null
    private var todaysDate: String? = null
    private var currentDay: Int = 0
    internal var asyncTask = GetScheduleFromServer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_schedule)

        sched = findViewById(R.id.schedule_view) as TextView
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
    }

    override fun processFinish(output: String) {
        dayOfWeek!!.text = WEEKDAYS[currentDay]
        sched!!.text = output
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
            if (items[1] == todaysDate) {
                todaysSchedule += items[0] + "\n" + items[2] + " - " + items[4] + "\n\n"
            }
        }

        return if (todaysSchedule == "") {
            "Schedule Not Found"
        } else {
            todaysSchedule
        }

    }

    companion object {
        private val WEBSERVER = "https://grover.ssfs.org/menus/calendar.csv"
        private val WEEKDAYS = arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
    }
}
