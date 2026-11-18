package com.pagoflex.usuarios.repository;

import com.pagoflex.usuarios.model.Usuario;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByOrderByIdUsuarioAsc();

    Optional<Usuario> findByCorreoIgnoreCase(String correo);

    Optional<Usuario> findByRut(String rut);

    @Query("select coalesce(max(u.idUsuario), 0) from Usuario u")
    Integer maximoId();
}
