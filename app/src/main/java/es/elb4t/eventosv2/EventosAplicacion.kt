package es.elb4t.eventosv2

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.iid.FirebaseInstanceId
import es.elb4t.eventosv2.utils.Dialogo
import es.elb4t.eventosv2.utils.RequestVolley


/**
 * Created by eloy on 10/2/18.
 */

class EventosAplicacion : Application() {
    private val ITEMS_CHILD_NAME = "eventos"

    companion object {
        val URL_SERVIDOR = "http://cursoandroid.hol.es/notificaciones/"
        var ID_PROYECTO = "eventos-3161f"
        var idRegistro = ""

        internal val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        var appContext: Context? = null
            private set
        fun mostrarDialogo(context: Context, mensaje: String) {
            val intent = Intent(context, Dialogo::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.putExtra("mensaje", mensaje)
            context.startActivity(intent)
        }
        fun guardarIdRegistro(context: Context, idRegistro: String) {
            val parametros = HashMap<String,String>()
            parametros["iddevice"] = idRegistro
            parametros["idapp"] = ID_PROYECTO
            RequestVolley(context).post(URL_SERVIDOR + "registrar.php",parametros){response, code ->
                if (code == 200 &&  !response.contains("Duplicate entry '' for key 'PRIMARY'"))
                    Toast.makeText(appContext,"Dispositivo registrado correctamente", Toast.LENGTH_SHORT).show()
                else
                    Toast.makeText(appContext,"Error al registrar el dispositivo", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        Log.e("EventosAplicacion", "--------FCM Token Refresh: " + FirebaseInstanceId.getInstance().token)
        EventosAplicacion.appContext = applicationContext
    }


}