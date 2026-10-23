package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.DonacionVista;
import pe.edu.utp.hoseg.web.dto.ImpactoCliente;
import pe.edu.utp.hoseg.web.dto.ImpactoCompra;
import pe.edu.utp.hoseg.web.dto.ResumenImpacto;
import pe.edu.utp.hoseg.web.dto.UnidadesDonadas;
import pe.edu.utp.hoseg.web.model.Comunidad;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoLote;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.LoteDonacion;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.ComunidadRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.LoteDonacionRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

/** Impacto social: cifras públicas, trazabilidad de cada donación e impacto de cada cliente. */
@Service
@Transactional(readOnly = true)
public class ImpactoService {

    private final DonacionRepository donacionRepository;
    private final LoteDonacionRepository loteDonacionRepository;
    private final VentaRepository ventaRepository;
    private final ComunidadRepository comunidadRepository;

    public ImpactoService(DonacionRepository donacionRepository,
                          LoteDonacionRepository loteDonacionRepository,
                          VentaRepository ventaRepository,
                          ComunidadRepository comunidadRepository) {
        this.donacionRepository = donacionRepository;
        this.loteDonacionRepository = loteDonacionRepository;
        this.ventaRepository = ventaRepository;
        this.comunidadRepository = comunidadRepository;
    }

    /** Cifras de la Home y de Nosotros: lo ENTREGADO, no lo prometido. */
    public ResumenImpacto resumen() {
        return new ResumenImpacto(
                donacionRepository.sumarUnidades(TipoCompromiso.ABRIGO, EstadoDonacion.ENTREGADA),
                donacionRepository.sumarUnidades(TipoCompromiso.ARBOL, EstadoDonacion.ENTREGADA),
                loteDonacionRepository.contarComunidades(EstadoLote.ENTREGADO),
                ventaRepository.contarConDonacion(EstadoVenta.PAGADO));
    }

    /** Nombres para los chips de "Comunidades beneficiadas". */
    public List<String> nombresComunidades() {
        return comunidadRepository.findAllByOrderByNombreAsc().stream().map(Comunidad::getNombre).toList();
    }

    /** Línea de tiempo de cada donación de una venta ("Detalle de pedido"). */
    public List<DonacionVista> donacionesDeVenta(Integer idVenta) {
        return donacionRepository.deLaVenta(idVenta).stream().map(ImpactoService::aVista).toList();
    }

    /**
     * "Tu impacto" del cliente (la fidelidad del portal): abrigos y árboles
     * de sus pedidos pagados, cuántos ya se entregaron y cuántos esperan pago.
     */
    public ImpactoCliente impactoDelCliente(Integer idCliente) {
        List<UnidadesDonadas> unidades = donacionRepository.delCliente(idCliente);
        return new ImpactoCliente(
                sumar(unidades, u -> u.estado() != EstadoDonacion.EN_ESPERA),
                sumar(unidades, u -> u.estado() == EstadoDonacion.ENTREGADA),
                sumar(unidades, u -> u.estado() == EstadoDonacion.EN_ESPERA));
    }

    /** Suma abrigos y árboles de las filas que cumplen la condición. */
    static ImpactoCompra sumar(Collection<UnidadesDonadas> unidades, Predicate<UnidadesDonadas> condicion) {
        long abrigos = 0;
        long arboles = 0;
        for (UnidadesDonadas u : unidades) {
            if (!condicion.test(u)) {
                continue;
            }
            if (u.tipo() == TipoCompromiso.ABRIGO) {
                abrigos += u.unidades();
            } else {
                arboles += u.unidades();
            }
        }
        return new ImpactoCompra(abrigos, arboles);
    }

    static DonacionVista aVista(Donacion d) {
        LoteDonacion lote = d.getLote();
        return new DonacionVista(
                d.getId(),
                d.getTipo(),
                d.getCantidad(),
                d.getProducto().getNombre(),
                d.getEstado(),
                lote == null ? null : lote.getComunidad().getNombre(),
                lote == null ? null : lote.getOng().getNombre(),
                lote == null ? null : lote.getFechaDespacho(),
                lote == null ? null : lote.getFechaEntrega());
    }
}
