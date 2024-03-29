package com.athompson.cafe.ui.activities

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import com.athompson.cafe.R
import com.athompson.cafe.databinding.ActivityLoginBinding
import com.athompson.cafe.firestore.FireStoreUser
import com.athompson.cafe.ui.snackbars.IconSnackbar
import com.athompson.cafelib.extensions.ActivityExtensions.logError
import com.athompson.cafelib.extensions.ActivityExtensions.showErrorSnackBar
import com.athompson.cafelib.extensions.ContextExtensions.isOnline
import com.athompson.cafelib.extensions.ResourceExtensions.asString
import com.athompson.cafelib.extensions.ViewExtensions.isEmpty
import com.athompson.cafelib.extensions.ViewExtensions.loopAVD
import com.athompson.cafelib.extensions.ViewExtensions.trimmed
import com.athompson.cafelib.models.User
import com.athompson.cafelib.shared.SharedConstants
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@Suppress("DEPRECATION")
class LoginActivity : BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private var mGoogleClient: GoogleSignInClient? = null

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        binding.tvForgotPassword.setOnClickListener{
            val intent = Intent(this@LoginActivity, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
        binding.btnLogin.setOnClickListener{

            if(isOnline())
                logInRegisteredUser()
        }
        binding.tvRegister.setOnClickListener{
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.googleSignInButton.setOnClickListener {

                IconSnackbar.make(
                    window.decorView as ViewGroup,
                    "Hello World!",
                    Snackbar.LENGTH_LONG
                ).show()

         //   googleSignIn()
        }

        initGoogleClient()

        binding.image.loopAVD(R.drawable.coffeecup)
    }

    private fun googleSignIn() {
        showProgressDialog(R.string.please_wait.asString())
        val signInIntent: Intent? = mGoogleClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    private fun initGoogleClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(R.string.default_web_client_id.asString())
                .requestEmail()
                .build()

        mGoogleClient = this.let { GoogleSignIn.getClient(it, gso) }

    }

    private fun authWithGoogle(account: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                hideProgressDialog()
                loginSuccessGoogle()
            } else {
                hideProgressDialog()
                loginErrorGoogle(task.exception?.message.toString())
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result != null) {
                if (result.isSuccess) {
                    val account = result.signInAccount
                    authWithGoogle(account)
                } else {
                    hideProgressDialog()
                }
            }
        }
    }

    private fun validateLoginDetails(): Boolean {
        return when {
            binding.etEmail.isEmpty() -> {
                showErrorSnackBar(R.string.err_msg_enter_email.asString(), true)
                false
            }
            binding.etPassword.isEmpty()  -> {
                showErrorSnackBar(R.string.err_msg_enter_password.asString(), true)
                false
            }
            else -> {
                true
            }
        }
    }

    private fun logInRegisteredUser() {

        if (validateLoginDetails()) {

            showProgressDialog(R.string.please_wait.asString())

            val email = binding.etEmail.trimmed()
            val password = binding.etPassword.trimmed()

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->



                    if (task.isSuccessful) {
                        FireStoreUser().get(::success,::failure)
                    } else {
                        hideProgressDialog()
                        loginErrorGoogle(task.exception?.message.toString())
                    }
                }
        }
    }


    private fun success(user: User) {
        // Hide the progress dialog.
        hideProgressDialog()

        user.toString()

        if (user.profileCompleted == 0) {
            // If the user profile is incomplete then launch the UserProfileActivity.
            val intent = Intent(this@LoginActivity, UserProfileActivity::class.java)
            intent.putExtra(SharedConstants.EXTRA_USER_DETAILS, user)
            startActivity(intent)
        } else {
            // Redirect the user to Main Screen after log in.
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
        }
        finish()
    }

    private fun failure(e:Exception)
    {
        logError(e.message.toString())
    }


    private fun loginErrorGoogle(message:String)
    {
        showErrorSnackBar(message, true)
    }

    private fun loginSuccessGoogle()
    {
        showErrorSnackBar(R.string.successful_login.asString(), false)
        lifecycleScope.launch(context = Dispatchers.Main) {
            delay(1000)
            startActivity(Intent(this@LoginActivity, DashboardActivity::class.java))
            finish()
        }
    }
}