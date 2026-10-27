package UD01.Ejercicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

public class Ejercicio08 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 1. Pedir los datos al usuario
            System.out.print("Introduce la ruta del fichero: ");
            Path rutaOriginal = Paths.get(scanner.nextLine().trim());

            if (Files.notExists(rutaOriginal)) {
                System.out.println("Error: El fichero original no existe.");
                scanner.close();
                return;
            }

            System.out.print("Introduce el número de línea que quieres BORRAR (1 para la primera): ");
            int numLineaBorrar = 0;
            try {
                numLineaBorrar = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Error: Debes introducir un número entero válido.");
                scanner.close();
                return;
            }

            Path rutaTemporal = null;

            // 2. Proceso de lectura/escritura con fichero temporal
            try {
                // Creamos el archivo temporal en el mismo directorio
                rutaTemporal = Files.createTempFile(rutaOriginal.toAbsolutePath().getParent(), "temp_borrado_", ".txt");

                // Abrimos los flujos de lectura y escritura usando NIO.2
                try (BufferedReader reader = Files.newBufferedReader(rutaOriginal);
                     BufferedWriter writer = Files.newBufferedWriter(rutaTemporal)) {

                    String lineaActual;
                    int contadorLinea = 1;
                    boolean lineaBorrableEncontrada = false;

                    // Leemos línea a línea hasta el final del fichero
                    while ((lineaActual = reader.readLine()) != null) {

                        if (contadorLinea == numLineaBorrar) {
                            // ¡MAGIA! Si es la línea a borrar, NO escribimos nada.
                            // Simplemente marcamos que la hemos encontrado.
                            lineaBorrableEncontrada = true;
                        } else {
                            // Si es cualquier otra línea, la copiamos al archivo temporal
                            writer.write(lineaActual);
                            writer.newLine();
                        }

                        contadorLinea++;
                    }

                    if (!lineaBorrableEncontrada) {
                        System.out.println("Aviso: El fichero tiene menos de " + numLineaBorrar + " líneas. No se borró nada.");
                    } else {
                        System.out.println("Línea " + numLineaBorrar + " eliminada correctamente en el fichero temporal.");
                    }
                }

                // 3. Reemplazar el archivo original por el temporal
                // (Si no se borró nada, se reemplazará por una copia exacta, lo cual también es seguro)
                Files.move(rutaTemporal, rutaOriginal, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("Fichero original actualizado con éxito.");

            } catch (IOException e) {
                System.out.println("Ocurrió un error en la entrada/salida: " + e.getMessage());

                // Bloque de seguridad: limpiamos la "basura" si algo falla
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
