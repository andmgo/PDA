package gescazone.demo.presentation.controller;

import gescazone.demo.domain.model.RegistroParqueaderoVisitanteModel;
import gescazone.demo.application.service.RegistroParqueaderoVisitanteService;
import gescazone.demo.application.service.ParqueaderoService;
import gescazone.demo.application.service.ResidenteService;
import gescazone.demo.application.service.ApartamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/registro-parqueadero")
public class RegistroParqueaderoVisitanteController {

    @Autowired
    private RegistroParqueaderoVisitanteService registroService;
    
    @Autowired
    private ParqueaderoService parqueaderoService;
    
    @Autowired
    private ResidenteService residenteService;
    
    @Autowired
    private ApartamentoService apartamentoService;

    /**
     * Listar todos los registros
     */
    @GetMapping("/lista")
    public String consultarTodos(Model model) {
        model.addAttribute("registros", registroService.consultarTodos());
        return "registro-parqueadero/lista";
    }

    /**
     * Listar solo registros activos (ocupados)
     */
    @GetMapping("/activos")
    public String consultarActivos(Model model) {
        model.addAttribute("registros", registroService.consultarActivos());
        model.addAttribute("filtro", "Registros Activos");
        return "registro-parqueadero/lista";
    }

    /**
     * Mostrar formulario para registrar entrada
     */
    @GetMapping("/nuevo")
    public String mostrarFormularioCrear(Model model) {
        model.addAttribute("registro", new RegistroParqueaderoVisitanteModel());
        model.addAttribute("parqueaderos", parqueaderoService.consultarTodos());
        model.addAttribute("residentes", residenteService.consultarTodos());
        model.addAttribute("apartamentos", apartamentoService.consultarTodos());
        return "registro-parqueadero/formulario";
    }

    /**
     * Crear un nuevo registro
     */
    @PostMapping("/crear")
    public String crear(@ModelAttribute RegistroParqueaderoVisitanteModel registro,
                        @RequestParam String idResidente,
                        @RequestParam String idParqueadero,
                        @RequestParam(required = false) String idApartamento,
                        RedirectAttributes redirectAttributes) {
        try {
            String mensaje = registroService.crear(registro, idResidente, idParqueadero, idApartamento);
            redirectAttributes.addFlashAttribute("success", mensaje);
            return "redirect:/registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            redirectAttributes.addFlashAttribute("registro", registro);
            return "redirect:/registro-parqueadero/nuevo";
        }
    }

    /**
     * Mostrar formulario para editar registro
     */
    @GetMapping("/editar/{idResidente}/{idParqueadero}")
    public String mostrarFormularioEditar(@PathVariable String idResidente,
                                          @PathVariable String idParqueadero,
                                          Model model,
                                          RedirectAttributes redirectAttributes) {
        try {
            RegistroParqueaderoVisitanteModel registro = registroService.consultar(idResidente, idParqueadero);
            if (registro == null) {
                redirectAttributes.addFlashAttribute("error", "Registro no encontrado");
                return "redirect:/registro-parqueadero/lista";
            }
            model.addAttribute("registro", registro);
            model.addAttribute("parqueaderos", parqueaderoService.consultarTodos());
            model.addAttribute("residentes", residenteService.consultarTodos());
            model.addAttribute("apartamentos", apartamentoService.consultarTodos());
            return "registro-parqueadero/formulario";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }

    /**
     * Actualizar registro
     */
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute RegistroParqueaderoVisitanteModel registro,
                            @RequestParam String id,
                            RedirectAttributes redirectAttributes) {
        try {
            String mensaje = registroService.actualizar(id, registro);
            redirectAttributes.addFlashAttribute("success", mensaje);
            return "redirect:/registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/editar/" + id;
        }
    }

    @PostMapping("/registrar-salida")
    public String registrarSalida(@RequestParam String id,
                                @RequestParam String fechaHoraSalida,
                                RedirectAttributes redirectAttributes) {
        try {
            String mensaje = registroService.registrarSalida(id, fechaHoraSalida);
            redirectAttributes.addFlashAttribute("success", mensaje);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registro-parqueadero/lista";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable String id,
                        RedirectAttributes redirectAttributes) {
        try {
            String mensaje = registroService.eliminar(id);
            redirectAttributes.addFlashAttribute("success", mensaje);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registro-parqueadero/lista";
    }

    /**
     * Ver detalle de un registro
     */
    @GetMapping("/detalle/{idResidente}/{idParqueadero}")
    public String verDetalle(@PathVariable String idResidente,
                             @PathVariable String idParqueadero,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        try {
            RegistroParqueaderoVisitanteModel registro = registroService.consultar(idResidente, idParqueadero);
            if (registro == null) {
                redirectAttributes.addFlashAttribute("error", "Registro no encontrado");
                return "redirect:/registro-parqueadero/lista";
            }
            model.addAttribute("registro", registro);
            return "registro-parqueadero/detalle";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }

    /**
     * Consultar registros por ID de residente
     */
    @GetMapping("/residente/{idResidente}")
    public String consultarPorResidente(@PathVariable String idResidente,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarPorResidente(idResidente));
            model.addAttribute("filtro", "Residente ID: " + idResidente);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }

    /**
     * Consultar registros por ID de parqueadero
     */
    @GetMapping("/parqueadero/{idParqueadero}")
    public String consultarPorParqueadero(@PathVariable String idParqueadero,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarPorParqueadero(idParqueadero));
            model.addAttribute("filtro", "Parqueadero ID: " + idParqueadero);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }
    
    /**
     * Consultar registros por ID de apartamento
     */
    @GetMapping("/apartamento/{idApartamento}")
    public String consultarPorApartamento(@PathVariable String idApartamento,
                                         Model model,
                                         RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarPorApartamento(idApartamento));
            model.addAttribute("filtro", "Apartamento ID: " + idApartamento);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }

    /**
     * Consultar registros por número de parqueadero
     */
    @GetMapping("/parqueadero/numero/{numero}")
    public String consultarPorNumeroParqueadero(@PathVariable String numero,
                                                Model model,
                                                RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarPorNumeroParqueadero(numero));
            model.addAttribute("filtro", "Parqueadero Número: " + numero);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }

    /**
     * Buscar por placa
     */
    @GetMapping("/placa/{placa}")
    public String buscarPorPlaca(@PathVariable String placa,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.buscarPorPlaca(placa));
            model.addAttribute("filtro", "Placa: " + placa);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }
    
    /**
     * Consultar visitantes activos por parqueadero
     */
    @GetMapping("/parqueadero/{idParqueadero}/activos")
    public String consultarActivosPorParqueadero(@PathVariable String idParqueadero,
                                                 Model model,
                                                 RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarActivosPorParqueadero(idParqueadero));
            model.addAttribute("filtro", "Visitantes activos en parqueadero ID: " + idParqueadero);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }
    
    /**
     * Consultar visitantes activos por apartamento
     */
    @GetMapping("/apartamento/{idApartamento}/activos")
    public String consultarActivosPorApartamento(@PathVariable String idApartamento,
                                                 Model model,
                                                 RedirectAttributes redirectAttributes) {
        try {
            model.addAttribute("registros", registroService.consultarActivosPorApartamento(idApartamento));
            model.addAttribute("filtro", "Visitantes activos del apartamento ID: " + idApartamento);
            return "registro-parqueadero/lista";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/registro-parqueadero/lista";
        }
    }
}