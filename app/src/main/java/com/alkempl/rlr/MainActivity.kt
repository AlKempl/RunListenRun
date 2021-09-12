package com.alkempl.rlr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val button: Button = findViewById(R.id.switch_button)
        val label: TextView = findViewById(R.id.label_text)
        button.setOnClickListener {
            label.text = "Pressed!";
            // Code here executes on main thread after user presses button
        }
    }

}