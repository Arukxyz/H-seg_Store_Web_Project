package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.ResumenImpacto;
import pe.edu.utp.hoseg.web.model.Comunidad;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoLote;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.repository.ComunidadRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.LoteDonacionRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.util.List;

/** Cifras públicas de impacto: lo que ya se entregó, contado sobre la base compartida. */
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
}
