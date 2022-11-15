package com.udacity.project4.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.map

//AuthenticationViewModel can stores authentication state as a enum value, contains authentication state variables
class AuthenticationViewModel: ViewModel() {

    enum class AuthState {
        IS_AUTHENTICATED, IS_UNAUTHENTICATED
    }

    //use FirebaseUserLiveData() to populate our authenticationState
    val authenticationState = FirebaseUserLiveData().map { user ->
        // IS_AUTHENTICATED if user not null
        if (user != null) {
            AuthState.IS_AUTHENTICATED
        } else {
            //if is null user IS_UNAUTHENTICATED
            AuthState.IS_UNAUTHENTICATED
        }
    }
}