package pe.edu.utp.hoseg.web.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.utp.hoseg.web.dto.CompraRegistrada;
import pe.edu.utp.hoseg.web.dto.DatosEnvioForm;
import pe.edu.utp.hoseg.web.dto.DetallePedido;
import pe.edu.utp.hoseg.web.dto.DonacionVista;
import pe.edu.utp.hoseg.web.dto.EnvioVista;
import pe.edu.utp.hoseg.web.dto.ImpactoCompra;
import pe.edu.utp.hoseg.web.dto.ItemCarrito;
import pe.edu.utp.hoseg.web.dto.LineaPedido;
import pe.edu.utp.hoseg.web.dto.ProblemaCarrito;
import pe.edu.utp.hoseg.web.dto.ResumenPedido;
import pe.edu.utp.hoseg.web.dto.UnidadesDonadas;
import pe.edu.utp.hoseg.web.model.Cliente;
import pe.edu.utp.hoseg.web.model.DetalleVenta;
import pe.edu.utp.hoseg.web.model.Donacion;
import pe.edu.utp.hoseg.web.model.Envio;
import pe.edu.utp.hoseg.web.model.EstadoDonacion;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.EstadoVenta;
import pe.edu.utp.hoseg.web.model.OrigenVenta;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.model.Venta;
import pe.edu.utp.hoseg.web.repository.ClienteRepository;
import pe.edu.utp.hoseg.web.repository.DonacionRepository;
import pe.edu.utp.hoseg.web.repository.EnvioRepository;
import pe.edu.utp.hoseg.web.repository.ProductoRepository;
import pe.edu.utp.hoseg.web.repository.VentaRepository;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Ventas web. La web CREA el pedido (PENDIENTE) y el escritorio lo confirma:
 * aquí nunca se descuenta stock ni se registran movimientos de inventario,
 * solo se valida que alcance (CLAUDE.md, regla 2).
 */
@Service
@Transactional(readOnly = true)
public class VentaService {

    private final VentaRepository ventaRepository;
    private final ProductoRepository productoRepository;
    private final ClienteRepository clienteRepository;
    private final DonacionRepository donacionRepository;
    private final EnvioRepository envioRepository;
    private final ImpactoService impactoService;
    private final String prefijoComprobante;
    private final String secuenciaComprobante;
    private final BigDecimal costoDelivery;

    public VentaService(VentaRepository ventaRepository,
                        ProductoRepository productoRepository,
                        ClienteRepository clienteRepository,
                        DonacionRepository donacionRepository,
                        EnvioRepository envioRepository,
                        ImpactoService impactoService,
                        @Value("${hoseg.comprobante.prefijo}") String prefijoComprobante,
                        @Value("${hoseg.comprobante.secuencia}") String secuenciaComprobante,
                        @Value("${hoseg.envio.costo-delivery}") BigDecimal costoDelivery) {
        this.ventaRepository = ventaRepository;
        this.productoRepository = productoRepository;
        this.clienteRepository = clienteRepository;
        this.donacionRepository = donacionRepository;
        this.envioRepository = envioRepository;
        this.impactoService = impactoService;
        this.prefijoComprobante = prefijoComprobante;
        this.secuenciaComprobante = secuenciaComprobante;
        this.costoDelivery = costoDelivery;
    }

    /** "Mis pedidos" (2.5, CU-13), del más reciente al más antiguo. */
    public List<ResumenPedido> pedidosDe(Integer idCliente) {
        List<Venta> ventas = ventaRepository.findByClienteIdOrderByFechaDesc(idCliente);
        if (ventas.isEmpty()) {
            return List.of();
        }
        Map<Integer, Envio> envios = envioRepository.findByVentaIdIn(ventas.stream().map(Venta::getId).toList()).stream()
                .collect(Collectors.toMap(e -> e.getVenta().getId(), Function.identity()));
        Map<Integer, List<UnidadesDonadas>> donadas = donacionRepository.delCliente(idCliente).stream()
                .collect(Collectors.groupingBy(UnidadesDonadas::idVenta));
        return ventas.stream().map(v -> {
            Envio envio = envios.get(v.getId());
            BigDecimal costo = envio == null ? BigDecimal.ZERO : envio.getCosto();
            return new ResumenPedido(v.getCodigoComprobante(), v.getFecha(), v.getEstado(), v.getTotal(), costo,
                    v.getTotal().add(costo),
                    ImpactoService.sumar(donadas.getOrDefault(v.getId(), List.of()), u -> true),
                    envio == null ? null : envio.getEstado());
        }).toList();
    }

