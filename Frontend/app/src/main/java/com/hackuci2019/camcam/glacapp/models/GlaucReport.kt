package com.hackuci2019.camcam.glacapp.models

import com.squareup.moshi.Json

data class GlaucReport(
    @Json(name = "serverResponse") var serverResponse: String
)