package UD01.Ejemplos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GestionDirectorios {
    public static void main(String args[]){
    Path carpetaCatalogo = Path.of("catalogo");
    Path subcarpetaImagenes = carpetaCatalogo.resolve("imagenes");

        try {
        // Crea el directorio "catalogo" si no existe, y también los padres necesarios
        Files.createDirectories(subcarpetaImagenes);
        System.out.println("Directorios creados correctamente.");

        Path ficheroConfig = carpetaCatalogo.resolve("config.txt");
        if (!Files.exists(ficheroConfig)) {
            Files.createFile(ficheroConfig);
        }
    } catch (
    IOException e) {
        System.err.println("Error al crear la estructura de directorios: " + e.getMessage());
    }
}

}
