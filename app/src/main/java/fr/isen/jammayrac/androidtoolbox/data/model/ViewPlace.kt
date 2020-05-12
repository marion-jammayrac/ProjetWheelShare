package fr.isen.jammayrac.androidtoolbox.data.model

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import fr.isen.jammayrac.androidtoolbox.Common
import fr.isen.jammayrac.androidtoolbox.IGoogleAPIServices
import fr.isen.jammayrac.androidtoolbox.R
import fr.isen.jammayrac.androidtoolbox.model.PlaceDetail
import kotlinx.android.synthetic.main.activity_view_place.*
import retrofit2.Call
import retrofit2.Response

class ViewPlace : AppCompatActivity() {

    internal lateinit var mService: IGoogleAPIServices
    var mPlace : PlaceDetail?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_place)

        mService = Common.googleApiService

        place_name.text = ""
        place_address.text = ""
        place_hours.text = ""


        btn_show_on_map.setOnClickListener {
            val mapIntent = Intent(Intent.ACTION_VIEW, Uri.parse(mPlace!!.result!!.url))
            startActivity(mapIntent)
        }


        if(Common.currentResult!!.photos != null && Common.currentResult!!.photos!!.isNotEmpty()){
            Picasso.get()
                .load(getPhotoOfPlace(Common.currentResult!!.photos!![0].photo_reference!!, 1000))
                .into(photo)
        }

        if(Common.currentResult!!.rating != null){
            rating_bar.rating = Common.currentResult!!.rating.toFloat()
        }else{
            rating_bar.visibility= View.GONE
        }

        if(Common.currentResult!!.opening_hours != null){
            place_hours.text = "Open now : " + Common.currentResult!!.opening_hours!!.open_now
        }else{
            place_hours.visibility = View.GONE
        }

        mService.getDetailPlaces(getDetailPlaceUrl(Common.currentResult!!.place_id!!))
            .enqueue(object: retrofit2.Callback<PlaceDetail>{
                override fun onFailure(call: Call<PlaceDetail>, t: Throwable) {
                    Toast.makeText(baseContext,"" + t.message, Toast.LENGTH_SHORT).show()
                }

                override fun onResponse(call: Call<PlaceDetail>, response: Response<PlaceDetail>) {
                    mPlace = response.body()
                    place_address.text = mPlace!!.result!!.formatted_address
                    place_name.text = mPlace!!.result!!.name
                }
            })
    }

    private fun getDetailPlaceUrl(placeId: String): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/details/json")
        url.append("?place_id=$placeId")
        url.append("&key=AIzaSyAOgLYcZxKFUyjrFvv58zNg6_AViWAFwpc")
        return url.toString()
    }

    private fun getPhotoOfPlace(photo_reference: String, maxWidth: Int): String {
        val url = StringBuilder("https://maps.googleapis.com/maps/api/place/photo")
        url.append("?maxwidth=$maxWidth")
        url.append("&photoreference=$photo_reference")
        url.append("&key=AIzaSyAOgLYcZxKFUyjrFvv58zNg6_AViWAFwpc")
        return url.toString()
    }
}
