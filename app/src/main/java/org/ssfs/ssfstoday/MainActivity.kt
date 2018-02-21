package org.ssfs.ssfstoday

import android.app.ActionBar
import android.app.FragmentTransaction
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.Toolbar
import android.view.View
import java.util.ArrayList

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


}
