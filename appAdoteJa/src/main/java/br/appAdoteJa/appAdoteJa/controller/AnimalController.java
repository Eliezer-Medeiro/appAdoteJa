package br.appAdoteJa.appAdoteJa.controller;

// Importações necessárias do Cloudinary
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import java.util.Map;

// Importações antigas (algumas não são mais necessárias)
import java.io.File; // Não é mais necessário para o upload
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Value; // Não é mais necessário
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Foto;
import br.appAdoteJa.appAdoteJa.model.Usuario;
import br.appAdoteJa.appAdoteJa.repository.AnimalRepository;
import br.appAdoteJa.appAdoteJa.repository.UsuarioRepository;
import br.appAdoteJa.appAdoteJa.service.CookieService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
@RequestMapping("/animais")
public class AnimalController {
	
	// NÃO PRECISAMOS MAIS DISSO:
 	// @Value("${file.upload-dir}")
 	// private String uploadBaseDir;
	
	@Autowired
	private Cloudinary cloudinary; // <-- ETAPA 1: INJETAMOS O CLOUDINARY
	
	@Autowired
	private AnimalRepository animalRepository;
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	@Autowired
	private CookieService cookieService;
	
	// Página de cadastro de novo animal
	@GetMapping("/cadastro-animal")
	public String exibirFormularioCadastro(Animal animal, Model model, HttpServletRequest request) throws UnsupportedEncodingException {
		// Adiciona o nome do usuário ao modelo para o cabeçalho
		String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
		model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
		model.addAttribute("animal", animal); // Garante que o objeto animal exista
		return "cadastro_animal";
	}
	
	@PostMapping("/cadastro-animal")
	public String cadastrarAnimal(
	        @Valid Animal animal, BindingResult result,
	        @RequestParam("fotosUpload") List<MultipartFile> files,
	        HttpServletRequest request, RedirectAttributes attributes, Model model)
			throws UnsupportedEncodingException {
	
	    String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
	    model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
	
	    // erros de validação do Animal
	    if (result.hasErrors()) {
	        model.addAttribute("animal", animal);
	        return "cadastro_animal";
	    }
	
	    // Filtrar apenas arquivos realmente enviados
	    List<MultipartFile> fotosValidas = files.stream()
	            .filter(f -> !f.isEmpty())
	            .toList();
	
	    if (fotosValidas.isEmpty()) {
	        attributes.addFlashAttribute("erro", "É obrigatório enviar pelo menos uma foto.");
	        return "redirect:/animais/cadastro-animal";
	    }
	
	    // Validar sessão
	    String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
	
	    if (usuarioIdStr == null || usuarioIdStr.isEmpty()) {
	        attributes.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
	        return "redirect:/login";
	    }
	
	    try {
	        Long donoId = Long.parseLong(usuarioIdStr);
	        Usuario dono = usuarioRepository.findById(donoId).orElse(null);
	
	        if (dono == null) {
	            attributes.addFlashAttribute("erro", "Dono não encontrado. Faça login novamente.");
	            return "redirect:/login";
	        }
	
	        // Associa o dono
	        animal.setDono(dono);
	
	        // Upload Cloudinary
	        for (MultipartFile file : fotosValidas) {
	            Map<?, ?> uploadResult = cloudinary.uploader().upload(
	                    file.getBytes(),
	                    ObjectUtils.emptyMap()
	            );
	
	            String urlSalva = (String) uploadResult.get("secure_url");
	            Foto novaFoto = new Foto(urlSalva);
	            animal.adicionarFoto(novaFoto);
	        }
	
	        // Salva Animal + Fotos
	        animalRepository.save(animal);
	
	        attributes.addFlashAttribute("sucesso",
	                "Animal '" + animal.getNome() + "' cadastrado com sucesso!");
	
	    } catch (IOException e) {
	        attributes.addFlashAttribute("erro", "Erro ao fazer upload das imagens.");
	        return "redirect:/animais/cadastro-animal";
	    }
	
	    return "redirect:/animais/meus-animais";
	}

	
	// Lista apenas os animais do usuário logado
	@GetMapping("/meus-animais")
	public String listarMeusAnimais(Model model, HttpServletRequest request, RedirectAttributes attributes) throws UnsupportedEncodingException {
	    
	    String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
	    String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
	    model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
	    
	    if (usuarioIdStr == null || usuarioIdStr.isEmpty()) {
	        attributes.addFlashAttribute("erro", "Sessão expirada. Faça login para ver seus animais.");
	        return "redirect:/login";
	    }
	    
	    try {
	        Long donoId = Long.parseLong(usuarioIdStr);
	        
	        // 1. Executa a consulta, que pode estar falhando devido ao JOIN FETCH
	        List<Animal> meusAnimais = animalRepository.findByDonoId(donoId);
	        
	        // 2. SOLUÇÃO: Itera sobre a lista para forçar o carregamento LAZY de 'fotos'
	        // Isso acessa a lista de fotos e dispara a query necessária enquanto a sessão está ativa.
	        for (Animal animal : meusAnimais) {
	            // Acessar o tamanho da coleção força a inicialização, evitando LazyInitializationException.
	            animal.getFotos().size(); 
	        }
	        
	        model.addAttribute("animais", meusAnimais);
	        
	    } catch (NumberFormatException e) {
	        attributes.addFlashAttribute("erro", "Erro de sessão. Faça login novamente.");
	        return "redirect:/login";
	    } catch (Exception e) {
	        // Captura o erro 500 de JPA/Hibernate e o loga.
	        e.printStackTrace(); 
	        attributes.addFlashAttribute("erro", "Erro ao carregar a lista de animais. Tente novamente.");
	        return "redirect:/"; // Redireciona para a home em caso de falha grave
	    }
	    
	    return "meus_animais";
	}

