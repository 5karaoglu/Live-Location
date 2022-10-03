package com.besirkaraoglu.locationsharingsampleii.ui.login

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.besirkaraoglu.locationsharingsampleii.ui.main.MainActivity
import com.besirkaraoglu.locationsharingsampleii.R
import com.huawei.agconnect.auth.AGConnectAuth
import com.huawei.agconnect.auth.HwIdAuthProvider
import com.huawei.hms.support.account.AccountAuthManager
import com.huawei.hms.support.account.request.AccountAuthParams
import com.huawei.hms.support.account.request.AccountAuthParamsHelper
import com.huawei.hms.support.hwid.ui.HuaweiIdAuthButton

class LoginActivity : AppCompatActivity() {
    companion object {
        const val SIGN_CODE = 212
        const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initButton()
        isUserSignedIn()
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
                startActivityForResult(service!!.signInIntent, SIGN_CODE)
            }
        }
    }

    private fun isUserSignedIn() {
        val user = AGConnectAuth.getInstance().currentUser
        if (user != null) {
            navigateToMain()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_CODE) {
            val authAccountTask = AccountAuthManager.parseAuthResultFromIntent(data)
            if (authAccountTask.isSuccessful) {
                val authAccount = authAccountTask.result
                signIn(authAccount.accessToken)
                Log.i(TAG, "accessToken:" + authAccount.accessToken)
            }
        }
    }

    private fun signIn(accessToken: String) {
        val credential = HwIdAuthProvider.credentialWithToken(accessToken)
        AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener {
            // onSuccess
            val user = it.user
            navigateToMain()
        }.addOnFailureListener {
            // onFail
            Log.e(TAG, "signIn: ${it.message}")
            Toast.makeText(this, "Login failed! Cause: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}