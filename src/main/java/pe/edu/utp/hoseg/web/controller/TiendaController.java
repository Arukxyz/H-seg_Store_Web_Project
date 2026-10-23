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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pe.edu.utp.hoseg.web.config.ClienteAutenticado;
import pe.edu.utp.hoseg.web.dto.DetalleProducto;
import pe.edu.utp.hoseg.web.dto.FiltroCatalogo;
import pe.edu.utp.hoseg.web.dto.OrdenCatalogo;
import pe.edu.utp.hoseg.web.dto.ResenaForm;
import pe.edu.utp.hoseg.web.dto.TarjetaProducto;
import pe.edu.utp.hoseg.web.model.TipoCompromiso;
import pe.edu.utp.hoseg.web.service.CatalogoService;
import pe.edu.utp.hoseg.web.service.NegocioException;
import pe.edu.utp.hoseg.web.service.ResenaService;

import java.util.List;

/** Tienda y detalle de producto con sus reseñas (bloque 1: CU-02, CU-03, CU-22). */
@Controller
public class TiendaController {

    private final CatalogoService catalogoService;
    private final ResenaService resenaService;

    public TiendaController(CatalogoService catalogoService, ResenaService resenaService) {
        this.catalogoService = catalogoService;
        this.resenaService = resenaService;
    }

    /** /tienda?q=&categoria=&coleccion=&impacto=&disponibles=&orden= (todos opcionales). */
    @GetMapping("/tienda")
    public String tienda(FiltroCatalogo filtro, Model model) {
        List<TarjetaProducto> productos = catalogoService.tarjetas(filtro);
        model.addAttribute("productos", productos);
        model.addAttribute("total", productos.size());
        model.addAttribute("filtro", filtro);
        model.addAttribute("categorias", catalogoService.categorias());
        model.addAttribute("colecciones", catalogoService.colecciones());
        model.addAttribute("compromisos", TipoCompromiso.values());
        model.addAttribute("ordenes", OrdenCatalogo.values());
        return "tienda/lista";
    }

    @GetMapping("/tienda/{codigo}")
    public String detalle(@PathVariable String codigo,
                          @AuthenticationPrincipal ClienteAutenticado cliente,
                          Model model) {
        DetalleProducto producto = catalogoService.detalle(codigo);
        prepararDetalle(producto, idDe(cliente), model);
        resenaService.formularioPara(idDe(cliente), producto.nombre())
                .ifPresent(form -> model.addAttribute("resenaForm", form));
        return "tienda/detalle";
    }

    /** Publica o edita la reseña del cliente (solo con una compra pagada del modelo). */
    @PostMapping("/tienda/{codigo}/resenas")
    public String resenar(@PathVariable String codigo,
                          @AuthenticationPrincipal ClienteAutenticado cliente,
                          @Valid @ModelAttribute("resenaForm") ResenaForm form,
                          BindingResult errores,
                          Model model,
                          RedirectAttributes flash) {
        if (errores.hasErrors()) {
            prepararDetalle(catalogoService.detalle(codigo), cliente.id(), model);
            return "tienda/detalle";
        }
        try {
            resenaService.guardar(cliente.id(), codigo, form);
            flash.addFlashAttribute("exito", "¡Gracias! Tu reseña ya está publicada.");
        } catch (NegocioException e) {
            flash.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/tienda/" + codigo + "#resenas";
    }

    private void prepararDetalle(DetalleProducto producto, Integer idCliente, Model model) {
        model.addAttribute("producto", producto);
        model.addAttribute("resumenResenas", resenaService.resumen(producto.nombre()));
        model.addAttribute("resenas", resenaService.publicadas(producto.nombre()));
        model.addAttribute("yaReseno", resenaService.yaReseno(idCliente, producto.nombre()));
    }

    private static Integer idDe(ClienteAutenticado cliente) {
        return cliente == null ? null : cliente.id();
    }
}
