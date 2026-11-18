package com.pagoflex.pagos.config;

import java.util.List;
import javax.sql.DataSource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DatosIniciales implements CommandLineRunner {

    private static final List<String> TABLAS = List.of("pago", "tarjeta");

    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;

    public DatosIniciales(DataSource dataSource, JdbcTemplate jdbcTemplate) {
        this.dataSource = dataSource;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        Integer filas = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM tarjeta", Integer.class);
        if (filas == null || filas == 0) {
            cargar();
        }
    }

    @Transactional
    public void restablecer() {
        TABLAS.forEach(tabla -> jdbcTemplate.execute("DELETE FROM " + tabla));
        cargar();
    }

    private void cargar() {
        ResourceDatabasePopulator cargador = new ResourceDatabasePopulator(new ClassPathResource("datos.sql"));
        cargador.setSqlScriptEncoding("UTF-8");
        cargador.execute(dataSource);
    }
}
