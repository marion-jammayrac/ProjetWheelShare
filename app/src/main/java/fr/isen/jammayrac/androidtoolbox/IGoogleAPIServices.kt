package fr.isen.jammayrac.androidtoolbox

import fr.isen.jammayrac.androidtoolbox.model.PlaceDetail
import fr.isen.jammayrac.androidtoolbox.model.RootObject
import retrofit2.http.GET;
import retrofit2.http.Query
import retrofit2.http.Url
import kotlin.String as String1

public interface IGoogleAPIServices {
    @GET
    fun getNearbyPlaces(@Url url: String1): retrofit2.Call<RootObject>

    @GET
    fun getDetailPlaces(@Url url: String1): retrofit2.Call<PlaceDetail>

    @GET ("maps/api/directions/json")
    fun getDirections(@Query("origin") origin: kotlin.String, @Query("destination") destination: kotlin.String):retrofit2.Call<String>

}