package com.madtoast.flyingboat.ui.activities.ui.login

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
class LoginViewModelFactory(private val cacheFile: File, private val context: Context) :
    ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            return LoginViewModel(
                floatplaneRepository = FloatplaneRepository(
                    dataSource = FloatplaneDataSource(context),
                    cacheFile
                )
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}