package me.ismartin.beforesleep.hardware

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import me.ismartin.beforesleep.administration.DeviceAdmin

class ScreenActions(val context:Context) {

    private var componentName: ComponentName
    private var devicePolicyManager: DevicePolicyManager

    init {
        componentName = ComponentName(context, DeviceAdmin::class.java)
        devicePolicyManager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    fun isDeviceAdmin() = devicePolicyManager.isAdminActive(componentName)

    fun turnOffScreen() = devicePolicyManager.lockNow()

}