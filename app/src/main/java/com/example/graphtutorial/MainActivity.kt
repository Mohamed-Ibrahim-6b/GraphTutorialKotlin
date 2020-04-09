package com.example.graphtutorial

import android.os.Bundle
import android.util.Log
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
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.models.extensions.User
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException

private const val SAVED_IS_SIGNED_IN = "isSignedIn"
private const val SAVED_USER_NAME = "userName"
private const val SAVED_USER_EMAIL = "userEmail"

class MainActivity : AppCompatActivity() {

    private lateinit var drawer: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var headerView: View

    private var isSignedIn = false
    private var userName: String = "Please sign in"
    private var userEmail: String = ""

    private lateinit var authHelper: AuthenticationHelper
    private var attemptInteractiveSignIn: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Set the toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar).apply {
            setSupportActionBar(this)
        }

        drawer = findViewById<DrawerLayout>(R.id.drawer_layout).apply {
            // Add the hamburger menu icon
            val toggle = ActionBarDrawerToggle(
                this@MainActivity,
                this,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
            )
            addDrawerListener(toggle)
            toggle.syncState()
        }

        navigationView = findViewById<NavigationView>(R.id.nav_view).apply {
            headerView = getHeaderView(0)

            // Listen for item select events on menu
            setNavigationItemSelectedListener { menuItem ->
                // Load the fragment that corresponds to the selected item
                when (menuItem.itemId) {
                    R.id.nav_home -> openHomeFragment(userName)
                    R.id.nav_calendar -> openCalendarFragment()
                    R.id.nav_signin -> signIn()
                    R.id.nav_signout -> signOut()
                }

                drawer.closeDrawer(GravityCompat.START)
                true
            }
        }
        // Set user name and email
        setSignedInState(isSignedIn)

        // Get the authentication helper
        authHelper = AuthenticationHelper.getInstance(applicationContext)

        savedInstanceState?.apply {
            // Restore state
            isSignedIn = getBoolean(SAVED_IS_SIGNED_IN)
            userName = get(SAVED_USER_NAME).toString()
            userEmail = get(SAVED_USER_EMAIL).toString()
            setSignedInState(isSignedIn)
        } ?: run {
            // Load the home fragment by default on startup
            openHomeFragment(userName)
            doSilentSignIn()
        }
        attemptInteractiveSignIn = true
    }

    override fun onSaveInstanceState(outState: Bundle) = with(outState) {
        super.onSaveInstanceState(this)
        putBoolean(SAVED_IS_SIGNED_IN, isSignedIn)
        putString(SAVED_USER_NAME, userName)
        putString(SAVED_USER_EMAIL, userEmail)
    }

    override fun onBackPressed() = if (drawer.isDrawerOpen(GravityCompat.START)) {
        drawer.closeDrawer(GravityCompat.START)
    } else {
        super.onBackPressed()
    }

    private fun showProgressBar() {
        findViewById<FrameLayout>(R.id.fragment_container).visibility = View.GONE
        findViewById<ProgressBar>(R.id.progressbar).visibility = View.VISIBLE
    }

    private fun hideProgressBar() {
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

        if (isSignedIn) {
            //userName = "Megan Bowen"
            //userEmail = "meganb@contoso.com"
        } else {
            userName = "Please sign in"
            userEmail = ""
        }

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
            .commitAllowingStateLoss()
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
        showProgressBar()
        // Attempt silent sign in first
        // if this fails, the callback will handle doing
        // interactive sign in
        doSilentSignIn()
    }

    private fun signOut() {
        authHelper.signOut()
        setSignedInState(false)
        openHomeFragment(userName)
    }

    // Silently sign in - used if there is already a
    // user account in the MSAL cache
    private fun doSilentSignIn() = authHelper.acquireTokenSilently(getAuthCallback())

    // Prompt the user to sign in
    private fun doInteractiveSignIn() =
        authHelper.acquireTokenInteractively(this, getAuthCallback())

    // Handles the authentication result
    private fun getAuthCallback(): AuthenticationCallback = object : AuthenticationCallback {
        override fun onSuccess(authenticationResult: IAuthenticationResult?) =
            authenticationResult?.accessToken.let {
                // Log the token for debug purposes
                Log.d("AUTH", String.format("Access token: %s", it))

                // Get Graph client and get user
                GraphHelper.getInstance().getUser(it!!, getUserCallback())
            }

        override fun onCancel() {
            // User canceled the authentication
            Log.d("AUTH", "Authentication canceled")
            hideProgressBar()
        }

        override fun onError(exception: MsalException?) = with(exception) {
            // Check the type of "this" exception and handle appropriately
            if (this is MsalUiRequiredException) {
                Log.d("AUTH", "Interactive login required")
                if (attemptInteractiveSignIn) doInteractiveSignIn()
            } else if (this is MsalClientException) {
                if (errorCode == "no_current_account") {
                    Log.d("AUTH", "No current account, interactive login required")
                    if (attemptInteractiveSignIn) doInteractiveSignIn()
                } else {
                    // Exception inside MSAL, more info inside MsalError.java
                    Log.e("AUTH", "Client error authenticating", exception)
                }
            } else if (this is MsalServiceException) {
                // Exception when communicating with the auth server, likely config issue
                Log.e("AUTH", "Service error authenticating", exception)
            }
            hideProgressBar()
        }
    }

    private fun getUserCallback(): ICallback<User> = object : ICallback<User> {
        override fun success(result: User?) {
            result?.apply {
                Log.d("AUTH", "User: $displayName")
                userName = displayName
                userEmail = mail ?: userPrincipalName
            }
            updateUI()
        }

        override fun failure(ex: ClientException?) {
            Log.e("AUTH", "Error getting /me", ex)
            userName = "ERROR"
            userEmail = "ERROR"
            updateUI()
        }

        private fun updateUI() = runOnUiThread {
            hideProgressBar()
            setSignedInState(true)
            openHomeFragment(userName)
        }
    }
}