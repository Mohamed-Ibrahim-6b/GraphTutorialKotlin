package com.example.graphtutorial

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

private const val SAVED_IS_SIGNED_IN = "isSignedIn"
private const val SAVED_USER_NAME = "userName"
private const val SAVED_USER_EMAIL = "userEmail"

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var headerView: View

    private var isSignedIn = false
    private var userName: String = "Please sign in"
    private var userEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawer = findViewById(R.id.drawer_layout)

        // Add the hamburger menu icon
        val toggle = ActionBarDrawerToggle(
            this,
            drawer,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        navigationView = findViewById(R.id.nav_view)

        // Set user name and email
        headerView = navigationView.getHeaderView(0)
        setSignedInState(isSignedIn)

        // Listen for item select events on menu
        navigationView.setNavigationItemSelectedListener(this)

        if (savedInstanceState == null) {
            // Load the home fragment by default on startup
            openHomeFragment(userName)
        } else {
            // Restore state
            savedInstanceState.apply {
                isSignedIn = getBoolean(SAVED_IS_SIGNED_IN)
                userName = get(SAVED_USER_NAME).toString()
                userEmail = get(SAVED_USER_EMAIL).toString()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
            super.onSaveInstanceState(this)
            putBoolean(SAVED_IS_SIGNED_IN, isSignedIn)
            putString(SAVED_USER_NAME, userName)
            putString(SAVED_USER_EMAIL, userEmail)
        }
    }

    override fun onNavigationItemSelected(menuItem: MenuItem): Boolean {
        // Load the fragment that corresponds to the selected item
        when (menuItem.itemId) {
            R.id.nav_home -> openHomeFragment(userName)
            R.id.nav_calendar -> openCalendarFragment()
            R.id.nav_signin -> signIn()
            R.id.nav_signout -> signOut()
        }

        drawer.closeDrawer(GravityCompat.START)

        return true
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    fun showProgressBar() {
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressbar).visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
        findViewById<ProgressBar>(R.id.progressbar).visibility = View.GONE
    }

    // Update the menu and get the user's name and email
    private fun setSignedInState(isSignedIn: Boolean) {
        this.isSignedIn = isSignedIn

        navigationView.menu.apply {
            findItem(R.id.nav_signin).isVisible = !isSignedIn
            findItem(R.id.nav_calendar).isVisible = isSignedIn
            findItem(R.id.nav_signout).isVisible = isSignedIn
        }

        // For testing
        userName = if (isSignedIn) "Megan Bowen" else "Please sign in"
        userEmail = if (isSignedIn) "meganb@contoso.com" else ""

        // Set the user name and email in the nav drawer
        headerView.apply {
            findViewById<TextView>(R.id.user_name).text = userName
            findViewById<TextView>(R.id.user_email).text = userEmail
        }
    }

    // Load the "Home" fragment
    private fun openHomeFragment(userName: String) {
        val fragment = HomeFragment.createInstance(userName)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
        navigationView.setCheckedItem(R.id.nav_home)
    }

    // Load the "Calendar" fragment
    private fun openCalendarFragment() {
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.fragment_container, CalendarFragment())
            .commit()
        navigationView.setCheckedItem(R.id.nav_calendar)
    }

    private fun signIn() {
        setSignedInState(true)
        openHomeFragment(userName)
    }

    private fun signOut() {
        setSignedInState(false)
        openHomeFragment(userName)
    }
}