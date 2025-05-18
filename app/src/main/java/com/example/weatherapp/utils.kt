package com.example.weatherapp

import android.content.Context
import androidx.compose.ui.graphics.vector.ImageVector
import kotlin.math.pow
import kotlin.math.sqrt

sealed class TabItem(val label: String) {
    class TextTab(label: String) : TabItem(label)
    class IconTab(val icon: ImageVector, label: String) : TabItem(label)
}

//changes special letters to latin
fun cityNameWithoutSpecialLetters(cityName: String): String {

    val map = mapOf(
        'ą' to 'a', 'ć' to 'c', 'ę' to 'e', 'ł' to 'l', 'ń' to 'n',
        'ó' to 'o', 'ś' to 's', 'ż' to 'z', 'ź' to 'z',
        'Ą' to 'A', 'Ć' to 'C', 'Ę' to 'E', 'Ł' to 'L', 'Ń' to 'N',
        'Ó' to 'O', 'Ś' to 'S', 'Ż' to 'Z', 'Ź' to 'Z'
    )

    return cityName.map { letter -> map[letter] ?: letter }.joinToString("")

}

fun isTablet(context: Context): Boolean {
    val displayMetrics = context.resources.displayMetrics
    val widthInches = displayMetrics.widthPixels / displayMetrics.xdpi
    val heightInches = displayMetrics.heightPixels / displayMetrics.ydpi
    val diagonalInches = sqrt(widthInches.toDouble().pow(2.0) + heightInches.toDouble().pow(2.0))

    return diagonalInches >= 7.0
}

