package fr.isen.jammayrac.androidtoolbox

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import kotlinx.android.synthetic.main.activity_bluethooth.*
import kotlinx.android.synthetic.main.activity_home.*


class HomeActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val SharedPreferences = getSharedPreferences("ID_ID", Context.MODE_PRIVATE)

        setContentView(R.layout.activity_home)

        Decobutton.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java) //f
            startActivity(intent)
            val editor = SharedPreferences.edit()
            editor.clear()
            editor.apply()
        }


        MapsButton.setOnClickListener() {
            val intent = Intent(this, MapsActivity2::class.java) //f
            startActivity(intent)
        }

        reglages_buton.setOnClickListener() {
            val intent = Intent(this, ReglagesActivity::class.java) //f
            startActivity(intent)
        }

        searchingButton.setOnClickListener {
            val intent = Intent(this, MapsActivity3::class.java) //f
            intent.putExtra("From",fromText.text.toString())
            intent.putExtra("To",ToText.text.toString())
            startActivity(intent)

        }

    }

}
// on create on start on resume