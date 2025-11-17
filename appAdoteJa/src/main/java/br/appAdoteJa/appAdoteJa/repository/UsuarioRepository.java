package br.appAdoteJa.appAdoteJa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import br.appAdoteJa.appAdoteJa.model.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    
    @Query(value="SELECT u FROM Usuario u WHERE u.email = :email AND u.senha = :senha")
    public Usuario login(@Param("email") String email, @Param("senha") String senha);

    Optional<Usuario> findByEmail(String email);
}
