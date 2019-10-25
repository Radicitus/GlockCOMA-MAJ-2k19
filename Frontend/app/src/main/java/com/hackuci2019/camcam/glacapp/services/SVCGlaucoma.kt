package com.hackuci2019.camcam.glacapp.services

import com.hackuci2019.camcam.glacapp.HelpMe
import com.hackuci2019.camcam.glacapp.models.Completion
import com.hackuci2019.camcam.glacapp.models.GlaucReport
import okhttp3.MediaType
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Headers
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import okhttp3.RequestBody
import retrofit2.Callback
import retrofit2.Response
import java.io.PrintWriter
import java.io.StringWriter


class SVCGlaucoma
{
    //Private Properties

    private val api = HelpMe.retro.fitThis<IGlaucomaAPI>()

    //endregion
    //region Associated

    private interface IGlaucomaAPI
    {
        @Multipart
        @POST("/upload")
        @Headers("Content-Type: text/html", "charset: utf-8")
        fun upload(@Part imagePart: MultipartBody.Part): Call<GlaucReport>
    }

    //endregion
    //region Services

    fun upload(imgFile: File, onDone: (Completion<GlaucReport>) -> Unit)
    {
        val requestBody = RequestBody.create(
                MediaType.parse("image/*"), imgFile
        )
        val part: MultipartBody.Part = MultipartBody.Part.createFormData(
                "upload", imgFile.name, requestBody
        ) ?: return onDone(Completion.Failure(errStr = "Could not prepare image for upload"))

        api.upload(imagePart = part).enqueue(object: Callback<GlaucReport> {
            override fun onFailure(call: Call<GlaucReport>, t: Throwable)
            {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                t.printStackTrace(pw)
                val trace = sw.toString().toLowerCase()
                val err: String = when
                {
                    trace.contains("timeout")                           ->
                    {
                        "Request timed out"
                    }
                    trace.contains("json") &&
                    (trace.contains("moshi") || trace.contains("gson")) ->
                    {
                        //JSON parsing error
                        "Incoming data is corrupted"
                    }
                    else                                                ->
                    {
                        //These errors are unhandled
                        println(trace)
                        "Something bad happened..."
                    }
                }
                return onDone(Completion.Failure(errStr = err))
            }

            override fun onResponse(call: Call<GlaucReport>, response: Response<GlaucReport>)
            {
                val body = response.body()
                return if (response.isSuccessful && body != null)
                {
                    onDone(Completion.Success(obj = body))
                }
                else
                {
                    val dangarang = response.errorBody()?.string()
                    response.errorBody()?.close()
                    println(dangarang)
                    onDone(Completion.Failure(errStr = "Something failed :("))
                }
            }
        })
    }

    //endregion
}