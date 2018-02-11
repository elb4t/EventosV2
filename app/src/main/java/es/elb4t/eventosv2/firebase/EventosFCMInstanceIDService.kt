package es.elb4t.eventosv2.firebase

import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService
import es.elb4t.eventosv2.Comun.Companion.guardarIdRegistro

/**
 * Created by eloy on 9/2/18.
 */
class EventosFCMInstanceIDService : FirebaseInstanceIdService() {
    private val TAG = EventosFCMInstanceIDService::class.java.simpleName

    override fun onTokenRefresh() {
        val idPush = FirebaseInstanceId.getInstance().token
        guardarIdRegistro(applicationContext, idPush!!)
        Log.e(TAG, "--------FCM Token Refresh: " + idPush!!)
    }
}