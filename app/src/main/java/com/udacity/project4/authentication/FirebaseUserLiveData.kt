package com.udacity.project4.authentication

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
// to observe current firebase user login or not
class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

    private val firebase = FirebaseAuth.getInstance()

    private val authenticationStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        //get updates to current firebase logged into app
        //currentUser is listener will get callback every time current user has changed login or logout
        value = firebaseAuth.currentUser
    }

    override fun onActive() {
        firebase.addAuthStateListener(authenticationStateListener)
    }

    override fun onInactive() {
        firebase.addAuthStateListener(authenticationStateListener)
    }


}