package es.elb4t.eventosv2

import android.app.Application
import android.content.Context
import android.content.Intent
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import es.elb4t.eventosv2.utils.Dialogo


/**
 * Created by eloy on 10/2/18.
 */

class EventosAplicacion : Application() {
    private val ITEMS_CHILD_NAME = "eventos"

    companion object {
        internal val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        var itemsReference: DatabaseReference? = null
            private set
        var appContext: Context? = null
            private set
        fun mostrarDialogo(context: Context, mensaje: String) {
            val intent = Intent(context, Dialogo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("mensaje", mensaje)
            context.startActivity(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        EventosAplicacion.appContext = applicationContext
        val database = FirebaseDatabase.getInstance()
        database.setPersistenceEnabled(true)
        itemsReference = database.getReference(ITEMS_CHILD_NAME)
    }


}