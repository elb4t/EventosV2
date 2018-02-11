package es.elb4t.eventosv2

import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.messaging.FirebaseMessaging
import es.elb4t.eventosv2.Comun.Companion.mostrarDialogo
import kotlinx.android.synthetic.main.temas.*


/**
 * Created by eloy on 11/2/18.
 */
class Temas : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.temas)

        checkBoxDeportes.isChecked = consultarSuscripcionATemaEnPreferencias(
                applicationContext, "Deportes")
        checkBoxTeatro.isChecked = consultarSuscripcionATemaEnPreferencias(
                applicationContext, "Teatro")
        checkBoxCine.isChecked = consultarSuscripcionATemaEnPreferencias(
                applicationContext, "Cine")
        checkBoxFiestas.isChecked = consultarSuscripcionATemaEnPreferencias(
                applicationContext, "Fiestas")

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
    }

    private fun mantenimientoSuscripcionesATemas(tema: String, suscribir: Boolean) {
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