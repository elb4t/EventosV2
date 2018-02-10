package es.elb4t.eventosv2.utils

import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity


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
                        finish()
                    })
            alertDialog.show()
            extras.remove("mensaje")
        }
    }
}