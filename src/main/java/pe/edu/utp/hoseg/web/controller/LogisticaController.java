package pe.edu.utp.hoseg.web.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.AdministradorAutenticado;
import pe.edu.utp.hoseg.web.dto.ProductoEdicionForm;
import pe.edu.utp.hoseg.web.model.EstadoEnvio;
import pe.edu.utp.hoseg.web.model.Producto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.model.TipoEnvio;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.LogisticaService;
import pe.edu.utp.hoseg.web.service.NegocioException;

import java.util.List;

@Controller
@RequestMapping("/logistica")
public class LogisticaController {

    private final LogisticaService logisticaService;
    private final CatalogoService catalogoService;

    public LogisticaController(LogisticaService logisticaService, CatalogoService catalogoService) {
        this.logisticaService = logisticaService;
        this.catalogoService = catalogoService;
    }

       @GetMapping("/pedidos")
    public String pedidos(@RequestParam(defaultValue = "DELIVERY") String tab, Model model) {
        TipoEnvio tipoActivo = "RECOJO".equalsIgnoreCase(tab) ? TipoEnvio.RECOJO_TIENDA : TipoEnvio.DELIVERY;
        List<Producto> criticos = logisticaService.inventarioActivo().stream()
                .filter(p -> p.getStockComercial() <= p.getStockMinimo())
                .sorted(java.util.Comparator.comparingInt(Producto::getStockComercial))
                .toList();
        model.addAttribute("tab", tipoActivo == TipoEnvio.RECOJO_TIENDA ? "RECOJO" : "DELIVERY");
        model.addAttribute("pedidos", logisticaService.pedidosPagadosPorTipo(tipoActivo));
        model.addAttribute("productosCriticos", criticos);
        return "logistica/pedidos";
    }


    @PostMapping("/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Integer id,
                                @RequestParam EstadoEnvio estado,
                                @RequestParam(required = false) String courier,
                                @RequestParam(required = false) String codigoSeguimiento,
                                @RequestParam(defaultValue = "DELIVERY") String tab,
                                RedirectAttributes flash) {
        try {
            logisticaService.avanzarEstado(id, estado, courier, codigoSeguimiento);
            flash.addFlashAttribute("exito", "Estado del pedido actualizado a " + estado.getEtiqueta() + ".");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/pedidos?tab=" + tab;
    }

       @GetMapping("/inventario")
    public String inventario(Model model) {
        List<Producto> productos = logisticaService.inventarioActivo().stream()
                .sorted(java.util.Comparator.comparingInt(Producto::getStockComercial))
                .toList();
        List<Producto> criticos = productos.stream()
                .filter(p -> p.getStockComercial() <= p.getStockMinimo())
                .toList();
        model.addAttribute("productos", productos);
        model.addAttribute("productosCriticos", criticos);
        model.addAttribute("totalCriticos", criticos.size());
        // Opciones de los modales "Nuevo producto" y "Editar": categoria y coleccion son claves foráneas
        model.addAttribute("categorias", catalogoService.categorias());
        model.addAttribute("colecciones", catalogoService.colecciones());
        model.addAttribute("compromisos", TipoCompromiso.values());
        return "logistica/inventario";
    }

    /** Guarda el modal "Editar producto" del inventario (sin código ni stock). */
    @PostMapping("/inventario/{codigo}/editar")
    public String editarProducto(@PathVariable String codigo,
                                 @Valid @ModelAttribute ProductoEdicionForm form,
                                 BindingResult errores,
                                 RedirectAttributes flash) {
        if (errores.hasErrors()) {
            flash.addFlashAttribute("error", errores.getAllErrors().get(0).getDefaultMessage());
            return "redirect:/logistica/inventario";
        }
        try {
            logisticaService.editarProducto(codigo, form);
            flash.addFlashAttribute("exito", "Producto " + codigo + " actualizado.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/inventario";
    }


    @PostMapping("/inventario/{codigo}/reponer")
    public String reponerStock(@PathVariable String codigo,
                               @RequestParam int cantidad,
                               RedirectAttributes flash) {
        try {
            logisticaService.reponerStock(codigo, cantidad);
            flash.addFlashAttribute("exito", "Stock actualizado (+ " + cantidad + " unidades) para " + codigo + ".");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/inventario";
    }

        @GetMapping("/proveedores")
    public String proveedores(@RequestParam(required = false) String producto, Model model) {
        model.addAttribute("proveedores", logisticaService.proveedoresActivos());
        model.addAttribute("ordenes", logisticaService.pedidosProveedores());
        model.addAttribute("productos", logisticaService.inventarioActivo());
        model.addAttribute("productoPreseleccionado", producto);
        return "logistica/proveedores";
    }


    @PostMapping("/proveedores")
    public String crearProveedor(@RequestParam String nombreTaller,
                                 @RequestParam(required = false) String ruc,
                                 @RequestParam(required = false) String contacto,
                                 @RequestParam(required = false) String telefono,
                                 RedirectAttributes flash) {
        try {
            logisticaService.registrarProveedor(nombreTaller, ruc, contacto, telefono);
            flash.addFlashAttribute("exito", "Proveedor registrado correctamente.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }

    @PostMapping("/proveedores/ordenes")
    public String crearOrden(@AuthenticationPrincipal AdministradorAutenticado usuario,
                             @RequestParam Integer idProveedor,
                             @RequestParam(required = false) String codigoProducto,
                             @RequestParam(required = false) String descripcion,
                             @RequestParam int cantidad,
                             RedirectAttributes flash) {
        try {
            logisticaService.crearOrdenProveedor(idProveedor, codigoProducto, descripcion, cantidad, usuario.id());
            flash.addFlashAttribute("exito", "Orden de producción enviada al taller.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }

    @PostMapping("/proveedores/ordenes/{id}/recibir")
    public String recibirOrden(@PathVariable Integer id, RedirectAttributes flash) {
        try {
            logisticaService.recibirOrdenProveedor(id);
            flash.addFlashAttribute("exito", "Orden recibida e ingresada automáticamente al inventario.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/proveedores";
    }

        @PostMapping("/inventario/nuevo")
    public String crearProducto(@RequestParam String codigo,
                                @RequestParam String nombre,
                                @RequestParam String categoria,
                                @RequestParam(required = false) String coleccion,
                                @RequestParam(required = false) String talla,
                                @RequestParam java.math.BigDecimal precio,
                                @RequestParam(defaultValue = "0") int stockInicial,
                                @RequestParam(defaultValue = "3") int stockMinimo,
                                @RequestParam(required = false) pe.edu.utp.hoseg.web.model.TipoCompromiso tipoCompromiso,
                                RedirectAttributes flash) {
        try {
            logisticaService.crearProducto(codigo, nombre, categoria, coleccion, talla, precio, stockInicial, stockMinimo, tipoCompromiso);
            flash.addFlashAttribute("exito", "Producto " + codigo.toUpperCase() + " registrado en el catálogo e inventario.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/logistica/inventario";
    }

}
