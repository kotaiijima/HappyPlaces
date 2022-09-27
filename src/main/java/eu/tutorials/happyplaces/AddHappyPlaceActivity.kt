package eu.tutorials.happyplaces

import android.app.DatePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import eu.tutorials.happyplaces.databinding.ActivityAddHappyPlaceBinding
import eu.tutorials.happyplaces.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
	private var calendar  = Calendar.getInstance()
	private  lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
	private lateinit var binding: ActivityAddHappyPlaceBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_add_happy_place)

		binding = ActivityAddHappyPlaceBinding.inflate(layoutInflater)
		val view = binding.root
		setContentView(view)
		setSupportActionBar(binding.toolbarAddPlace)
//		先に進んだときに戻るボタンを出す
		supportActionBar?.setDisplayHomeAsUpEnabled(true)
		binding.toolbarAddPlace.setNavigationOnClickListener{
			onBackPressed()
		}

		binding.etDate.setOnClickListener(this)
		//日付をセットしたらonDateSetListenerが作動
		dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
			calendar.set(Calendar.YEAR, year)
			calendar.set(Calendar.MONTH, month)
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
			updateDateInView()
		}
	}

	override fun onClick(v: View?) {
		when(v!!.id){
			R.id.et_date ->{
				DatePickerDialog(
					this@AddHappyPlaceActivity,
					dateSetListener,
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH)).show()
			}
		}
	}

	private fun updateDateInView(){
		val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
		binding.etDate.setText(sdf.format(calendar.time).toString())
	}
}