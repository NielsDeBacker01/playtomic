package ap.edu.play2mic

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class ListItemDetailedActivity : AppCompatActivity() {
    private val TAG = "Location Detail Activity"
    private lateinit var tvSelectedDate: TextView
    private lateinit var spinner: Spinner
    private var selectedHour: String? = null
    private var selectedDate: String? = null
    private var playType: String? = null
    private var level: String? = null
    private var allowedGenders: String? = null
    private var fieldId: String? = null
    private var matches: QuerySnapshot? = null
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val db = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    val timeOptions =  mutableListOf<String>()
    val reservedTimes =  mutableListOf<Date>()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item_detailed)

        val extras = intent.extras

        //load item specific data in
        if (extras != null) {
            fieldId = extras.getString("Id")
            val name = extras.getString("Name")
            val price = extras.getString("Price") + "€/uur"
            val streetname = extras.getString("Streetname") + " (" + extras.getString("City") + ")"
            val startTimeString = extras.getString("OpeningTime")
            val endTimeString = extras.getString("ClosingTime")

            if (!name.isNullOrEmpty()) {
                val nameTextView = findViewById<TextView>(R.id.name)
                nameTextView.text = name
            }

            if (!price.isNullOrEmpty()) {
                val priceTextView = findViewById<TextView>(R.id.price)
                priceTextView.text = price
            }

            if (!streetname.isNullOrEmpty()) {
                val locationTextView = findViewById<TextView>(R.id.location)
                locationTextView.text = streetname
            }

            val startTime = startTimeString?.let { convertStringToTime(it) }
            val endTime = endTimeString?.let { convertStringToTime(it) }


            val calendar = Calendar.getInstance()
            calendar.time = startTime
            while (calendar.time.before(endTime)) {
                timeOptions.add(dateFormat.format(calendar.time))
                calendar.add(Calendar.MINUTE, 30)
            }
        }

        //setup ui
        setupUiElements()

        //get match reservations for time options filtering
        val matchesRef = db.collection("matches")
        val docRef: DocumentReference = FirebaseFirestore.getInstance().document("locations/$fieldId")
        Log.d(TAG, docRef.toString())
        matchesRef.whereEqualTo("location", docRef).get().addOnSuccessListener { querySnapshot ->
            matches = querySnapshot
            for (document in querySnapshot) {
                Log.d(TAG, "Matches: ${document.id} => ${document.data}")
            }
            Log.d(TAG, "finished getting matches")
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }

        //get user values for creation
        val usersRef = db.collection("users")
        usersRef.whereEqualTo("__name__", currentUser!!.uid).get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot) {
                level = document.get("skillLevel") as? String
                Log.d(TAG, "Users: ${document.id} => ${document.data}")
            }
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents.", exception)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                // Handle the selected date
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val selectedDateCalendar = Calendar.getInstance()
                selectedDateCalendar.set(selectedYear, selectedMonth, selectedDay)
                selectedDate = format.format(selectedDateCalendar.time)

                // Update the TextView with the selected date
                if(selectedDate != null && selectedHour != null)
                {
                    reservedTimes.clear()
                    val btnDatePicker: Button = findViewById(R.id.btnDatePicker)
                    val displayTextView = findViewById<TextView>(R.id.tvSelectedDate)
                    val spinner = findViewById<Spinner>(R.id.spinnerOptions)
                    displayTextView.setVisibility(View.VISIBLE)
                    spinner.setVisibility(View.VISIBLE)
                    tvSelectedDate.text = "Selected Date: $selectedDate + $selectedHour"
                    btnDatePicker.text = "$selectedDate"

                    if(matches != null )
                    {
                        for (document in matches!!) {
                            val timestamp = document.getTimestamp("time")
                            val dateTime = timestamp!!.toDate()
                            val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            val date = formatter.format(dateTime)
                            if(date == selectedDate)
                            {
                                reservedTimes.add(dateTime)
                                Log.d(TAG, "yes:$date")
                            }
                            else
                            {
                                Log.d(TAG, "no:$date")
                            }
                        }
                        loadSpinner()
                    }
                }
                else
                {
                    tvSelectedDate.text = ""
                }
            },
            year,
            month,
            day
        )

        // Show the date picker dialog
        datePickerDialog.show()
    }

    fun convertStringToTime(timeString: String): Date? {
        val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return try {
            dateFormat.parse(timeString)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupUiElements()
    {
        val btnDatePicker: Button = findViewById(R.id.btnDatePicker)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)
        val btnMatchMaker: Button = findViewById(R.id.btnMatchMaker)


        btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }
        loadSpinner()

        val radioGroupPlayType = findViewById<RadioGroup>(R.id.radio_group_playtype)
        playType = "Competitive"
        radioGroupPlayType.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_competitive -> {
                    playType = "Competitive"
                }
                R.id.radio_friendly -> {
                    playType = "Friendly"
                }
            }
        }

        val radioGroupGenders = findViewById<RadioGroup>(R.id.radio_group_genders)
        allowedGenders = "Mixed"
        radioGroupGenders.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radio_all -> {
                    allowedGenders = "All"
                }
                R.id.radio_mixed -> {
                    allowedGenders = "Mixed"
                }
                R.id.radio_men -> {
                    allowedGenders = "Men only"
                }
            }
        }

        btnMatchMaker.setOnClickListener {
            createMatches()
        }
    }

    private fun loadSpinner() {
        spinner = findViewById(R.id.spinnerOptions)

        val adapter = CustomSpinnerAdapter(this, timeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        //filter times
        for(reservation in reservedTimes)
        {
            val formatterDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = formatterDate.format(reservation)
            val formatterTime = SimpleDateFormat("HH:mm", Locale.getDefault())
            val time = formatterTime.format(reservation)
            for(option in timeOptions)
            {
                if(option == time)
                {
                    val index = timeOptions.indexOf(time)
                    adapter.disableItem(index - 2)
                    adapter.disableItem(index - 1)
                    adapter.disableItem(index)
                    adapter.disableItem(index + 1)
                    adapter.disableItem(index + 2)
                    Log.d(TAG, "yes:$time")
                }
                else
                {
                    Log.d(TAG, "no:$time")
                }
            }
        }

        //disable if outdated
        if (selectedDate != null) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            if (dateFormat.parse(selectedDate).before(Calendar.getInstance().time)) {
                for (i in 0..timeOptions.size - 1) {
                    adapter.disableItem(i)
                }
                Log.d(TAG, "time disabled")
            }
        }

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: android.view.View?, position: Int, id: Long) {
                selectedHour = timeOptions[position]
                tvSelectedDate.text = "Selected Date: $selectedDate $selectedHour"
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }
    }

    private fun createMatches() {
        val currentUser = Firebase.auth.currentUser

        data class Match(
            val allowedGenders: String?,
            val level: String?,
            val location: DocumentReference,
            val playType: String?,
            val players: List<DocumentReference>,
            val time: Timestamp
        )

        //set time
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        val date = formatter.parse("$selectedDate $selectedHour:00")
        val timestamp = Timestamp(date.time);

        //get locationref
        val location = db.collection("locations").document(fieldId!!)

        //get playerrefs
        val userRef = db.collection("users").document(currentUser!!.uid)
        val players = listOf<DocumentReference>(userRef!!)

        val match = Match(allowedGenders, level, location, playType, players, timestamp)
        Log.d(TAG, "DocumentSnapshot added with Data: $match")

        //disable if outdated
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        if (dateFormat.parse(selectedDate).before(Calendar.getInstance().time)) {
            val errorTextView = findViewById<TextView>(R.id.error)
            errorTextView.text = "no time selected"
            Log.d(TAG, "failed: old date")
        }
        else
        {
            db.collection("matches")
                .add(match)
                .addOnSuccessListener { documentReference: DocumentReference ->
                    Log.d(TAG, "DocumentSnapshot added with ID: ${documentReference.id}")
                }
                .addOnFailureListener { e: Exception ->
                    Log.w(TAG, "Error adding document", e)
                }
            finish()
        }
    }
}

class CustomSpinnerAdapter(context: Context, private val items: List<String>) : ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, items) {

    private val enabledItems = items.indices.toMutableList()

    fun disableItem(position: Int) {
        enabledItems.remove(position)
    }

    override fun isEnabled(position: Int): Boolean {
        return enabledItems.contains(position)
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        if (!isEnabled(position)) {
            view.isEnabled = false
        }
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        if (!isEnabled(position)) {
            view.isEnabled = false
        }
        return view
    }
}