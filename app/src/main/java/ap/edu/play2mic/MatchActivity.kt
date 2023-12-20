package ap.edu.play2mic

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.CalendarView
import android.widget.TextView
import androidx.activity.ComponentActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.type.DateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class MatchActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match)

        auth = FirebaseAuth.getInstance()

        val extras = intent.extras
        val matchId = extras?.getString("matchId")

        // Check if matchId is not null before proceeding with the data retrieval
        if (matchId != null) {
            val matchRef = db.collection("matches").document(matchId)

            matchRef.get().addOnSuccessListener { documentSnapshot: DocumentSnapshot ->
                if (documentSnapshot.exists()) {
                    var address = ""
                    val level = documentSnapshot.getString("level")
                    val matchType = documentSnapshot.getString("playType")
                    val location = documentSnapshot.get("location") as DocumentReference
                    val players = documentSnapshot.get("players") as List<DocumentReference>
                    val emails = ArrayList<String>()
                    val allowedGenders = documentSnapshot.getString("allowedGenders")
                    val time = documentSnapshot.getTimestamp("time")

                    Log.d("Location", "players added: ${players}")

                    location.get().continueWith { locationDocument ->
                        val locationData = locationDocument.result?.data
                        Log.d("Location", "location data added: ${locationData.toString()}")
                        address += locationData?.get("Name").toString() + ": " + locationData?.get("Streetname").toString() + " "+ locationData?.get("City").toString()

                        Log.d("Location", "address added: ${address}")

                        updateUI(level, matchType, address, emails, allowedGenders, time)
                    }

                    val scope = CoroutineScope(Dispatchers.IO)

                    scope.launch {
                        val deferreds = players.map { playerRef ->
                            async {
                                val playerDocument = playerRef.get().await()
                                if (playerDocument.exists()) {
                                    playerDocument.getString("email").toString()
                                } else {
                                    null
                                }
                            }
                        }

                        emails.addAll(deferreds.awaitAll().filterNotNull())
                        runOnUiThread {
                            updateUI(level, matchType, address, emails, allowedGenders, time)
                        }
                    }

                    val joinMatchButton = findViewById<Button>(R.id.joinMatchButton)
                    joinMatchButton.setOnClickListener {
                        handleJoinMatch(matchId, players)
                    }
                }
            }
        }
    }

    private fun updateUI(
        level: String?,
        matchType: String?,
        location: String?,
        players: ArrayList<String>?,
        allowedGenders: String?,
        time: Timestamp?
    ) {
        val playersTextView = findViewById<TextView>(R.id.players)
        playersTextView.text = players?.joinToString(", ")

        val levelTextView = findViewById<TextView>(R.id.level)
        levelTextView.text = level

        val matchTypeTextView = findViewById<TextView>(R.id.matchType)
        matchTypeTextView.text = matchType

        val locationTextView = findViewById<TextView>(R.id.location)
        locationTextView.text = location

        val allowedGendersTextView = findViewById<TextView>(R.id.allowedGenders)
        allowedGendersTextView.text = allowedGenders

        val timeTextView = findViewById<TextView>(R.id.time)
        val instant = Instant.ofEpochSecond(time?.seconds ?: 0L)
        val zonedDateTime = instant.atZone(ZoneId.systemDefault())
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        timeTextView.text = zonedDateTime.format(formatter)
    }

    private fun handleJoinMatch(matchId: String, players: List<DocumentReference>?) {
        val user = auth.currentUser
        if (user != null && players != null && players.size < 4 && !players.any { it.id == user.uid }) {
            val newPlayers = players.toMutableList()
            newPlayers.add(db.collection("users").document(user.uid))

            val matchRef = db.collection("matches").document(matchId)
            matchRef.update("players", FieldValue.arrayUnion(*newPlayers.toTypedArray()))

            val userRef = db.collection("users").document(user.uid)
            userRef.update("matches", FieldValue.arrayUnion(matchRef))
        }
    }
}
