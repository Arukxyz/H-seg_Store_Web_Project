package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.model.Comunidad;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoLote;
import pe.edu.utp.hoseg.web.model.LoteDonacion;
import pe.edu.utp.hoseg.web.model.Ong;
import pe.edu.utp.hoseg.web.repository.ComunidadRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.LoteDonacionRepository;
import pe.edu.utp.hoseg.web.repository.OngRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;

import java.time.OffsetDateTime;
import java.time.Year;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Transactional(readOnly = true)
public class GestorImpactoService {

    private final LoteDonacionRepository loteRepository;
    private final DonacionRepository donacionRepository;
    private final ComunidadRepository comunidadRepository;
    private final OngRepository ongRepository;
    private final ProductoRepository productoRepository;

    public GestorImpactoService(LoteDonacionRepository loteRepository,
                                DonacionRepository donacionRepository,
                                ComunidadRepository comunidadRepository,
                                OngRepository ongRepository,
                                ProductoRepository productoRepository) {
        this.loteRepository = loteRepository;
        this.donacionRepository = donacionRepository;
        this.comunidadRepository = comunidadRepository;
        this.ongRepository = ongRepository;
        this.productoRepository = productoRepository;
    }

    public List<LoteDonacion> lotes() {
        return loteRepository.todosConComunidadYOng();
    }

    public List<Donacion> donacionesPendientes() {
        return donacionRepository.porEstadoConVenta(EstadoDonacion.PENDIENTE);
    }

    public List<Comunidad> comunidades() {
        return comunidadRepository.findAllByOrderByNombreAsc();
    }

    public List<Ong> ongs() {
        return ongRepository.findAllByOrderByNombreAsc();
    }

    @Transactional
    public LoteDonacion crearLote(Integer idComunidad, Integer idOng, Integer idUsuarioResponsable, String observaciones) {
        Comunidad comunidad = comunidadRepository.findById(idComunidad)
                .orElseThrow(() -> new RecursoNoEncontradoException("Comunidad no encontrada: " + idComunidad));
        Ong ong = ongRepository.findById(idOng)
                .orElseThrow(() -> new RecursoNoEncontradoException("ONG no encontrada: " + idOng));

        LoteDonacion lote = new LoteDonacion();
        lote.setCodigoLote("LOTE-" + Year.now().getValue() + "-" + ThreadLocalRandom.current().nextInt(1000, 9999));
        lote.setComunidad(comunidad);
        lote.setOng(ong);
        lote.setIdUsuarioResponsable(idUsuarioResponsable);
        lote.setFechaCreacion(OffsetDateTime.now());
        lote.setEstado(EstadoLote.PENDIENTE);
        lote.setObservaciones(observaciones == null || observaciones.isBlank() ? null : observaciones.trim());
        return loteRepository.save(lote);
    }

    @Transactional
    public int asignarDonaciones(Integer idLote, List<Integer> idsDonacion) {
        if (idsDonacion == null || idsDonacion.isEmpty()) {
            throw new NegocioException("Selecciona al menos una donación pendiente para asignar.");
        }
        LoteDonacion lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado: " + idLote));
        if (lote.getEstado() == EstadoLote.ENTREGADO) {
            throw new NegocioException("No se pueden asignar donaciones a un lote ya entregado.");
        }

        int asignadas = 0;
        for (Integer idDonacion : idsDonacion) {
            Donacion d = donacionRepository.findById(idDonacion)
                    .orElseThrow(() -> new RecursoNoEncontradoException("Donación no encontrada: " + idDonacion));
            if (d.getEstado() == EstadoDonacion.PENDIENTE) {
                d.setLote(lote);
                d.setEstado(EstadoDonacion.ASIGNADA);
                donacionRepository.save(d);
                asignadas++;
            }
        }
        if (asignadas == 0) {
            throw new NegocioException("Ninguna de las donaciones seleccionadas estaba pendiente.");
        }
        return asignadas;
    }

    @Transactional
    public void despacharLote(Integer idLote) {
        LoteDonacion lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado: " + idLote));
        if (lote.getEstado() != EstadoLote.PENDIENTE) {
            throw new NegocioException("Solo se puede despachar a ruta un lote en estado Pendiente.");
        }
        List<Donacion> items = donacionRepository.delLote(idLote);
        if (items.isEmpty()) {
            throw new NegocioException("Asigna al menos una donación al lote antes de despacharlo.");
        }
        lote.setEstado(EstadoLote.EN_RUTA);
        lote.setFechaDespacho(OffsetDateTime.now());
        loteRepository.save(lote);
    }

    @Transactional
    public void entregarLote(Integer idLote) {
        LoteDonacion lote = loteRepository.findById(idLote)
                .orElseThrow(() -> new RecursoNoEncontradoException("Lote no encontrado: " + idLote));
        if (lote.getEstado() == EstadoLote.ENTREGADO) {
            throw new NegocioException("Este lote ya fue entregado.");
        }
        List<Donacion> items = donacionRepository.delLote(idLote);
        if (items.isEmpty()) {
            throw new NegocioException("El lote no tiene donaciones asignadas para entregar.");
        }

        OffsetDateTime ahora = OffsetDateTime.now();
        lote.setEstado(EstadoLote.ENTREGADO);
        lote.setFechaEntrega(ahora);
        if (lote.getFechaDespacho() == null) {
            lote.setFechaDespacho(ahora);
        }
        loteRepository.save(lote);

        for (Donacion d : items) {
            d.setEstado(EstadoDonacion.ENTREGADA);
            donacionRepository.save(d);
            productoRepository.descontarStockComprometido(d.getProducto().getCodigo(), d.getCantidad());
        }
    }
}
