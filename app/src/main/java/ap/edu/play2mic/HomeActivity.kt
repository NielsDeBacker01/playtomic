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
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity() {
    private val TAG = "HomeActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.w(TAG, "opened home activity")

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val profileButton = findViewById<Button>(R.id.profile_button)
        profileButton.setOnClickListener {
            Log.w(TAG, "Profilebutton:pressed")
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}