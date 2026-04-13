package com.kourt.app.ui.screens.club.create

interface CreateClubScreenActions {
    fun onClubNameChange(value: String)
    fun onShortNameChange(value: String)
    fun onPresidentChange(value: String)
    fun onTechnicalDirectorChange(value: String)
    fun onCountryChange(value: String)
    fun onCityChange(value: String)
    fun onCreateClub()
    fun onLogoUploadTap()
}
