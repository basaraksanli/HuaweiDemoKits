package com.example.basardemo

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.huawei.hms.common.ApiException
import com.huawei.hms.support.hwid.HuaweiIdAuthManager
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService


class AccountActivity : AppCompatActivity() {

    private val TAG = "AccountActivity"
    private var mAuthManager: HuaweiIdAuthService? = null
    private var mAuthParam: HuaweiIdAuthParams? = null
    private var REQUEST_SIGN_IN_LOGIN = 8008
    private var REQUEST_SIGN_IN_LOGIN_CODE = 9009


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_SIGN_IN_LOGIN -> {
                //login success
                //get user message by parseAuthResultFromIntent
                val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                if (authHuaweiIdTask.isSuccessful) {
                    val huaweiAccount = authHuaweiIdTask.result
                    printLog("${huaweiAccount.displayName} signIn success \n\n")
                    printLog("AccessToken: ${huaweiAccount.accessToken}\n\n")
                    printLog("ID token: ${huaweiAccount.idToken}\n\n")
                }
                else {
                    printLog("signIn failed: ${(authHuaweiIdTask.exception as ApiException).statusCode}\n\n")
                }
            }
            REQUEST_SIGN_IN_LOGIN_CODE -> {
                //login success
                val authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data)
                if (authHuaweiIdTask.isSuccessful) {
                    val huaweiAccount = authHuaweiIdTask.result
                    printLog("signIn get code success.\n\n")
                    printLog("ServerAuthCode: ${huaweiAccount.authorizationCode}\n\n")
                } else {
                    printLog("signIn get code failed: ${(authHuaweiIdTask.exception as ApiException).statusCode}\n\n")
                }
            }else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    fun printLog(log: String) {
        val logText = findViewById<TextView>(R.id.logText)
        logText.append(log)
    }

    private fun signInToken() {
        mAuthParam = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setIdToken()
            .setAccessToken()
            .createParams()
        mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParam)
        startActivityForResult(
            (mAuthManager as HuaweiIdAuthService).signInIntent,
            REQUEST_SIGN_IN_LOGIN
        )
    }

    private fun signInCode() {
        mAuthParam = HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM)
            .setProfile()
            .setAuthorizationCode()
            .createParams()
        mAuthManager = HuaweiIdAuthManager.getService(this, mAuthParam)
        startActivityForResult(
            (mAuthManager as HuaweiIdAuthService).signInIntent,
            REQUEST_SIGN_IN_LOGIN_CODE
        )
    }
    private fun signOut() {
        val signOutTask = mAuthManager!!.signOut()
        signOutTask.addOnSuccessListener { Log.i(TAG, "signOut Success\n\n")
        printLog("$TAG: signOut success\n\n")}
            .addOnFailureListener { Log.i(TAG, "signOut fail\n\n")
            printLog("$TAG: signOut fail\n\n")}
    }

    fun IDTokenClick(view: View) {signInToken()}
    fun AuthCodeClick(view: View) {signInCode()}
    fun signOutClick(view: View) {signOut()}

}