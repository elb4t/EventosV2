package es.elb4t.eventosv2.model

/**
 * Created by eloy on 10/2/18.
 */

class Evento {
    var evento: String? = null
    var ciudad: String? = null
    var fecha: String? = null
    var imagen: String? = null

    constructor() {}

    constructor(evento: String, ciudad: String,
                fecha: String, imagen: String) {
        this.evento = evento
        this.ciudad = ciudad
        this.fecha = fecha
        this.imagen = imagen
    }
}