package UD01.Ejemplos;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class ExploradorFicheros {
    static void main(String[] args) throws IOException {
        Path raiz = Path.of("catalogo");

        // Listar solo el contenido directo (no recursivo)
        try (Stream<Path> listado = Files.list(raiz)) {
            listado.forEach(System.out::println);
        }

        // Recorrer TODO el árbol de subdirectorios (recursivo)
        try (Stream<Path> arbol = Files.walk(raiz)) {
            arbol.filter(Files::isRegularFile)
                    .forEach(p -> System.out.println("Fichero encontrado: " + p));
        }
    }
}
