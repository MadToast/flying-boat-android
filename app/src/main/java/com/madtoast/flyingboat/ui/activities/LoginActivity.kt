package com.madtoast.flyingboat.ui.activities

import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.api.floatplane.model.authentication.AuthResponse
import com.madtoast.flyingboat.databinding.ActivityLoginBinding
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModel
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max
import java.util.concurrent.ArrayBlockingQueue
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    lateinit var creatorImageQueue: ArrayBlockingQueue<Drawable>

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        val binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(cacheDir, this)
        )[LoginViewModel::class.java]

        //Initialize the loginViewModel
        loginViewModel.init()

        //Start the immersion
        setupImmersion(binding)

        //Initialize the login observers
        setupObservers(binding, loginViewModel)

        //Initialize login fields
        setupLoginFields(binding, loginViewModel)

        //Initialize the button events
        setupButtonEvents(binding, loginViewModel)

        //Check if user's logged in
        binding.loginFields.visibility = View.GONE
        showLoading(getString(R.string.initial_app_loading), binding)
        CoroutineScope(Dispatchers.IO).launch {
            loginViewModel.checkUserLoggedIn()
            loginViewModel.getAllPlatformCreators()
        }
    }

    private fun setupImmersion(binding: ActivityLoginBinding) {
        (binding.clouds.drawable as AnimatedVectorDrawable).start()
    }

    private fun setupButtonEvents(binding: ActivityLoginBinding, loginViewModel: LoginViewModel) {
        val btnLogin = binding.loginButton
        val btnSendToken = binding.sendTokenButton

        btnLogin.setOnClickListener {
            tryLogin(binding, loginViewModel)
        }

        btnSendToken.setOnClickListener {
            trySendToken(binding, loginViewModel)
        }
    }

    private fun setupLoginFields(binding: ActivityLoginBinding, loginViewModel: LoginViewModel) {
        val username = binding.usernameText
        val password = binding.passwordText
        val twoFactorToken = binding.twoFactorToken

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
                        tryLogin(binding, loginViewModel)
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
                        trySendToken(binding, loginViewModel)
                    }
                }
                false
            }
        }
    }

    private fun setupObservers(binding: ActivityLoginBinding, loginViewModel: LoginViewModel) {
        val twoFactor = binding.TwoFactorFields
        val loginFields = binding.loginFields
        val username = binding.usernameText
        val password = binding.passwordText
        val twoFactorToken = binding.twoFactorToken
        val btnLogin = binding.loginButton

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
            var backCallback: OnBackPressedCallback? = null
            hideLoading(binding)
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
                binding.loginFields.visibility = View.VISIBLE
                twoFactor.visibility = View.GONE
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
                            backCallback?.remove()
                        }
                    }
                } else {
                    finishLoginProcess(loginResult.success)
                }
            }
        })

        loginViewModel.creatorsResult.observe(this@LoginActivity, Observer {
            val creatorResult = it ?: return@Observer
            if (creatorResult.success != null) {
                showCreatorsOnScreen(binding, creatorResult.success)
            }
        })
    }

    private fun showCreatorsOnScreen(
        binding: ActivityLoginBinding,
        creators: Array<com.madtoast.flyingboat.api.floatplane.model.creator.Creator>
    ) {
        val activityContext = this
        creatorImageQueue = ArrayBlockingQueue<Drawable>(creators.size)
        var creatorWithLogos = 0
        for (creator in creators) {
            creator.icon?.apply {
                path?.apply {
                    creatorWithLogos++
                    Glide
                        .with(activityContext)
                        .load(this)
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                //Do nothing but log it
                                Log.e(TAG, "Failed to load the ${creator.title} creator image")
                                creatorImageQueue.add(null)
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                creatorImageQueue.add(resource)
                                return true
                            }
                        })
                        .submit();
                }
            }
        }
        processCreatorLogoQueue(creatorWithLogos, binding)
    }

    private fun processCreatorLogoQueue(creatorCount: Int, binding: ActivityLoginBinding) {
        CoroutineScope(Dispatchers.IO).launch {
            var creatorCount = creatorCount
            while (creatorCount > 0) {
                try {
                    createImageForCreator(binding, creatorImageQueue.take())
                } catch (e: Exception) {
                    // App was probably restarted due to configuration change
                    Log.d(TAG, "Exception while creating creator logo")
                }

                creatorCount--
                delay(5000L)
            }
        }
    }

    private fun createImageForCreator(binding: ActivityLoginBinding, creatorLogo: Drawable?) {
        if (creatorLogo == null) {
            return
        }

        //Get the drawable from resources
        val creatorBalloon =
            ResourcesCompat.getDrawable(resources, R.drawable.baloon_creator, null) as LayerDrawable
        creatorBalloon.mutate()
        creatorBalloon.setDrawableByLayerId(R.id.creatorLogo, creatorLogo)

        // Setup the required values
        val creatorBalloonView = ImageView(this)
        val maxPositionY = binding.creatorHolder.height
        val partsOfScreen = maxPositionY / 6
        val drawableOffsetX = creatorBalloon.minimumWidth
        val positionX = (binding.creatorHolder.width + drawableOffsetX).toFloat()
        val positionY = (partsOfScreen * Random.nextInt(5)).toFloat()
        val scale = getScaleBasedOnPositionY(positionY, maxPositionY)
        val zIndex = getZIndexBasedOnScale(scale)

        // Setup the view
        creatorBalloonView.setImageDrawable(creatorBalloon)
        creatorBalloonView.scaleY = scale
        creatorBalloonView.scaleX = scale
        creatorBalloonView.translationZ = zIndex
        creatorBalloonView.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).also {
            it.gravity = (Gravity.START or Gravity.TOP)
        }

        // Setup the animation
        val animation = TranslateAnimation(
            positionX,
            ((-creatorBalloon.minimumWidth) * 2).toFloat(), positionY, positionY
        )
        animation.duration = getDurationBasedOnPositionY(positionY, maxPositionY)
        animation.interpolator = LinearInterpolator()
        animation.repeatMode = Animation.RESTART
        animation.repeatCount = Animation.INFINITE

        CoroutineScope(Dispatchers.Main).launch {
            binding.creatorHolder.addView(creatorBalloonView)
            creatorBalloonView.startAnimation(animation)
        }
    }

    private fun getDurationBasedOnPositionY(position: Float, viewHeight: Int): Long {
        val minDuration = 20000f
        val maxDuration = 30000f

        // The bigger the position, the higher the speed
        return (maxDuration - ((position * minDuration) / viewHeight)).toLong()
    }

    private fun getScaleBasedOnPositionY(position: Float, viewHeight: Int): Float {
        val minScale = 0.3f
        val maxScale = 1.4f

        return max((position * maxScale) / viewHeight, minScale)
    }

    private fun getZIndexBasedOnScale(scale: Float): Float {
        val minPosition = 0f
        val maxPosition = 10f
        val maxScale = 2.5f

        return max((scale * maxPosition) / maxScale, minPosition)
    }

    private fun tryLogin(binding: ActivityLoginBinding, loginViewModel: LoginViewModel) {
        showLoading(getString(R.string.logging_in_string), binding)
        CoroutineScope(Dispatchers.IO).launch {
            loginViewModel.login(
                binding.usernameText.text.toString(),
                binding.passwordText.text.toString()
            )
        }
    }

    private fun trySendToken(binding: ActivityLoginBinding, loginViewModel: LoginViewModel) {
        showLoading(getString(R.string.two_factor_logging_in_string), binding)
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
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(text: String, binding: ActivityLoginBinding) {
        binding.loading.visibility = View.VISIBLE
        binding.loadingText.text = text
    }

    private fun hideLoading(binding: ActivityLoginBinding) {
        binding.loading.visibility = View.GONE
    }

    companion object {
        const val TAG = "LoginActivity"
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