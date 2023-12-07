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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import android.Manifest
import android.content.ContentValues.TAG
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.result.ActivityResultLauncher
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

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
            finish()
        }

        val user = Firebase.auth.currentUser
        user?.let {

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    val skillLevel = document.getString("skillLevel")
                    val handedness = document.getString("handedness")
                    val position = document.getString("position")
                    val matchType = document.getString("match type")
                    val playTime = document.getString("play time")

                    val emailTextView = findViewById<TextView>(R.id.email)
                    profileImageView = findViewById(R.id.profileImageView)

                    val skillLevelEditText = findViewById<EditText>(R.id.skillLevel)
                    skillLevelEditText.setText(skillLevel, TextView.BufferType.EDITABLE)

                    val handednessSpinner = findViewById<Spinner>(R.id.handedness)
                    val positionSpinner = findViewById<Spinner>(R.id.position)
                    val matchTypeSpinner = findViewById<Spinner>(R.id.match_type)
                    val playTimeSpinner = findViewById<Spinner>(R.id.time)

                    handednessSpinner.setSelection(
                        (handednessSpinner.adapter as ArrayAdapter<String>).getPosition(
                            handedness
                        )
                    )
                    positionSpinner.setSelection(
                        (positionSpinner.adapter as ArrayAdapter<String>).getPosition(
                            position
                        )
                    )
                    matchTypeSpinner.setSelection(
                        (matchTypeSpinner.adapter as ArrayAdapter<String>).getPosition(
                            matchType
                        )
                    )
                    playTimeSpinner.setSelection(
                        (playTimeSpinner.adapter as ArrayAdapter<String>).getPosition(
                            playTime
                        )
                    )
                    emailTextView.text = user.email
                    val photoUrl = user.photoUrl
                    if (photoUrl != null) {
                        updateProfilePicture(photoUrl)
                    }

                    profileImageView.setOnClickListener {
                        openImagePicker()
                    }
                } else {
                    Log.d(TAG, "No such document")
                }

                val saveButton = findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener {
                    val skillLevel = findViewById<EditText>(R.id.skillLevel).text.toString()
                    val handedness = findViewById<Spinner>(R.id.handedness).selectedItem.toString()
                    val position = findViewById<Spinner>(R.id.position).selectedItem.toString()
                    val matchType = findViewById<Spinner>(R.id.match_type).selectedItem.toString()
                    val playTime = findViewById<Spinner>(R.id.time).selectedItem.toString()

                    val userUpdates = hashMapOf(
                        "skillLevel" to skillLevel,
                        "handedness" to handedness,
                        "position" to position,
                        "matchType" to matchType,
                        "playTime" to playTime
                    )

                    userRef.update(userUpdates as Map<String, Any>)
                    finish()
                }
            }

            imagePickerResultLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    if (uri != null) {
                        updateProfilePicture(uri)
                    }
                }

            if (user != null) {
                val handednessSpinner = findViewById<Spinner>(R.id.handedness)
                val handednessOptions = arrayOf("Left", "Right", "Either")
                val handednessAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, handednessOptions)
                handednessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                handednessSpinner.adapter = handednessAdapter

                val positionSpinner = findViewById<Spinner>(R.id.position)
                val positionOptions = arrayOf("Front", "Back", "Either")
                val positionAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, positionOptions)
                positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                positionSpinner.adapter = positionAdapter

                val matchTypeSpinner = findViewById<Spinner>(R.id.match_type)
                val matchTypeOptions = arrayOf("Competitive", "Friendly", "Either")
                val matchTypeAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, matchTypeOptions)
                matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                matchTypeSpinner.adapter = matchTypeAdapter

                val playTimeSpinner = findViewById<Spinner>(R.id.time)
                val playTimeOptions = arrayOf("Morning", "Noon", "Evening", "No preference")
                val playTimeAdapter =
                    ArrayAdapter(this, android.R.layout.simple_spinner_item, playTimeOptions)
                playTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                playTimeSpinner.adapter = playTimeAdapter


            }
        }
    }

    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            imagePickerResultLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                REQUEST_CODE_PICK_IMAGE
            )
        }
    }

   private fun updateProfilePicture(imageUri: Uri) {
        val user = Firebase.auth.currentUser
        user?.let {
            val uploadTask =
                Firebase.storage.reference.child("profileImages").child(user.uid).putFile(imageUri)
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
}

