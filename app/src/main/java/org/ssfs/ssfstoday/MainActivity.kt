package org.ssfs.ssfstoday

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    /** Called when the user taps the Send button */
    fun goToLunchActivity(view: View) {
        // Do something in response to button
        val myIntent = Intent(this@MainActivity,
                Lunch::class.java)
        startActivity(myIntent)
    }

    fun goToAthleticsActivity(view: View) {
        // Do something in response to button
        val myIntent = Intent(this@MainActivity,
                Athletics::class.java)
        startActivity(myIntent)
    }

    fun goToBeestroActivity(view: View) {
        // Do something in response to button
        val myIntent = Intent(this@MainActivity,
                LibraryBeestro::class.java)
        startActivity(myIntent)
    }

    fun goToWildezine(view: View) {
        // Do something in response to button
        val myIntent = Intent(this@MainActivity,
                Wildezine::class.java)
        startActivity(myIntent)
    }

    fun goToSchedule(view: View) {
        // Do something in response to button
        val myIntent = Intent(this@MainActivity,
                Schedule::class.java)
        startActivity(myIntent)
    }


}
