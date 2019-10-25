package com.hackuci2019.camcam.glacapp

import android.app.Activity
import android.arch.lifecycle.ViewModelProviders
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.hackuci2019.camcam.glacapp.models.Completion
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), VMMain.VMMainDelegate
{
    //region Private Properties

    private lateinit var vm: VMMain

    private var uploadBt: Button? = null
    private var selectImgBt: ImageButton? = null
    private var tvImgNotUp: TextView? = null
    private var vwProgressSpinner: ProgressBar? = null
    private var vwDarkness: View? = null

    //endregion
    //region Associated

    companion object
    {
        private val REQUEST_TAKE_PHOTO = 0
        private val REQUEST_SELECT_IMAGE_IN_ALBUM = 1
    }

    //endregion
    //region Lifecycle

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        //Grab view model
        vm = ViewModelProviders.of(this).get(VMMain::class.java)
        vm.delegate = this

        setContentView(R.layout.activity_main)

        //Assign these here AFTER the view has been inflated
        uploadBt = BTN_up_image     //Kotlin is nice because there's a baked in library that
        selectImgBt = ImgB_up_prev  //searches UI pieces for you
        tvImgNotUp = TV_image_not_up
        vwProgressSpinner = vw_progress_spinner
        vwDarkness = vw_darkness

        //Set up UI behaviors
        uploadBt?.setOnClickListener {
            vm.upload()
        }
        selectImgBt?.setOnClickListener {
            openImageGallery()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?)
    {
        super.onActivityResult(requestCode, resultCode, data)

        //Important to reassign delegate here because sometimes the app loses state when switching
        //apps
        vm.delegate = this

        //When images have been selected from media gallery, give the poss result to the vm
        if (requestCode == REQUEST_SELECT_IMAGE_IN_ALBUM && resultCode == Activity.RESULT_OK)
        {
            vm.handleImageSelectionFrom(possActionResult = data)
        }
    }

    override fun onResume()
    {
        super.onResume()
        vm.delegate = this
        vm.performActionsOnResume()
    }

    override fun onPause()
    {
        vm.delegate = null
        super.onPause()
    }

    //region VM Delegate Conformance

    override fun vmDidUpdate(state: VMMain.MainState)
    {
        //Reset UI
        vwProgressSpinner?.visibility = View.GONE
        vwDarkness?.visibility = View.GONE
        uploadBt?.isEnabled = false
        tvImgNotUp?.visibility = View.VISIBLE
        selectImgBt?.setImageDrawable(null)

        //Something really beautiful about Kotlin is that the compiler forces handling all cases
        //of a sealed class
        when (state)
        {
            VMMain.MainState.NoPic               ->
            {
                //Reset UI takes care of this
            }
            is VMMain.MainState.PicSelected      ->
            {
                selectImgBt?.setImageBitmap(state.pic)
                uploadBt?.isEnabled = true
                tvImgNotUp?.visibility = View.INVISIBLE
            }
            VMMain.MainState.PicUploading        ->
            {
                vwProgressSpinner?.animate()
                vwProgressSpinner?.visibility = View.VISIBLE
                vwDarkness?.visibility = View.VISIBLE
            }
            is VMMain.MainState.DisplayingResult ->
            {
                when (state.result) {
                    is Completion.Success -> toastError(str = state.result.obj.serverResponse)
                    is Completion.Failure -> toastError(str = state.result.errStr)
                }.let {  }
            }
        }.let { }   //This let trigger the compiler enforcement
    }

    //endregion
    //regionPrivate Functions

//    private fun sendPhoto(view: View)
//    {
//        khttp.post(
//                url = "http://localhost:8080/upload",
//                files = listOf("some,data,to,send\nanother,row,to,send\n".fileLike(name = "report.csv"))
//        )
//    }

    private fun toastError(str: String)
    {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show()
    }

    private fun openImageGallery()
    {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        if (intent.resolveActivity(packageManager) != null)
        {
            startActivityForResult(intent, REQUEST_SELECT_IMAGE_IN_ALBUM)
        }
        else
        {
            toastError(str = "Couldn't open gallery... :(")
        }
    }

    //endregion
}

