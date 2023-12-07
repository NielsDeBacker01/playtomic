package ap.edu.play2mic

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class SearchFragment: Fragment(R.layout.fragment_search) {

    private val TAG = "SearchFragment"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.w(TAG, "opened home fragment")

        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val locationsButton = view.findViewById<Button>(R.id.locations)
        locationsButton.setOnClickListener {
            val intent = Intent(activity, LocationsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}