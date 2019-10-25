package com.hackuci2019.camcam.glacapp.services

import com.hackuci2019.camcam.glacapp.Gimme
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object Retro
{
    //For Kotlin to use. Increases app size.
    inline fun <reified X> fitThis(): X
    {
        val cliento = OkHttpClient.Builder()
        return Retrofit.Builder()
                .baseUrl(Gimme.theBaseURL)
                .addConverterFactory(
                        MoshiConverterFactory.create(
                                Moshi.Builder()
                                        .add(KotlinJsonAdapterFactory())
                                        .build()
                        )
                )
                .client(cliento.build())
                .build()
                .create(X::class.java)
    }
}