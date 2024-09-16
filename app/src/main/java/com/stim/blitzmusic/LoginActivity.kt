package com.stim.blitzmusic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.stim.blitzmusic.databinding.ActivityLoginBinding
import java.util.regex.Pattern

class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginBtn.setOnClickListener{
            val email = binding.emailEdittext.text.toString()
            val password = binding.passwordEdittext.text.toString()

            if(!Pattern.matches(Patterns.EMAIL_ADDRESS.pattern(),email)){
                binding.emailEdittext.setError("Invalid email")
                return@setOnClickListener
            }

            if (password.length < 6){
                binding.passwordEdittext.setError("Length shoul be char 6")
                return@setOnClickListener
            }
            loginWithFirebase(email,password)
        }
        binding.gotoSignupBtn.setOnClickListener {
            startActivity(Intent(this,SignupActivity::class.java))
        }

        binding.btnForgotPassword.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }

    }

    fun loginWithFirebase(email : String, password : String){
        setInProgress(true)
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password)
            .addOnSuccessListener{
                setInProgress(false)
               startActivity(Intent(this@LoginActivity,MainActivity::class.java))
                finish()
            }.addOnFailureListener{
                setInProgress(false)
                Toast.makeText(applicationContext,"Create account failed", Toast.LENGTH_SHORT).show()
            }


    }

    override fun onResume() {
        super.onResume()
        FirebaseAuth.getInstance().currentUser?.apply{
            startActivity(Intent(this@LoginActivity,MainActivity::class.java))
            finish()
        }
    }

    fun setInProgress(inProgress : Boolean){
        if(inProgress){
            binding.loginBtn.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
        }else{
            binding.loginBtn.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

}