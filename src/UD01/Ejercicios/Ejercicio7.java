package UD01.Ejercicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class Ejercicio7 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 1. Pedir los datos al usuario
            System.out.print("Introduce la ruta del fichero a modificar: ");
            Path rutaOriginal = Paths.get(scanner.nextLine().trim());

            if (Files.notExists(rutaOriginal)) {
                System.out.println("Error: El fichero original no existe.");
                scanner.close();
                return;
            }

            System.out.print("Introduce el número de línea que quieres modificar (1 para la primera): ");
            int numLineaModificar = 0;
            try {
                numLineaModificar = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número entero válido.");
                scanner.close();
                return;
            }

            System.out.print("Introduce el nuevo texto para la línea " + numLineaModificar + ": ");
            String nuevoTexto = scanner.nextLine().trim();

            Path rutaTemporal = null;

            // 2. Proceso de lectura/escritura con fichero temporal
            try {
                /*
                 * Files.createTempFile crea un archivo temporal real en el sistema.
                 * Le decimos que lo cree en el mismo directorio que el original para
                 * evitar problemas de permisos al moverlo después.
                 */
                rutaTemporal = Files.createTempFile(rutaOriginal.toAbsolutePath().getParent(), "temp_", ".txt");

                // Abrimos BufferedReader (para leer el original) y BufferedWriter (para escribir en el temp)
                try (BufferedReader reader = Files.newBufferedReader(rutaOriginal);
                     BufferedWriter writer = Files.newBufferedWriter(rutaTemporal)) {

                    String lineaActual;
                    int contadorLinea = 1;
                    boolean lineaEncontrada = false;

                    // Leemos línea a línea hasta el final del fichero
                    while ((lineaActual = reader.readLine()) != null) {

                        if (contadorLinea == numLineaModificar) {
                            // Si es la línea a modificar, escribimos el NUEVO texto
                            writer.write(nuevoTexto);
                            lineaEncontrada = true;
                        } else {
                            // Si no, copiamos la línea ORIGINAL tal cual
                            writer.write(lineaActual);
                        }

                        writer.newLine(); // Añadimos el salto de línea
                        contadorLinea++;
                    }

                    if (!lineaEncontrada) {
                        System.out.println("Aviso: El fichero tiene menos de " + numLineaModificar + " líneas. No se modificó nada.");
                    } else {
                        System.out.println("Línea " + numLineaModificar + " modificada correctamente en el fichero temporal.");
                    }
                }

                // 3. Reemplazar el archivo original por el temporal
                /*
                 * Files.move es atómico en muchos sistemas de archivos.
                 * REPLACE_EXISTING asegura que el archivo original se sobrescriba.
                 */
                Files.move(rutaTemporal, rutaOriginal, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Fichero original actualizado con éxito.");

            } catch (IOException e) {
                System.out.println("Error en la entrada/salida: " + e.getMessage());

                // Buena práctica: Si ocurre un error (ej. se va la luz o falla el disco en mitad del proceso),
                // intentamos borrar el archivo temporal basura que se haya quedado a medias.
                if (rutaTemporal != null) {
                    try {
                        Files.deleteIfExists(rutaTemporal);
                    } catch (IOException ex) {
                        System.err.println("No se pudo limpiar el fichero temporal.");
                    }
                }
            } finally {
                scanner.close();
            }
        }
}
