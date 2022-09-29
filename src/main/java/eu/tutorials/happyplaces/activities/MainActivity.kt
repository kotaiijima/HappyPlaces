package eu.tutorials.happyplaces.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import eu.tutorials.happyplaces.R
import eu.tutorials.happyplaces.databinding.ActivityMainBinding
import eu.tutorials.happyplaces.detabase.DatabaseHandler
import eu.tutorials.happyplaces.models.HappyPlaceModel

class MainActivity : AppCompatActivity() {
	companion object{
		private const val CAMERA_PERISSION_CODE = 1
		private const val CAMERA = 2
	}
	private lateinit var binding: ActivityMainBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		binding = ActivityMainBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)
		getHappyPlacesListFromLovalDB()

		binding.fabAddHappyPlace.setOnClickListener {
			val intent = Intent(this, AddHappyPlaceActivity::class.java)
			startActivity(intent)
		}

	}

	private fun getHappyPlacesListFromLovalDB(){
		val dbHandler = DatabaseHandler(this)
		val getHappyPlaceList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

		if (getHappyPlaceList.size > 0){
			for(i in getHappyPlaceList){
				Log.e("S", i.title)
			}
		}
	}
}