package com.waxd.pos.fcmb.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.storage.FirebaseStorage
import com.waxd.pos.fcmb.R
import com.waxd.pos.fcmb.app.FcmbApp
import com.waxd.pos.fcmb.base.BaseActivity
import com.waxd.pos.fcmb.base.DataResult
import com.waxd.pos.fcmb.databinding.ActivityMainBinding
import com.waxd.pos.fcmb.datastore.DataStoreWrapper
import com.waxd.pos.fcmb.datastore.KeyStore
import com.waxd.pos.fcmb.datastore.KeyStore.decryptData
import com.waxd.pos.fcmb.ui.initial.InitialActivity
import com.waxd.pos.fcmb.ui.main.fragments.profile.ProfileViewModel
import com.waxd.pos.fcmb.utils.Util.loadImage
import com.waxd.pos.fcmb.utils.Util.visible
import com.waxd.pos.fcmb.utils.firebase.FirebaseWrapper
import com.waxd.pos.fcmb.utils.handlers.ILogoutHandler
import com.waxd.pos.fcmb.utils.handlers.ViewClickHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseActivity(), ViewClickHandler, ILogoutHandler {

    private val viewModel: MainViewModel by viewModels()
    private var navHostFragment: NavHostFragment? = null

    @Inject
    lateinit var firebaseWrapper: FirebaseWrapper

    @Inject
    lateinit var dataStoreWrapper: DataStoreWrapper

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewClickHandler = this



        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as NavHostFragment
        navHostFragment?.navController?.let { binding.bottomNavigation.setupWithNavController(it) }

        // Add a listener to handle changes in the navigation destination
        navHostFragment?.navController?.addOnDestinationChangedListener { _, destination, _ ->
            // Show or hide main UI elements based on the destination ID
            when (destination.id) {

                R.id.agentDashboardFragment, R.id.searchFragment, R.id.profileFragment -> {
                    binding.tvTitleLeft.visible(isVisible = destination.id == R.id.agentDashboardFragment)

                    updateMainUIElements(
                        isVisible = true
                    )
                }

                else -> {
                    binding.tvTitleLeft.visible(isVisible = false)
                    updateMainUIElements(isVisible = false)
                }
            }

        }


        FcmbApp.instance.setLogoutHandler(this)

        setObserver()

        val uid = decryptData(KeyStore.USER_UID)
        viewModel.getAgentById(uid)

    }

    private fun setObserver() {
        viewModel.response.observe(this) {
            when (it) {
                is DataResult.Failure -> {

                    it.message?.let { it1 -> showToast(it1) }
                }

                DataResult.Loading -> {
                }

                is DataResult.Success -> {
                    if (it.data.profileUri != null) {
                        binding.ivUser.loadImage(it.data.profileUri, R.color.white)
                    } else
                        it.data.profileImage?.let { it1 ->
                            val url =
                                "gs://fcmb-aggri-staging.firebasestorage.app/profile_images/" + it1.split(
                                    "/"
                                )[5]
                            println(url)
                            FirebaseStorage.getInstance()
                                .getReferenceFromUrl(url).downloadUrl.addOnSuccessListener { uri ->
                                    println(uri)
                                    it.data.profileUri = uri
                                    binding.ivUser.loadImage(uri, R.color.white)

                                }

                        }

                }
            }
        }
    }

    /**
     * Updates the visibility of main UI elements.
     *
     * @param isVisible A boolean indicating whether the UI elements should be visible.
     */
    private fun updateMainUIElements(isVisible: Boolean) {
        binding.ivUser.visible(isVisible)
        binding.ivBack.visible(!isVisible)
        binding.bottomNavigation.visible(isVisible = isVisible)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.ivNotification -> {
                navHostFragment?.navController?.navigate(R.id.notificationFragment)
            }

            R.id.ivBack->{
                navHostFragment?.navController?.popBackStack()
            }
        }
    }

    /**
     * Sets the title of the activity.
     *
     * @param title The title to be set.
     */
    fun setTitle(title: String) {
        binding.tvTitle.text = title
    }

    fun logOut() {
        lifecycleScope.launch {
            dataStoreWrapper.clearAllPreferences()
            viewModel.clear()
            binding.ivUser.loadImage(R.drawable.ic_user)
            KeyStore.deleteAllKeys()
            startActivity(Intent(this@MainActivity, InitialActivity::class.java))
            finishAffinity()
        }
    }

    override fun logout() {
        logOut()
    }


}