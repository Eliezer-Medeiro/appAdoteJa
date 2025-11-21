package br.appAdoteJa.appAdoteJa.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Usuario;
import br.appAdoteJa.appAdoteJa.repository.AnimalRepository;
import br.appAdoteJa.appAdoteJa.repository.UsuarioRepository;
import br.appAdoteJa.appAdoteJa.service.CookieService;
import br.appAdoteJa.appAdoteJa.service.AnimalService;
import org.springframework.web.bind.annotation.RequestParam; 

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

@Controller
public class LoginController {
    
	@Autowired
	private UsuarioRepository ur;

    @Autowired 
    private CookieService cookieService; 
	
    @Autowired
    private AnimalRepository animalRepository;
    
    @Autowired // Serviço injetado para filtros
    private AnimalService animalService;
    
	@GetMapping("/login")
	public String login(Model model) {
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
		return "login";
	}
	
    // ============================
    // DASHBOARD (HOME) - MÉTODO ÚNICO E COMPLETO COM 4 FILTROS
    // ============================
	@GetMapping("/")
	public String dashboard(
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) String sexo,
            @RequestParam(required = false) String idade,
            Model model, 
            HttpServletRequest request
    ) throws UnsupportedEncodingException {
	    
	    String idUsuarioString = cookieService.getCookie(request, "usuarioId"); 
	    
	    Long idUsuarioLogado = 0L;
	    
	    // 1. VERIFICAÇÃO DE LOGIN E EXTRAÇÃO DO ID
	    if (idUsuarioString != null && !idUsuarioString.isEmpty()) {
	        try {
	            idUsuarioLogado = Long.parseLong(idUsuarioString);
	        } catch (NumberFormatException e) {
	            System.err.println("Erro ao converter ID do usuário de String para Long: " + idUsuarioString);
	            idUsuarioLogado = 0L;
	        }
	    }
        
        // 2. BUSCA O NOME (APENAS PARA EXIBIÇÃO)
        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
	    model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
	    
	    List<Animal> animais = animalService.filtrar(especie, sexo, idUsuarioLogado, idade);
	    
	    model.addAttribute("animais", animais);
	    return "home";
	}
	
	@PostMapping("/logar")
	public String loginUsuario(Usuario usuario, Model model, HttpServletResponse response) throws UnsupportedEncodingException {
		Usuario usuarioLogado = this.ur.login(usuario.getEmail(), usuario.getSenha());
		if(usuarioLogado != null){
			// Tempo de expiração do cookie (em segundos) - 100000 segundos ~ 27 horas
			cookieService.setCookie(response, "usuarioId", String.valueOf(usuarioLogado.getId()), 100000);
            cookieService.setCookie(response, "nomeUsuario", usuarioLogado.getNome(), 100000); 
            
			return "redirect:/";
		}
		
		model.addAttribute("erro", "E-mail ou Senha inválidos!");
        model.addAttribute("usuario", new Usuario());
		return "login";
	}
	
	@GetMapping("/sair")
	public String sair(HttpServletResponse response, Model model) throws UnsupportedEncodingException {
			// Expira os cookies
			cookieService.setCookie(response, "usuarioId", "", 0);
            cookieService.setCookie(response, "nomeUsuario", "", 0);
			model.addAttribute("usuario", new Usuario());
			return "login";
	}
	
	@GetMapping("/cadastro")
	public String cadastro(Model model) {
        model.addAttribute("usuario", new Usuario());
		return "cadastro";
	}
	
	@PostMapping("/cadastro")
	public String cadastroUsuario(@Valid Usuario usuario, BindingResult result, Model model, RedirectAttributes attributes) {

        Optional<Usuario> usuarioExistente = ur.findByEmail(usuario.getEmail());
        
        if (usuarioExistente.isPresent()) {
            model.addAttribute("usuario", usuario);
            model.addAttribute("erro_email", "Este e-mail já está em uso.");
            return "cadastro";
        }

		if(result.hasErrors()) {
            model.addAttribute("usuario", usuario);
			return "cadastro";
		}
        
		ur.save(usuario);

		return "redirect:/login";
	}
}
