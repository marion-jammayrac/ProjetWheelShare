package fr.isen.jammayrac.androidtoolbox

import okhttp3.Call
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

public interface IGoogleApiService {
    @GET
    fun getNearbyPlaces(@Url url:String): Call<RootObject>


}