package pe.edu.utp.hoseg.web.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.LineaLogisticaVista;
import pe.edu.utp.hoseg.web.dto.PedidoLogisticaVista;
import pe.edu.utp.hoseg.web.dto.ProductoEdicionForm;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoPedidoProveedor;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.PedidoProveedor;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.Proveedor;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.repository.CategoriaRepository;
import pe.edu.utp.hoseg.web.repository.ColeccionRepository;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;
import pe.edu.utp.hoseg.web.repository.PedidoProveedorRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;
import pe.edu.utp.hoseg.web.repository.ProveedorRepository;

import java.time.OffsetDateTime;
import java.util.List;
import java.math.BigDecimal;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;

@Service
@Transactional(readOnly = true)
public class LogisticaService {

    private final EnvioRepository envioRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final PedidoProveedorRepository pedidoProveedorRepository;
    private final CategoriaRepository categoriaRepository;
    private final ColeccionRepository coleccionRepository;

    public LogisticaService(EnvioRepository envioRepository,
                            ProductoRepository productoRepository,
                            ProveedorRepository proveedorRepository,
                            PedidoProveedorRepository pedidoProveedorRepository,
                            CategoriaRepository categoriaRepository,
                            ColeccionRepository coleccionRepository) {
        this.envioRepository = envioRepository;
        this.productoRepository = productoRepository;
        this.proveedorRepository = proveedorRepository;
        this.pedidoProveedorRepository = pedidoProveedorRepository;
        this.categoriaRepository = categoriaRepository;
        this.coleccionRepository = coleccionRepository;
    }

    // ---------- 1. Despachos de pedidos web ----------

    public List<PedidoLogisticaVista> pedidosPagadosPorTipo(TipoEnvio tipo) {
        return envioRepository.findByVentaEstadoOrderByCreadoEnDesc(EstadoVenta.PAGADO).stream()
                .filter(e -> e.getTipo() == tipo)
                .map(LogisticaService::aVista)
                .toList();
    }

    @Transactional
    public void avanzarEstado(Integer idEnvio, EstadoEnvio nuevoEstado, String courier, String codigoSeguimiento) {
        Envio envio = envioRepository.findById(idEnvio)
                .orElseThrow(() -> new RecursoNoEncontradoException("Envío no encontrado: " + idEnvio));

        if (envio.getVenta().getEstado() != EstadoVenta.PAGADO) {
            throw new NegocioException("Solo se pueden atender pedidos con pago confirmado.");
        }

        EstadoEnvio actual = envio.getEstado();
        boolean esDelivery = envio.getTipo() == TipoEnvio.DELIVERY;

        if (nuevoEstado == EstadoEnvio.EN_CAMINO) {
            if (!esDelivery || actual != EstadoEnvio.PREPARANDO) {
                throw new NegocioException("Transición no permitida hacia En Camino.");
            }
            envio.setEstado(EstadoEnvio.EN_CAMINO);
            envio.setCourier(limpio(courier) != null ? limpio(courier) : "HÖSÉG Express");
            envio.setCodigoSeguimiento(limpio(codigoSeguimiento));
            envio.setFechaDespacho(OffsetDateTime.now());
        } else if (nuevoEstado == EstadoEnvio.ENTREGADO) {
            boolean valido = (esDelivery && actual == EstadoEnvio.EN_CAMINO)
                    || (!esDelivery && actual == EstadoEnvio.PREPARANDO);
            if (!valido) {
                throw new NegocioException("Transición no permitida hacia Entregado.");
            }
            envio.setEstado(EstadoEnvio.ENTREGADO);
            envio.setFechaEntrega(OffsetDateTime.now());
        } else {
            throw new NegocioException("Estado de destino no válido para logística.");
        }

        envioRepository.save(envio);
    }

    // ---------- 2. Inventario y stock crítico ----------

    public List<Producto> inventarioActivo() {
        return productoRepository.findByActivoTrueOrderByNombreAscCodigoAsc();
    }

