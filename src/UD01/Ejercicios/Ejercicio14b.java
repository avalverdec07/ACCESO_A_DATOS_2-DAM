package UD01.Ejercicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class Ejercicio14b {
        public static void main(String[] args) {
            // 1. Definimos las rutas de entrada y salida
            Path rutaEntrada = Paths.get("entrada.txt");
            Path rutaSalida = Paths.get("salida.txt");

            // 2. Comprobamos que el fichero de entrada exista antes de intentar leerlo
            if (Files.notExists(rutaEntrada)) {
                System.out.println("Error: El fichero de origen '" + rutaEntrada.getFileName() + "' no existe.");
                System.out.println("Por favor, crea el fichero y vuelve a intentarlo.");
                return; // Salimos del programa
            }

            // 3. Abrimos los flujos con buffer indicando EXPLÍCITAMENTE la codificación UTF-8
            try (BufferedReader reader = Files.newBufferedReader(rutaEntrada, StandardCharsets.UTF_8);
                 BufferedWriter writer = Files.newBufferedWriter(rutaSalida,
                         StandardCharsets.UTF_8,
                         StandardOpenOption.CREATE,
                         StandardOpenOption.TRUNCATE_EXISTING)) {

                String lineaActual;
                int numeroLinea = 1;

                System.out.println("Procesando el fichero...");

                // 4. Leemos línea a línea hasta llegar al final del fichero
                while ((lineaActual = reader.readLine()) != null) {

                    // Convertimos la línea a mayúsculas
                    String lineaMayusculas = lineaActual.toUpperCase();

                    // Construimos el formato deseado: "1 - TEXTO" (puedes ajustar el guion o espacio a tu gusto)
                    String lineaFormateada = numeroLinea + " - " + lineaMayusculas;

                    // Escribimos en el fichero de salida
                    writer.write(lineaFormateada);
                    writer.newLine(); // No te olvides del salto de línea

                    numeroLinea++;
                }

                System.out.println("Éxito. Se ha generado '" + rutaSalida.getFileName() + "' con " + (numeroLinea - 1) + " líneas.");

            } catch (IOException e) {
                System.out.println("Ocurrió un error durante la lectura/escritura: " + e.getMessage());
            }
        }
    }
