package com.pagoflex.usuarios.service;

import com.pagoflex.usuarios.client.MovimientosClient;
import com.pagoflex.usuarios.dto.ActualizacionUsuario;
import com.pagoflex.usuarios.dto.CambioAvatar;
import com.pagoflex.usuarios.dto.CambioClave;
import com.pagoflex.usuarios.dto.Credenciales;
import com.pagoflex.usuarios.dto.MovimientoDto;
import com.pagoflex.usuarios.dto.RegistroUsuario;
import com.pagoflex.usuarios.model.Usuario;
import com.pagoflex.usuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UsuarioService {

    private static final int TAMANO_MAXIMO_AVATAR = 2 * 1024 * 1024;

    private final UsuarioRepository repositorio;
    private final MovimientosClient movimientos;

    public UsuarioService(UsuarioRepository repositorio, MovimientosClient movimientos) {
        this.repositorio = repositorio;
        this.movimientos = movimientos;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repositorio.findAllByOrderByIdUsuarioAsc();
    }

    @Transactional(readOnly = true)
    public Usuario obtener(Integer id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe el usuario " + id));
    }

    @Transactional(readOnly = true)
    public Usuario iniciarSesion(Credenciales credenciales) {
        if (credenciales == null || credenciales.correo() == null || credenciales.clave() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Escribe tu correo y tu contraseña");
        }
        return repositorio.findByCorreoIgnoreCase(credenciales.correo().trim())
                .filter(usuario -> usuario.getClaveHash().equals(Claves.hashClave(usuario.getRut(), credenciales.clave())))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos"));
    }

    @Transactional
    public synchronized Usuario registrar(RegistroUsuario registro) {
        Usuario usuario = registro.usuario();
        validarDatos(usuario);
        if (!Claves.claveSegura(registro.clave())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple los 5 requisitos de seguridad");
        }
        usuario.setRut(Claves.normalizarRut(usuario.getRut()));
        usuario.setCorreo(usuario.getCorreo().trim());
        validarUnicos(usuario, null);
        if (usuario.getIdUsuario() == null || usuario.getIdUsuario() <= 0) {
            usuario.setIdUsuario(repositorio.maximoId() + 1);
        } else if (repositorio.existsById(usuario.getIdUsuario())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Ya existe un usuario con el id " + usuario.getIdUsuario());
        }
        if (usuario.getActivo() == null) {
            usuario.setActivo(true);
        }
        if (usuario.getFechaCreacion() == null) {
            usuario.setFechaCreacion(LocalDateTime.now());
        }
        usuario.setClaveHash(Claves.hashClave(usuario.getRut(), registro.clave()));
        Usuario guardado = repositorio.save(usuario);
        movimientos.registrar(conUsuario(registro.movimientos(), guardado.getIdUsuario()));
        return guardado;
    }

    @Transactional
    public Usuario actualizar(Integer id, ActualizacionUsuario actualizacion) {
        Usuario actual = obtener(id);
        Usuario datos = actualizacion.usuario();
        validarDatos(datos);
        datos.setRut(Claves.normalizarRut(datos.getRut()));
        datos.setCorreo(datos.getCorreo().trim());
        validarUnicos(datos, id);
        actual.setRol(datos.getRol());
        actual.setRut(datos.getRut());
        actual.setNombres(datos.getNombres());
        actual.setApellidos(datos.getApellidos());
        actual.setCorreo(datos.getCorreo());
        actual.setTelefono(datos.getTelefono());
        if (datos.getActivo() != null) {
            actual.setActivo(datos.getActivo());
        }
        actual.setIdComuna(datos.getIdComuna());
        actual.setFechaIncorporacion(datos.getFechaIncorporacion());
        actual.setAvisosActivos(datos.getAvisosActivos());
        actual.setDiasAnticipacion(datos.getDiasAnticipacion());
        if (datos.getAvatar() != null) {
            actual.setAvatar(datos.getAvatar());
        }
        Usuario guardado = repositorio.save(actual);
        movimientos.registrar(conUsuario(actualizacion.movimientos(), id));
        return guardado;
    }

    @Transactional
    public Usuario cambiarAvatar(Integer id, CambioAvatar cambio) {
        if (cambio == null || cambio.avatar() == null || cambio.avatar().length == 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Falta la foto");
        }
        if (cambio.avatar().length > TAMANO_MAXIMO_AVATAR) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "La foto pesa más de 2 MB");
        }
        Usuario usuario = obtener(id);
        usuario.setAvatar(cambio.avatar());
        return repositorio.save(usuario);
    }

    @Transactional
    public Usuario cambiarClave(Integer id, CambioClave cambio) {
        Usuario usuario = obtener(id);
        if (cambio.claveActual() != null) {
            if (!usuario.getClaveHash().equals(Claves.hashClave(usuario.getRut(), cambio.claveActual()))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "La contraseña actual no es correcta");
            }
            if (cambio.claveActual().equals(cambio.claveNueva())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "La contraseña nueva debe ser distinta a la actual");
            }
        }
        if (!Claves.claveSegura(cambio.claveNueva())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple los 5 requisitos de seguridad");
        }
        usuario.setClaveHash(Claves.hashClave(usuario.getRut(), cambio.claveNueva()));
        Usuario guardado = repositorio.save(usuario);
        movimientos.registrar(conUsuario(cambio.movimientos(), id));
        return guardado;
    }

    private void validarDatos(Usuario usuario) {
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Faltan los datos del usuario");
        }
        if (vacio(usuario.getRol()) || vacio(usuario.getRut()) || vacio(usuario.getNombres())
                || vacio(usuario.getApellidos()) || vacio(usuario.getCorreo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rol, RUT, nombres, apellidos y correo son obligatorios");
        }
        if (!usuario.getRol().equals("USUARIO_FINAL") && !usuario.getRol().equals("ADMINISTRADOR")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El rol debe ser USUARIO_FINAL o ADMINISTRADOR");
        }
    }

    private void validarUnicos(Usuario usuario, Integer idActual) {
        repositorio.findByCorreoIgnoreCase(usuario.getCorreo())
                .filter(otro -> !otro.getIdUsuario().equals(idActual))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese correo ya está registrado");
                });
        repositorio.findByRut(usuario.getRut())
                .filter(otro -> !otro.getIdUsuario().equals(idActual))
                .ifPresent(otro -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Ese RUT ya está registrado");
                });
    }

    private static boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    static List<MovimientoDto> conUsuario(List<MovimientoDto> lista, Integer idUsuario) {
        return lista == null ? List.of() : lista.stream().map(movimiento -> movimiento.conUsuario(idUsuario)).toList();
    }
}
