package com.quijotelui.controller

import com.quijotelui.electronico.ejecutar.Electronica
import com.quijotelui.electronico.util.TipoComprobante
import com.quijotelui.model.ReporteFactura
import com.quijotelui.service.*
import com.quijotelui.subscription.SuscripcionManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.concurrent.TimeUnit

@RestController
@RequestMapping("/rest/v1")
class ReporteFacturaRestApi {

    @Autowired
    lateinit var reporteFacturaService : IReporteFacturaService

    @Autowired
    lateinit var facturaService : IFacturaService

    @Autowired
    lateinit var parametroService : IParametroService

    @Autowired
    lateinit var electronicoService : IElectronicoService

    @Autowired
    lateinit var informacionService : IInformacionService

    @Value("\${key.property}")
    lateinit var keyProperty: String

    @CrossOrigin(value = "*")
    @GetMapping("/reporte_factura/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}")
    fun getFacturaByFechas(@PathVariable(value = "fechaInicio") fechaInicio : String,
                    @PathVariable(value = "fechaFin") fechaFin : String)
            : ResponseEntity<MutableList<ReporteFactura>> {

        val reporteFactura = reporteFacturaService.findByFechas(fechaInicio, fechaFin)
        return ResponseEntity<MutableList<ReporteFactura>>(reporteFactura, HttpStatus.OK)
    }

    @CrossOrigin(value = "*")
    @GetMapping("/reporte_factura_estado/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}/estado/{estado}")
    fun getFacturaByFechasEstado(@PathVariable(value = "fechaInicio") fechaInicio : String,
                          @PathVariable(value = "fechaFin") fechaFin : String,
                          @PathVariable(value = "estado") estado : String)
            : ResponseEntity<MutableList<ReporteFactura>> {

        val reporteFactura = reporteFacturaService.findByFechasEstado(fechaInicio, fechaFin, estado)
        return ResponseEntity<MutableList<ReporteFactura>>(reporteFactura, HttpStatus.OK)
    }

    @CrossOrigin(value = "*")
    @GetMapping("/factura_autoriza/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}")
    fun autorizarFactura(@PathVariable(value = "fechaInicio") fechaInicio : String,
                        @PathVariable(value = "fechaFin") fechaFin : String) : ResponseEntity<MutableList<ReporteFactura>> {

        val subscription = SuscripcionManager(parametroService)
        if (!subscription.isAlive(keyProperty)) {
            return ResponseEntity(HttpStatus.UNAUTHORIZED)
        }

        var reporteFactura = reporteFacturaService.findByFechasEstado(
                fechaInicio,
                fechaFin,
                "NoAutorizados")

        println("Envía facturas entre: $fechaInicio y  $fechaFin")
        if (reporteFactura.size > 0) {
            for (i in reporteFactura.indices) {
                val row = reporteFactura.get(i)
                println("$i - ${row.codigo} ${row.numero}, Estado -> ${row.estado}")

                val factura = facturaService.findByComprobante(row.codigo.toString(), row.numero.toString())

                if (!factura.isEmpty() && !row.estado.equals("RECIBIDA")) {
                    val genera = Electronica(facturaService,
                            row.codigo.toString(),
                            row.numero.toString(),
                            parametroService,
                            keyProperty,
                            electronicoService)

                    genera.enviar(TipoComprobante.FACTURA)
                }
            }
            println("Espere 3 segundos para empezar la verificación")
            TimeUnit.SECONDS.sleep(3)
            println("Verifica facturas entre: $fechaInicio y  $fechaFin")
            for (i in reporteFactura.indices) {
                val row = reporteFactura.get(i)
                println("$i - ${row.codigo} ${row.numero} verificando")

                val factura = facturaService.findByComprobante(row.codigo.toString(), row.numero.toString())

                if (!factura.isEmpty()) {
                    val genera = Electronica(facturaService,
                            row.codigo.toString(),
                            row.numero.toString(),
                            parametroService,
                            keyProperty,
                            electronicoService)

                    val respuesta = row.estado
                    println("Respuesta: $respuesta")

                    genera.comprobar(informacionService, TipoComprobante.FACTURA)
                }
            }
        }

        reporteFactura.clear()

        reporteFactura = reporteFacturaService.findByFechas(fechaInicio, fechaFin)
        if (reporteFactura.size > 0) {
            println("Estado de facturas")
            for (i in reporteFactura.indices) {
                val row = reporteFactura.get(i)
                println("$i - ${row.codigo} ${row.numero} ${row.estado}")
            }
        }

        return ResponseEntity<MutableList<ReporteFactura>>(reporteFactura, HttpStatus.OK)
    }

    @CrossOrigin(value = "*")
    @GetMapping("/factura_verifica/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}")
    fun verificarFactura(@PathVariable(value = "fechaInicio") fechaInicio : String,
                          @PathVariable(value = "fechaFin") fechaFin : String) : ResponseEntity<MutableList<ReporteFactura>> {

        var factura = reporteFacturaService.findByFechasEstado(
                fechaInicio,
                fechaFin,
                "NoAutorizados")

        println("Facturas entre: $fechaInicio y  $fechaFin")
        if (factura.size > 0) {
            for (i in factura.indices) {
                val row = factura.get(i)

                val factura = facturaService.findByComprobante(row.codigo.toString(), row.numero.toString())

                if (!factura.isEmpty() && (row.estado == "RECIBIDA" || row.estado == "DEVUELTA")) {
                    println("$i - ${row.codigo} ${row.numero} ${row.estado}")
                    val genera = Electronica(facturaService,
                            row.codigo.toString(),
                            row.numero.toString(),
                            parametroService,
                            keyProperty,
                            electronicoService)

                    genera.comprobar(informacionService, TipoComprobante.FACTURA)
                }
            }
        }

        return ResponseEntity<MutableList<ReporteFactura>>(factura, HttpStatus.OK)
    }

    @CrossOrigin(value = "*")
    @GetMapping("/factura_enviar/fechaInicio/{fechaInicio}/fechaFin/{fechaFin}")
    fun enviarFactura(@PathVariable(value = "fechaInicio") fechaInicio : String,
                         @PathVariable(value = "fechaFin") fechaFin : String) : ResponseEntity<MutableList<ReporteFactura>> {

        var reporteFactura = reporteFacturaService.findByFechasEstado(
                fechaInicio,
                fechaFin,
                "NoAutorizados")

        println("Facturas entre: $fechaInicio y  $fechaFin")
        if (reporteFactura.size > 0) {
            for (i in reporteFactura.indices) {
                val row = reporteFactura.get(i)
                println("$i - ${row.codigo} ${row.numero}")

                val factura = facturaService.findByComprobante(row.codigo.toString(), row.numero.toString())

                if (!factura.isEmpty()) {
                    val genera = Electronica(facturaService,
                            row.codigo.toString(),
                            row.numero.toString(),
                            parametroService,
                            keyProperty,
                            electronicoService)

                    val respuesta = genera.enviar(TipoComprobante.FACTURA)
                    println("Restpuesta: $respuesta")

                }
            }
        }

        reporteFactura.clear()

        reporteFactura = reporteFacturaService.findByFechas(fechaInicio, fechaFin)
        if (reporteFactura.size > 0) {
            println("Estado de facturas")
            for (i in reporteFactura.indices) {
                val row = reporteFactura.get(i)
                println("$i - ${row.codigo} ${row.numero} ${row.estado}")
            }
        }

        return ResponseEntity<MutableList<ReporteFactura>>(reporteFactura, HttpStatus.OK)
    }
}