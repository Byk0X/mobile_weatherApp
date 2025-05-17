package com.example.weatherapp

import androidx.compose.ui.graphics.vector.ImageVector

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