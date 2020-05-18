package fr.isen.jammayrac.androidtoolbox

import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import dmax.dialog.SpotsDialog
import fr.isen.jammayrac.androidtoolbox.Helper.DirectionJSONParser
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Response
import kotlin.text.StringBuilder
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import retrofit2.Callback

class ViewDirections : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var mLastLocation: Location
    lateinit var mCurrentMarker: Marker
    //var polyLine: Polyline? = null
    lateinit var polyLine: Polyline

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    lateinit var mService: IGoogleAPIServices


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_directions)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mService = Common.googleApiServiceScalars


        if (checkLocationPermission()) {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(pO: LocationResult?) {
                mLastLocation = pO!!.lastLocation
                //val latLng = LatLng(latitude,longitude)

                val markerOptions = MarkerOptions()
                    .position(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                    .title("Your position")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                mCurrentMarker = mMap!!.addMarker(markerOptions)

                //Move camera
                mMap!!.moveCamera(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            mLastLocation.latitude,
                            mLastLocation.longitude
                        )
                    )
                )
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12.05f))

                //create markers dest
                val destinationLatLng = LatLng(
                    Common.currentResult!!.geometry!!.location!!.lat.toDouble(),
                    Common.currentResult!!.geometry!!.location!!.lng.toDouble()
                )

                mMap!!.addMarker(
                    MarkerOptions().position(destinationLatLng)
                        .title(Common.currentResult!!.name)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                )

                drawPath(mLastLocation, Common.currentResult!!.geometry!!.location!!)

            }
        }
    }

    private fun drawPath(
        mLastLocation: Location?,
        location: fr.isen.jammayrac.androidtoolbox.model.Location
    ) {
        if (polyLine != null)
            polyLine!!.remove()

        val origin = StringBuilder()

        origin.append(mLastLocation?.latitude.toString())
            .append(",")
            .append(mLastLocation?.longitude.toString())

        val destination = StringBuilder()

        destination.append(location?.lat.toString())
            .append(",")
            .append(location?.lng.toString())

        val test = object : Callback<String> {

            override fun onFailure(call: Call<String>?, t: Throwable?) {
                Log.d("EDMTDEV", t?.message)
            }

            override fun onResponse(
                call: Call<String>?,
                response: Response<String>?
            ) {
                ParserTask().execute(response?.body()!!.toString())
            }
        }

       // mService.getDirections(origin.toString(), destination.toString()).enqueue(test)

    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 15f
    }

    private fun checkLocationPermission(): Boolean =
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1000
                )
            } else {
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ), 1000
                )
            }
            false
        } else {
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            1000 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (checkLocationPermission()) {
                            buildLocationRequest()
                            buildLocationCallback()

                            fusedLocationProviderClient =
                                LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(
                                locationRequest,
                                locationCallback,
                                Looper.myLooper()
                            )
                            mMap.isMyLocationEnabled = true
                        }
                    }
                } else {
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onStop() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        super.onStop()
    }


    inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        internal val waitingDialog: SpotsDialog = SpotsDialog(this@ViewDirections)

        override fun onPreExecute() {
            super.onPreExecute()
            waitingDialog.show()
            waitingDialog.setMessage("Please waiting...")
        }

        override fun doInBackground(vararg params: String?): List<List<HashMap<String, String>>>? {
            val jsonObject: JSONObject
            var routes: List<List<HashMap<String, String>>>? = null
            try {
                jsonObject = JSONObject(params[0])
                val parser = DirectionJSONParser()
                routes = parser.parse(jsonObject)
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return routes
        }

        override fun onPostExecute(result: List<List<HashMap<String, String>>>?) {
            super.onPostExecute(result)
            var points: ArrayList<LatLng>? = null
            var polylineOptions: PolylineOptions? = null

            for (i in result!!.indices) {
                points = ArrayList()
                polylineOptions = PolylineOptions()

                val path = result[i]

                for (j: Int in path.indices) {
                    val point = path[j]
                    val lat = point["lat"]!!.toDouble()
                    val lgn = point["lgn"]!!.toDouble()
                    val position = LatLng(lat, lgn)

                    points.add(position)
                }
                polylineOptions.addAll(points)
                polylineOptions.width(12f)
                polylineOptions.color(Color.RED)
                polylineOptions.geodesic(true)
            }
            polyLine = mMap!!.addPolyline(polylineOptions)
            waitingDialog.dismiss()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        mMap.uiSettings.isZoomControlsEnabled = true

        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->

            mLastLocation = location

            val markerOptions = MarkerOptions()
                .position(LatLng(mLastLocation.latitude, mLastLocation.longitude))
                .title("Your position")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))

            mCurrentMarker = mMap!!.addMarker(markerOptions)

            //Move camera
            mMap!!.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        mLastLocation.latitude,
                        mLastLocation.longitude
                    )
                )
            )
            mMap!!.moveCamera(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        mLastLocation.latitude,
                        mLastLocation.longitude
                    )
                )
            )
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12.05f))

            //create markers dest
            val destinationLatLng = LatLng(
                Common.currentResult!!.geometry!!.location!!.lat.toDouble(),
                Common.currentResult!!.geometry!!.location!!.lng.toDouble()
            )

            mMap!!.addMarker(
                MarkerOptions().position(destinationLatLng)
                    .title(Common.currentResult!!.name)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
            )

            drawPath(mLastLocation, Common.currentResult!!.geometry!!.location!!)
        }

        mService = Common.googleApiServiceScalars

        if (checkLocationPermission()) {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        } else {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }

    }

}

