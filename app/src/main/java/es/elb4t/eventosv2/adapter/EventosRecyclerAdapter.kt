package es.elb4t.eventosv2.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.squareup.picasso.Picasso
import es.elb4t.eventosv2.R
import es.elb4t.eventosv2.model.EventoItem

/**
 * Created by eloy on 10/2/18.
 */
class EventosRecyclerAdapter(private val mContext: Context, options: FirebaseRecyclerOptions<EventoItem>) :
        FirebaseRecyclerAdapter<EventoItem, EventosRecyclerAdapter.EventoViewHolder>(options) {

    override fun onBindViewHolder(holder: EventoViewHolder, position: Int, item: EventoItem) {
        val txtEvento = item.evento
        val txtCiudad = item.ciudad
        val txtFecha = item.fecha

        holder.txtEvento.text = txtEvento
        holder.txtCiudad.text = txtCiudad
        holder.txtFecha.text = txtFecha
        Picasso.with(mContext)
                .load(item.imagen)
                .error(R.mipmap.ic_launcher_round)
                //.networkPolicy(NetworkPolicy.NO_CACHE)
                .resize(300, 200)
                .centerCrop()
                .onlyScaleDown()
                .into(holder.imagen)
        Log.e("ADAPTER------","----------------------------")
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventoViewHolder {
        Log.e("CRATE------","VIEW----------------------------")
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.evento, parent, false) as ViewGroup
        return EventoViewHolder(view)
    }

    class EventoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtEvento: TextView = itemView.findViewById(R.id.txtEvento)
        val txtCiudad: TextView = itemView.findViewById(R.id.txtCiudad)
        val txtFecha: TextView = itemView.findViewById(R.id.txtFecha)
        val imagen: ImageView = itemView.findViewById(R.id.imgImagen)
    }
}
