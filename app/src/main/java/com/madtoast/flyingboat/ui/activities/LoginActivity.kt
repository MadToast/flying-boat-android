package com.madtoast.flyingboat.ui.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.databinding.ActivityLoginBinding
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModel
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var backCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val username = binding.usernameText
        val password = binding.passwordText
        val twoFactorToken = binding.twoFactorToken
        val btnLogin = binding.loginButton
        val btnSendToken = binding.sendTokenButton
        val loading = binding.loading
        val twoFactor = binding.TwoFactorFields
        val loginFields = binding.loginFields

        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(cacheDir, this)
        )[LoginViewModel::class.java]

        //Initialize the loginViewModel
        loginViewModel.init()

        loginViewModel.loginFormState.observe(this@LoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            btnLogin.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
            if (loginState.twoFactorError != null) {
                twoFactorToken.error = getString(loginState.twoFactorError)
            }
        })

        loginViewModel.loginResult.observe(this@LoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                if (loginResult.success.needs2FA) {
                    loginFields.visibility = View.GONE
                    twoFactor.visibility = View.VISIBLE

                    backCallback = onBackPressedDispatcher.addCallback(this) {
                        // Handle the back button event
                        if (twoFactor.visibility == View.VISIBLE) {
                            twoFactor.visibility = View.GONE
                            loginFields.visibility = View.VISIBLE
                            backCallback.remove()
                        }
                    }
                } else {
                    finishLoginProcess(loginResult.success)
                }
            }
        })

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE -> {
                        tryLogin()
                    }
                }
                false
            }
        }

        twoFactorToken.apply {
            afterTextChanged {
                loginViewModel.twoFactorDataChanged(
                    twoFactorToken.text.toString(),
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_GO -> {
                        trySendToken()
                    }
                }
                false
            }
        }

        btnLogin.setOnClickListener {
            tryLogin()
        }

        btnSendToken.setOnClickListener {
            trySendToken()
        }
    }

    private fun tryLogin() {
        binding.loading.visibility = View.VISIBLE
        binding.loadingText.text = getString(R.string.logging_in_string)
        CoroutineScope(Dispatchers.IO).launch {
            loginViewModel.login(
                binding.usernameText.text.toString(),
                binding.passwordText.text.toString()
            )
        }
    }

    private fun trySendToken() {
        binding.loading.visibility = View.VISIBLE
        binding.loadingText.text = getString(R.string.two_factor_logging_in_string)
        CoroutineScope(Dispatchers.IO).launch {
            loginViewModel.check2Fa(
                binding.twoFactorToken.text.toString(),
            )
        }
    }

    private fun finishLoginProcess(model: AuthResponse) {
        val welcome = getString(R.string.welcome)
        val userName = model.user?.username

        Toast.makeText(
            applicationContext,
            "$welcome $userName !",
            Toast.LENGTH_LONG
        ).show()

        //Set the Activity result
        setResult(Activity.RESULT_OK)

        //Start next Activity
        val myIntent = Intent(this@LoginActivity, MainActivity::class.java)
        startActivity(myIntent)

        //Complete and destroy login activity once successful
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}