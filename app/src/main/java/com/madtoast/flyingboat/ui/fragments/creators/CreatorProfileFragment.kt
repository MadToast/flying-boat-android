package com.madtoast.flyingboat.ui.fragments.creators

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.madtoast.flyingboat.R

class CreatorProfileFragment : Fragment() {

    companion object {
        fun newInstance() = CreatorProfileFragment()
    }

    private lateinit var viewModel: CreatorProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_creator_profile, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CreatorProfileViewModel::class.java)
        // TODO: Use the ViewModel
    }

}