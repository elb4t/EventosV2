package es.elb4t.eventosv2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import es.elb4t.eventosv2.utils.RequestVolley
import kotlinx.android.synthetic.main.enviar_evento.*

class EnviarEvento : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.enviar_evento)
        setSupportActionBar(toolbar)

        fabEnviarEvento.setOnClickListener { view ->
                val parametros = HashMap<String,String>()
                parametros["apiKey"] = resources.getString(R.string.keyApp)
                parametros["idapp"] = Comun.ID_PROYECTO
                parametros["mensaje"] = textEnviarEvento.text.toString()
                RequestVolley(applicationContext).post(Comun.URL_SERVIDOR + "notificar.php",parametros){ response, code ->
                        Toast.makeText(Comun.appContext,"Mensaje enviado correctamente", Toast.LENGTH_SHORT).show()
                }

        }
    }

}
