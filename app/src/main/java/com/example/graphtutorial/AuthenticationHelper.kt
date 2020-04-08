package com.example.graphtutorial

import android.app.Activity
import android.content.Context
import android.util.Log
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IPublicClientApplication
import com.microsoft.identity.client.ISingleAccountPublicClientApplication
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.exception.MsalException

// Singleton class - the app only needs a single instance of PublicClientApplication
class AuthenticationHelper private constructor(ctx: Context) {

    private var pca: ISingleAccountPublicClientApplication? = null
    private val scopes = arrayOf("User.Read", "Calendars.Read")

    init {
        PublicClientApplication.createSingleAccountPublicClientApplication(
            ctx.applicationContext,
            R.raw.msal_config,
            object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication?) {
                    pca = application
                }

                override fun onError(exception: MsalException?) {
                    Log.e("AUTHHELPER", "Error creating MSAL application", exception)
                }
            })
    }

    companion object {
        private var INSTANCE: AuthenticationHelper? = null

        @Synchronized
        fun getInstance(ctx: Context): AuthenticationHelper =
            INSTANCE ?: AuthenticationHelper(ctx).apply {
                INSTANCE = this
            }

        // Version called from fragments. Does not create an instance if one doesn't exist
        @Synchronized
        fun getInstance(): AuthenticationHelper = INSTANCE ?: throw IllegalStateException(
            "AuthenticationHelper has not been initialized from MainActivity"
        )
    }

    fun acquireTokenInteractively(activity: Activity, callback: AuthenticationCallback) =
        pca!!.signIn(activity, "", scopes, callback)

    fun acquireTokenSilently(callback: AuthenticationCallback) = pca!!.apply {
        // Get the authority from MSAL config
        val authority = configuration.defaultAuthority.authorityURL.toString()
        Log.d("AUTHHELPER", "acquireTokenSilently: authority=$authority")
        acquireTokenSilentAsync(scopes, authority, callback)
    }

    fun signOut() = pca!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
        override fun onSignOut() {
            Log.d("AUTHHELPER", "Signed out")
        }

        override fun onError(exception: MsalException) {
            Log.d("AUTHHELPER", "MSAL error signing out", exception)
        }
    })
}