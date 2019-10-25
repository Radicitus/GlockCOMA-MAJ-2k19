package com.hackuci2019.camcam.glacapp

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.webkit.MimeTypeMap
import com.hackuci2019.camcam.glacapp.models.Completion
import com.hackuci2019.camcam.glacapp.models.GlaucReport
import com.hackuci2019.camcam.glacapp.services.SVCGlaucoma
import java.io.File
import java.io.InputStream

class VMMain(private val app: Application) : AndroidViewModel(app)
{
    //region Properties

    var delegate: VMMainDelegate? = null

    //endregion
    //region Private Properties

    private var currentPhoto: File? = null
    private var currentState: VMMain.MainState = MainState.NoPic

    //endregion
    //region Associated

    sealed class MainState
    {
        object NoPic : MainState()
        class PicSelected(val pic: Bitmap) : MainState()
        object PicUploading : MainState()
        class DisplayingResult(val result: Completion<GlaucReport>) : MainState()
    }

    interface VMMainDelegate
    {
        fun vmDidUpdate(state: MainState)
    }

    companion object
    {
        private const val FOLDER_NAME = "TMP"   //This'll show up in the Android file system
        private const val PHOTO_NAME = "selectedImage"  //This too
        private fun getTMPDirectory(fromContext: Context): File?
        {
            //Try to get a reference to the local base directory
            val baseDir = fromContext.filesDir ?: return null

            //Specify the folder to use
            val dir = File(baseDir, "/$FOLDER_NAME")

            //Create the folder if it's not there
            dir.mkdirs()

            //Return the reference to the directory
            return dir
        }
    }

    //endregion
    //region Functions

    fun performActionsOnResume()
    {
        delegate?.vmDidUpdate(state = currentState)
    }

    fun handleImageSelectionFrom(possActionResult: Intent?)
    {
        var possErr: String? = null
        when
        {
            possActionResult?.data != null ->
            {
                //There's only one URI since only one image was chosen
                val selectedImgPath: Uri? = possActionResult.data
                if (selectedImgPath != null)
                {
                    val possSave = saveToTMPDirectory(tmpImgPath = selectedImgPath)
                    currentPhoto = possSave //Keep a reference
                }
            }
            else                           ->
            {
                possErr = "Could not get image data from gallery picker intent " +
                          "because bot data and clip data are null"
                println(possErr)
            }
        }
        val stateUpdate = if (possErr == null)
        {
            MainState.PicSelected(pic = BitmapFactory.decodeFile(currentPhoto?.path))
        }
        else
        {
            //Kotlin's nice, it knows that possErr isn't nil in this scope, so no unwrapping needed
            MainState.DisplayingResult(result = Completion.Failure(errStr = possErr))
        }
        //Trigger a state update
        currentState = stateUpdate
        delegate?.vmDidUpdate(state = stateUpdate)
    }

    fun upload()
    {
        val foto = currentPhoto
        if (foto != null)
        {
            delegate?.vmDidUpdate(state = MainState.PicUploading)
            SVCGlaucoma().upload(imgFile = foto)
            {
                val state = MainState.DisplayingResult(result = it)
                currentState = state
                delegate?.vmDidUpdate(state = state)
            }
        }
        else
        {
            val state = MainState.DisplayingResult(
                    result = Completion.Failure(errStr = "The filesystem lost the photo")
            )
            currentState = state
            delegate?.vmDidUpdate(state = state)
        }
    }

    //endregion
    //region Private Functions

    private fun saveToTMPDirectory(tmpImgPath: Uri): File?
    {
        val resultsDirectory: File? = getTMPDirectory(fromContext = app)

        //Open a data stream
        val inStream: InputStream? = app.contentResolver.openInputStream(tmpImgPath)

        //Try to figure out the file extension
        val extension: String? = MimeTypeMap
                .getSingleton()
                .getExtensionFromMimeType(
                        app.contentResolver.getType(tmpImgPath)
                )
        return finishSaveOperation(resultsDirectory, inStream, extension)
    }

    private fun finishSaveOperation(resultsDirectory: File?,
                                    inStream: InputStream?,
                                    extension: String?): File?
    {
        //Make sure all components are there
        if (resultsDirectory == null || inStream == null || extension == null)
        {
            println("Could not save to temp directory")
            return null
        }
        val newImgFile = File(resultsDirectory, "$PHOTO_NAME.$extension")

        //Overwrite old
        if (newImgFile.exists())
        {
            newImgFile.delete()
        }
        newImgFile.createNewFile()

        //Write data there
        newImgFile.outputStream().use { fOut -> inStream.copyTo(fOut) }

        //Ayy lmao
        return newImgFile
    }

    //endregion
}
