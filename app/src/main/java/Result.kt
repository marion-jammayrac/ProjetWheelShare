package fr.isen.jammayrac.androidtoolbox

import android.media.Image
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.Localisation
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.Nom
import fr.isen.jammayrac.androidtoolbox.fr.isen.jammayrac.androidtoolbox.Tof

data class Result(
    val email: String,
    val location: Localisation,
    val name: Nom,
    val picture: Tof
)