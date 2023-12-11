package ap.edu.play2mic

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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
    private var allowedGenders: String? = null
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_item_detailed)

        val extras = intent.extras
        val timeOptions =  mutableListOf<String>()

        if (extras != null) {
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

        val btnDatePicker: Button = findViewById(R.id.btnDatePicker)
        spinner = findViewById(R.id.spinnerOptions)
        tvSelectedDate = findViewById(R.id.tvSelectedDate)

        btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, timeOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: android.view.View?, position: Int, id: Long) {
                selectedHour = timeOptions[position]
                tvSelectedDate.text = "Selected Date: $selectedDate $selectedHour"
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }

        playType = "Competitive"
        findViewById<RadioButton>(R.id.radio_competitive).setOnCheckedChangeListener { buttonView, isChecked ->
            playType = "Competitive"
        }
        findViewById<RadioButton>(R.id.radio_friendly).setOnCheckedChangeListener { buttonView, isChecked ->
            playType = "Friendly"
        }

        allowedGenders = "Mixed"
        findViewById<RadioButton>(R.id.radio_all).setOnCheckedChangeListener { buttonView, isChecked ->
            playType = "All"
        }
        findViewById<RadioButton>(R.id.radio_mixed).setOnCheckedChangeListener { buttonView, isChecked ->
            playType = "Mixed"
        }
        findViewById<RadioButton>(R.id.radio_men).setOnCheckedChangeListener { buttonView, isChecked ->
            playType = "Men only"
        }

        val btnMatchMaker: Button = findViewById(R.id.btnMatchMaker)
        btnMatchMaker.setOnClickListener {
            createMatches()
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
                    tvSelectedDate.text = "Selected Date: $selectedDate + $selectedHour"
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

    private fun createMatches() {
        Log.d("test", "Selected Date: $selectedDate $selectedHour, Type: $playType, Gender: $allowedGenders")
    }
}