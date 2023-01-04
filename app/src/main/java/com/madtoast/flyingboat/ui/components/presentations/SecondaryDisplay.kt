package com.madtoast.flyingboat.ui.components.presentations

import android.app.Presentation
import android.content.Context
import android.os.Bundle
import android.view.Display
import android.widget.Button
import android.widget.TextView
import com.madtoast.flyingboat.R


class SecondaryDisplay
/**
 * Creates a new presentation that is attached to the specified display
 * using the default theme.
 *
 * @param outerContext The context of the application that is showing the presentation.
 * The presentation will create its own context (see [.getContext]) based
 * on this context and information about the associated display.
 * @param display      The display to which the presentation should be attached.
 */(outerContext: Context?, display: Display?) : Presentation(outerContext, display) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.secondary_display_layout)

        val button = findViewById<Button>(R.id.testButton)
        val textView2 = findViewById<TextView>(R.id.textView2)
        val focusableView = findViewById<TextView>(R.id.focusableView)
        button.setOnClickListener {
            textView2.text = "Button was clicked!"
        }

        focusableView.setOnKeyListener { _, _, event ->
            focusableView.text = "Keycode: " + event.keyCode.toString()
            true
        }
    }
}