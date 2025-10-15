package com.group20.lbdemo.network

import com.google.gson.annotations.SerializedName

data class ApiResponse(
    @SerializedName("message")
    val message: String? = null,

    @SerializedName("server")
    val server: String? = null,

    @SerializedName("status")
    val status: String? = null,

    @SerializedName("type")
    val type: String? = null
)
