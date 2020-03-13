package fr.isen.jammayrac.androidtoolbox

import android.app.DatePickerDialog
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.DatePicker
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import kotlinx.android.synthetic.main.activity_sauvegarde.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SauvegardeActivity : AppCompatActivity() {

    var age = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sauvegarde)

        Sentbutton.setOnClickListener {
            val Name = editTextName.text.toString()
            val LastName = editTextLastName.text.toString()
            val Date = Datebutton.text.toString()
            val answer = JSONObject()
            answer.put("DATE", Date)
            answer.put("NAME", Name)
            answer.put("LASTNAME", LastName)
           // answer.put("DATE", Date)

            val json = answer.toString()
            File(cacheDir.absolutePath + "Name.json").writeText(json)

            Toast.makeText(this, json, Toast.LENGTH_SHORT)
                .show()
        }



        val cal = Calendar.getInstance()
        val dateSetListener =
            DatePickerDialog.OnDateSetListener{ datePicker : DatePicker, year : Int, monthOfYear : Int, dayOfMonth : Int ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, monthOfYear)
                cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                val sdf = SimpleDateFormat("dd/MM/yyyy")
                Datebutton.text = sdf.format(cal.time)

                val today = Calendar.getInstance()

                age = today.get(Calendar.YEAR) - cal.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < cal.get(Calendar.DAY_OF_YEAR))
                    age--
            }

        fun showDatePicker(dateSetListener:DatePickerDialog.OnDateSetListener){
            val cal = Calendar.getInstance()
            DatePickerDialog(this, dateSetListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        Datebutton.setOnClickListener {
            showDatePicker(dateSetListener)
        }
        Readbutton.setOnClickListener {
            /*val dialogBuilder = AlertDialog.Builder(this)
            dialogBuilder.setMessage(File(cacheDir.absolutePath + "Name.json").readText(Charsets.UTF_8))
                .setCancelable(false)
                .setPositiveButton("OK", DialogInterface.OnClickListener {
                        dialog, id -> finish()
                })
            val alert = dialogBuilder.create()
            alert.setTitle("POP-UP DU SWAG")
            alert.show() */

            val result = File(cacheDir.absolutePath + "Name.json").readText(Charsets.UTF_8)
            val json = JSONObject(result)
            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setMessage("LastName : "+json.get("LASTNAME").toString()+"\n" + "Name : "+json.get("NAME").toString() + "\n" +"Date de naissance : "+json.get("DATE").toString()+"\n" + "Age : +$age +ans" + "\n")
               .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, id ->
                   finish()
               })
            val alert = alertDialogBuilder.create()
            alert.setTitle("POP-UP DU SWAG")
            alert.show()
        }
    }

}


