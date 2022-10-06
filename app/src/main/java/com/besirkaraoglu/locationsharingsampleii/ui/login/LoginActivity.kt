package com.besirkaraoglu.locationsharingsampleii.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.besirkaraoglu.locationsharingsampleii.ui.main.MainActivity
import com.besirkaraoglu.locationsharingsampleii.R
import com.besirkaraoglu.locationsharingsampleii.util.HUAWEI_ID_SIGN_IN
import com.besirkaraoglu.locationsharingsampleii.util.Resource
import com.besirkaraoglu.locationsharingsampleii.util.showToastLong
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
        val TAG = "LoginActivity"

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        isUserSignedIn()
        initButton()

        observeData()
    }

    private fun initButton() {
        val buttonAuth = findViewById<HuaweiIdAuthButton>(R.id.buttonHuaweiAuth)
        buttonAuth.setOnClickListener {
            val user = AGConnectAuth.getInstance().currentUser
            if (user == null) {
                val authParams: AccountAuthParams =
                    AccountAuthParamsHelper(AccountAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
                        .setAccessToken()
                        .createParams();
                val service = AccountAuthManager.getService(this@LoginActivity, authParams)

                // Start the sign-in process when necessary. For example, you can create a button and call the following method in the button tap event:
                startActivityForResult(service!!.signInIntent, HUAWEI_ID_SIGN_IN)
            }
        }
    }

    private fun isUserSignedIn() {
        val user = AGConnectAuth.getInstance().currentUser
        if (user != null) {
            navigateToMain()
        }
    }


    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == HUAWEI_ID_SIGN_IN) {
            viewModel.signInWithHuaweiId(requestCode, data)
        }
    }

    private fun observeData() {
        viewModel.getSignInHuaweiIdLiveData().observe(this, Observer {
            handleSignInReturn(it)
        })
    }

    private fun handleSignInReturn(data: Resource<*>) {
        when (data) {
            is Resource.Success<*> -> {
                navigateToMain()
            }
            is Resource.Error -> {
                data.exception.message?.let { showToastLong(this, it) }
            }
            else -> {

            }
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}