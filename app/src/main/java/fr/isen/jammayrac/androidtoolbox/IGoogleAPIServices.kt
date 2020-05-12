package fr.isen.jammayrac.androidtoolbox

import android.telecom.Call
import fr.isen.jammayrac.androidtoolbox.model.PlaceDetail
import fr.isen.jammayrac.androidtoolbox.model.RootObject
import retrofit2.http.GET;
import retrofit2.http.Url

public interface IGoogleAPIServices {
    @GET
    fun getNearbyPlaces(@Url url:String): retrofit2.Call<RootObject>

    @GET
    fun getDetailPlaces(@Url url:String): retrofit2.Call<PlaceDetail>

}