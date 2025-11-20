package br.appAdoteJa.appAdoteJa.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import br.appAdoteJa.appAdoteJa.model.Animal;
import br.appAdoteJa.appAdoteJa.model.Foto;
import br.appAdoteJa.appAdoteJa.repository.AnimalRepository;

@Service
public class AnimalService {

    @Autowired
    private AnimalRepository animalRepository;

    // ============================
    // SALVAR
    // ============================
    public Animal salvar(Animal animal) {
        return animalRepository.save(animal);
    }

    // salvar edição
    public Animal salvarEdicao(Animal animal) {
        return animalRepository.save(animal);
    }

    // ============================
    // BUSCAR POR ID
    // ============================
    public Animal buscarPorId(Long id) {
        return animalRepository.findByIdWithFotos(id)
                .orElseThrow(() -> new RuntimeException("Animal não encontrado: " + id));
    }

    // ============================
    // LISTAR POR DONO
    // ============================
    public List<Animal> listarPorDono(Long donoId) {
        return animalRepository.findByDonoId(donoId);
    }

    // ============================
    // FILTRAR
    // ============================
    public List<Animal> filtrar(String especie, String sexo, String porte, Long donoId) {
        return animalRepository.filtrar(especie, sexo, porte, donoId);
    }

    // ============================
    // SALVAR ANIMAL COM MULTIPLAS FOTOS (CORRIGIDO)
    // ============================
    public void salvarAnimalComFotos(Animal animal, List<MultipartFile> arquivosFotos) {

        List<Foto> fotos = new ArrayList<>();

        for (MultipartFile arquivo : arquivosFotos) {
            if (!arquivo.isEmpty()) {

                // por enquanto salva apenas o nome do arquivo
                String caminho = "/uploads/" + arquivo.getOriginalFilename();

                Foto foto = new Foto(caminho);
                foto.setAnimal(animal);

                fotos.add(foto);
            }
        }

        animal.setFotos(fotos);

        animalRepository.save(animal);
    }

    // ============================
    // ADICIONAR FOTOS EM UM ANIMAL EXISTENTE
    // ============================
    public void adicionarFotos(Long animalId, List<MultipartFile> arquivosFotos) {

        Animal animal = buscarPorId(animalId);

        if (animal.getFotos() == null) {
            animal.setFotos(new ArrayList<>());
        }

        for (MultipartFile arquivo : arquivosFotos) {
            if (!arquivo.isEmpty()) {

                String caminho = "/uploads/" + arquivo.getOriginalFilename();

                Foto foto = new Foto(caminho);
                foto.setAnimal(animal);

                animal.getFotos().add(foto);
            }
        }

        animalRepository.save(animal);
    }

    // ============================
    // LISTAR POR STATUS
    // ============================
    public List<Animal> listarPorStatus(String status) {
        return animalRepository.findByStatus(status);
    }

    // ============================
    // HOME: status + excluir dono
    // ============================
    public List<Animal> listarPorStatusExcluindoDono(String status, Long donoId) {
        return animalRepository.findByStatusAndDonoIdNot(status, donoId);
    }
}
