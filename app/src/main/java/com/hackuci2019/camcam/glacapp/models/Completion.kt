package com.hackuci2019.camcam.glacapp.models

sealed class Completion<X>  //Lint says this isn't needed, but it is
{
    class Success<X>(val obj: X) : Completion<X>()
    class Failure<X>(val errStr: String) : Completion<X>()
}
