package es.elb4t.eventosv2

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.messaging.FirebaseMessaging
import es.elb4t.eventosv2.Comun.Companion.eliminarIdRegistro
import es.elb4t.eventosv2.Comun.Companion.guardarIdRegistro
import es.elb4t.eventosv2.Comun.Companion.mostrarDialogo
import kotlinx.android.synthetic.main.temas.*


/**
 * Created by eloy on 11/2/18.
 */
class Temas : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temas)

        var noRecibirNotificaciones = consultarSuscripcionATemaEnPreferencias(
                applicationContext, "Todos")
        checkBoxNoRecibirNotificaciones.isChecked = noRecibirNotificaciones

        checkBoxDeportes.isChecked = !noRecibirNotificaciones
        checkBoxTeatro.isChecked = !noRecibirNotificaciones
        checkBoxCine.isChecked = !noRecibirNotificaciones
        checkBoxFiestas.isChecked = !noRecibirNotificaciones

        checkBoxDeportes.setOnCheckedChangeListener({ compoundButton, isChecked ->
            mantenimientoSuscripcionesATemas("Deportes", isChecked)
        })
        checkBoxTeatro.setOnCheckedChangeListener({ compoundButton, isChecked ->
            mantenimientoSuscripcionesATemas("Teatro", isChecked)
        })
        checkBoxCine.setOnCheckedChangeListener({ compoundButton, isChecked ->
            mantenimientoSuscripcionesATemas("Cine", isChecked)
        })
        checkBoxFiestas.setOnCheckedChangeListener({ compoundButton, isChecked ->
            mantenimientoSuscripcionesATemas("Fiestas", isChecked)
        })
        checkBoxNoRecibirNotificaciones.setOnCheckedChangeListener { compoundButton, isChecked ->
            mantenimientoSuscripcionesATemas("Todos", isChecked)
        }
    }

    private fun mantenimientoSuscripcionesATemas(tema: String, suscribir: Boolean) {
        if (tema.equals("Todos")) {
            if (suscribir) {
                eliminarIdRegistro(applicationContext)
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema)
                guardarSuscripcionATemaEnPreferencias(applicationContext, tema, true)
                checkBoxDeportes.isChecked = false
                checkBoxTeatro.isChecked = false
                checkBoxCine.isChecked = false
                checkBoxFiestas.isChecked = false
            } else {
                FirebaseMessaging.getInstance().subscribeToTopic(tema)
                Log.e("__________","token: "+FirebaseInstanceId.getInstance().token!!)
                guardarIdRegistro(applicationContext, FirebaseInstanceId.getInstance().token!!)
                guardarSuscripcionATemaEnPreferencias(applicationContext, tema, false)
            }
            checkBoxDeportes.isEnabled = !suscribir
            checkBoxTeatro.isEnabled = !suscribir
            checkBoxCine.isEnabled = !suscribir
            checkBoxFiestas.isEnabled = !suscribir
        } else {
            if (suscribir) {
                mostrarDialogo(this, "Te has suscrito a: $tema")
                FirebaseMessaging.getInstance().subscribeToTopic(tema)
                guardarSuscripcionATemaEnPreferencias(applicationContext, tema, true)
            } else {
                mostrarDialogo(this, "Te has dado de baja de: $tema")
                FirebaseMessaging.getInstance().unsubscribeFromTopic(tema)
                guardarSuscripcionATemaEnPreferencias(applicationContext, tema, false)
            }
        }
    }


    fun guardarSuscripcionATemaEnPreferencias(context: Context, tema: String, suscrito: Boolean?) {
        val prefs = context.getSharedPreferences("Temas", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putBoolean(tema, suscrito!!)
        editor.commit()
    }

    fun consultarSuscripcionATemaEnPreferencias(context: Context, tema: String): Boolean {
        val preferencias = context.getSharedPreferences("Temas", Context.MODE_PRIVATE)
        return preferencias.getBoolean(tema, false)
    }
}