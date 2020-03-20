package fr.isen.jammayrac.androidtoolbox

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_home.*

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val SharedPreferences = getSharedPreferences("ID_ID", Context.MODE_PRIVATE)

        setContentView(R.layout.activity_home)
        CycleDeVieButton.setOnClickListener() {
            val intent = Intent(this, CycleDeVieActivity::class.java) //f
            startActivity(intent)
        }
        Decobutton.setOnClickListener() {
            val intent = Intent(this, LoginActivity::class.java) //f
            startActivity(intent)
            val editor = SharedPreferences.edit()
            editor.clear()
            editor.apply()
        }
        SauvegardeButton.setOnClickListener() {
            val intent = Intent(this, SauvegardeActivity::class.java) //f
            startActivity(intent)
        }
        WebServicebutton.setOnClickListener() {
            val intent = Intent(this, WebServiceActivity::class.java) //f
            startActivity(intent)
        }

        Blebutton.setOnClickListener() {
            val intent = Intent(this, BleActivity::class.java) //f
            startActivity(intent)
        }

        PermissionButton.setOnClickListener() {
            val intent = Intent(this, PermissionActivity::class.java) //f
            startActivity(intent)
        }
    }
}
// on create on start on resume