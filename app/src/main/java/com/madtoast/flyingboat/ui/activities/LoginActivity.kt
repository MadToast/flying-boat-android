package com.madtoast.flyingboat.ui.activities

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.graphics.PorterDuff
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.animation.*
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
import com.jakewharton.threetenabp.AndroidThreeTen
import com.madtoast.flyingboat.R
import com.madtoast.flyingboat.databinding.ActivityLoginBinding
import com.madtoast.flyingboat.network.NetworkLiveData
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModel
import com.madtoast.flyingboat.ui.activities.ui.login.LoginViewModelFactory
import com.madtoast.flyingboat.ui.utilities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Float.max
import kotlin.math.ceil
import kotlin.random.Random


class LoginActivity : AppCompatActivity() {

    lateinit var binding: ActivityLoginBinding
    lateinit var loginViewModel: LoginViewModel
    lateinit var networkLiveData: NetworkLiveData
    var creatorsDisplayed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)

        AndroidThreeTen.init(this);

        binding = ActivityLoginBinding.inflate(layoutInflater)

        setContentView(binding.root)

        loginViewModel = ViewModelProvider(
            this,
            LoginViewModelFactory(cacheDir, this)
        )[LoginViewModel::class.java]

        networkLiveData = NetworkLiveData()

        //Initialize the loginViewModel
        loginViewModel.init()

        //Initialize the network live data
        networkLiveData.init(application)

        //Initialize the login observers
        setupObservers(binding, loginViewModel, networkLiveData)

        //Initialize login fields
        setupLoginFields(binding, loginViewModel)

        //Initialize the button events
        setupButtonEvents(binding, loginViewModel)
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

    override fun onLowMemory() {
        super.onLowMemory()
        Glide
            .with(this)
            .onLowMemory()
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        Glide
            .with(this)
            .onTrimMemory(level)
    }

    private fun doPreChecks() {
        //Check if user's logged in
        showLoading(getString(R.string.initial_app_loading), binding)
        CoroutineScope(Dispatchers.IO).launch {
            loginViewModel.checkUserLoggedIn()
        }
    }

    private fun startAnimations() {
        (binding.clouds.drawable as AnimatedVectorDrawable).start()
        (binding.skyBackground.drawable as AnimatedVectorDrawable).start()
        (binding.plane.drawable as AnimatedVectorDrawable).start()

        if (loginViewModel.isFirstTimeLaunch) {
            startFirstStartAnimations()
            loginViewModel.isFirstTimeLaunch = false
            return
        }

        startPlaneIdleAnimations()
    }

    private fun startFirstStartAnimations() {
        val loginEnter = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> AnimationUtils.loadAnimation(
                this,
                R.anim.login_slide_up
            )
            else -> AnimationUtils.loadAnimation(this, R.anim.login_slide_down)
        }
        binding.loginCardContainer.visibility = View.INVISIBLE

        //Handle the plane rotation
        with(
            binding.plane.withAnimatorByFloat(
                "rotation",
                -45f,
                -4f, true
            )
        ) {
            duration = 1400
            startDelay = 1000
            interpolator = AccelerateDecelerateInterpolator()
            doOnAnimatorEnd {
                doPreChecks()
                startPlaneIdleAnimations()
                binding.loginCardContainer.visibility = View.VISIBLE
                binding.loginCardContainer.startAnimation(loginEnter)
            }
            start()
        }

        // Use window insets on version code R and upper
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
            //Fly plane on screen
            with(
                binding.plane.withAnimatorBasedOnWindowPercentage(
                    "translationY",
                    windowManager.currentWindowMetrics,
                    0.8f,
                    0f
                )
            ) {
                duration = 2200
                interpolator = DecelerateInterpolator()
                start()
            }
            with(
                binding.plane.withAnimatorBasedOnWindowPercentage(
                    "translationX",
                    windowManager.currentWindowMetrics,
                    -0.5f,
                    0f
                )
            ) {
                duration = 2200
                interpolator = DecelerateInterpolator()
                start()
            }

            //Cloud parallax
            with(
                binding.starsBackground.withAnimatorBasedOnWindowPercentage(
                    "translationY",
                    windowManager.currentWindowMetrics,
                    -0.85f,
                    0f, true
                )
            ) {
                duration = 1800
                startDelay = 400
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            with(
                binding.clouds.withAnimatorBasedOnWindowPercentage(
                    "translationY",
                    windowManager.currentWindowMetrics,
                    -0.85f,
                    0f, true
                )
            ) {
                duration = 1800
                startDelay = 400
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        } else {
            val displayMetrics = DisplayMetrics()
            @Suppress("DEPRECATION") //No need to alert of deprecation as we have it behind a flag
            windowManager.defaultDisplay.getMetrics(displayMetrics)

            //Fly on screen
            with(
                binding.plane.withAnimatorBasedOnDisplayPercentage(
                    "translationY",
                    displayMetrics,
                    0.8f,
                    0f
                )
            ) {
                duration = 2000
                interpolator = DecelerateInterpolator()
                start()
            }
            with(
                binding.plane.withAnimatorBasedOnDisplayPercentage(
                    "translationX",
                    displayMetrics,
                    -0.5f,
                    0f
                )
            ) {
                duration = 2000
                interpolator = DecelerateInterpolator()
                start()
            }

            //Cloud parallax
            with(
                binding.starsBackground.withAnimatorBasedOnDisplayPercentage(
                    "translationY",
                    displayMetrics,
                    -0.85f,
                    0f, true
                )
            ) {
                duration = 1800
                startDelay = 400
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
            with(
                binding.clouds.withAnimatorBasedOnDisplayPercentage(
                    "translationY",
                    displayMetrics,
                    -0.85f,
                    0f, true
                )
            ) {
                duration = 1800
                startDelay = 400
                interpolator = AccelerateDecelerateInterpolator()
                start()
            }
        }
    }

    private fun startPlaneIdleAnimations() {
        val percentageEnd = when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> 0.1f
            else -> 0.6f
        }

        with(binding.plane.withAnimatorByPercentage("translationY", 0f, percentageEnd)) {
            startDelay = 2000
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }

        with(ObjectAnimator.ofFloat(binding.plane, "rotation", -4f, 4f)) {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            start()
        }
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

    private fun setupObservers(
        binding: ActivityLoginBinding,
        loginViewModel: LoginViewModel,
        networkLiveData: NetworkLiveData
    ) {
        val twoFactor = binding.TwoFactorFields
        val loginFields = binding.loginFields
        val username = binding.usernameText
        val password = binding.passwordText
        val twoFactorToken = binding.twoFactorToken
        val btnLogin = binding.loginButton

        networkLiveData.observe(this@LoginActivity, Observer {
            if (it && !loginViewModel.creatorsLoaded) {
                CoroutineScope(Dispatchers.IO).launch {
                    loginViewModel.getAllPlatformCreators()
                }
            } else if (it && !creatorsDisplayed && loginViewModel.creatorsResult.value?.success != null) {
                showCreatorsOnScreen(binding, loginViewModel.creatorsResult.value!!.success!!)
            }
        })

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
                    finishLoginProcess()
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
        binding.creatorHolder.removeAllViews()
        creatorsDisplayed = true

        var animationDelay = 0L
        for (creator in creators) {
            creator.id
            val currentAnimationDelay = animationDelay
            //Freeze this
            creator.icon?.apply {
                path?.apply {
                    Glide
                        .with(this@LoginActivity)
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
                                createImageForCreator(
                                    binding,
                                    creator.id,
                                    resource,
                                    currentAnimationDelay
                                )
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
        creatorId: String,
        creatorLogo: Drawable?,
        animationDelay: Long
    ) {
        if (creatorLogo == null) {
            return
        }
        // Post to creator holder to make sure layout is drawn and measured
        binding.creatorHolder.post {
            //Get the drawable from resources
            creatorLogo.setTint(
                ResourcesCompat.getColor(
                    resources,
                    R.color.baloon_overlay,
                    null
                )
            )
            creatorLogo.setTintMode(PorterDuff.Mode.SRC_ATOP)
            val creatorBalloon =
                ResourcesCompat.getDrawable(
                    resources,
                    R.drawable.baloon_creator,
                    null
                ) as LayerDrawable
            creatorBalloon.mutate()
            creatorBalloon.setDrawableByLayerId(R.id.creatorLogo, creatorLogo)

            // Setup the required values
            val creatorBalloonView = ImageView(this)

            //Show creators on top of screen if portrait to avoid creators being shown behind login screen
            val maxPositionY =
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT)
                    binding.creatorHolder.height / 2
                else
                    binding.creatorHolder.height

            val partsOfScreen = (maxPositionY / 6)
            val positionX = binding.creatorHolder.width.toFloat()
            val positionY = (partsOfScreen * Random.nextInt(5)).toFloat()
            val scale = getRandomScale()
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

            CoroutineScope(Dispatchers.Main).launch {
                binding.creatorHolder.addView(creatorBalloonView)

                // Setup the animation
                with(
                    creatorBalloonView.withAnimatorByFloat(
                        "rotation",
                        getRotationBasedOnPositionY(positionY, maxPositionY),
                        3f,
                        true
                    )
                ) {
                    duration = 1400
                    startDelay = 1000
                    interpolator = AccelerateDecelerateInterpolator()
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = ObjectAnimator.INFINITE
                    start()
                }
                with(
                    creatorBalloonView.withAnimatorByFloat(
                        "translationY",
                        positionY,
                        positionY + 20,
                        true,

                        )
                ) {
                    duration = 1500
                    interpolator = AccelerateDecelerateInterpolator()
                    repeatMode = ObjectAnimator.REVERSE
                    repeatCount = ObjectAnimator.INFINITE
                    start()
                }

                if (loginViewModel.creatorsPositionsOnScreen.containsKey(creatorId) && loginViewModel.creatorsPositionsOnScreen[creatorId]!! > 0) {
                    var updatedPositionX = loginViewModel.creatorsPositionsOnScreen[creatorId]!!
                    if (loginViewModel.creatorsPreviousOrientation[creatorId] != resources.configuration.orientation) {
                        updatedPositionX =
                            (binding.creatorHolder.width * updatedPositionX) / binding.creatorHolder.height
                    }

                    with(
                        creatorBalloonView.withAnimatorByFloat(
                            "translationX",
                            updatedPositionX,
                            ((-creatorBalloon.minimumWidth) * 2).toFloat(),
                            true,
                            ::registerCreatorPositionOnScreen,
                            creatorId
                        )
                    ) {
                        duration =
                            ceil((updatedPositionX * 60000L) / (positionX + ((creatorBalloon.minimumWidth) * 2))).toLong()
                        interpolator = LinearInterpolator()
                        doOnAnimatorEnd {
                            startCreatorAnimation(
                                creatorBalloonView,
                                positionX,
                                creatorBalloon.minimumWidth,
                                creatorId
                            )
                        }
                        start()
                    }
                } else {
                    creatorBalloonView.translationX = positionX // Set the position before the delay
                    delay(animationDelay)
                    startCreatorAnimation(
                        creatorBalloonView,
                        positionX,
                        creatorBalloon.minimumWidth,
                        creatorId
                    )
                }

                // Set the current orientation
                loginViewModel.creatorsPreviousOrientation[creatorId] =
                    resources.configuration.orientation
            }
        }
    }

    private fun startCreatorAnimation(
        creatorBalloonView: View,
        positionX: Float,
        minimumWidth: Int,
        creatorId: String
    ) {
        with(
            creatorBalloonView.withAnimatorByFloat(
                "translationX",
                positionX,
                ((-minimumWidth) * 2).toFloat(),
                true,
                ::registerCreatorPositionOnScreen,
                creatorId
            )
        ) {
            duration = 60000L
            interpolator = LinearInterpolator()
            repeatMode = android.animation.ObjectAnimator.RESTART
            repeatCount = android.animation.ObjectAnimator.INFINITE
            start()
        }
    }

    private fun registerCreatorPositionOnScreen(creator: String, newValue: Float) {
        loginViewModel.creatorsPositionsOnScreen[creator] = newValue
    }

    private fun getRotationBasedOnPositionY(position: Float, viewHeight: Int): Float {
        val minRotation = 6f
        val maxRotation = 11f

        // The bigger the position, the higher the speed
        return minRotation - ((position * maxRotation) / viewHeight)
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

    private fun finishLoginProcess() {
        //Set the Activity result
        setResult(Activity.RESULT_OK)

        //Start next Activity
        val myIntent = Intent(this@LoginActivity, MainActivity2::class.java)
        startActivity(myIntent)

        //Finish the login activity
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        if (errorString != R.string.enter_credentials)
            Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(text: String, binding: ActivityLoginBinding) {
        binding.loginFields.visibility = View.GONE
        binding.TwoFactorFields.visibility = View.GONE
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