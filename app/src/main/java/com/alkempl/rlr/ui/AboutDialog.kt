package com.alkempl.rlr.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.alkempl.rlr.BuildConfig
import com.alkempl.rlr.R

class AboutDialog: DialogFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog!!.window?.setBackgroundDrawableResource(R.drawable.round_corner);

        val binding = inflater.inflate(R.layout.version_dialog, container, false)

        val versionCaption = binding!!.findViewById<TextView>(R.id.versionCaption)
        versionCaption?.let {
            versionCaption.text = "${BuildConfig.VERSION_NAME}, ${BuildConfig.GitHash}"
        }

        val okButton = binding!!.findViewById<Button>(R.id.btn_close_about)
        okButton?.let {
            it.setOnClickListener {
                dismiss()
            }
        }

        return binding.rootView
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        val height = (resources.displayMetrics.heightPixels * 0.40).toInt()
        dialog!!.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

}