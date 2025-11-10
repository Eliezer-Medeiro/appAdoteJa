package br.appAdoteJa.appAdoteJa.controller;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

@Controller
@RequestMapping("/animais")
public class AnimalController {
    
    @Value("${file.upload-dir}")
    private String uploadBaseDir;
    
    @Autowired
    private AnimalRepository animalRepository;
    
    @Autowired
    private UsuarioRepository usuarioRepository;
    
    @Autowired
    private CookieService cookieService;
    
    // Página de cadastro de novo animal
    @GetMapping("/cadastro-animal")
    public String exibirFormularioCadastro(Animal animal, Model model) {
        return "cadastro_animal";
    }
    
    @PostMapping("/cadastro-animal")
    public String cadastrarAnimal(@Valid Animal animal, BindingResult result,
            @RequestParam("fotosUpload") List<MultipartFile> files,
            HttpServletRequest request, RedirectAttributes attributes) throws UnsupportedEncodingException {
        
        // Validação do formulário
        if (result.hasErrors()) {
            return "cadastro_animal";
        }
        
        // Verifica se foi enviada pelo menos uma foto
        if (files.isEmpty() || files.stream().allMatch(MultipartFile::isEmpty)) {
            attributes.addFlashAttribute("erro", "É obrigatório enviar pelo menos uma foto do animal.");
            return "redirect:/animais/cadastro-animal";
        }
        
        // 1. Busca o ID do usuário logado via cookie
        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        
        if (usuarioIdStr == null || usuarioIdStr.isEmpty()) {
            attributes.addFlashAttribute("erro", "Sessão expirada. Faça login novamente.");
            return "redirect:/login";
        }
        
        try {
            // 2. Converte o ID e busca o usuário no banco
            Long donoId = Long.parseLong(usuarioIdStr);
            Usuario dono = usuarioRepository.findById(donoId).orElse(null);
            
            if (dono == null) {
                attributes.addFlashAttribute("erro", "Dono não encontrado. Faça login novamente.");
                return "redirect:/login";
            }
            
            // 3. Associa o dono ao animal
            animal.setDono(dono);
            
            // 4. Cria o diretório se não existir
            File diretorio = new File(uploadBaseDir, "animais");
            if (!diretorio.exists()) {
                diretorio.mkdirs();
            }
            
            // 5. Salva cada foto no disco e associa ao animal
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String nomeArquivo = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                    File destino = new File(diretorio, nomeArquivo);
                    file.transferTo(destino);
                    
                    // URL pública para exibir a imagem no navegador
                    String urlSalva = "/uploads/animais/" + nomeArquivo;
                    
                    Foto novaFoto = new Foto(urlSalva);
                    animal.adicionarFoto(novaFoto);
                }
            }
            
            // 6. Salva o animal e as fotos (cascata)
            animalRepository.save(animal);
            
            attributes.addFlashAttribute("sucesso", "Animal '" + animal.getNome() + "' e fotos cadastradas com sucesso!");
            
        } catch (NumberFormatException e) {
            attributes.addFlashAttribute("erro", "Erro de sessão. Faça login novamente.");
            return "redirect:/login";
            
        } catch (IOException e) {
            e.printStackTrace();
            attributes.addFlashAttribute("erro", "Erro ao salvar os arquivos de imagem.");
            return "redirect:/animais/cadastro-animal";
        }
        
        return "redirect:/animais/meus-animais";
    }
    
    // Lista apenas os animais do usuário logado
    @GetMapping("/meus-animais")
    public String listarMeusAnimais(Model model, HttpServletRequest request, RedirectAttributes attributes) throws UnsupportedEncodingException {
        
        String usuarioIdStr = cookieService.getCookie(request, "usuarioId");
        
        if (usuarioIdStr == null || usuarioIdStr.isEmpty()) {
            attributes.addFlashAttribute("erro", "Sessão expirada. Faça login para ver seus animais.");
            return "redirect:/login";
        }
        
        try {
            Long donoId = Long.parseLong(usuarioIdStr);
            List<Animal> meusAnimais = animalRepository.findByDonoId(donoId);
            
            model.addAttribute("animais", meusAnimais);
            
        } catch (NumberFormatException e) {
            attributes.addFlashAttribute("erro", "Erro de sessão. Faça login novamente.");
            return "redirect:/login";
        }
        
        return "meus_animais";
    }
    
    // Mostra os detalhes de um animal específico
    @GetMapping("/detalhes/{id}")
    public String detalhesAnimal(@PathVariable Long id, Model model, RedirectAttributes attributes) {
        
        Animal animal = animalRepository.findById(id).orElse(null);
        
        if (animal == null) {
            attributes.addFlashAttribute("erro", "Animal não encontrado.");
            return "redirect:/";
        }
        
        model.addAttribute("animal", animal);
        return "detalhes_animal";
    }
}
