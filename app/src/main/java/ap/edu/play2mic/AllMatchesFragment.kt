package ap.edu.play2mic

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore

class AllMatchesFragment : Fragment(R.layout.fragment_all_matches) {

    val db = FirebaseFirestore.getInstance()
    val matches = db.collection("matches")
    val matchesList = mutableListOf<Map<String, Any>>()
    val filteredList = mutableListOf<Map<String, Any>>()
    private var columnCount = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_all_matches, container, false)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_view_all_matches)
        recyclerView.layoutManager = when {
            columnCount <= 1 -> LinearLayoutManager(context)
            else -> GridLayoutManager(context, columnCount)
        }

        val searchView = view.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filter(newText)
                recyclerView.adapter?.notifyDataSetChanged()
                return false
            }
        })

        matches.get().addOnSuccessListener { result ->

            var processedDocuments = 0
            val totalDocuments = result.size()
            for (document in result) {
                val matchData = document.data
                matchData["id"] = document.id

                val locationId = matchData["location"] as DocumentReference
                locationId.get().continueWith { locationDocument ->
                    val locationData = locationDocument.result?.data
                    matchData["location"] = locationData?.get("Name") ?: ""

                    processedDocuments++
                    if (processedDocuments == totalDocuments) {
                        recyclerView.adapter = ListAdapter(view.context, filteredList as ArrayList<Map<String, Any>>)
                        recyclerView.adapter?.notifyDataSetChanged()
                    }
                }
                filteredList.add(matchData)
                matchesList.add(matchData)
            }
        }
        return view
    }

    fun filter(text: String?) {
        filteredList.clear()
        if (text == null || text.isEmpty()) {
            filteredList.addAll(matchesList)
        } else {
            for (item in matchesList) {
                if (item["location"].toString().contains(text, ignoreCase = true)) {
                    filteredList.add(item)
                }
            }
        }
    }
    class ListAdapter(context: Context, dataArrayList: ArrayList<Map<String, Any>>) :
        RecyclerView.Adapter<ListAdapter.ViewHolder>() {

        private val mContext = context
        private val mDataArrayList = dataArrayList
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(mContext).inflate(R.layout.match_list_item, parent, false)
            return ViewHolder(view)
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val listData = mDataArrayList[position]
            holder.level.text = listData["level"].toString()
            holder.matchType.text = listData["playType"].toString()
            holder.location.text = listData["location"].toString()
            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, MatchActivity::class.java)
                var bundle = Bundle()
                bundle.putString("matchId", listData["id"].toString())

                Log.d("ListAdapter", "MatchId added to bundle: ${listData["id"]}")

                intent.putExtras(bundle)
                mContext.startActivity(intent)
            }
        }

        override fun getItemCount(): Int {
            return mDataArrayList.size
        }



        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val level: TextView = itemView.findViewById(R.id.skillLevel)
            val matchType: TextView = itemView.findViewById(R.id.match_type)
            val location: TextView = itemView.findViewById(R.id.location)
        }
    }
}