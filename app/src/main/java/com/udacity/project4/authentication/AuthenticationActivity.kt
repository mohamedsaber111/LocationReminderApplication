package com.udacity.project4.authentication

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityAuthenticationBinding

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthenticationBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticationBinding.inflate(layoutInflater)
        val views = binding.root
        setContentView(views)


        binding.login.setOnClickListener { launchToSignInFlow() }


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
}
