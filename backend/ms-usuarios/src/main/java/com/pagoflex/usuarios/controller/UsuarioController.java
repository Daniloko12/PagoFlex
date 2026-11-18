package com.pagoflex.usuarios.controller;

import com.pagoflex.usuarios.dto.ActualizacionUsuario;
import com.pagoflex.usuarios.dto.CambioAvatar;
import com.pagoflex.usuarios.dto.CambioClave;
import com.pagoflex.usuarios.dto.Credenciales;
import com.pagoflex.usuarios.dto.RegistroUsuario;
import com.pagoflex.usuarios.model.Usuario;
import com.pagoflex.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "Usuarios", description = "Cuentas de clientes y administradores")
public class UsuarioController {

    private final UsuarioService servicio;

    public UsuarioController(UsuarioService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    @Operation(summary = "Lista todos los usuarios")
    public List<Usuario> listar() {
        return servicio.listar();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Busca un usuario por su id")
    public Usuario obtener(@PathVariable("id") Integer id) {
        return servicio.obtener(id);
    }

    @PostMapping("/login")
    @Operation(summary = "Inicia sesión con correo y contraseña")
    public Usuario iniciarSesion(@RequestBody Credenciales credenciales) {
        return servicio.iniciarSesion(credenciales);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Crea una cuenta nueva")
    public Usuario registrar(@RequestBody RegistroUsuario registro) {
        return servicio.registrar(registro);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualiza los datos de un usuario")
    public Usuario actualizar(@PathVariable("id") Integer id, @RequestBody ActualizacionUsuario actualizacion) {
        return servicio.actualizar(id, actualizacion);
    }

    @PutMapping("/{id}/avatar")
    @Operation(summary = "Cambia la foto de perfil (imagen en Base64)")
    public Usuario cambiarAvatar(@PathVariable("id") Integer id, @RequestBody CambioAvatar cambio) {
        return servicio.cambiarAvatar(id, cambio);
    }

    @PutMapping("/{id}/clave")
    @Operation(summary = "Cambia la contraseña; si viene la actual, se revisa antes")
    public Usuario cambiarClave(@PathVariable("id") Integer id, @RequestBody CambioClave cambio) {
        return servicio.cambiarClave(id, cambio);
    }
}
