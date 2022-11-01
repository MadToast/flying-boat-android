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
import android.view.animation.AnimationUtils
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
import kotlinx.coroutines.launch
import java.lang.Float.max
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(cacheDir, this)
        )[LoginViewModel::class.java]

        //Initialize the loginViewModel
        loginViewModel.init()

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

    override fun onStart() {
        super.onStart()

        //Start the animations
        startAnimations()
    }

    override fun onStop() {
        super.onStop()

        //Stop the animations to avoid background battery usage
        stopAnimations()
    }

    private fun startAnimations() {
        (binding.clouds.drawable as AnimatedVectorDrawable).start()
        (binding.skyBackground.drawable as AnimatedVectorDrawable).start()

        val animation = AnimationUtils.loadAnimation(this, R.anim.slide_in_left);
        binding.plane.startAnimation(animation)
    }

    private fun stopAnimations() {
        (binding.clouds.drawable as AnimatedVectorDrawable).stop()
        (binding.skyBackground.drawable as AnimatedVectorDrawable).stop()
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
        var animationDelay = 0L
        for (creator in creators) {
            creator.id
            val currentAnimationDelay = animationDelay
            //Freeze this
            creator.icon?.apply {
                path?.apply {
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
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                createImageForCreator(binding, resource, currentAnimationDelay)
                                return true
                            }
                        })
                        .submit();
                }
            }
            animationDelay += 10000L
        }
    }

    private fun createImageForCreator(
        binding: ActivityLoginBinding,
        creatorLogo: Drawable?,
        animationDelay: Long
    ) {
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
        val scale = getRandomScale()
        val zIndex = getZIndexBasedOnScale(scale)

        // Setup the view
        creatorBalloonView.setImageDrawable(creatorBalloon)
        creatorBalloonView.setColorFilter(
            ResourcesCompat.getColor(
                resources,
                R.color.baloon_overlay,
                null
            )
        )
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
        val acrossScreenAnimation = TranslateAnimation(
            positionX,
            ((-creatorBalloon.minimumWidth) * 2).toFloat(), positionY, positionY
        )
        acrossScreenAnimation.duration = getDurationBasedOnPositionY(positionY, maxPositionY)
        acrossScreenAnimation.interpolator = LinearInterpolator()
        acrossScreenAnimation.repeatMode = Animation.RESTART
        acrossScreenAnimation.repeatCount = Animation.INFINITE
        acrossScreenAnimation.startOffset = animationDelay

        CoroutineScope(Dispatchers.Main).launch {
            binding.creatorHolder.addView(creatorBalloonView)
            creatorBalloonView.startAnimation(acrossScreenAnimation)
        }
    }

    private fun getDurationBasedOnPositionY(position: Float, viewHeight: Int): Long {
        val minDuration = 50000f
        val maxDuration = 60000f

        // The bigger the position, the higher the speed
        return (maxDuration - ((position * minDuration) / viewHeight)).toLong()
    }

    private fun getRandomScale(): Float {
        val minScale = 0.5
        val maxScale = 0.7

        return Random.nextDouble(minScale, maxScale).toFloat()
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
        if (errorString != R.string.enter_credentials)
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