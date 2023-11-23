package ap.edu.play2mic

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_profile)
        auth = com.google.firebase.ktx.Firebase.auth
        val signOutButton = findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }

        val user = Firebase.auth.currentUser
        user?.let {
            // Name, email address, and profile photo Url
            val email = it.email

            // The user's ID, unique to the Firebase project. Do NOT use this value to
            // authenticate with your backend server, if you have one. Use
            // FirebaseUser.getIdToken() instead.
            val uid = it.uid
        }

        if (user != null) {
            val emailTextView = findViewById<TextView>(R.id.email)

            emailTextView.text = user.email
        }
    }
}