package com.example.graphtutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

private const val USER_NAME = "userName"

class HomeFragment : Fragment() {

    companion object {
        fun createInstance(userName: String): HomeFragment {
            val args = Bundle().apply {
                putString(USER_NAME, userName)
            }

            return HomeFragment().apply {
                arguments = args
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val homeView = inflater.inflate(R.layout.fragment_home, container, false)

        arguments?.let {
            // Replace the "Please sign in" with the userName
            homeView.findViewById<TextView>(R.id.home_page_username).text = it.getString(USER_NAME)
        }

        return homeView
    }
}