package fr.isen.jammayrac.androidtoolbox

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_reglages.*

class ReglagesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reglages)

        Blebutton.setOnClickListener() {
            val intent = Intent(this, BluetoothActivity::class.java) //f
            startActivity(intent)
        }
    }
}
