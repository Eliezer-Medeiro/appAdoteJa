package br.appAdoteJa.appAdoteJa.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import br.appAdoteJa.appAdoteJa.model.Usuario;

public interface UsuarioRepository extends CrudRepository<Usuario, Long> {

    @Query(value="SELECT * FROM usuario WHERE email = :email AND senha = :senha", nativeQuery = true)
    public Usuario login(@Param("email") String email, @Param("senha") String senha);

    Optional<Usuario> findByEmail(String email);
}