    /**
     * "Detalle de pedido" (2.6, CU-13): líneas, envío y línea de tiempo de cada donación.
     *
     * @throws RecursoNoEncontradoException si el código no existe o es de otro cliente
     */
    public DetallePedido detallePedido(Integer idCliente, String codigoComprobante) {
        Venta venta = ventaRepository.findByCodigoComprobanteAndClienteId(codigoComprobante, idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Pedido inexistente: " + codigoComprobante));
        List<LineaPedido> lineas = venta.getDetalles().stream()
                .map(d -> new LineaPedido(d.getProducto().getCodigo(), d.getProducto().getNombre(),
                        d.getProducto().getTallaMostrada(), d.getCantidad(), d.getPrecioUnitario(), d.getSubtotal(),
                        d.getProducto().tieneCompromiso() ? d.getProducto().getTipoCompromiso() : null))
                .toList();
        EnvioVista envio = envioRepository.findByVentaId(venta.getId()).map(VentaService::aVista).orElse(null);
        BigDecimal costo = envio == null ? BigDecimal.ZERO : envio.costo();
        List<DonacionVista> donaciones = impactoService.donacionesDeVenta(venta.getId());
        long abrigos = donaciones.stream().filter(d -> d.tipo() == TipoCompromiso.ABRIGO).mapToLong(DonacionVista::cantidad).sum();
        long arboles = donaciones.stream().filter(d -> d.tipo() == TipoCompromiso.ARBOL).mapToLong(DonacionVista::cantidad).sum();
        return new DetallePedido(venta.getCodigoComprobante(), venta.getFecha(), venta.getEstado(), lineas,
                venta.getTotal(), envio, venta.getTotal().add(costo), donaciones,
                new ImpactoCompra(abrigos, arboles), venta.getEstado() == EstadoVenta.PAGADO);
    }

    private static EnvioVista aVista(Envio e) {
        return new EnvioVista(e.getTipo(), e.getEstado(), e.getNombreReceptor(), e.getTelefonoReceptor(),
                e.getDireccion(), e.getReferencia(), e.getDistrito(), e.getProvincia(), e.getDepartamento(),
                e.getCosto(), e.getCourier(), e.getCodigoSeguimiento(), e.getFechaDespacho(), e.getFechaEntrega());
    }

    /**
     * Problemas que impiden comprar, uno por línea (CU-15.1): producto que ya
     * no se vende o cantidad mayor al stock comercial. Vacío si se puede comprar.
     * La vista del carrito lo usa para avisar antes de llegar a confirmar.
     */
    public List<ProblemaCarrito> validarCarrito(List<ItemCarrito> items) {
        return validar(items, cargarProductos(items));
    }

    /** Costo del envío según el tipo; siempre lo calcula el servidor, nunca el formulario. */
    public BigDecimal costoEnvio(TipoEnvio tipo) {
        return tipo == TipoEnvio.DELIVERY ? costoDelivery : BigDecimal.ZERO;
    }

    /** Formulario de entrega precargado con los datos de la cuenta (1.5). */
    public DatosEnvioForm datosEnvioIniciales(Integer idCliente) {
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente inexistente: " + idCliente));
        DatosEnvioForm form = new DatosEnvioForm();
        form.setNombreReceptor(cliente.getNombreCompleto());
        form.setTelefonoReceptor(cliente.getTelefono());
        form.setDireccion(cliente.getDireccion());
        return form;
    }

    /**
     * Confirma la compra (CU-15) en una sola transacción: venta PENDIENTE con
     * código de la secuencia, sus líneas con el precio vigente en la base, una
     * donación EN_ESPERA por cada línea con compromiso y el envío PENDIENTE.
     * Si algo falla, no queda nada a medias. Vaciar el carrito le toca al
     * controlador después de que esto termine bien.
     *
     * @throws CarritoInvalidoException si alguna línea no se puede comprar
     */
    @Transactional
    public CompraRegistrada confirmarCompra(Integer idCliente, List<ItemCarrito> items, DatosEnvioForm datosEnvio) {
        if (idCliente == null) {
            throw new NegocioException("Inicia sesión para confirmar tu compra; tu carrito se conserva.");
        }
        Map<String, Producto> productos = cargarProductos(items);
        List<ProblemaCarrito> problemas = validar(items, productos);
        if (!problemas.isEmpty()) {
            throw new CarritoInvalidoException(problemas);
        }
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente inexistente: " + idCliente));

        Venta venta = new Venta();
        venta.setCodigoComprobante(nuevoCodigoComprobante());
        venta.setFecha(OffsetDateTime.now());
        venta.setCliente(cliente);
        venta.setOrigen(OrigenVenta.WEB);
        venta.setEstado(EstadoVenta.PENDIENTE);
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCarrito item : items) {
            Producto producto = productos.get(item.codigoProducto());
            DetalleVenta linea = new DetalleVenta();
            linea.setProducto(producto);
            linea.setCantidad(item.cantidad());
            linea.setPrecioUnitario(producto.getPrecio());
            linea.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(item.cantidad())));
            venta.agregarDetalle(linea);
            total = total.add(linea.getSubtotal());
        }
        venta.setTotal(total);
        ventaRepository.save(venta);   // IDENTITY: venta y líneas ya tienen id

        long abrigos = 0;
        long arboles = 0;
        for (DetalleVenta linea : venta.getDetalles()) {
            Producto producto = linea.getProducto();
            if (!producto.tieneCompromiso()) {
                continue;
            }
            Donacion donacion = new Donacion();
            donacion.setDetalleVenta(linea);
            donacion.setProducto(producto);
            donacion.setCantidad(linea.getCantidad());
            donacion.setTipo(producto.getTipoCompromiso());
            donacion.setEstado(EstadoDonacion.EN_ESPERA);
            donacionRepository.save(donacion);
            if (producto.getTipoCompromiso() == TipoCompromiso.ABRIGO) {
                abrigos += linea.getCantidad();
            } else {
                arboles += linea.getCantidad();
            }
        }

        Envio envio = nuevoEnvio(venta, datosEnvio);
        envioRepository.save(envio);

        return new CompraRegistrada(venta.getCodigoComprobante(), total, envio.getCosto(),
                total.add(envio.getCosto()), envio.getTipo(), new ImpactoCompra(abrigos, arboles));
    }

    private Envio nuevoEnvio(Venta venta, DatosEnvioForm datos) {
        Envio envio = new Envio();
        envio.setVenta(venta);
        envio.setTipo(datos.getTipo());
        envio.setEstado(EstadoEnvio.PENDIENTE);
        envio.setNombreReceptor(datos.getNombreReceptor().trim());
        envio.setTelefonoReceptor(limpio(datos.getTelefonoReceptor()));
        envio.setCosto(costoEnvio(datos.getTipo()));
        if (datos.esDelivery()) {
            envio.setDireccion(limpio(datos.getDireccion()));
            envio.setReferencia(limpio(datos.getReferencia()));
            envio.setDistrito(limpio(datos.getDistrito()));
            envio.setProvincia(limpio(datos.getProvincia()));
            envio.setDepartamento(limpio(datos.getDepartamento()));
        }
        return envio;
    }

    /** WEB-000110: el número lo da Postgres con nextval, nunca un MAX + 1. */
    private String nuevoCodigoComprobante() {
        long numero = ventaRepository.siguienteComprobante(secuenciaComprobante);
        return prefijoComprobante + String.format("%06d", numero);
    }

    private Map<String, Producto> cargarProductos(List<ItemCarrito> items) {
        List<String> codigos = items.stream().map(ItemCarrito::codigoProducto).toList();
        return productoRepository.findAllById(codigos).stream()
                .collect(Collectors.toMap(Producto::getCodigo, Function.identity()));
    }

    private static List<ProblemaCarrito> validar(List<ItemCarrito> items, Map<String, Producto> productos) {
        List<ProblemaCarrito> problemas = new ArrayList<>();
        if (items.isEmpty()) {
            problemas.add(new ProblemaCarrito(null, "Tu carrito está vacío."));
            return problemas;
        }
        for (ItemCarrito item : items) {
            Producto producto = productos.get(item.codigoProducto());
            String nombre = item.nombre() + " (" + item.talla() + ")";
            if (producto == null || !producto.isActivo() || !producto.isVisibleWeb()) {
                problemas.add(new ProblemaCarrito(item.codigoProducto(), nombre + " ya no está a la venta. Quítalo para continuar."));
            } else if (producto.getStockComercial() <= 0) {
                problemas.add(new ProblemaCarrito(item.codigoProducto(), nombre + " se agotó. Quítalo para continuar."));
            } else if (producto.getStockComercial() < item.cantidad()) {
                problemas.add(new ProblemaCarrito(item.codigoProducto(),
                        "Solo quedan " + producto.getStockComercial() + " unidades de " + nombre + "."));
            }
        }
        return problemas;
    }

    private static String limpio(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
