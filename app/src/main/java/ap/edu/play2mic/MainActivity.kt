package ap.edu.play2mic

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import ap.edu.play2mic.ui.theme.MyApplicationTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //this page is not correct and has no code
        setContentView(R.layout.activity_home)

        // Check if the user is already signed in
        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            // User is not signed in, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }
        else
        {
            // User is signed in, redirect to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish() // Close the current activity
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("Android")
    }
}