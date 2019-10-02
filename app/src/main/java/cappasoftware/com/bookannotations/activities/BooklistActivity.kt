package cappasoftware.com.bookannotations.activities

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import cappasoftware.com.bookannotations.R
import cappasoftware.com.bookannotations.dataclasses.Book
import cappasoftware.com.bookannotations.dataclasses.ConstantStrings
import cappasoftware.com.bookannotations.fragments.FragmentAnnotate
import cappasoftware.com.bookannotations.fragments.FragmentHome
import cappasoftware.com.bookannotations.fragments.FragmentMyBooks
import cappasoftware.com.bookannotations.fragments.FragmentSettings
import io.paperdb.Paper
import kotlinx.android.synthetic.main.activity_booklist.*
import org.jetbrains.anko.doAsync

/**
 * Created by Nico on 7/4/2018.
 */

class BooklistActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val tas = this

// INTERFACES
    // Interface to decide what happens when a navigation menu item is selected (From the drawer menu)
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        //  Checks for which nav menu item is clicked then initiates a fragment and places it in the activity. Kotlin equivalent of switch-case statement.
        when (item.itemId) {
            R.id.nav_home -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentHome.newInstance()).commit()
            R.id.nav_books -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentMyBooks.newInstance()).commit()
            R.id.nav_settings -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentSettings.newInstance()).commit()
            R.id.nav_feedback_suggestion -> supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentAnnotate.newInstance()).commit()
        }

        // Close the drawer after the user selects an item from it
        drawer.closeDrawer(GravityCompat.START)

        // Tell the app that something was indeed selected from the menu
        return true
    }

// FIELDS
    private lateinit var drawer: DrawerLayout
    private lateinit var preferences: SharedPreferences

// OVERRIDES
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_booklist)

        // Initialize Paper db
        Paper.init(this)

        preferences = getSharedPreferences("cappasoftware.com.bookannotations", Context.MODE_PRIVATE)

        // preferences.edit().putBoolean("isFirstRun", true).commit()

        // If this is the first time the user is running this app
        if (preferences.getBoolean("isFirstRun", true)) {
            val list: MutableList<Book> = mutableListOf()

            doAsync {
                Paper.book(ConstantStrings.PAPERBOOK_USERBOOKLIST).write(ConstantStrings.PAPERKEY_ALLBOOKS, list)
                Log.d("PAPERIO>>>>", "ADDED EMPTY TO DB")
            }

            preferences.edit().putBoolean("isFirstRun", false).apply()
        }

        // The Main Layout of the activity
        drawer = findViewById(R.id.drawer_layout)

        // Init toolbar
        val toolbar = custom_toolbar

        // Set the toolbar as actionbar
        setSupportActionBar(toolbar)


        // The next 4 lines basically just add the hamburger menu to the nav bar
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        // Passing the interface for selection logic to the navigation view
        val navView : NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)



        // Safety check for when android does something like kill the activity and start it back up
        if (savedInstanceState == null){
            // Initializing the home page fragment
            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, FragmentHome.newInstance()).commit()

            // Tell the navigation view that the Home menu item should be selected
            navView.setCheckedItem(R.id.nav_home)
        }

    }

    override fun onBackPressed() {

        // If the drawer is open, override the back button function and close it instead
        if (drawer.isDrawerOpen(GravityCompat.START)){
            drawer.closeDrawer(GravityCompat.START)
        }

        // Else the drawer is hidden, allow the onBackPressed to do whatever it normally does
        else {
            super.onBackPressed()
        }
    }
}
