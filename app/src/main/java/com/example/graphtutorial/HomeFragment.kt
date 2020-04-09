package com.example.graphtutorial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment

private const val ARG_USER_NAME = "userName"

class HomeFragment : Fragment() {

    companion object {
        fun createInstance(userName: String) = HomeFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_USER_NAME, userName)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)?.apply {
            // Replace the "Please sign in" with the userName
            findViewById<TextView>(R.id.home_page_username).text =
                arguments?.getString(ARG_USER_NAME)
        }
    }
}