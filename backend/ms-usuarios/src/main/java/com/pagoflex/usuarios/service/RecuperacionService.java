package com.pagoflex.usuarios.service;

import com.pagoflex.usuarios.client.MovimientosClient;
import com.pagoflex.usuarios.dto.CodigoCreado;
import com.pagoflex.usuarios.dto.RecuperacionClave;
import com.pagoflex.usuarios.dto.ResultadoVerificacion;
import com.pagoflex.usuarios.dto.SolicitudCodigo;
import com.pagoflex.usuarios.dto.VerificacionCodigo;
import com.pagoflex.usuarios.model.CodigoRecuperacion;
import com.pagoflex.usuarios.model.Usuario;
import com.pagoflex.usuarios.repository.CodigoRecuperacionRepository;
import com.pagoflex.usuarios.repository.UsuarioRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RecuperacionService {

    public static final int MINUTOS_VIGENCIA = 10;
    public static final int INTENTOS_MAXIMOS = 3;

    private final CodigoRecuperacionRepository codigos;
    private final UsuarioRepository usuarios;
    private final MovimientosClient movimientos;

    public RecuperacionService(CodigoRecuperacionRepository codigos, UsuarioRepository usuarios, MovimientosClient movimientos) {
        this.codigos = codigos;
        this.usuarios = usuarios;
        this.movimientos = movimientos;
    }

    @Transactional
    public CodigoCreado solicitar(SolicitudCodigo solicitud) {
        Usuario usuario = cuentaActiva(solicitud.idUsuario());
        if (solicitud.codigo() == null || !solicitud.codigo().matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El código debe tener 6 números");
        }
        LocalDateTime ahora = solicitud.ahora() == null ? LocalDateTime.now() : solicitud.ahora();
        codigos.findByIdUsuarioAndUsadoFalse(usuario.getIdUsuario()).forEach(anterior -> anterior.setUsado(true));
        CodigoRecuperacion nuevo = new CodigoRecuperacion();
        nuevo.setIdUsuario(usuario.getIdUsuario());
        nuevo.setCodigoHash(Claves.hashCodigo(usuario.getIdUsuario(), solicitud.codigo()));
        nuevo.setFechaCreacion(ahora);
        nuevo.setFechaExpiracion(ahora.plusMinutes(MINUTOS_VIGENCIA));
        nuevo.setIntentos(0);
        nuevo.setUsado(false);
        CodigoRecuperacion guardado = codigos.save(nuevo);
        return new CodigoCreado(guardado.getIdCodigo(), guardado.getFechaExpiracion());
    }

    @Transactional
    public ResultadoVerificacion verificar(VerificacionCodigo verificacion) {
        LocalDateTime ahora = verificacion.ahora() == null ? LocalDateTime.now() : verificacion.ahora();
        return revisar(verificacion.idUsuario(), verificacion.codigo(), ahora);
    }

    @Transactional
    public ResultadoVerificacion recuperarClave(RecuperacionClave recuperacion) {
        LocalDateTime ahora = recuperacion.ahora() == null ? LocalDateTime.now() : recuperacion.ahora();
        ResultadoVerificacion resultado = revisar(recuperacion.idUsuario(), recuperacion.codigo(), ahora);
        if (!"CORRECTO".equals(resultado.resultado())) {
            return resultado;
        }
        Usuario usuario = cuentaActiva(recuperacion.idUsuario());
        if (!Claves.claveSegura(recuperacion.claveNueva())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La contraseña no cumple los 5 requisitos de seguridad");
        }
        String hashNuevo = Claves.hashClave(usuario.getRut(), recuperacion.claveNueva());
        if (hashNuevo.equals(usuario.getClaveHash())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esa ya es tu contraseña. Puedes ingresar con ella o elegir otra.");
        }
        CodigoRecuperacion codigo = vigente(usuario.getIdUsuario()).orElseThrow();
        codigo.setUsado(true);
        codigos.save(codigo);
        usuario.setClaveHash(hashNuevo);
        usuarios.save(usuario);
        movimientos.registrar(UsuarioService.conUsuario(recuperacion.movimientos(), usuario.getIdUsuario()));
        return resultado;
    }

    private ResultadoVerificacion revisar(Integer idUsuario, String ingresado, LocalDateTime ahora) {
        Optional<CodigoRecuperacion> encontrado = idUsuario == null ? Optional.empty() : vigente(idUsuario);
        if (encontrado.isEmpty()) {
            return new ResultadoVerificacion("SIN_CODIGO", 0);
        }
        CodigoRecuperacion codigo = encontrado.get();
        if (codigo.getIntentos() >= INTENTOS_MAXIMOS) {
            return new ResultadoVerificacion("SIN_INTENTOS", 0);
        }
        if (ahora.isAfter(codigo.getFechaExpiracion())) {
            return new ResultadoVerificacion("VENCIDO", 0);
        }
        String texto = ingresado == null ? "" : ingresado.trim();
        if (!Claves.hashCodigo(idUsuario, texto).equals(codigo.getCodigoHash())) {
            codigo.setIntentos(codigo.getIntentos() + 1);
            codigos.save(codigo);
            return new ResultadoVerificacion("INCORRECTO", INTENTOS_MAXIMOS - codigo.getIntentos());
        }
        return new ResultadoVerificacion("CORRECTO", INTENTOS_MAXIMOS - codigo.getIntentos());
    }

    private Optional<CodigoRecuperacion> vigente(Integer idUsuario) {
        return codigos.findFirstByIdUsuarioAndUsadoFalseOrderByIdCodigoDesc(idUsuario);
    }

    private Usuario cuentaActiva(Integer idUsuario) {
        return Optional.ofNullable(idUsuario)
                .flatMap(usuarios::findById)
                .filter(usuario -> Boolean.TRUE.equals(usuario.getActivo()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe una cuenta activa con ese id"));
    }
}
