package ap.edu.play2mic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore


class LocationsActivity : AppCompatActivity() {
    private val TAG = "Location Activity"

    val db = FirebaseFirestore.getInstance()
    val locations = db.collection("locations")
    val locationList = mutableListOf<Map<String, Any>>()
    val filteredList = mutableListOf<Map<String, Any>>()

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_locations)

        recyclerView = findViewById<RecyclerView>(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val searchView = findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                return false
            }
        })

        locations.get().addOnSuccessListener { result ->
            for (document in result) {
                val element = document.data
                element.put("Uid", document.id)
                locationList.add(element)
            }
            Log.d(TAG, "$locationList")
            filteredList.addAll(locationList)
            recyclerView.adapter = ListAdapter(this, filteredList as ArrayList<Map<String, Any>>)
            //listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, locationList)
        }.addOnFailureListener { exception ->
            Log.w(TAG, "Error getting documents: ", exception)
        }
    }

    private fun filter(text: String?) {
        filteredList.clear()
        if (text == null || text.isEmpty()) {
            filteredList.addAll(locationList)
        } else {
            for (item in locationList) {
                if (item["Name"].toString().contains(text, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun filter2(text: String?) {
        filteredList.clear()
        if (text == null || text.isEmpty()) {
            filteredList.addAll(locationList)
        } else {
            for (item in locationList) {
                if (item["Name"].toString().contains(text, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
        recyclerView.adapter?.notifyDataSetChanged()
    }
}

class ListAdapter(context: Context, dataArrayList: ArrayList<Map<String, Any>>) :
    RecyclerView.Adapter<ListAdapter.ViewHolder>() {

    private val mContext = context
    private val mDataArrayList = dataArrayList
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.list_item, parent, false)
        return ViewHolder(view)
    }
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val listData = mDataArrayList[position]
        holder.name.text = listData["Name"].toString()
        holder.price.text = listData["Price"].toString() + "€/uur"
        holder.location.text = listData["Streetname"].toString() + " (" + listData["City"].toString() + ")"

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, ListItemDetailedActivity::class.java)
            var bundle = Bundle()
            bundle.putString("Id", listData["Uid"].toString())
            bundle.putString("Name", listData["Name"].toString())
            bundle.putString("City", listData["City"].toString())
            bundle.putString("Streetname", listData["Streetname"].toString())
            bundle.putString("Price", listData["Price"].toString())
            bundle.putString("Fields", listData["Fields"].toString())
            bundle.putString("OpeningTime", listData["OpeningTime"].toString())
            bundle.putString("ClosingTime", listData["ClosingTime"].toString())
            intent.putExtras(bundle)
            mContext.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return mDataArrayList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.name)
        val price: TextView = itemView.findViewById(R.id.price)
        val location: TextView = itemView.findViewById(R.id.location)
    }
}