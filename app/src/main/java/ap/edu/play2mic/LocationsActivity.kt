package ap.edu.play2mic

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore

class LocationsActivity : AppCompatActivity() {
    private val TAG = "Location Activity"

    val db = FirebaseFirestore.getInstance()
    val locations = db.collection("locations")
    val locationList = mutableListOf<Map<String, Any>>()

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        val listView = findViewById<ListView>(R.id.list_view)
        locations.get().addOnSuccessListener { result ->
            for (document in result) {
                locationList.add(document.data)
            }
            Log.d(TAG, "$locationList")
            listView.adapter = ListAdapter(this, locationList as ArrayList<Map<String, Any>>)
            //listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }
    }
}

class ListAdapter(context: Context, dataArrayList: ArrayList<Map<String, Any>>) :
    ArrayAdapter<Map<String, Any>>(context, R.layout.list_item, dataArrayList) {
    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var view = view
        val listData = getItem(position)
        if (view == null) {
            view = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false)
        }
        val name = view!!.findViewById<TextView>(R.id.name)
        val price = view.findViewById<TextView>(R.id.price)
        val location = view.findViewById<TextView>(R.id.location)
        name.text = listData!!["Name"].toString()
        price.text = listData["Price"].toString() + "€/uur"
        location.text = listData["Streetname"].toString() + " (" + listData["City"].toString() + ")"
        return view
    }
}