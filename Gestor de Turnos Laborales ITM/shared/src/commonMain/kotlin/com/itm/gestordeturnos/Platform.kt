package com.itm.gestordeturnos

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform