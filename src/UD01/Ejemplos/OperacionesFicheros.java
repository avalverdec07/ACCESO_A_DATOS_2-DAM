package UD01.Ejemplos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class OperacionesFicheros {
    static void main(String[] args) throws IOException {
        Path origen  = Path.of("catalogo/config.txt");
        Path copia   = Path.of("catalogo/config_copia.txt");
        Path destino = Path.of("catalogo/backup/config.txt");

        // Copiar (sobrescribiendo si ya existe)
        try {
            Files.copy(origen, copia, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Mover / renombrar
        Files.createDirectories(destino.getParent());
        Files.move(copia, destino, StandardCopyOption.REPLACE_EXISTING);

        // Borrar de forma segura
        boolean borrado = Files.deleteIfExists(Path.of("catalogo/fichero_temporal.tmp"));
        System.out.println("¿Se borró el fichero temporal? " + borrado);
    }
    }
