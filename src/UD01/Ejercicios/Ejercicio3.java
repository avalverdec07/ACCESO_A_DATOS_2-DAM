package UD01.Ejercicios;

import java.io.IOException;
import java.nio.file.*;
import java.util.Scanner;

public class Ejercicio3 {

    public static void main(String[] args) {
        // 1. Comprobar que se ha pasado el parámetro
        if (args.length == 0) {
            System.out.println("Uso: java BorrarFicheroSeguro <ruta_del_fichero>");
            System.out.println("Ejemplo: java BorrarFicheroSeguro datos.txt");
            return;
        }

        Path rutaFichero = Paths.get(args[0]);
        Scanner sc = new Scanner(System.in);

        System.out.println("=== GESTIÓN DE BORRADO DE FICHEROS ===");
        System.out.println("Fichero a borrar: " + rutaFichero.toAbsolutePath());

        // 2. Comprobación: ¿Existe el fichero?
        // Usamos notExists para manejar el caso de que no esté
        if (Files.notExists(rutaFichero)) {
            System.out.println(">> ERROR: El fichero NO existe. No se puede borrar.");
            sc.close();
            return;
        }

        // 3. Comprobación: ¿Es realmente un fichero y no una carpeta?
        // (Opcional pero recomendado para evitar accidentes graves)
        if (Files.isDirectory(rutaFichero)) {
            System.out.println(">> ERROR: La ruta corresponde a un DIRECTORIO.");
            System.out.println(">> Este programa solo borra ficheros individuales.");
            System.out.println(">> (Para borrar carpetas se requiere recursividad).");
            sc.close();
            return;
        }

        // 4. Pedir confirmación al usuario
        System.out.print(">> ¿Estás SEGURO de que quieres borrar este fichero? (S/N): ");
        String respuesta = sc.nextLine().trim().toUpperCase();

        if (respuesta.equals("S") || respuesta.equals("SI") || respuesta.equals("Y") || respuesta.equals("YES")) {
            try {
                // 5. Borrar el fichero
                // deleteIfExists() es seguro: borra si existe, no falla si ya se borró
                boolean borrado = Files.deleteIfExists(rutaFichero);

                if (borrado) {
                    System.out.println(">> ÉXITO: Fichero borrado correctamente.");
                } else {
                    // Este caso es raro si antes comprobamos exists(), pero posible por concurrencia
                    System.out.println(">> AVISO: El fichero desapareció antes de borrarlo (otro proceso?).");
                }

            } catch (IOException e) {
                // Captura errores de E/S (ej: archivo abierto por otro programa, permisos denegados)
                System.out.println(">> ERROR: No se ha podido borrar el fichero.");
                System.out.println(">> Detalle: " + e.getMessage());

                // Ayuda adicional según el tipo de error
                if (e instanceof AccessDeniedException) {
                    System.out.println(">> Sugerencia: Comprueba que el archivo no está abierto o en uso.");
                }
            }
        } else {
            System.out.println(">> Operación cancelada por el usuario. El fichero NO se ha borrado.");
        }

        sc.close();
    }
}
