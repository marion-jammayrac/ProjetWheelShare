package fr.isen.jammayrac.androidtoolbox

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beust.klaxon.*
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import fr.isen.jammayrac.androidtoolbox.model.RootObject
import kotlinx.android.synthetic.main.activity_bluethooth.*
import kotlinx.android.synthetic.main.activity_maps2.botton_navigation_view
import kotlinx.android.synthetic.main.activity_maps3.*
import org.jetbrains.anko.async
import org.jetbrains.anko.uiThread
import java.io.IOException
import java.net.URL


class MapsActivity3() : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var handler: Handler
    private var mScanning: Boolean = false
    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG: String = "services"

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isBLEEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true


    private lateinit var mMap: GoogleMap

    private var latitude:Double=0.toDouble()
    private var longitude:Double=0.toDouble()

    private lateinit var mLastLocation: Location
    private var mMarker: Marker?=null

    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    lateinit var locationRequest: LocationRequest
    lateinit var locationCallback: LocationCallback

    lateinit var mService: IGoogleAPIServices
    internal lateinit var currentPlace: RootObject

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps3)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        mService = Common.googleApiService

        if (checkLocationPermission()) {
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        }
        else{
            buildLocationRequest()
            buildLocationCallback()

            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

        }


        botton_navigation_view.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_pin -> {
                    val Ble1 = intent.getStringExtra("Ble")
                    Toast.makeText(this,"CAREFUL", Toast.LENGTH_LONG).show()
                    val markerOptions = MarkerOptions().position(LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("DANGER1")
                        .icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.warning_logo12
                        ))
                    mMarker = mMap.addMarker(markerOptions)
                }
                R.id.action_pin2 -> {
                    //Toast.makeText(this,"DANGER", Toast.LENGTH_LONG).show()
                    Toast.makeText(this,"DANGER", Toast.LENGTH_LONG).show()
                    val markerOptions = MarkerOptions().position(LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("DANGER2")
                        .icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.warning_logo2
                        ))
                    mMarker = mMap.addMarker(markerOptions)
                }
                R.id.action_pin3 -> {
                    Toast.makeText(this,"STOP", Toast.LENGTH_LONG).show()
                    val markerOptions = MarkerOptions().position(LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("DANGER3")
                        .icon(BitmapDescriptorFactory.fromResource(
                            R.drawable.stop_logo
                        ))
                    mMarker = mMap.addMarker(markerOptions)
                }
            }
            true
        }

        test_button2.setOnClickListener {
            when {
                isBLEEnabled -> {
                    //init scan
                    initBLEScan()
                    initScan()
                }
                bluetoothAdapter != null -> {
                    //ask for permission
                    val enableBTIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBTIntent, BluetoothActivity.REQUEST_ENABLE_BT)
                }
                else -> {
                    //device is not compatible with your device
                    bleTextFailed.visibility = View.VISIBLE
                }
            }
        }

        }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // declare bounds object to fit whole route in screen
        val LatLongB = LatLngBounds.Builder()
        // Add markers
        var intent = intent
        val sydney2 = intent.getStringExtra("From")
        val opera2 = intent.getStringExtra("To")
        val sydney = getLocationFromAddress(this,sydney2)
        val opera =getLocationFromAddress(this,opera2)

        mMap!!.addMarker(MarkerOptions().position(sydney!!).title("Maison"))
        mMap!!.addMarker(MarkerOptions().position(opera!!).title("ISEN"))

        // Declare polyline object and set up color and width
        val options = PolylineOptions()
        options.color(Color.RED)
        options.width(5f)

        // build URL to call API
        val url = getURL(sydney, opera)


        async {
            // Connect to URL, download content and convert into string asynchronously
            val result = URL(url).readText()
            uiThread {
                // When API call is done, create parser and convert into JsonObjec
                val parser: Parser = Parser()
                val stringBuilder: StringBuilder = StringBuilder(result)
                val json: JsonObject = parser.parse(stringBuilder) as JsonObject
                // get to the correct element in JsonObject
                val routes = json.array<JsonObject>("routes")
                val points = routes!!["legs"]["steps"][0] as JsonArray<JsonObject>
                // For every element in the JsonArray, decode the polyline string and pass all points to a List
                val polypts = points.flatMap { decodePoly(it.obj("polyline")?.string("points")!!)  }
                // Add  points to polyline and bounds
                options.add(sydney)
                LatLongB.include(sydney)
                for (point in polypts)  {
                    options.add(point)
                    LatLongB.include(point)
                }
                options.add(opera)
                LatLongB.include(opera)
                // build bounds
                val bounds = LatLongB.build()
                // add polyline to the map
                mMap!!.addPolyline(options)
                // show map with route centered
                mMap!!.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100))
            }

        }

    }


    fun getLocationFromAddress(context: MapsActivity3, strAddress: String?): LatLng? {
        val coder = Geocoder(context)
        val address: List<Address>?
        var p1: LatLng? = null
        try { // May throw an IOException
            address = coder.getFromLocationName(strAddress, 5)
            if (address == null) {
                return null
            }
            val location: Address = address[0]
            p1 = LatLng(location.getLatitude(), location.getLongitude())
        } catch (ex: IOException) {
            ex.printStackTrace()
        }
        return p1
    }

    private fun getURL(from : LatLng, to : LatLng) : String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val params = "$origin&$dest&$sensor&mode=walking&key=AIzaSyDA-1Uecj_W7IBiNkcyNLp87AzajQSb_YA"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    /**
     * Method to decode polyline points
     * Courtesy : https://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     */
    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }

    private fun buildLocationCallback() {
        locationCallback = object : LocationCallback(){
            override fun onLocationResult(pO: LocationResult?) {
                mLastLocation = pO!!.lastLocation

                if(mMarker != null){
                    mMarker!!.remove()
                }

                latitude = mLastLocation.latitude
                longitude = mLastLocation.longitude

                val latLng = LatLng(latitude,longitude)
                val markerOptions = MarkerOptions().position(latLng).title("Your position").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                //mMarker = mMap.addMarker(markerOptions)

                //Move camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng))
                mMap.animateCamera(CameraUpdateFactory.zoomTo(15f))
            }
        }
    }

    private fun buildLocationRequest() {
        locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 5000
        locationRequest.fastestInterval = 3000
        locationRequest.smallestDisplacement = 15f
    }

    private fun checkLocationPermission(): Boolean =
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 1000)
            }else{
                ActivityCompat.requestPermissions(this, arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 1000)
            }
            false
        } else{
            true
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            1000 -> {
                if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                        if(checkLocationPermission()){
                            buildLocationRequest()
                            buildLocationCallback()

                            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
                            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
                            mMap.isMyLocationEnabled = true
                        }
                    }
                }else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun initScan() {
        handler = Handler()
        scanLeDevice(true)
    }

    private fun scanLeDevice(enable: Boolean) {
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            if (enable) {
                Log.w("BLE", "Scanning for devices")
                handler.postDelayed({
                    mScanning = false
                    stopScan(leScanCallback)
                }, SCAN_PERIOD)
                mScanning = true
                startScan(leScanCallback)
            } else {
                mScanning = false
                stopScan(leScanCallback)
            }
        }
    }

    private val leScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            Log.w("BLE", "${result.device}")
            if (result.device.address == "E9:75:0F:B7:7D:B3") {
                bluetoothGatt = result.device.connectGatt(applicationContext, true, gattCallback)
                scanLeDevice(false)
            }
        }
    }

    private fun initBLEScan() {
        handler = Handler()

        scanLeDevice(true)
    }

    private fun onDeviceClicked(device: BluetoothDevice) {
        val intent = Intent(this, BluetoothDetails::class.java)
        intent.putExtra("ble_device", device)
        Toast.makeText(this, device.address, Toast.LENGTH_SHORT).show()
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                if (isBLEEnabled) {
                    Toast.makeText(this, "Bluetooth has been enabled", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Bluetooth has been disabled", Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth enabling has been canceled", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
                        //connectionState.text = STATE_CONNECTED
                        //name.text = device?.name
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i("b", "Connected to GATT")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        //connectionState.text = STATE_DISCONNECTED
                    }
                    Log.i("a", "Disconnected from GATT")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)

            if (gatt != null) {
                setCharacteristicNotificationInternal(
                    gatt,
                    gatt.services[2].characteristics[1],
                    true
                )
                Log.e(
                    "TAG",
                    "Services : actif"
                )
            }
            runOnUiThread {

            }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val value = characteristic.getStringValue(0)
            Log.e(
                "TAG",
                "onCharacteristicRead: " + value + " UUID " + characteristic.uuid.toString()
            )
            runOnUiThread {

            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            val value = characteristic.value
            Log.e(
                "TAG",
                "onCharacteristicWrite: " + value + " UUID " + characteristic.uuid.toString()
            )
            runOnUiThread {

            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic
        ) {
            val value = byteArrayToHexString(characteristic.value)
            Log.e(
                "TAG",
                "onCharacteristicChanged: " + value + " UUID " + characteristic.uuid.toString()
            )
            runOnUiThread {
                if (gatt != null) {
                    //Toast.makeText(applicationContext,byteArrayToHexString(gatt.services[2].characteristics[1].value) ,Toast.LENGTH_SHORT).show()

                    if(byteArrayToHexString(gatt.services[2].characteristics[1].value)=="54"){
                        Toast.makeText(applicationContext,"BLE CONNECTED/ CALIBRATION" ,Toast.LENGTH_SHORT).show()

                    }

                    if(byteArrayToHexString(gatt.services[2].characteristics[1].value)=="01"){
                        val markerOptions = MarkerOptions().position(LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("DANGER1")
                            .icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.warning_logo12
                            ))
                        Toast.makeText(applicationContext,"YOU HAVE CROSS A DANGER LEVEL 1" ,Toast.LENGTH_SHORT).show()
                        mMarker = mMap.addMarker(markerOptions)
                    }

                    if(byteArrayToHexString(gatt.services[2].characteristics[1].value)=="FF"){
                        val markerOptions = MarkerOptions().position(LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude())).title("DANGER2")
                            .icon(BitmapDescriptorFactory.fromResource(
                                R.drawable.warning_logo2
                            ))
                        Toast.makeText(applicationContext,"YOU HAVE CROSS A DANGER LEVEL 2" ,Toast.LENGTH_SHORT).show()
                        mMarker = mMap.addMarker(markerOptions)
                    }

                    //val BLE3 = byteArrayToHexString(gatt.services[2].characteristics[1].value);
                    // + " / y : " + byteArrayToHexString(gatt.services[2].characteristics[2].value) +
                    //" / z : " + byteArrayToHexString(gatt.services[2].characteristics[3].value),
                }
            }
        }
    }

    private fun byteArrayToHexString(array: ByteArray): String {
        val result = StringBuilder(array.size * 2)
        for (byte in array) {
            val toAppend = String.format("%X", byte) // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'
        return result.toString()
    }

    private fun setCharacteristicNotificationInternal(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        enabled: Boolean
    ) {
        gatt.setCharacteristicNotification(characteristic, enabled)

        if (characteristic.descriptors.size > 0) {

            val descriptors = characteristic.descriptors
            for (descriptor in descriptors) {

                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                    descriptor.value =
                        if (enabled) BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                } else if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_INDICATE != 0) {
                    descriptor.value =
                        if (enabled) BluetoothGattDescriptor.ENABLE_INDICATION_VALUE else BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE
                }
                gatt.writeDescriptor(descriptor)
            }
        }
    }

    override fun onStop() {
        super.onStop()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        scanLeDevice(false)
        bluetoothGatt?.close()
    }

    companion object {
        private const val SCAN_PERIOD: Long = 60000
        private const val REQUEST_ENABLE_BT = 44
        private const val STATE_DISCONNECTED = "Statut : Déconnecté"
    }



}
