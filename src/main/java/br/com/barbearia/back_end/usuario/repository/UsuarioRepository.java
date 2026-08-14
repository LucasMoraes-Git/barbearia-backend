package br.com.barbearia.back_end.usuario.repository;

import br.com.barbearia.back_end.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTelefone(String telefone);
    Optional<Usuario> findByEmailIgnoreCase(String email);

}
