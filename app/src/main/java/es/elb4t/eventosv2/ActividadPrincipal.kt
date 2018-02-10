package es.elb4t.eventosv2

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GooglePlayServicesUtil
import com.google.firebase.database.DatabaseReference
import es.elb4t.eventosv2.EventosAplicacion.Companion.PLAY_SERVICES_RESOLUTION_REQUEST
import es.elb4t.eventosv2.EventosAplicacion.Companion.itemsReference
import es.elb4t.eventosv2.adapter.EventosRecyclerAdapter
import es.elb4t.eventosv2.adapter.EventosRecyclerAdapter.EventoViewHolder
import es.elb4t.eventosv2.model.EventoItem
import kotlinx.android.synthetic.main.activity_actividad_principal.*


class ActividadPrincipal : AppCompatActivity() {
    private lateinit var databaseReference: DatabaseReference
    private var adapter: FirebaseRecyclerAdapter<EventoItem, EventoViewHolder>? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actividad_principal)

        if (!comprobarGooglePlayServices()) {
            Toast.makeText(this, "Error Google Play Services: no está instalado o no es válido.", Toast.LENGTH_LONG)
            finish()
        }

        val app: EventosAplicacion = applicationContext as EventosAplicacion
        databaseReference = itemsReference!!
        adapter = EventosRecyclerAdapter(R.layout.evento, databaseReference, this)
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
}
