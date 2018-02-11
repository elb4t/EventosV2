package es.elb4t.eventosv2

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import es.elb4t.eventosv2.firebase.EventosFCMService

/**
 * Created by eloy on 11/2/18.
 */
class ReceptorInicio : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        context.startService(Intent(context, EventosFCMService::class.java))
    }
}