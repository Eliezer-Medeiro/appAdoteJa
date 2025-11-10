package br.appAdoteJa.appAdoteJa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import br.appAdoteJa.appAdoteJa.model.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
    
    //Encontra todos os animais pelo status
    List<Animal> findByStatus(String status);

    //Encontra animais pelo Status E cujo ID do Dono seja DIFERENTE
    List<Animal> findByStatusAndDonoIdNot(String status, Long donoId);

    //Encontra animais pelo ID do Dono
    List<Animal> findByDonoId(Long donoId);
}