    @Transactional
    public void reponerStock(String codigoProducto, int cantidad) {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad a ingresar debe ser mayor a cero.");
        }
        int filas = productoRepository.sumarStockComercial(codigoProducto, cantidad);
        if (filas == 0) {
            throw new RecursoNoEncontradoException("Producto no encontrado: " + codigoProducto);
        }
    }

        @Transactional
    public void crearProducto(String codigo, String nombre, String categoria, String coleccion,
                              String talla, BigDecimal precio, int stockInicial, int stockMinimo,
                              TipoCompromiso tipoCompromiso) {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank()) {
            throw new NegocioException("El código y el nombre del producto son obligatorios.");
        }
        String codLimpio = codigo.trim().toUpperCase();
        if (productoRepository.existsById(codLimpio)) {
            throw new NegocioException("Ya existe un producto con el código " + codLimpio + ".");
        }
        if (precio == null || precio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NegocioException("El precio debe ser mayor a cero.");
        }
        Producto p = new Producto();
        p.setCodigo(codLimpio);
        p.setNombre(nombre.trim());
        p.setMarca("HÖSÉG");
        p.setCategoria(categoria == null || categoria.isBlank() ? "Prendas" : categoria.trim());
        p.setColeccion(limpio(coleccion));
        p.setTalla(limpio(talla));
        p.setPrecio(precio);
        p.setStockComercial(Math.max(0, stockInicial));
        p.setStockComprometido(0);
        p.setStockMinimo(Math.max(0, stockMinimo));
        p.setTipoCompromiso(tipoCompromiso);
        p.setVisibleWeb(true);
        p.setActivo(true);
        p.setDestacado(false);
        productoRepository.save(p);
    }

    /**
     * Edita los datos comerciales de una variante. No toca el código (clave referenciada por
     * ventas, donaciones y movimientos) ni el stock (eso es "reponer" u órdenes a talleres).
     * {@code aplica_triple_impacto} se recalcula sola en la base a partir del compromiso.
     */
    @Transactional
    public void editarProducto(String codigo, ProductoEdicionForm form) {
        Producto p = productoRepository.findById(codigo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + codigo));

        // categoria y coleccion son claves foráneas por nombre (tablas categoria y coleccion, 07)
        String categoria = form.getCategoria().trim();
        if (!categoriaRepository.existsById(categoria)) {
            throw new NegocioException("La categoría «" + categoria + "» no existe.");
        }
        String coleccion = limpio(form.getColeccion());
        if (coleccion != null && !coleccionRepository.existsById(coleccion)) {
            throw new NegocioException("La colección «" + coleccion + "» no existe.");
        }

        p.setNombre(form.getNombre().trim());
        p.setCategoria(categoria);
        p.setColeccion(coleccion);
        p.setTalla(limpio(form.getTalla()));
        p.setPrecio(form.getPrecio());
        p.setStockMinimo(form.getStockMinimo());
        p.setTipoCompromiso(form.getTipoCompromiso());
    }

    // ---------- 3. Proveedores y órdenes a talleres ----------

    public List<Proveedor> proveedoresActivos() {
        return proveedorRepository.findByActivoTrueOrderByNombreTallerAsc();
    }

    public List<PedidoProveedor> pedidosProveedores() {
        return pedidoProveedorRepository.todosConDetalle();
    }

    @Transactional
    public void registrarProveedor(String nombreTaller, String ruc, String contacto, String telefono) {
        if (nombreTaller == null || nombreTaller.isBlank()) {
            throw new NegocioException("Ingresa el nombre del taller o proveedor.");
        }
        Proveedor p = new Proveedor();
        p.setNombreTaller(nombreTaller.trim());
        p.setRuc(limpio(ruc));
        p.setContacto(limpio(contacto));
        p.setTelefono(limpio(telefono));
        p.setActivo(true);
        proveedorRepository.save(p);
    }

    @Transactional
    public void crearOrdenProveedor(Integer idProveedor, String codigoProducto, String descripcion, int cantidad, Integer idUsuario) {
        if (cantidad <= 0) {
            throw new NegocioException("La cantidad solicitada debe ser mayor a cero.");
        }
        Proveedor proveedor = proveedorRepository.findById(idProveedor)
                .orElseThrow(() -> new RecursoNoEncontradoException("Proveedor no encontrado: " + idProveedor));

        Producto producto = null;
        if (codigoProducto != null && !codigoProducto.isBlank()) {
            producto = productoRepository.findById(codigoProducto.trim())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Producto no encontrado: " + codigoProducto));
        }

        PedidoProveedor orden = new PedidoProveedor();
        orden.setProveedor(proveedor);
        orden.setProducto(producto);
        orden.setDescripcion(limpio(descripcion));
        orden.setCantidad(cantidad);
        orden.setEstado(EstadoPedidoProveedor.SOLICITADO);
        orden.setIdUsuario(idUsuario);
        pedidoProveedorRepository.save(orden);
    }

        @Transactional
    public void recibirOrdenProveedor(Integer idOrden) {
        PedidoProveedor orden = pedidoProveedorRepository.findById(idOrden)
                .orElseThrow(() -> new RecursoNoEncontradoException("Orden no encontrada: " + idOrden));
        if (orden.getEstado() != EstadoPedidoProveedor.SOLICITADO
                && orden.getEstado() != EstadoPedidoProveedor.EN_PROCESO) {
            throw new NegocioException("Solo se pueden recibir órdenes en estado Solicitado.");
        }
        orden.setEstado(EstadoPedidoProveedor.RECIBIDO);
        pedidoProveedorRepository.save(orden);

        if (orden.getProducto() != null) {
            productoRepository.sumarStockComercial(orden.getProducto().getCodigo(), orden.getCantidad());
        }
    }


    private static PedidoLogisticaVista aVista(Envio e) {
        List<LineaLogisticaVista> lineas = e.getVenta().getDetalles().stream()
                .map(d -> new LineaLogisticaVista(
                        d.getProducto().getCodigo(),
                        d.getProducto().getNombre(),
                        d.getProducto().getTallaMostrada(),
                        d.getCantidad()))
                .toList();

        String destino = e.getTipo() == TipoEnvio.DELIVERY
                ? String.join(", ",
                    java.util.stream.Stream.of(e.getDireccion(), e.getDistrito(), e.getProvincia())
                            .filter(s -> s != null && !s.isBlank()).toList())
                : "Recojo en tienda HÖSÉG";

        return new PedidoLogisticaVista(
                e.getId(),
                e.getVenta().getId(),
                e.getVenta().getCodigoComprobante(),
                e.getVenta().getFecha(),
                e.getTipo(),
                e.getEstado(),
                e.getNombreReceptor(),
                e.getTelefonoReceptor(),
                destino,
                e.getCourier(),
                e.getCodigoSeguimiento(),
                lineas
        );
    }

    private static String limpio(String v) {
        return v == null || v.isBlank() ? null : v.trim();
    }
}
