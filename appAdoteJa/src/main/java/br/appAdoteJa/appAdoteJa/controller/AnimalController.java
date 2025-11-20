package br.appAdoteJa.appAdoteJa.controller;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Usuario;
import br.appAdoteJa.appAdoteJa.service.AnimalService;
import br.appAdoteJa.appAdoteJa.service.CookieService;
import br.appAdoteJa.appAdoteJa.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.UnsupportedEncodingException;
import java.util.List;

@Controller
@RequestMapping("/animais")
public class AnimalController {

    @Autowired
    private AnimalService animalService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CookieService cookieService;


    // ============================
    // HOME COM FILTROS
    // ============================
    @GetMapping("/")
    public String listarAnimais(
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String porte,
            Model model,
            HttpServletRequest request
    ) {

        model.addAttribute("animais", animalService.filtrar(especie, sexo, porte));
        model.addAttribute("nome", request.getAttribute("nome"));

        return "home";
    }


    // ============================
    // FORMULÁRIO DE CADASTRO
    // ============================
    @GetMapping("/cadastro-animal")
    public String exibirFormularioCadastro(Animal animal, Model model, HttpServletRequest request)
            throws UnsupportedEncodingException {

        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
        model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");

        model.addAttribute("animal", animal);
        return "cadastro_animal";
    }


    // ============================
    // CADASTRAR ANIMAL + FOTOS
    // ============================
    @PostMapping("/cadastro-animal")
    public String cadastrarAnimal(
            @Valid @ModelAttribute Animal animal,
            BindingResult result,
            @RequestParam("fotosUpload") List<MultipartFile> fotosUpload,
            HttpServletRequest request,
            RedirectAttributes ra,
            Model model
    ) throws UnsupportedEncodingException {

        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
        model.addAttribute("nome", nomeUsuario);

        // Erros de validação
        if (result.hasErrors()) {
            return "cadastro_animal";
        }

        // Sessão
        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) {
            ra.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
            return "redirect:/login";
        }

        Long usuarioId = Long.parseLong(usuarioIdStr);
        Usuario dono = usuarioRepository.findById(usuarioId).orElse(null);

        if (dono == null) {
            ra.addFlashAttribute("erro", "Usuário não encontrado.");
            return "redirect:/login";
        }

        animal.setDono(dono);

        // ⚠️ Agora o método recebe List<MultipartFile>, não List<String>
        animalService.salvarAnimalComFotos(animal, fotosUpload);

        ra.addFlashAttribute("sucesso", "Animal cadastrado com sucesso!");
        return "redirect:/animais/meus-animais";
    }


    // ============================
    // LISTAR ANIMAIS DO USUÁRIO
    // ============================
    @GetMapping("/meus-animais")
    public String listarMeusAnimais(Model model, HttpServletRequest request, RedirectAttributes ra)
            throws UnsupportedEncodingException {

        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");

        model.addAttribute("nome", nomeUsuario);

        if (usuarioIdStr == null) {
            ra.addFlashAttribute("erro", "Faça login para ver seus animais.");
            return "redirect:/login";
        }

        Long donoId = Long.parseLong(usuarioIdStr);
        List<Animal> meusAnimais = animalService.listarPorDono(donoId);

        model.addAttribute("animais", meusAnimais);
        return "meus_animais";
    }


    // ============================
    // EDITAR ANIMAL
    // ============================
    @GetMapping("/editar/{id}")
    public String carregarPaginaEdicao(@PathVariable Long id, Model model, RedirectAttributes ra, HttpServletRequest request)
            throws UnsupportedEncodingException {

        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) {
            ra.addFlashAttribute("erro", "Sessão expirada.");
            return "redirect:/login";
        }

        Animal animal = animalService.buscarPorId(id);
        if (animal == null) {
            ra.addFlashAttribute("erro", "Animal não encontrado!");
            return "redirect:/animais/meus-animais";
        }

        model.addAttribute("animal", animal);
        return "editar_animal";
    }


    @PostMapping("/editar/{id}")
    public String salvarEdicao(
            @PathVariable Long id,
            @ModelAttribute Animal animalAtualizado,
            RedirectAttributes ra,
            HttpServletRequest request
    ) throws UnsupportedEncodingException {

        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        if (usuarioIdStr == null) {
            ra.addFlashAttribute("erro", "Sessão expirada.");
            return "redirect:/login";
        }

        Animal animal = animalService.buscarPorId(id);
        if (animal == null) {
            ra.addFlashAttribute("erro", "Animal não encontrado!");
            return "redirect:/animais/meus-animais";
        }

        // Atualiza campos
        animal.setNome(animalAtualizado.getNome());
        animal.setRaca(animalAtualizado.getRaca());
        animal.setEspecie(animalAtualizado.getEspecie());
        animal.setSexo(animalAtualizado.getSexo());
        animal.setPorte(animalAtualizado.getPorte());
        animal.setIdade(animalAtualizado.getIdade());
        animal.setDescricao(animalAtualizado.getDescricao());
        animal.setStatus(animalAtualizado.getStatus());

        animalService.salvarEdicao(animal);

        ra.addFlashAttribute("sucesso", "Animal atualizado!");
        return "redirect:/animais/meus-animais";
    }


    // ============================
    // DETALHES DO ANIMAL
    // ============================
    @GetMapping("/detalhes/{id}")
    public String detalhesAnimal(@PathVariable Long id, Model model, RedirectAttributes ra, HttpServletRequest request)
            throws UnsupportedEncodingException {

        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
        model.addAttribute("nome", nomeUsuario);

        Animal animal = animalService.buscarPorId(id);
        if (animal == null) {
            ra.addFlashAttribute("erro", "Animal não encontrado.");
            return "redirect:/";
        }

        model.addAttribute("animal", animal);
        return "detalhes_animal";
    }
}
