package com.alkempl.rlr

import android.app.ProgressDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.alkempl.rlr.databinding.FragmentUploadImageBinding
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast

import androidx.core.app.ActivityCompat.startActivityForResult
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import androidx.core.app.ActivityCompat.startActivityForResult
import java.io.IOException
import java.net.URI
import java.util.*


class UploadImageFragment : Fragment() {

    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var binding: FragmentUploadImageBinding
    private lateinit var filePath: Uri

    private val PICK_IMAGE_REQUEST = 22
    private val RESULT_OK = -1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentUploadImageBinding.inflate(inflater, container, false)
        storage = FirebaseStorage.getInstance()
        storageReference = storage.getReference()

        binding.btnChoose.setOnClickListener {
            SelectImage()
        }

        binding.btnUpload.setOnClickListener {
            UploadImage()
        }
        return binding.root
    }

    private fun SelectImage() {
        // Defining Implicit Intent to mobile gallery
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(
            Intent.createChooser(
                intent,
                "Select Image from here..."
            ),
            PICK_IMAGE_REQUEST
        )
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // checking request code and result code
        // if request code is PICK_IMAGE_REQUEST and
        // resultCode is RESULT_OK
        // then set image in the image view

        if (requestCode == PICK_IMAGE_REQUEST
            && resultCode == RESULT_OK
            && data != null
            && data.data != null
        ) {
            // Get the Uri of data
            filePath = data.data!!
            try {
                // Setting image on image view using Bitmap
                val bitmap = MediaStore.Images.Media.getBitmap(context?.contentResolver, filePath)
                binding.imgView.setImageBitmap(bitmap)
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun UploadImage() {
        if (filePath != null) {
            var prgd = ProgressDialog(context)
            prgd.setTitle("Uploading...")
            prgd.show()

            val ref = storageReference.child("images/" + UUID.randomUUID().toString())
            ref.putFile(filePath)
                .addOnSuccessListener {
                    prgd.dismiss()
                    Toast.makeText(context, "Image uploaded", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    prgd.dismiss()
                    Toast.makeText(context, "Failed: " + it.message, Toast.LENGTH_SHORT).show()
                }
                .addOnProgressListener {
                    val progress = (100.0 * it.bytesTransferred / it.totalByteCount)
                    prgd.setMessage("Uploaded: " + progress.toInt() + "%")
                }
        }
    }
}