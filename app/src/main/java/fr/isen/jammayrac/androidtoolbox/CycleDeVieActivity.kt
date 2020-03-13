package fr.isen.jammayrac.androidtoolbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_cycle_de_vie.*
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.activity_login.*

class CycleDeVieActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cycle_de_vie)
        TextOnCreate.text =  TextOnCreate.text.toString() + "\n OnCreate"
    }

    override fun onStart() {
        super.onStart()
        TextOnCreate.text =   TextOnCreate.text.toString() + "\n OnStart "
    }

    override fun onResume() {
        super.onResume()
        TextOnCreate.text =   TextOnCreate.text.toString() + "\n OnResume "
        Toast.makeText(applicationContext, "APP RESUME!", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onPause() {
        super.onPause()
        Log.i("PAUSE", "OnPause");
        TextOnCreate.text =   TextOnCreate.text.toString() + "\n OnPause "
        Toast.makeText(applicationContext, "APP PAUSE!", Toast.LENGTH_SHORT)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        TextOnCreate.text =   TextOnCreate.text.toString() + "\n OnDestroy "
        Toast.makeText(applicationContext, "APP DESTROY!", Toast.LENGTH_SHORT)
            .show()
    }
}
