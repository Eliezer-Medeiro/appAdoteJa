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

	@Query("""
    SELECT a FROM Animal a
    WHERE a.status = 'Disponível'                                  
      AND (:donoId IS NULL OR a.dono.id <> :donoId) 
      AND (:especie IS NULL OR :especie = '' OR a.especie = :especie)
      AND (:sexo IS NULL OR :sexo = '' OR a.sexo = :sexo)
      AND (:idade IS NULL OR :idade = '' OR a.idade = :idade)
    """)
List<Animal> filtrar(
    @Param("especie") String especie,
    @Param("sexo") String sexo,
    @Param("donoId") Long donoId,
    @Param("idade") String idade // Idade é String
);
}
