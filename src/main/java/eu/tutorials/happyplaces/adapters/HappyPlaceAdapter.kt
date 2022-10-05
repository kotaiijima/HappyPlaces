package eu.tutorials.happyplaces.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import eu.tutorials.happyplaces.R
import eu.tutorials.happyplaces.activities.AddHappyPlaceActivity
import eu.tutorials.happyplaces.activities.MainActivity
import eu.tutorials.happyplaces.detabase.DatabaseHandler
import eu.tutorials.happyplaces.models.HappyPlaceModel
import kotlinx.android.synthetic.main.activity_add_happy_place.view.*
import kotlinx.android.synthetic.main.item_happy_place.view.*

// TODO (Step 6: Creating an adapter class for binding it to the recyclerview in the new package which is adapters.)
// START
open class HappyPlacesAdapter(
	private val context: Context,
	private var list: ArrayList<HappyPlaceModel>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

	private var onClickListener: OnClickListener? = null
	/**
	 * Inflates the item views which is designed in xml layout file
	 *
	 * create a new
	 * {@link ViewHolder} and initializes some private fields to be used by RecyclerView.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

		return MyViewHolder(
			LayoutInflater.from(context).inflate(
				R.layout.item_happy_place,
				parent,
				false
			)
		)
	}

	fun setOnClickListener(onClickListener: OnClickListener){
		this.onClickListener = onClickListener
	}

	/**
	 * Binds each item in the ArrayList to a view
	 *
	 * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
	 * an item.
	 *
	 * This new ViewHolder should be constructed with a new View that can represent the items
	 * of the given type. You can either create a new View manually or inflate it from an XML
	 * layout file.
	 */
	override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
		val model = list[position]

		if (holder is MyViewHolder) {
			holder.itemView.civ_image.setImageURI(Uri.parse(model.image))
			holder.itemView.tv_title.text = model.title
			holder.itemView.tv_desc.text = model.description

			holder.itemView.setOnClickListener{
				if (onClickListener != null){
					onClickListener!!.onClick(position, model)
				}
			}
		}
	}

	interface OnClickListener {
		fun onClick(position: Int, model: HappyPlaceModel)
	}
	/**
	 * Gets the number of items in the list
	 */
	override fun getItemCount(): Int {
		return list.size
	}

	/**
	 * A ViewHolder describes an item view and metadata about its place within the RecyclerView.
	 */
	private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)

	fun notifyEditItem(activity: Activity, position: Int, requestCode: Int){
		val intent = Intent(context, AddHappyPlaceActivity::class.java)
		intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS, list[position])
		activity.startActivityForResult(intent, requestCode)
		notifyItemChanged(position)
	}

	fun removeAt(position: Int){
		val dbHandler = DatabaseHandler(context)
		val isDelete = dbHandler.deleteHappyPlace(list[position])
		if (isDelete > 0){
			list.removeAt(position)
			notifyItemRemoved(position)
		}
	}
}
// END