package com.stim.blitzmusic;

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var gotoSignupButton: TextView

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        emailEditText = findViewById(R.id.editForgotPasswordEmail)
        resetButton = findViewById(R.id.btnReset)
        progressBar = findViewById(R.id.progress_bar)
        gotoSignupButton = findViewById(R.id.goto_signup_btn)

        resetButton.setOnClickListener {
            resetPassword()
        }

        gotoSignupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    private fun resetPassword() {
        val email = emailEditText.text.toString().trim()

        if (email.isEmpty()) {
            emailEditText.error = "Email is required"
            emailEditText.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                progressBar.visibility = View.GONE
                if (task.isSuccessful) {
                    // Password reset email sent successfully
                    showMessageAndRedirect("Email sent successfully", LoginActivity::class.java)
                } else {
                    // If the user is not found, Firebase returns a FirebaseAuthInvalidUserException
                    if (task.exception is FirebaseAuthInvalidUserException) {
                        showMessage("User not found. Please sign up.")
                    } else {
                        // Handle other errors here
                        showMessage("Password reset failed. Please try again.")
                    }
                }
            }
    }

    private fun showMessage(message: String) {
        // Display a simple pop-up message
        // You can customize the pop-up appearance as needed
        // For simplicity, I'm using a Toast here
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun showMessageAndRedirect(message: String, destinationActivity: Class<*>) {
        showMessage(message)
        // Redirect to the specified activity
        startActivity(Intent(this, destinationActivity))
        // Finish the current activity to prevent going back to it from the redirected activity
        finish()
    }
}
