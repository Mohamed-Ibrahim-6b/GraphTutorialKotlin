package com.example.graphtutorial

import com.microsoft.graph.authentication.IAuthenticationProvider
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.http.IHttpRequest
import com.microsoft.graph.models.extensions.IGraphServiceClient
import com.microsoft.graph.models.extensions.User
import com.microsoft.graph.options.Option
import com.microsoft.graph.options.QueryOption
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.microsoft.graph.requests.extensions.IEventCollectionPage

// Singleton class - the app only needs a single instance of the Graph client
class GraphHelper : IAuthenticationProvider {

    private lateinit var accessToken: String
    private var client: IGraphServiceClient = GraphServiceClient.builder()
        .authenticationProvider(this)
        .buildClient()

    companion object {
        private var INSTANCE: GraphHelper? = null

        @Synchronized
        fun getInstance(): GraphHelper = INSTANCE ?: GraphHelper().apply { INSTANCE = this }
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
        client.me().buildRequest().get(callback)
    }

    fun getEvents(accessToken: String, callback: ICallback<IEventCollectionPage>) {
        this.accessToken = accessToken

        // Use query options to sort by created time
        val options = mutableListOf<Option>().apply {
            add(QueryOption("orderby", "createdDateTime DESC"))
        }

        // Get /me/events
        client.me().events().buildRequest(options).select("subject,organizer,start,end")
            .get(callback)
    }

    // Debug function to get the JSON representation of a Graph object
    fun serializeObject(obj: Any): String = client.serializer.serializeObject(obj)
}