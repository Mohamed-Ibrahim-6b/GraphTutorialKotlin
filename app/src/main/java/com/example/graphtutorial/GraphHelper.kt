package com.example.graphtutorial

import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.microsoft.graph.models.extensions.User
import com.microsoft.graph.requests.extensions.GraphServiceClient

// Singleton class - the app only needs a single instance
// of the Graph client
class GraphHelper : IAuthenticationProvider {

    private var client: IGraphServiceClient? = null
    private var accessToken: String? = null

    init {
        client = GraphServiceClient.builder()
            .authenticationProvider(this)
            .buildClient()
    }

    companion object {
        private var INSTANCE: GraphHelper? = null

        @Synchronized
        fun getInstance(): GraphHelper {
            if (INSTANCE == null) {
                INSTANCE = GraphHelper()
            }

            return INSTANCE!!
        }
    }

    // Part of the Graph IAuthenticationProvider interface
    // This method is called before sending the HTTP request
    override fun authenticateRequest(request: IHttpRequest?) {
        // Add the access token in the Authorization header
        request?.addHeader("Authorization", "Bearer $accessToken")
    }

    fun getUser(accessToken: String, callback: ICallback<User>) {
        this.accessToken = accessToken

        // GET /me (logged in user)
        client!!.me().buildRequest().get(callback)
    }
}