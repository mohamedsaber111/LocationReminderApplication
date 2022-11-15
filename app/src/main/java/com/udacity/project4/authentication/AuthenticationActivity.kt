package com.udacity.project4.authentication

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.observe
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding

    private val loginViewModel by viewModels<AuthenticationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val views = binding.root
        setContentView(views)


        binding.login.setOnClickListener { launchToSignInFlow() }

        loginViewModel.authenticationState.observe(this) { authState ->
            when (authState) {
                //if AUTHENTICATED go to reminderActivity
                AuthenticationViewModel.AuthState.IS_AUTHENTICATED -> navigation()
                //if it is UNAUTHENTICATED show message that , sign in is unSuccessful
                else -> Log.e(
                    "login",
                    "Auth state doesn't require user interface change $authState"
                )
            }
        }

    }

    //allow user sign in uses email address or google account
    //since we use firebase UI library this follow will be handled for us
    private fun launchToSignInFlow() {

        val providers = arrayListOf(
            //sign in using email or google account
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //utilizing the firebase UI library to launch sign in flow for us
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE_IS
        )
    }

    companion object {
        const val SIGN_IN_RESULT_CODE_IS = 10
    }
    private fun navigation() {
        val navigationIntent = Intent(this, RemindersActivity::class.java)
        startActivity(navigationIntent)
    }
}
