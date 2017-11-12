package com.quijotelui.controller

import com.quijotelui.electronico.ejecutar.Electronica
import com.quijotelui.model.Factura
import com.quijotelui.service.IElectronicoService
import com.quijotelui.service.IFacturaService
import com.quijotelui.service.IParametroService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.TimeUnit


@RestController
@RequestMapping("/rest/v1")
class FacturaRestApi {

    @Autowired
    lateinit var facturaService : IFacturaService

    @Autowired
    lateinit var parametroService : IParametroService

    @Autowired
    lateinit var electronicoService : IElectronicoService

    @GetMapping("/facturas")
    fun getFacturas() : ResponseEntity<MutableList<Factura>> {
        val factura = facturaService.findAll()
        return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
    }

    @GetMapping("/facturaFecha/{fecha}")
    fun getByFecha(@PathVariable(value = "fecha") fecha : String) : ResponseEntity<MutableList<Factura>> {

        val factura = facturaService.findByFecha(fecha)
        return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
    }

    @GetMapping("/facturaFechas/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}")
    fun getByFechas(@PathVariable(value = "fechaInicio") fechaInicio : String,
                    @PathVariable(value = "fechaFin") fechaFin : String) : ResponseEntity<MutableList<Factura>> {

        val factura = facturaService.findByFechas(fechaInicio, fechaFin)
        return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
    }

    /*
    Genera, firma y envía el comprobante electrónico
     */
    @GetMapping("/facturaEnviar/codigo/{codigo}/numero/{numero}")
    fun enviaXml(@PathVariable(value = "codigo") codigo : String,
                 @PathVariable(value = "numero") numero : String) : ResponseEntity<MutableList<Factura>> {

        if (codigo == null || numero == null) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        else {
            val factura = facturaService.findByComprobante(codigo, numero)

            if (factura.isEmpty()) {
                return ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                val genera = Electronica(facturaService, codigo, numero, parametroService, electronicoService)

                genera.enviarFactura()
                return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
            }
        }
    }

    /*
    Autoriza el comprobante electrónico
    */
    @GetMapping("/facturaAutorizar/codigo/{codigo}/numero/{numero}")
    fun autorizarXml(@PathVariable(value = "codigo") codigo : String, @PathVariable(value = "numero") numero : String) : ResponseEntity<MutableList<Factura>> {

        if (codigo == null || numero == null) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        else {
            val factura = facturaService.findByComprobante(codigo, numero)

            if (factura.isEmpty()) {
                return ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                val genera = Electronica(facturaService, codigo, numero, parametroService, electronicoService)

                genera.comprobarFactura()
                return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
            }
        }
    }

    /*
    Envía y Autoriza el comprobante electrónico
    */
    @GetMapping("/facturaProcesar/codigo/{codigo}/numero/{numero}")
    fun procesarXml(@PathVariable(value = "codigo") codigo : String, @PathVariable(value = "numero") numero : String) : ResponseEntity<MutableList<Factura>> {

        if (codigo == null || numero == null) {
            return ResponseEntity(HttpStatus.CONFLICT)
        }
        else {
            val factura = facturaService.findByComprobante(codigo, numero)

            if (factura.isEmpty()) {
                return ResponseEntity(HttpStatus.NOT_FOUND)
            } else {
                val genera = Electronica(facturaService, codigo, numero, parametroService, electronicoService)

                genera.enviarFactura()

                println("Espere 3 segundos por favor")
                TimeUnit.SECONDS.sleep(3)

                genera.comprobarFactura()
                return ResponseEntity<MutableList<Factura>>(factura, HttpStatus.OK)
            }
        }

    }

    @GetMapping("/contribuyentefactura")
    fun getContribuyenteFactura() : ResponseEntity<MutableList<Any>> {
        val factura = facturaService.findContribuyenteByComprobante("FAC","001003003004626")
        return ResponseEntity<MutableList<Any>>(factura, HttpStatus.OK)
    }

}