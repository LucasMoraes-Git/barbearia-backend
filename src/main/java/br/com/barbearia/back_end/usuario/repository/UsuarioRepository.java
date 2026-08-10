package br.com.barbearia.back_end.usuario.repository;

import br.com.barbearia.back_end.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
}
