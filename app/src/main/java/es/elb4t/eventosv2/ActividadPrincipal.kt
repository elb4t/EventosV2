package es.elb4t.eventosv2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.Toolbar
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.firebase.database.DatabaseReference
import es.elb4t.eventosv2.EventosAplicacion.Companion.PLAY_SERVICES_RESOLUTION_REQUEST
import es.elb4t.eventosv2.EventosAplicacion.Companion.itemsReference
import es.elb4t.eventosv2.EventosAplicacion.Companion.mostrarDialogo
import es.elb4t.eventosv2.adapter.EventosRecyclerAdapter
import es.elb4t.eventosv2.adapter.EventosRecyclerAdapter.EventoViewHolder
import es.elb4t.eventosv2.model.EventoItem
import kotlinx.android.synthetic.main.activity_actividad_principal.*


class ActividadPrincipal : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private var adapter: FirebaseRecyclerAdapter<EventoItem, EventoViewHolder>? = null
    private var current: ActividadPrincipal? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_principal)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.app_name)

        if (!comprobarGooglePlayServices()) {
            Toast.makeText(this, "Error Google Play Services: no está instalado o no es válido.", Toast.LENGTH_LONG)
            finish()
        }

        databaseReference = itemsReference!!

        val options = FirebaseRecyclerOptions.Builder<EventoItem>()
                .setQuery(databaseReference, EventoItem::class.java)
                .build()
        adapter = EventosRecyclerAdapter(this, options)
        reciclerViewEventos.layoutManager = LinearLayoutManager(this)
        reciclerViewEventos.adapter = adapter
    }

    private fun comprobarGooglePlayServices(): Boolean {
        val resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode))
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show()
            else
                finish()

            return false
        }
        return true
    }

    override fun onStart() {
        super.onStart()
        adapter!!.startListening()
        current = this
    }

    override fun onStop() {
        super.onStop()
        adapter!!.stopListening()
    }

    fun getCurrentContext(): ActividadPrincipal {
        return current!!
    }

    override fun onResume() {
        super.onResume()
        var extras = intent.extras
        if (extras != null && extras.keySet().size > 4) {
            var evento = ""
            evento = "Evento: " + extras.getString("evento") + "\n"
            evento = evento + "Día: " + extras.getString("dia") + "\n"
            evento = evento + "Ciudad: " + extras.getString("ciudad") + "\n"
            evento = evento + "Comentario: " + extras.getString("comentario")
            mostrarDialogo(applicationContext, evento)
            for (key in extras.keySet()) {
                intent.removeExtra(key)
            }
            extras.clear()
        }
    }
}
