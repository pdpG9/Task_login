package com.example.task_login

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.task_login.databinding.ActivityMain2Binding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class MainActivity2 : AppCompatActivity() {
    private lateinit var binding: ActivityMain2Binding
    private var uId = ""
    private val storage = FirebaseStorage.getInstance()
    private lateinit var storageRef:StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        uId = intent.getStringExtra("uId").toString()
        storageRef = storage.reference.child("images/${uId}")
        uploadImage()
        binding.pickImage.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    openGallery()
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                }
            } else {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                    == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                        REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION
                    )
                }
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                }
                return
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION && Environment.isExternalStorageManager()) {
            openGallery()
        } else {
            Log.d("TTT", "onActivityResult: Environment.isExternalStorageManager is failed")
        }

        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == Activity.RESULT_OK && data != null) {
            val imageUri = data.data


            if (imageUri != null) {
                storageRef.putFile(imageUri)
                    .addOnSuccessListener { taskSnapshot ->
                        Log.d("TTT", "upload:${taskSnapshot.bytesTransferred}")
                    uploadImage()
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(this, exception.localizedMessage, Toast.LENGTH_SHORT).show()
                    }
            }

        }
    }
    private fun uploadImage(){
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            val imageUrl = uri.toString()
            Glide.with(this).load(imageUrl).into(binding.imageView)
        }
    }

    companion object {
        private const val REQUEST_CODE_SELECT_IMAGE = 1
        private const val REQUEST_CODE_READ_EXTERNAL_STORAGE_PERMISSION = 10
        private const val REQUEST_CODE_ALL_FILES_ACCESS_PERMISSION = 100
    }
}
