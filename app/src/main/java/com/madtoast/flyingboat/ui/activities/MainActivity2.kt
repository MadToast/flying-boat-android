package com.madtoast.flyingboat.ui.activities

import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.updateLayoutParams
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigationrail.NavigationRailView
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.databinding.ActivityMain2Binding

class MainActivity2 : AppCompatActivity() {

    private lateinit var binding: ActivityMain2Binding
    private lateinit var displayManager: DisplayManager
    private lateinit var presentationDisplays: Array<Display>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)

        //TODO: Implement External Display support
        displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        presentationDisplays =
            displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        displayManager.registerDisplayListener(object : DisplayManager.DisplayListener {
            override fun onDisplayAdded(displayId: Int) {
                TODO("Not yet implemented")
            }

            override fun onDisplayRemoved(displayId: Int) {
                TODO("Not yet implemented")
            }

            override fun onDisplayChanged(displayId: Int) {
                TODO("Not yet implemented")
            }

        }, null)

        val navView: BottomNavigationView? = binding.navView
        val tabletNavView: NavigationRailView? = binding.tabletNavView

        val navController = findNavController(R.id.nav_host_fragment_activity_main2)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_creators, R.id.navigation_playlists
            ),
        )
        binding.toolbar.setupWithNavController(navController, appBarConfiguration)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.toolbarTitle.text = destination.label
        }

        navView?.setupWithNavController(navController)

        tabletNavView?.setupWithNavController(navController)

        //Don't scroll toolbar
        binding.toolbar.updateLayoutParams<AppBarLayout.LayoutParams> {
            scrollFlags = 0
        }
    }
}