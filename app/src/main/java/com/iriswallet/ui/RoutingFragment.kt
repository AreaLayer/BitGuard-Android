package com.iriswallet.ui

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricPrompt
import androidx.navigation.fragment.findNavController
import com.iriswallet.R
import com.iriswallet.data.SharedPreferencesManager
import com.iriswallet.data.SharedPreferencesManager.PREFS_PIN_LOGIN_CONFIGURED
import com.iriswallet.databinding.FragmentRoutingBinding
import com.iriswallet.utils.AppAuthenticationService
import com.iriswallet.utils.AppAuthenticationServiceListener
import com.iriswallet.utils.AppContainer

class RoutingFragment :
    MainBaseFragment<FragmentRoutingBinding>(FragmentRoutingBinding::inflate),
    AppAuthenticationServiceListener {

    private lateinit var appAuthenticationService: AppAuthenticationService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appAuthenticationService = AppAuthenticationService(this)
        AppContainer.storedMnemonic = SharedPreferencesManager.mnemonic
        if (AppContainer.storedMnemonic.isBlank())
            findNavController().navigate(R.id.action_routingFragment_to_firstRunFragment)
    }

    override fun onResume() {
        super.onResume()
        if (!mActivity.loggedIn && !mActivity.loggingIn) {
            if (SharedPreferencesManager.pinLoginConfigured) {
                mActivity.loggingIn = true
                appAuthenticationService.auth(PREFS_PIN_LOGIN_CONFIGURED)
            } else {
                authenticated(PREFS_PIN_LOGIN_CONFIGURED)
            }
        }
    }

    override fun authenticated(requestCode: String) {
        mActivity.loggedIn = true
        if (SharedPreferencesManager.pinLoginConfigured && !AppContainer.canUseBiometric)
            mActivity.hideSplashScreen = true
        findNavController().navigate(R.id.action_routingFragment_to_mainFragment)
    }

    override fun handleAuthError(requestCode: String, errorExtraInfo: String?, errCode: Int?) {
        when (errCode) {
            BiometricPrompt.ERROR_USER_CANCELED,
            BiometricPrompt.ERROR_NEGATIVE_BUTTON -> requireActivity().finish()
            BiometricPrompt.ERROR_LOCKOUT -> {
                mActivity.hideSplashScreen = true
                AlertDialog.Builder(requireContext())
                    .setMessage(getString(R.string.err_too_many_attempts))
                    .setPositiveButton(getString(R.string.exit)) { _, _ -> mActivity.finish() }
                    .setCancelable(false)
                    .create()
                    .show()
            }
            AppAuthenticationService.USER_DISABLED_AUTH -> {
                SharedPreferencesManager.pinLoginConfigured = false
                SharedPreferencesManager.pinActionsConfigured = false
                authenticated()
            }
            else -> appAuthenticationService.auth(PREFS_PIN_LOGIN_CONFIGURED)
        }
    }
}
