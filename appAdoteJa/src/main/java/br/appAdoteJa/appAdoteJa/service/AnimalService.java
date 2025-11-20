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

    private final AnimalRepository animalRepository;
    private final FotoRepository fotoRepository;
    private final Cloudinary cloudinary;

    public AnimalService(AnimalRepository animalRepository,
                         FotoRepository fotoRepository,
                         Cloudinary cloudinary) {

        this.animalRepository = animalRepository;
        this.fotoRepository = fotoRepository;
        this.cloudinary = cloudinary;
    }

    public Animal salvarAnimalComFotos(Animal animal, List<MultipartFile> fotosUpload) throws IOException {

        // 1. Salva o animal primeiro (gera ID)
        Animal animalSalvo = animalRepository.save(animal);

        // 2. Upload das fotos no Cloudinary
        if (fotosUpload != null) {
            for (MultipartFile file : fotosUpload) {

                if (file.isEmpty()) continue;

                // Upload
                Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                        "folder", "adoteja/" + animalSalvo.getId(),   // pasta organizada
                        "resource_type", "image"
                    )
                );

                // Pega a URL final da imagem
                String url = uploadResult.get("secure_url").toString();

                // 3. Cria objeto Foto
                Foto foto = new Foto();
                foto.setUrl(url);
                foto.setAnimal(animalSalvo);

                // 4. Salva no banco
                fotoRepository.save(foto);

                // 5. Adiciona na lista do animal
                animalSalvo.getFotos().add(foto);
            }
        }

        return animalSalvo;
    }

    // Buscar animal já com as fotos
    public Animal buscarPorId(Long id) {
        return animalRepository.findByIdWithFotos(id).orElse(null);
    }

    // Listar animais do usuário
    public List<Animal> listarPorDono(Long donoId) {
        return animalRepository.findByDonoId(donoId);
    }

    public List<Animal> filtrar(String especie, String sexo, String porte) {
        // Se não tiver nenhum filtro, retorna tudo
        if ((especie == null || especie.isEmpty()) &&
            (sexo == null || sexo.isEmpty()) &&
            (porte == null || porte.isEmpty())) {
    
            return animalRepository.findAll();
        }
    
        return animalRepository.filtrar(especie, sexo, porte);
    }

    public Animal salvarEdicao(Animal animal) {
        return animalRepository.save(animal);
    }

}
