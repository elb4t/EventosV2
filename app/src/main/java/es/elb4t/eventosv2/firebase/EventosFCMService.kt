package es.elb4t.eventosv2.firebase

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Created by eloy on 9/2/18.
 */

class EventosFCMService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (remoteMessage.data.isNotEmpty()) {
            var evento = ""
            evento = "Evento: " + remoteMessage.data["evento"] + "\n"
            evento = evento + "Día: " + remoteMessage.data["dia"] + "\n"
            evento = evento + "Ciudad: " +
                    remoteMessage.data["ciudad"] + "\n"
            evento = (evento + "Comentario: "
                    + remoteMessage.data["comentario"])
            //mostrarDialogo(getApplicationContext(), evento)
        } else {
            if (remoteMessage.notification != null) {
                //mostrarDialogo(getApplicationContext(), remoteMessage.getNotification().getBody())
            }
        }
    }
}