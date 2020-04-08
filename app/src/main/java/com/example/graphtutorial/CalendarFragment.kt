package com.example.graphtutorial

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.microsoft.graph.concurrency.ICallback
import com.microsoft.graph.core.ClientException
import com.microsoft.graph.models.extensions.Event
import com.microsoft.graph.requests.extensions.IEventCollectionPage
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.exception.MsalException

class CalendarFragment : Fragment() {

    private var eventList: MutableList<Event>? = null
    private lateinit var progress: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_calendar, container, false)?.apply {
            progress = activity!!.findViewById(R.id.progressbar)
            showProgressBar()

            // Get a current access token
            AuthenticationHelper.getInstance()
                .acquireTokenSilently(object : AuthenticationCallback {
                    override fun onSuccess(authenticationResult: IAuthenticationResult?) {
                        GraphHelper.getInstance().apply {
                            getEvents(authenticationResult!!.accessToken, getEventsCallback())
                        }
                    }

                    override fun onCancel() {
                        hideProgressBar()
                    }

                    override fun onError(exception: MsalException?) {
                        Log.e("AUTH", "Could not get token silently", exception)
                        hideProgressBar()
                    }
                })
        }
    }

    private fun showProgressBar() = activity!!.runOnUiThread { progress.visibility = View.VISIBLE }

    private fun hideProgressBar() = activity!!.runOnUiThread { progress.visibility = View.GONE }

    private fun getEventsCallback(): ICallback<IEventCollectionPage> =
        object : ICallback<IEventCollectionPage> {
            override fun success(result: IEventCollectionPage?) {
                eventList = result?.currentPage
                addEventsToList()

                // Temporary for debugging
                Log.d("GRAPH", GraphHelper.getInstance().serializeObject(eventList!!))

                hideProgressBar()
            }

            override fun failure(ex: ClientException?) {
                Log.e("GRAPH", "Error getting events", ex)
                hideProgressBar()
            }
        }

    private fun addEventsToList() = activity!!.runOnUiThread {
        val eventListView = view!!.findViewById<ListView>(R.id.eventlist)
        val listAdapter = EventListAdapter(activity!!, R.layout.event_list_item, eventList!!)
        eventListView.adapter = listAdapter
    }
}