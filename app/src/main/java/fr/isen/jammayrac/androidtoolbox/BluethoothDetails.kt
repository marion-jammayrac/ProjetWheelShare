package fr.isen.jammayrac.androidtoolbox

import android.bluetooth.*
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_bluethooth_details.*
import kotlinx.android.synthetic.main.activity_home.*


class BluetoothDetails : AppCompatActivity() {

    private var bluetoothGatt: BluetoothGatt? = null
    private var TAG: String = "services"
    private lateinit var adapter: BluetoothDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_bluethooth_details)

        val device: BluetoothDevice = intent.getParcelableExtra("ble_device")
        nameDevice.text = device.name
        bluetoothGatt = device.connectGatt(this, true, gattCallback)

    }

    //apk

    private val gattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status: Int,
            newState: Int
        ) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    runOnUiThread {
                        connectionState.text = STATE_CONNECTED
                        //name.text = device?.name
                    }
                    bluetoothGatt?.discoverServices()
                    Log.i(TAG, "Connected to GATT")
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    runOnUiThread {
                        connectionState.text = STATE_DISCONNECTED
                    }
                    Log.i(TAG, "Disconnected from GATT")
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            super.onServicesDiscovered(gatt, status)
            runOnUiThread {
                detailsView.adapter = BluetoothDetailsAdapter(
                    gatt?.services?.map {
                        BleService(
                            it.uuid.toString(),
                            it.characteristics
                        )
                    }?.toMutableList() ?: arrayListOf()
                    , this@BluetoothDetails, gatt)
                detailsView.layoutManager = LinearLayoutManager(this@BluetoothDetails)
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
                detailsView.adapter?.notifyDataSetChanged()


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
                detailsView.adapter?.notifyDataSetChanged()
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
                detailsView.adapter?.notifyDataSetChanged()
                Toast.makeText(applicationContext,characteristic.value.toString(), Toast.LENGTH_LONG).show()
                val intent = Intent(applicationContext, MapsActivity3::class.java)
                intent.putExtra("Ble", characteristic.value.toString())
            }
        }
    }

    private fun byteArrayToHexString(array: ByteArray): String {
        val result = StringBuilder(array.size * 2)
        for ( byte in array ) {
            val toAppend = String.format("%X", byte) // hexadecimal
            result.append(toAppend).append("-")
        }
        result.setLength(result.length - 1) // remove last '-'
        return result.toString()
    }

    override fun onStop() {
        super.onStop()
    }

    companion object {
        private const val STATE_DISCONNECTED = "Statut : Déconnecté"
        private const val STATE_CONNECTED = "Statut : Connecté"
    }


}