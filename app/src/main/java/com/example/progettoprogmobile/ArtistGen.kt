package com.example.progettoprogmobile

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class ArtistGen : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

       val rootView = inflater.inflate(R.layout.fragment_artist_gen, container, false)
        return rootView
    }

}