package com.madtoast.flyingboat.ui.fragments.creators.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.madtoast.flyingboat.data.FloatplaneDataSource
import com.madtoast.flyingboat.data.FloatplaneRepository
import java.io.File

/**
 * ViewModel provider factory to instantiate LoginViewModel.
 * Required given LoginViewModel has a non-empty constructor
 */
class CreatorsViewModelFactory(private val cacheFile: File, private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreatorsViewModel::class.java)) {
            return CreatorsViewModel(
                floatplaneRepository = FloatplaneRepository(
                    dataSource = FloatplaneDataSource(context),
                    cacheFile
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}