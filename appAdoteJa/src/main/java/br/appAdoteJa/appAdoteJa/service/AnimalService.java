package br.appAdoteJa.appAdoteJa.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private FotoRepository fotoRepository;

    @Autowired
    private Cloudinary cloudinary;


    public Animal salvar(Animal animal) {
        return animalRepository.save(animal);
    }

    public Animal salvarEdicao(Animal animal) {
        return animalRepository.save(animal);
    }

    public Animal buscarPorId(Long id) {
        return animalRepository.findByIdWithFotos(id)
                .orElseThrow(() -> new RuntimeException("Animal não encontrado: " + id));
    }

    public List<Animal> listarPorDono(Long donoId) {
        return animalRepository.findByDonoId(donoId);
    }

    public List<Animal> filtrar(String especie, String sexo, Long donoId, String idade) {
        return animalRepository.filtrar(especie, sexo, donoId, idade);
    }

    public void salvarAnimalComFotos(Animal animal, List<MultipartFile> arquivosFotos) {

        Animal animalSalvo = animalRepository.save(animal);

        if (animalSalvo.getFotos() == null) {
            animalSalvo.setFotos(new ArrayList<>());
        }

        for (MultipartFile arquivo : arquivosFotos) {
            if (!arquivo.isEmpty()) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(
                        arquivo.getBytes(),
                        ObjectUtils.asMap(
                            "folder", "adoteja_animais"
                        )
                    );

                    String cloudinaryUrl = (String) uploadResult.get("secure_url");

                    Foto foto = new Foto(cloudinaryUrl);
                    
                    animalSalvo.adicionarFoto(foto); 

                } catch (IOException e) {
                    throw new RuntimeException("Erro ao processar arquivo: " + e.getMessage(), e);
                } catch (Exception e) {
                    throw new RuntimeException("Falha no upload do Cloudinary: " + e.getMessage(), e);
                }
            }
        }
        
        animalRepository.save(animalSalvo); 
    }

    public void adicionarFotos(Long animalId, List<MultipartFile> arquivosFotos) {

        Animal animal = buscarPorId(animalId);

        if (animal.getFotos() == null) {
            animal.setFotos(new ArrayList<>());
        }

        for (MultipartFile arquivo : arquivosFotos) {
            if (!arquivo.isEmpty()) {
                try {
                    Map uploadResult = cloudinary.uploader().upload(
                        arquivo.getBytes(),
                        ObjectUtils.asMap(
                            "folder", "adoteja_animais"
                        )
                    );

                    String cloudinaryUrl = (String) uploadResult.get("secure_url");

                    Foto foto = new Foto(cloudinaryUrl);
                    
                    animal.adicionarFoto(foto); 

                } catch (IOException | RuntimeException e) {
                    throw new RuntimeException("Erro ao adicionar fotos: " + e.getMessage(), e);
                }
            }
        }

        animalRepository.save(animal);
    }

    public List<Animal> listarPorStatus(String status) {
        return animalRepository.findByStatus(status);
    }

    public List<Animal> listarPorStatusExcluindoDono(String status, Long donoId) {
        return animalRepository.findByStatusAndDonoIdNot(status, donoId);
    }

    public void mudarStatus(Long id, String novoStatus) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Animal não encontrado: " + id));
        animal.setStatus(novoStatus);
        animalRepository.save(animal);
    }
}
