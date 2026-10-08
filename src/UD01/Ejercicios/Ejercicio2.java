package UD01.Ejercicios;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Scanner;
import java.util.stream.Stream;

public class Ejercicio2 {

    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        System.out.print("Introduce la ruta de la carpeta a recorrer: ");
        String rutaInput = sc.nextLine();
        Path rutaCarpeta = Paths.get(rutaInput);

        // 1. Validación inicial: ¿Existe y es carpeta?
        if (Files.notExists(rutaCarpeta)) {
            System.out.println("Error: La ruta no existe.");
            return;
        }

        if (!Files.isDirectory(rutaCarpeta)) {
            System.out.println("Error: La ruta existe pero no es una carpeta.");
            return;
        }

        System.out.println("\n=== CONTENIDO DE '" + rutaCarpeta.getFileName() + "' Y SUBDIRECTORIOS ===\n");

        // 2. Usamos try-with-resources porque Stream debe cerrarse
        try (Stream<Path> stream = Files.walk(rutaCarpeta)) {

            // Opcional: Filtrar para no mostrar la carpeta raíz dos veces o dar formato
            stream.forEach(path -> {
                try {
                    // Leemos atributos para obtener info extra (tamaño, tipo)
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);

                    // Calculamos la "profundidad" (nivel de anidamiento)
                    // Restamos 1 porque la ruta base cuenta como nivel 0
                    int nivel = rutaCarpeta.relativize(path).getNameCount();

                    // Creamos una indentación visual basada en la profundidad
                    String indentacion = "  ".repeat(nivel);

                    String tipo = attrs.isDirectory() ? "[DIR]  " : "[FILE] ";
                    long tamaño = attrs.size();

                    // Formato condicional: si es carpeta no mostramos tamaño (o 0)
                    String infoTamaño = attrs.isDirectory() ? "" : "(" + tamaño + " bytes)";

                    System.out.println(indentacion + tipo + path.getFileName() + " " + infoTamaño);

                } catch (IOException e) {
                    // Si no podemos leer atributos de un archivo (ej: permisos), lo mostramos igual
                    System.out.println("  ".repeat(rutaCarpeta.relativize(path).getNameCount()) + "[ERROR] " + path.getFileName());
                }
            });

        } catch (IOException e) {
            System.out.println("Error al recorrer el directorio: " + e.getMessage());
        }

        sc.close();
    }
}
