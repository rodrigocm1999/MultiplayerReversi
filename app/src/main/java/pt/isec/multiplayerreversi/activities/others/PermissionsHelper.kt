package pt.isec.multiplayerreversi.activities.others

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class PermissionsHelper(private val activity: Activity) {

    private data class CallbackCode(
        val code: Int, val callback: () -> Unit, val permissions: Array<String>,
    )

    private var codeCounter = 0
    private val callbacks = ArrayList<CallbackCode>()

    fun withPermissions(permissions: Array<String>, callback: () -> Unit) {
        if (permissions.any {
                ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
            }) {
            val code = codeCounter++
            callbacks.add(CallbackCode(code, callback, permissions))
            ActivityCompat.requestPermissions(activity, permissions, code)
        } else {
            callback()
        }
    }

    //Chamar isto dentro do onRequestPermissionsResult da atividade
    fun onRequestPermissionsResult(requestCode: Int) {
        val callbackCode = callbacks.find { i -> i.code == requestCode } ?: return
        if (callbackCode.permissions.all {
                ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED
            }) {
            callbackCode.callback()
        }
        callbacks.remove(callbackCode)
    }
}