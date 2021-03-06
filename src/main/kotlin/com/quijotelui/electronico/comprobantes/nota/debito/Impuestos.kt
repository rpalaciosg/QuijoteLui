package com.quijotelui.electronico.comprobantes.nota.debito

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement
class Impuestos {
    @XmlElement
    private var impuesto: MutableCollection<Impuesto> = mutableListOf()

    fun setImpuesto(impuesto : Impuesto){
        this.impuesto.add(impuesto)
    }
}