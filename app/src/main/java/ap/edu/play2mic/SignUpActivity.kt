package ap.edu.play2mic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class SignUpActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val TAG = "SignInActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)
        auth = Firebase.auth

        val emailEditText = findViewById<EditText>(R.id.email)
        val passwordEditText = findViewById<EditText>(R.id.password)
        val signUpButton = findViewById<Button>(R.id.sign_up_button)
        signUpButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign up success
                        val user = auth.currentUser

                        val db = Firebase.firestore
                        val userRef = db.collection("users").document(user?.uid.toString())
                        val userUpdates = hashMapOf(
                            "skillLevel" to "1.0",
                            "handedness" to "Either",
                            "position" to "Either",
                            "matchType" to "Either",
                            "playTime" to "No preference",
                            "email" to email
                        )
                        userRef.set(userUpdates)

                        updateUI(user)
                    } else {
                        // Sign up fails
                        Log.w(TAG, "createUserWithEmail:failure", task.exception)
                        updateUI(null)
                    }
                }
        }
    }

    private fun updateUI(user: FirebaseUser?) {
        if (user != null) {
            // User is signed up, navigate to another activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Sign up failed, show an error message
            Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
        }
    }
}