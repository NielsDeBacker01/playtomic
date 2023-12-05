package ap.edu.play2mic

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.storage.storage
import android.Manifest
import androidx.activity.result.ActivityResultLauncher

class ProfileActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var imagePickerResultLauncher: ActivityResultLauncher<String>
    private val REQUEST_CODE_PICK_IMAGE = 101

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
            profileImageView = findViewById(R.id.profileImageView)

            emailTextView.text = user.email
            val photoUrl = user.photoUrl
            if (photoUrl != null) {
                updateProfilePicture(photoUrl)
            }

            imagePickerResultLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                if (uri != null) {
                    updateProfilePicture(uri)
                }
            }

            profileImageView.setOnClickListener {
                requestStoragePermission()
                openImagePicker()
            }
        }
    }
    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            imagePickerResultLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), REQUEST_CODE_PICK_IMAGE)
        }

    }

    private fun updateProfilePicture(imageUri: Uri) {
        val user = Firebase.auth.currentUser
        user?.let {
            val uploadTask = Firebase.storage.reference.child("profileImages").child(user.uid).putFile(imageUri)
            uploadTask.addOnSuccessListener {
                it.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                    val userProfileChangeRequest = UserProfileChangeRequest.Builder()
                        .setPhotoUri(uri)
                        .build()
                    user.updateProfile(userProfileChangeRequest)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("ProfileImage", "Profile image updated successfully")
                            } else {
                                Log.d("ProfileImage", "Profile image update failed")
                            }
                        }
                }
            }
        }

        Glide.with(this)
            .load(imageUri)
            .placeholder(R.drawable.default_pfp)
            .error(R.drawable.default_pfp)
            .into(profileImageView)
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PICK_IMAGE
            )
        }
    }

}