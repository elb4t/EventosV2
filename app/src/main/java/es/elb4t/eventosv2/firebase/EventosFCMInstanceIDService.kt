package es.elb4t.eventosv2.firebase

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

/**
 * Created by eloy on 9/2/18.
 */
class EventosFCMInstanceIDService : FirebaseInstanceIdService() {
    override fun onTokenRefresh() {
        val idPush: String? = FirebaseInstanceId.getInstance().token
        //guardarIdRegistro(applicationContext, idPush)
    }
}