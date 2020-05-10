package fr.isen.jammayrac.androidtoolbox

import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_ble.*
import kotlinx.android.synthetic.main.activity_home.*

class BleActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private val deviceList = mutableListOf<Device>()

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val isBLEEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    // private val REQUEST_ENABLE_BT = 1
    private var mScanning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ble)
        PlayButton.setOnClickListener {
            when {
                isBLEEnabled ->
                    initScan()
                // bleTextFailed.visibility = View.VISIBLE
                bluetoothAdapter != null -> {
                    // demande d'activation bluetooth
                    val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
                    bleTextFailed.visibility = View.GONE
                }
                else -> {
                    // device pas compatible BLE
                    bleTextFailed.visibility = View.VISIBLE
                }
            }
            recyclerViewBle.adapter = BleActivity2(deviceList)
            recyclerViewBle.layoutManager = LinearLayoutManager(this)

        }
    }

    private fun initScan() {
        progressBar.visibility = View.VISIBLE
        dividerBle.visibility = View.GONE
        handler = Handler()
        scanLeDevice(true)
    }

    private fun scanLeDevice(enable: Boolean) {
        bluetoothAdapter?.bluetoothLeScanner?.apply {
            if (enable) {
                Log.w("BleActivity", "Scanning for devices")
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
            Log.w("BleActivity", "${result.device}")
            deviceList += Device(result.device.name, result.device.address, result.rssi)
            runOnUiThread {
                dividerBle.visibility = View.GONE
            }
        }
    }

    private fun addDeviceToList(result: ScanResult) {
        for (i in 0 until deviceList.size) {
            if (result.device.address == deviceList[i].address) {
            } else {
                deviceList += Device(result.device.name, result.device.address, result.rssi)
            }
        }

    }

    companion object {
        private const val REQUEST_ENABLE_BT = 43
        private val SCAN_PERIOD: Long = 8000
    }

    override fun onStop() {
        super.onStop()
        if (isBLEEnabled) {
            scanLeDevice(false)
        }
    }

    data class Device(
        val name: String?,
        val address: String,
        val rssi: Int
    )
}
