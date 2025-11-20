package br.appAdoteJa.appAdoteJa.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Foto;
import br.appAdoteJa.appAdoteJa.repository.AnimalRepository;
import br.appAdoteJa.appAdoteJa.repository.FotoRepository;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    public void adicionarFotos(Long animalId, List<String> novasFotos) {

        // Carrega o animal com suas fotos atuais
        Animal animal = animalRepository.findByIdWithFotos(animalId)
            .orElseThrow(() -> new RuntimeException("Animal não encontrado"));

        // Se ainda não tiver lista, cria uma
        if (animal.getFotos() == null) {
            animal.setFotos(new ArrayList<>());
        }

        // Adiciona as novas fotos na lista atual
        animal.getFotos().addAll(novasFotos);

        // Salva tudo no banco
        animalRepository.save(animal);
    }
}

