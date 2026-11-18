package com.pagoflex.usuarios.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class Claves {

    public static final int LARGO_MINIMO = 8;
    public static final int LARGO_MAXIMO = 20;

    private Claves() {
    }

    public static String normalizarRut(String rut) {
        String limpio = rut.replace(".", "").replace("-", "").replace(" ", "").toUpperCase(Locale.ROOT);
        if (limpio.length() < 2) {
            return rut.trim();
        }
        String cuerpo = limpio.substring(0, limpio.length() - 1);
        char digitoVerificador = limpio.charAt(limpio.length() - 1);
        StringBuilder conPuntos = new StringBuilder();
        for (int i = 0; i < cuerpo.length(); i++) {
            if (i > 0 && (cuerpo.length() - i) % 3 == 0) {
                conPuntos.append('.');
            }
            conPuntos.append(cuerpo.charAt(i));
        }
        return conPuntos + "-" + digitoVerificador;
    }

    public static String hashClave(String rut, String clave) {
        return sha256(normalizarRut(rut) + ":" + clave);
    }

    public static String hashCodigo(Integer idUsuario, String codigo) {
        return sha256(idUsuario + ":" + codigo);
    }

    public static boolean claveSegura(String clave) {
        if (clave == null || clave.length() < LARGO_MINIMO || clave.length() > LARGO_MAXIMO) {
            return false;
        }
        boolean mayuscula = false;
        boolean minuscula = false;
        boolean numero = false;
        boolean especial = false;
        for (char caracter : clave.toCharArray()) {
            if (Character.isWhitespace(caracter)) {
                return false;
            }
            if (Character.isUpperCase(caracter)) {
                mayuscula = true;
            } else if (Character.isLowerCase(caracter)) {
                minuscula = true;
            } else if (Character.isDigit(caracter)) {
                numero = true;
            } else if (!Character.isLetterOrDigit(caracter)) {
                especial = true;
            }
        }
        return mayuscula && minuscula && numero && especial;
    }

    private static String sha256(String texto) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(texto.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 no está disponible", error);
        }
    }
}
