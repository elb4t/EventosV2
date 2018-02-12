package es.elb4t.eventosv2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso


class EventoDetalles : AppCompatActivity() {
    var txtEvento: TextView? = null
    var txtFecha:TextView? = null
    var txtCiudad:TextView? = null
    var imgImagen: ImageView? = null
    var evento: String? = null
    var registros: CollectionReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.evento_detalles)

        txtEvento = findViewById(R.id.txtEvento)
        txtFecha = findViewById(R.id.txtFecha)
        txtCiudad = findViewById(R.id.txtCiudad)
        imgImagen = findViewById(R.id.imgImagen)

        val extras = intent.extras
        evento = extras.getString("evento")
        if (evento == null) evento=""

        registros = FirebaseFirestore.getInstance().collection("eventos")
        registros!!.document(evento!!).get().addOnCompleteListener({
            if (it.isSuccessful){
                txtEvento!!.text = it.result.get("evento").toString()
                txtCiudad!!.text = it.result.get("ciudad").toString()
                txtFecha!!.text = it.result.get("fecha").toString()
                Picasso.with(applicationContext)
                        .load(it.result.get("imagen").toString())
                        .error(R.mipmap.ic_launcher_round)
                        //.networkPolicy(NetworkPolicy.NO_CACHE)
                        .resize(300, 200)
                        .centerCrop()
                        .onlyScaleDown()
                        .into(imgImagen)
            }
        })

    }
}
