package fr.isen.jammayrac.androidtoolbox

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val SharedPreferences = getSharedPreferences("ID_ID", Context.MODE_PRIVATE)


        val savedIdentifiant = SharedPreferences.getString("ID", "")
        val savedMotDePasse = SharedPreferences.getString("MDP", "")
        if (savedIdentifiant == "admin" && savedMotDePasse == "123") {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        Validatebutton.setOnClickListener() {
            // Toast.makeText(applicationContext,"Identifiant : ${editIdentifiant.text.toString()}",Toast.LENGTH_SHORT).show()
            // test avec le nom identif qui s'affiche.
            if (editIdentifiant.text.toString() == "lesBG") {
                if (editMotDePasse.text.toString() == "Potins") {
                    val idConnexion = editIdentifiant.text.toString().trim()
                    val mdp = editMotDePasse.text.toString().trim()

                    val editor = SharedPreferences.edit()
                    editor.putString("ID", idConnexion)
                    editor.putString("MDP", mdp)
                    editor.apply()

                    val intent = Intent(this, HomeActivity::class.java) //f
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(applicationContext, "password non valide !", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, "Identifiants non valide !", Toast.LENGTH_SHORT)
                    .show()

            }
        }

    }
}
