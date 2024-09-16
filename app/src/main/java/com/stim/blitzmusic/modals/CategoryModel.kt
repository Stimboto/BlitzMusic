package com.stim.blitzmusic.modals

data class CategoryModel(
    val name : String,
    val coverUrl : String,
    var songs : List<String>
){
    constructor() : this("", "", listOf())
}
