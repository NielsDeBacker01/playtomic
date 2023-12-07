package ap.edu.play2mic

import android.content.ContentValues.TAG
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.storage
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentSnapshot

class ProfileFragment : Fragment(R.layout.fragment_second) {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileImageView: ImageView
    private lateinit var imagePickerResultLauncher: ActivityResultLauncher<String>
    private val REQUEST_CODE_PICK_IMAGE = 101

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_second, container, false)
        auth = com.google.firebase.ktx.Firebase.auth
        val signOutButton = view.findViewById<Button>(R.id.sign_out_button)
        signOutButton.setOnClickListener {
            auth.signOut()
            val intent = Intent(getActivity(), SignInActivity::class.java)
            startActivity(intent)
            activity?.finish() // Close the current activity
        }

        val user = Firebase.auth.currentUser
        user?.let {

            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("users").document(user.uid)
            userRef.get().addOnSuccessListener { document ->
                if (document != null) {
                    updateProfilePage(document, view, user)

                    profileImageView.setOnClickListener {
                        openImagePicker()
                    }
                } else {
                    Log.d(TAG, "No such document")
                }

                val saveButton = view.findViewById<Button>(R.id.saveButton)
                saveButton.setOnClickListener {
                    val skillLevel = view.findViewById<EditText>(R.id.skillLevel).text.toString()
                    val handedness = view.findViewById<Spinner>(R.id.handedness).selectedItem.toString()
                    val position = view.findViewById<Spinner>(R.id.position).selectedItem.toString()
                    val matchType = view.findViewById<Spinner>(R.id.match_type).selectedItem.toString()
                    val playTime = view.findViewById<Spinner>(R.id.time).selectedItem.toString()

                    val userUpdates = hashMapOf(
                        "skillLevel" to skillLevel,
                        "handedness" to handedness,
                        "position" to position,
                        "matchType" to matchType,
                        "playTime" to playTime
                    )

                    userRef.update(userUpdates as Map<String, Any>)
                    userRef.get().addOnSuccessListener { document -> updateProfilePage(document,view,user)}

                }
            }

            imagePickerResultLauncher =
                registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    if (uri != null) {
                        updateProfilePicture(uri)
                    }
                }

            if (user != null) {
                val handednessSpinner = view.findViewById<Spinner>(R.id.handedness)
                val handednessOptions = arrayOf("Left", "Right", "Either")
                val handednessAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, handednessOptions)
                handednessAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                handednessSpinner.adapter = handednessAdapter

                val positionSpinner = view.findViewById<Spinner>(R.id.position)
                val positionOptions = arrayOf("Front", "Back", "Either")
                val positionAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, positionOptions)
                positionAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                positionSpinner.adapter = positionAdapter

                val matchTypeSpinner = view.findViewById<Spinner>(R.id.match_type)
                val matchTypeOptions = arrayOf("Competitive", "Friendly", "Either")
                val matchTypeAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, matchTypeOptions)
                matchTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                matchTypeSpinner.adapter = matchTypeAdapter

                val playTimeSpinner = view.findViewById<Spinner>(R.id.time)
                val playTimeOptions = arrayOf("Morning", "Noon", "Evening", "No preference")
                val playTimeAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, playTimeOptions)
                playTimeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                playTimeSpinner.adapter = playTimeAdapter


            }
        }

        if (user != null) {
            val emailTextView = view.findViewById<TextView>(R.id.email)

            emailTextView.text = user.email
        }

        return view
    }

    private fun updateProfilePage(
        document: DocumentSnapshot,
        view: View,
        user: FirebaseUser
    ) {
        val skillLevel = document.getString("skillLevel")
        val handedness = document.getString("handedness")
        val position = document.getString("position")
        val matchType = document.getString("matchType")
        val playTime = document.getString("playTime")

        val emailTextView = view.findViewById<TextView>(R.id.email)
        profileImageView = view.findViewById(R.id.profileImageView)

        val skillLevelEditText = view.findViewById<EditText>(R.id.skillLevel)
        skillLevelEditText.setText(skillLevel, TextView.BufferType.EDITABLE)

        val handednessSpinner = view.findViewById<Spinner>(R.id.handedness)
        val positionSpinner = view.findViewById<Spinner>(R.id.position)
        val matchTypeSpinner = view.findViewById<Spinner>(R.id.match_type)
        val playTimeSpinner = view.findViewById<Spinner>(R.id.time)

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
    }

    private fun openImagePicker() {
        if (ContextCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            imagePickerResultLauncher.launch("image/*")
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
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