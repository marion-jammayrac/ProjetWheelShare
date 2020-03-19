package fr.isen.jammayrac.androidtoolbox

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_web_service2.view.*
import java.util.*

class WebService2 (private val users: RandomUser, val context: Context) :
    RecyclerView.Adapter<WebService2.WebHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WebHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view: View = inflater.inflate(R.layout.activity_web_service2, parent, false)
        return WebHolder(users, view, context)
    }

    override fun getItemCount(): Int {
        return users.results.size
    }

    override fun onBindViewHolder(holder: WebHolder, position: Int) {
        holder.loadInfo(position)
    }

    class WebHolder(private val webUsers: RandomUser, view: View, val context: Context) :
        RecyclerView.ViewHolder(view) {
        private val name: TextView = view.NomWebServ
        private val image: ImageView = view.ImageWebServ
        private val address: TextView = view.AddresseWebServ
        private val email: TextView = view.MailWebServ


        fun loadInfo(index: Int) {
            val nameWebServ =
                webUsers.results[index].name.first + " " + webUsers.results[index].name.last
            val addressWebServ =
                webUsers.results[index].location.city

            Picasso.get()
                .load(webUsers.results[index].picture.large)
                .fit().centerInside()
                .into(image)

            name.text = nameWebServ
            email.text = webUsers.results[index].email
            address.text = addressWebServ
        }
    }
}
