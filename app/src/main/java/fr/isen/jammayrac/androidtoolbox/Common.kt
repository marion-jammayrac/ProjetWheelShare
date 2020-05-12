package fr.isen.jammayrac.androidtoolbox

import fr.isen.jammayrac.androidtoolbox.model.Results

object Common {
    private val GOOGLE_API_URL = "https://maps.googleapis.com/"

    var currentResult: Results?=null

    val googleApiService : IGoogleAPIServices
        get() = RetrofitClient.getClient(GOOGLE_API_URL).create((IGoogleAPIServices::class.java))

}