	@GetMapping("/editar/{id}")
	public String carregarPaginaEdicao(@PathVariable Long id, Model model, RedirectAttributes ra, HttpServletRequest request) throws UnsupportedEncodingException {
	
	    // Validar sessão
	    String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
	    if (usuarioIdStr == null) {
	        ra.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
	        return "redirect:/login";
	    }
	
	    Long usuarioId = Long.parseLong(usuarioIdStr);
	
	    Animal animal = animalRepository.findById(id)
	         .orElse(null);
	
	    if (animal == null) {
	        ra.addFlashAttribute("erro", "Animal não encontrado!");
	        return "redirect:/animais/meus-animais";
	    }
	
	    // Segurança EXTRA
	    if (!animal.getDono().getId().equals(usuarioId)) {
	        ra.addFlashAttribute("erro", "Você não tem permissão para editar este animal!");
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
	        HttpServletRequest request) throws UnsupportedEncodingException {
	
	    String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
	    if (usuarioIdStr == null) {
	        ra.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
	        return "redirect:/login";
	    }
	
	    Long usuarioId = Long.parseLong(usuarioIdStr);
	
	    Animal animal = animalRepository.findById(id)
	            .orElse(null);
	
	    if (animal == null) {
	        ra.addFlashAttribute("erro", "Animal não encontrado!");
	        return "redirect:/animais/meus-animais";
	    }
	
	    // Segurança
	    if (!animal.getDono().getId().equals(usuarioId)) {
	        ra.addFlashAttribute("erro", "Você não tem permissão para editar este animal!");
	        return "redirect:/animais/meus-animais";
	    }
	
	    // Atualizar campos
	    animal.setNome(animalAtualizado.getNome());
	    animal.setRaca(animalAtualizado.getRaca());
	    animal.setEspecie(animalAtualizado.getEspecie());
	    animal.setSexo(animalAtualizado.getSexo());
	    animal.setPorte(animalAtualizado.getPorte());
	    animal.setIdade(animalAtualizado.getIdade());
	    animal.setDescricao(animalAtualizado.getDescricao());
	    animal.setStatus(animalAtualizado.getStatus());
	
	    animalRepository.save(animal);
	
	    ra.addFlashAttribute("sucesso", "Animal atualizado com sucesso!");
	
	    return "redirect:/animais/meus-animais";
	}

	
	@PostMapping("/marcar-adotado/{id}")
	public String marcarComoAdotado(
	        @PathVariable Long id,
	        RedirectAttributes ra,
	        HttpServletRequest request) throws UnsupportedEncodingException {
	
	    String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
	    if (usuarioIdStr == null) {
	        ra.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
	        return "redirect:/login";
	    }
	
	    Long usuarioId = Long.parseLong(usuarioIdStr);
	
	    Animal animal = animalRepository.findById(id).orElse(null);
	
	    if (animal == null) {
	        ra.addFlashAttribute("erro", "Animal não encontrado!");
	        return "redirect:/animais/meus-animais";
	    }
	
	    // Segurança: verificar se o animal pertence ao usuário logado
	    if (!animal.getDono().getId().equals(usuarioId)) {
	        ra.addFlashAttribute("erro", "Você não tem permissão para alterar este animal!");
	        return "redirect:/animais/meus-animais";
	    }
	
	    animal.setStatus("Adotado");
	    animalRepository.save(animal);
	
	    ra.addFlashAttribute("sucesso", "Status atualizado para 'Adotado'!");
	
	    return "redirect:/animais/meus-animais";
	}
		
	
	// Mostra os detalhes de um animal específico
	@GetMapping("/detalhes/{id}")
	public String detalhesAnimal(@PathVariable Long id, Model model, RedirectAttributes attributes, HttpServletRequest request) throws UnsupportedEncodingException {
		
		String nomeUsuario = cookieService.getCookie(request, "nomeUsuario");
		model.addAttribute("nome", nomeUsuario != null ? nomeUsuario : "Visitante");
		
		// CORREÇÃO: Usamos findByIdWithFotos para forçar o carregamento das fotos na mesma consulta.
		Animal animal = animalRepository.findByIdWithFotos(id).orElse(null);
		
		if (animal == null) {
			attributes.addFlashAttribute("erro", "Animal não encontrado.");
			return "redirect:/";
		}
		
		model.addAttribute("animal", animal);
		return "detalhes_animal";
	}
}
