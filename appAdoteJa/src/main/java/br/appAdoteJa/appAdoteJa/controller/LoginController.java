package br.appAdoteJa.appAdoteJa.controller;

import java.io.UnsupportedEncodingException;
import java.util.List;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Usuario;
import br.appAdoteJa.appAdoteJa.repository.AnimalRepository;
import br.appAdoteJa.appAdoteJa.repository.UsuarioRepository;
import br.appAdoteJa.appAdoteJa.service.CookieService;

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
    
	@GetMapping("/login")
	public String login(Model model) {
        // Garante que o objeto usuario está no modelo, mesmo em caso de erro de login
        if (!model.containsAttribute("usuario")) {
            model.addAttribute("usuario", new Usuario());
        }
		return "login";
	}
	
	@GetMapping("/")
	public String dashboard(Model model, HttpServletRequest request) throws UnsupportedEncodingException {
	    // Busca o ID do usuário (CRUCIAL: o nome do cookie no logar é "usuarioId")
	    String idUsuarioString = cookieService.getCookie(request, "usuarioId"); 
	    
	    Long idUsuarioLogado = 0L;
	    
	    // 1. VERIFICAÇÃO DE LOGIN E EXTRAÇÃO DO ID
	    if (idUsuarioString == null || idUsuarioString.isEmpty()) {
	        // Se o usuário não está logado, use o padrão 0L (que não deve existir no banco)
	        idUsuarioLogado = 0L;
	    } else {
	        try {
	            // Tenta converter o ID do cookie para Long
	            idUsuarioLogado = Long.parseLong(idUsuarioString);
	        } catch (NumberFormatException e) {
	            System.err.println("Erro ao converter ID do usuário de String para Long: " + idUsuarioString);
	            // Se der erro, usa 0L para garantir que o filtro exclua este ID.
	            idUsuarioLogado = 0L;
	        }
	    }
        
        // 2. BUSCA O NOME (APENAS PARA EXIBIÇÃO)
        String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
	    model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
	    
	    // 3. FILTRO: Busca animais com Status "Disponível" E ID do Dono diferente do ID logado
	    List<Animal> animais = animalRepository.findByStatusAndDonoIdNot("Disponível", idUsuarioLogado);
	    
	    model.addAttribute("animais", animais);
	    return "home";
	}
	
	@PostMapping("/logar")
	public String loginUsuario(Usuario usuario, Model model, HttpServletResponse response) throws UnsupportedEncodingException {
		// NOTA: Para que o filtro funcione, o método login deve retornar o Usuario
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
        // Garante que o objeto usuario está no modelo
        model.addAttribute("usuario", new Usuario());
		return "cadastro";
	}
	
	@PostMapping("/cadastro")
	public String cadastroUsuario(@Valid Usuario usuario, BindingResult result, Model model, RedirectAttributes attributes) {

		if(result.hasErrors()) {
            model.addAttribute("usuario", usuario);
			return "cadastro";
		}
        

		ur.save(usuario);

        
		return "redirect:/login";
	}
}