package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.databinding.ActivityAuthenticationBinding
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private lateinit var binding : ActivityAuthenticationBinding

    companion object {
        const val TAG = "AuthenticationActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private val viewModel by viewModels<LoginViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Inflate the layout for this fragment.
       binding = ActivityAuthenticationBinding.inflate(layoutInflater)



        binding.loginButton.setOnClickListener { launchSignInFlow() }


        viewModel.authenticationState.observe(this,{ authenticateState ->
            when(authenticateState){
                LoginViewModel.AuthenticationState.AUTHENTICATED -> { }

                else -> Log.e(AuthenticationActivity.TAG, "\"Authenticate state that doesn't require any UI change $authenticateState")
            }

        })
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        setContentView(binding.root)

    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), AuthenticationActivity.SIGN_IN_RESULT_CODE
        )

        //Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        //If the user was authenticated, send him to RemindersActivity

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == AuthenticationActivity.SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                Log.i(
                    AuthenticationActivity.TAG,
                    "Successfully signed in user " +
                            "${FirebaseAuth.getInstance().currentUser?.displayName}!"


                )
                val intent = Intent(this, RemindersActivity::class.java).apply {
                    Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Log.i(AuthenticationActivity.TAG, "Sign in unsuccessful ${response?.error?.errorCode}")

            }
        }
    }


}
