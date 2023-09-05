package com.example.progettoprogmobile

import androidx.navigation.Navigation
import android.widget.TextView
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.widget.Button
class SecondFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_second, container, false)
        val prevButton2 = rootView.findViewById<Button>(R.id.prevButton2)
    prevButton2.setOnClickListener {
        val navController = Navigation.findNavController(requireView())
        navController.navigate(R.id.navigateToFirstFragment)
    }

        return rootView
    }

}