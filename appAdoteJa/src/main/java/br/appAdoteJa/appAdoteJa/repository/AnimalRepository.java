package br.appAdoteJa.appAdoteJa.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Importar @Query
import org.springframework.data.repository.query.Param; // Importar @Param

import br.appAdoteJa.appAdoteJa.model.Animal;

public interface AnimalRepository extends JpaRepository<Animal, Long> {
	
	//Encontra todos os animais pelo status
	List<Animal> findByStatus(String status);

	//Encontra animais pelo Status E cujo ID do Dono seja DIFERENTE (para a Home)
	List<Animal> findByStatusAndDonoIdNot(String status, Long donoId);
	// mostra o animal escolhido pelo ID e as suas fotos
	@Query("SELECT a FROM Animal a LEFT JOIN FETCH a.fotos f WHERE a.id = :id")
	Optional<Animal> findByIdWithFotos(@Param("id") Long id);

	@Query("SELECT DISTINCT a FROM Animal a LEFT JOIN FETCH a.fotos WHERE a.dono.id = :donoId")
	List<Animal> findByDonoId(@Param("donoId") Long donoId);
}
