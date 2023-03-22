package com.example.task_login

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.task_login.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class MainActivity : AppCompatActivity() {
    private lateinit var _binding:ActivityMainBinding
    private lateinit var auth:FirebaseAuth
    private var androidId = ""
    private var isAlreadySaved = false
    @SuppressLint("HardwareIds")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(_binding.root)
        auth = Firebase.auth
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        Log.d("TTT", "androidId: $androidId")

        _binding.btnSignIn.setOnClickListener {
            isShowProgress(true)
            if (checkInputs()){
                auth.signInWithEmailAndPassword(_binding.inputEmail.text.toString(),_binding.inpuPassword.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Sign in is success!", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.let { it1 -> moveToNextActivity(it1.uid) }
                    isShowProgress(false)
                    auth.currentUser?.uid?.let { it1 -> saveToFireStore(it1) }
                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    isShowProgress(false)
                }
            }else{
                isShowProgress(false)
                Toast.makeText(this, "email or password is invalid!", Toast.LENGTH_SHORT).show()
            }
        }
        _binding.btnSignUp.setOnClickListener {
            isShowProgress(true)
            if (checkInputs()){
                auth.createUserWithEmailAndPassword(_binding.inputEmail.text.toString(),_binding.inpuPassword.text.toString()).addOnSuccessListener {
                    Toast.makeText(this, "Sign in is success!", Toast.LENGTH_SHORT).show()
                    auth.currentUser?.let { it1 -> moveToNextActivity(it1.uid) }
                    isShowProgress(false)
                    auth.currentUser?.uid?.let { it1 -> saveToFireStore(it1) }

                }.addOnFailureListener {
                    Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT).show()
                    isShowProgress(false)
                }
            }else{
                isShowProgress(false)
                Toast.makeText(this, "email or password is invalid!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun saveToFireStore(uId:String){
        Log.d("TTT", "saveToFireStore: $androidId")
        val db = Firebase.firestore
        val user = hashMapOf(
            "id" to androidId,
            "uid" to uId
        )
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["id"]==androidId) {
                        isAlreadySaved = true
                    }
                }
            }
        if (!isAlreadySaved){
            db.collection("users")
                .add(user)
                .addOnSuccessListener {
                    Toast.makeText(this, "user is successfully saved!", Toast.LENGTH_SHORT).show()
                }.addOnFailureListener {
                    Toast.makeText(this, "user is not saved!", Toast.LENGTH_SHORT).show()
                }
        }
    }
    override fun onStart() {
        super.onStart()
        val currentUser = auth.currentUser
        if (currentUser!=null){
           moveToNextActivity(currentUser.uid)
        }else{
        val db = Firebase.firestore
        db.collection("users")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    if (document.data["id"]==androidId){
                        moveToNextActivity(document.data["uid"] as String)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.w("TTT", "Error getting documents.", exception)
            }
        }
    }
    private fun checkInputs():Boolean{
        if (!_binding.inputEmail.text.toString().endsWith("@gmail.com")) return false
        if (_binding.inpuPassword.text.toString().length<=6) return false
        return true
    }
    private fun moveToNextActivity(uId: String){
        val intent = Intent(this,MainActivity2::class.java)
        intent.putExtra("uId", uId)
        startActivity(intent)
        finish()
    }
    private fun isShowProgress(isShow:Boolean){
        if (isShow) _binding.progress.visibility = View.VISIBLE
        else _binding.progress.visibility = View.INVISIBLE
    }
}