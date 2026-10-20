package UD01.Ejercicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Ejercicio9 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 1. Pedir ficheros de entrada y salida
            System.out.print("Introduce la ruta del fichero ORIGINAL: ");
            Path rutaOriginal = Paths.get(scanner.nextLine().trim());

            if (Files.notExists(rutaOriginal)) {
                System.out.println("Error: El fichero original no existe.");
                scanner.close();
                return;
            }

            System.out.print("Introduce el nombre o ruta del fichero DESTINO a generar: ");
            Path rutaDestino = Paths.get(scanner.nextLine().trim());

            // 2. Procesamos el fichero
            // La opción TRUNCATE_EXISTING lo que hace es vaciar el fichero destino si existe antes de escribir.
            // Así nos aseguramos de no mezclar el texto con basura de ejecuciones anteriores.
            try (BufferedReader reader = Files.newBufferedReader(rutaOriginal);
                 BufferedWriter writer = Files.newBufferedWriter(rutaDestino,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                String lineaActual;

                // Leemos línea a línea
                while ((lineaActual = reader.readLine()) != null) {
                    // Llamamos a un metodo auxiliar que hace el intercambio
                    String lineaTransformada = invertirMayusculasMinusculas(lineaActual);

                    // Escribimos la línea transformada y un salto de línea
                    writer.write(lineaTransformada);
                    writer.newLine();
                }

                System.out.println("Fichero generado con éxito en: " + rutaDestino.toAbsolutePath());

            } catch (IOException e) {
                System.out.println("Ocurrió un error en la entrada/salida: " + e.getMessage());
            } finally {
                scanner.close();
            }
        }

        /**
         * Metodo auxiliar que recibe un String y devuelve otro con
         * las mayúsculas y minúsculas intercambiadas.
         */
        private static String invertirMayusculasMinusculas(String texto) {
            // Usamos StringBuilder porque es mucho más eficiente que concatenar Strings con '+'
            StringBuilder resultado = new StringBuilder();

            // Recorremos cada carácter del texto original
            for (char c : texto.toCharArray()) {
                if (Character.isUpperCase(c)) {
                    // Si es mayúscula, lo pasamos a minúscula
                    resultado.append(Character.toLowerCase(c));
                } else if (Character.isLowerCase(c)) {
                    // Si es minúscula, lo pasamos a mayúscula
                    resultado.append(Character.toUpperCase(c));
                } else {
                    // Si es un número, espacio o signo de puntuación, se queda igual
                    resultado.append(c);
                }
            }
            return resultado.toString();
        }
}
