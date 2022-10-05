package eu.tutorials.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.happyplaces.R
import eu.tutorials.happyplaces.adapters.HappyPlacesAdapter
import eu.tutorials.happyplaces.databinding.ActivityMainBinding
import eu.tutorials.happyplaces.detabase.DatabaseHandler
import eu.tutorials.happyplaces.models.HappyPlaceModel
import eu.tutorials.happyplaces.utils.SwipeToDeleteCallback
import eu.tutorials.happyplaces.utils.SwipeToEditCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
	companion object{
		var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1
		val EXTRA_PLACE_DETAILS = "extra_place_details"
	}

	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)
		getHappyPlacesListFromLocalDB()

		binding.fabAddHappyPlace.setOnClickListener {
			val intent = Intent(this, AddHappyPlaceActivity::class.java)
			startActivityForResult(intent, ADD_PLACE_ACTIVITY_REQUEST_CODE)
		}

	}
	private fun setUpHappyPlacesRecycleView(happyPlacesList : ArrayList<HappyPlaceModel>){
		rv_happy_places_list.layoutManager = LinearLayoutManager(this)
		rv_happy_places_list.setHasFixedSize(true)

		val placesAdapter = HappyPlacesAdapter(this, happyPlacesList)
		rv_happy_places_list.adapter = placesAdapter

		placesAdapter.setOnClickListener(object : HappyPlacesAdapter.OnClickListener{
			override fun onClick(position: Int, model: HappyPlaceModel) {
				val intent = Intent(this@MainActivity, HappyPlaceDetailActivity::class.java)
				intent.putExtra(EXTRA_PLACE_DETAILS, model)
				startActivity(intent)
			}
		})

		val editSwipeHandler = object : SwipeToEditCallback(this){
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
				adapter.notifyEditItem(this@MainActivity, viewHolder.adapterPosition, ADD_PLACE_ACTIVITY_REQUEST_CODE)
			}
		}

		val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
		editItemTouchHelper.attachToRecyclerView(rv_happy_places_list)

		val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
			override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
				val adapter = rv_happy_places_list.adapter as HappyPlacesAdapter
				adapter.removeAt(viewHolder.adapterPosition)

				getHappyPlacesListFromLocalDB()
			}
		}

		val deleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
		deleteItemTouchHelper.attachToRecyclerView(rv_happy_places_list)
	}

	private fun getHappyPlacesListFromLocalDB(){
		val dbHandler = DatabaseHandler(this)
		val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

		if (getHappyPlaceList.size > 0){
				rv_happy_places_list.visibility = View.VISIBLE
				tv_no_set_of_happy_place.visibility = View.GONE
				setUpHappyPlacesRecycleView(getHappyPlaceList)
		}else {
			rv_happy_places_list.visibility = View.GONE
			tv_no_set_of_happy_place.visibility = View.VISIBLE
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
			if (resultCode == Activity.RESULT_OK){
				getHappyPlacesListFromLocalDB()
			}else {
				Log.e("Activity: ", "error.")
			}
		}
	}

}