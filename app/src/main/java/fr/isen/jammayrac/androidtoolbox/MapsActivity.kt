package fr.isen.jammayrac.androidtoolbox

import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.Request
import java.lang.Exception
import java.lang.StringBuilder

class MapsActivity : AppCompatActivity() {

    lateinit var mapFragment: SupportMapFragment
    lateinit var googleMap: GoogleMap



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        mapFragment = supportFragmentManager.findFragmentById(R.id.fragment_map) as SupportMapFragment


        mapFragment.getMapAsync(OnMapReadyCallback {
            googleMap = it

            googleMap.isMyLocationEnabled = true

            val location1 = LatLng(43.1205625,5.9388437)
            googleMap.addMarker(MarkerOptions().position(location1).title("ISEN"))
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location1,15f)) //zoom sur le point entre 5 et 20

            val location2 = LatLng(43.1224762,5.9421886)
            googleMap.addMarker(MarkerOptions().position(location2).title("Maison"))

            val URL = getDirectionURL(location1,location2)
            GetDirection(URL).execute()
        })
    }
    private fun getUrl(origin:LatLng,dest:LatLng): String {
        var googlePlaceUrl = StringBuilder("https://maps.googleapis.com/maps/api/directions/json")
        googlePlaceUrl.append("?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}")
        googlePlaceUrl.append("&sensor=false&mode=walking&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA")

        return googlePlaceUrl.toString()
    }
    fun getDirectionURLbyNAME(): String {
        return "https://maps.googleapis.com/maps/api/directions/json?origin=Marseille&destination=Toulon&region=es&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA"
    }
    fun getDirectionURL(origin:LatLng,dest:LatLng) : String {
         //return "https://maps.googleapis.com/maps/api/directions/json?\n" + "origin=Toronto&destination=Montreal\n" + "&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA"
        return "https://maps.googleapis.com/maps/api/directions/json?origin=${origin.latitude},${origin.longitude}&destination=${dest.latitude},${dest.longitude}&sensor=false&mode=walking&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA"
    }

    inner class GetDirection(val url : String) : AsyncTask<Void, Void,List<List<LatLng>>>() {
        override fun doInBackground(vararg params: Void?): List<List<LatLng>> {
            val client = OkHttpClient()
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            val data = response.body().toString()
            val result = ArrayList<List<LatLng>>()
            try {
                val respObj = Gson().fromJson(data, GoogleMapDTO::class.java)
                val path = ArrayList<LatLng>()

                for (i in 0.. (respObj.routes[0].legs[0].steps.size-1)){
                    val startLatLng = LatLng(respObj.routes[0].legs[0].steps[i].start_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].start_location.long.toDouble())
                    path.add(startLatLng)
                    val endLatLng = LatLng(respObj.routes[0].legs[0].steps[i].end_location.lat.toDouble()
                        ,respObj.routes[0].legs[0].steps[i].end_location.long.toDouble())
                    path.add(endLatLng)

                }
                result.add(path)
            }catch (e:Exception){
                e.printStackTrace()
            }
            return result
        }

        override fun onPostExecute(result: List<List<LatLng>>) {
            val lineoption = PolylineOptions()
            for (i in result.indices){
                lineoption.addAll(result[i])
                lineoption.width(10f)
                lineoption.color(Color.BLUE)
                lineoption.geodesic(true)
            }
            googleMap.addPolyline(lineoption)
        }
    }
}

//https://maps.googleapis.com/maps/api/directions/json?origin=ADDRESS_1&destination=ADDRESS_2&waypoints=ADDRESS_X|ADDRESS_Y&key=API_KEY
//Direction API Request
// origin dest waypoints Key
//ex
//https://maps.googleapis.com/maps/api/directions/json?origin=Waterside Restaurant Whalers Inn, 121 Franklin Parade, Victor Harbor SA 5211&destination=Waterside Restaurant Whalers Inn, 121 Franklin Parade, Victor Harbor SA 5211&waypoints=8 Franklin Parade, Victor Harbor SA 5211|127 Victoria St, Victor Harbor SA 5211|136 Bay Rd, Encounter Bay SA 5211|45 Whalers Rd, Encounter Bay SA 5211&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA
