package es.elb4t.eventosv2.utils

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import es.elb4t.eventosv2.EventoDetalles


/**
 * Created by eloy on 10/2/18.
 */
class Dialogo : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val extras = intent.extras
        if (intent.hasExtra("mensaje")) {
            val alertDialog = AlertDialog.Builder(this).create()
            alertDialog.setTitle("Mensaje:")
            alertDialog.setMessage(extras!!.getString("mensaje"))
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "CERRAR",
                    { dialog, which ->
                        dialog.dismiss()
                        if (intent.hasExtra("extras") &&  extras.getString("extras") != "") {
                            val intent = Intent(applicationContext, EventoDetalles::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            intent.putExtra("evento", extras.getString("extras"))
                            startActivity(intent)
                        }
                        finish()
                    })
            alertDialog.show()
            extras.remove("mensaje")
        }
    }
}