package eu.tutorials.happyplaces.activities

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import eu.tutorials.happyplaces.R
import eu.tutorials.happyplaces.databinding.ActivityAddHappyPlaceBinding
import eu.tutorials.happyplaces.detabase.DatabaseHandler
import eu.tutorials.happyplaces.models.HappyPlaceModel
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
	private var calendar  = Calendar.getInstance()
	private  lateinit var dateSetListener : DatePickerDialog.OnDateSetListener
	private lateinit var binding: ActivityAddHappyPlaceBinding

	private var saveImageToInternalStorage : Uri? = null
	private var mLaditude : Double = 0.0
	private var mLongitude : Double = 0.0

	private lateinit var galleryImageResultLauncher: ActivityResultLauncher<Intent>
	private lateinit var cameraImageResultLauncher: ActivityResultLauncher<Intent>


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
		binding.tvAddImage.setOnClickListener(this)
		binding.btnSave.setOnClickListener(this)
		//日付をセットしたらonDateSetListenerが作動
		updateDateInView()
		dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
			calendar.set(Calendar.YEAR, year)
			calendar.set(Calendar.MONTH, month)
			calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
			updateDateInView()
		}

		registerOnActivityForGalleryResult()
		registerOnActivityForCameraResult()
	}

	override fun onClick(v: View?) {
		when(v!!.id){
			R.id.et_date ->{
				DatePickerDialog(
					this@AddHappyPlaceActivity,
					dateSetListener,
					calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH),
					calendar.get(Calendar.DAY_OF_MONTH)
				).show()
			}

			R.id.tv_add_image ->{
				val pictureDialog = AlertDialog.Builder(this)
				pictureDialog.setTitle("Select Action")
				val pictureDialogItems = arrayOf("ギャラリーから写真を選ぶ", "カメラで撮影する")
				pictureDialog.setItems(pictureDialogItems){
					_, which ->
					when(which){
						0 -> choosePhotoFromGallery()
						1 -> takePhotoFromCamera()
					}
				}
				pictureDialog.show()
			}

			R.id.btn_save ->{
				when{
					binding.etTitle.text.isNullOrEmpty() ->{
						Toast.makeText(this@AddHappyPlaceActivity, "タイトルを入力してください", Toast.LENGTH_SHORT).show()
					}
					binding.etDescription.text.isNullOrEmpty() ->{
						Toast.makeText(this@AddHappyPlaceActivity, "説明文を入力してください", Toast.LENGTH_SHORT).show()
					}
					binding.etLocation.text.isNullOrEmpty() ->{
						Toast.makeText(this@AddHappyPlaceActivity, "場所を入力してください", Toast.LENGTH_SHORT).show()
					}
					saveImageToInternalStorage == null ->{
						Toast.makeText(this@AddHappyPlaceActivity, "画像を選択してください", Toast.LENGTH_SHORT).show()
					}else ->{
						val happyPlaceModel = HappyPlaceModel(
							0,
							binding.etTitle.text.toString(),
							saveImageToInternalStorage.toString(),
							binding.etDescription.toString(),
							binding.etDate.text.toString(),
							binding.etLocation.text.toString(),
							mLaditude,
							mLongitude
						)
						val dbHandler = DatabaseHandler(this)
						val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
						if (addHappyPlace > 0){
							Toast.makeText(this, "場所が追加されました。", Toast.LENGTH_SHORT).show()
						finish()
						}
					}
				}
			}
		}
	}

	private fun registerOnActivityForGalleryResult() {
		galleryImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
					var data: Intent? = result.data
					if (data != null) {
						val contentURI = data.data
						try {
							Log.e("External image : " , "$contentURI")
							val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, contentURI)
							saveImageToInternalStorage = saveImageToInternalStorage(selectedImageBitmap)
							Log.e("Saved image : " , "$saveImageToInternalStorage")
							binding.ivPlaceImage.setImageURI(saveImageToInternalStorage)

						} catch (e: IOException) {
							e.printStackTrace()
							Toast.makeText(
								this@AddHappyPlaceActivity,
								"Failed to load!",
								Toast.LENGTH_SHORT
							).show()
						}
					}
				}
			}
		}

	private fun registerOnActivityForCameraResult() {
		cameraImageResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
			if (result.resultCode == Activity.RESULT_OK) {
				var data: Intent? = result.data
				if (data != null) {
					try {
						val sumbNail: Bitmap = data.extras?.get("data") as Bitmap
						saveImageToInternalStorage =  saveImageToInternalStorage(sumbNail)

						Log.e("Saved image : " , "$saveImageToInternalStorage")
						binding.ivPlaceImage.setImageBitmap(sumbNail)
					} catch (e: IOException) {
						e.printStackTrace()
						Toast.makeText(
							this@AddHappyPlaceActivity,
							"Failed to load!",
							Toast.LENGTH_SHORT
						).show()
					}
				}
			}
		}
	}

	private fun takePhotoFromCamera(){
		Dexter.withContext(this).withPermissions(
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
			android.Manifest.permission.CAMERA
		).withListener(object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport?)
			{if(report!!.areAllPermissionsGranted()){
				val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
				cameraImageResultLauncher.launch(cameraIntent)
			}
			}
			override fun onPermissionRationaleShouldBeShown(permission: MutableList<PermissionRequest> , token: PermissionToken) {
				showRationalDialogForPermissions()
			}
		}).onSameThread().check()
	}

	private fun choosePhotoFromGallery(){
		Dexter.withContext(this).withPermissions(
			android.Manifest.permission.READ_EXTERNAL_STORAGE,
			android.Manifest.permission.WRITE_EXTERNAL_STORAGE
		).withListener(object : MultiplePermissionsListener {
			override fun onPermissionsChecked(report: MultiplePermissionsReport?)
			{if(report!!.areAllPermissionsGranted()){
				val galleryIntent = Intent(Intent.ACTION_PICK,
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
				galleryImageResultLauncher.launch(galleryIntent)
			}
			}
			override fun onPermissionRationaleShouldBeShown(permission: MutableList<PermissionRequest> , token: PermissionToken) {
				showRationalDialogForPermissions()
			}
		}).onSameThread().check()
	}

	private fun showRationalDialogForPermissions(){
		AlertDialog.Builder(this).setMessage("きょかがおりませんでした！")
			.setPositiveButton("設定を行う"){
				_,_ ->
					try {
						val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						val uri = Uri.fromParts("package", packageName,null)
						intent.data = uri
						startActivity(intent)
					} catch (e : ActivityNotFoundException){
						e.printStackTrace()
					}
			}.setNegativeButton("Cancel"){
				dialog,_ ->
					dialog.dismiss()
			}.show()
	}

	private fun updateDateInView(){
		val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
		binding.etDate.setText(sdf.format(calendar.time).toString())
	}

	private fun saveImageToInternalStorage(bitmap : Bitmap):Uri{
		val wrapper = ContextWrapper(applicationContext)
		var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
		file = File(file, "${UUID.randomUUID()}.jpg")

		try {
			//writing data at file
			val stream : OutputStream = FileOutputStream(file)
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
			stream.flush()
			stream.close()
		}catch(e : IOException){
			e.printStackTrace()
		}
		return Uri.parse(file.absolutePath)
	}

	companion object {
		private const val IMAGE_DIRECTORY = "SaveOfImages"
	}
}