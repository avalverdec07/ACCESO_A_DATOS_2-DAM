package UD01.Ejercicios;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Ejercicio13 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            Path rutaFichero = Paths.get("notas_alumnos.dat");

            // Usamos NIO.2 para crear el flujo de salida. Usamos CREATE y APPEND
            // para poder añadir varios alumnos si ejecutamos el programa varias veces.
            try (OutputStream os = Files.newOutputStream(rutaFichero, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                 DataOutputStream dos = new DataOutputStream(os)) {

                System.out.println("--- REGISTRO DE NOTAS ---");
                boolean continuar = true;

                do {
                    System.out.print("\nNúmero de expediente (entero): ");
                    int expediente = Integer.parseInt(scanner.nextLine().trim());

                    System.out.print("Nota del alumno (decimal con punto, ej. 8.5): ");
                    double nota = Double.parseDouble(scanner.nextLine().trim());

                    System.out.print("Nombre del alumno: ");
                    String nombre = scanner.nextLine().trim();

                    // 1. Escribimos el expediente (4 bytes)
                    dos.writeInt(expediente);

                    // 2. Escribimos la nota (8 bytes)
                    dos.writeDouble(nota);

                    // 3. Escribimos el nombre convertido a bytes (UTF-8)
                    dos.write(nombre.getBytes(StandardCharsets.UTF_8));

                    // 4. Escribimos el carácter '\n' como terminador del String (1 byte)
                    dos.writeByte('\n');

                    System.out.println("Alumno registrado correctamente.");

                    System.out.print("¿Desea añadir otro alumno? (s/n): ");
                    if (scanner.nextLine().trim().equalsIgnoreCase("n")) {
                        continuar = false;
                    }

                } while (continuar);

                System.out.println("\nFichero binario generado/actualizado en: " + rutaFichero.toAbsolutePath());

            } catch (IOException e) {
                System.out.println("Error de E/S al escribir en el fichero: " + e.getMessage());
            } catch (NumberFormatException e) {
                System.out.println("Error: Formato de número incorrecto. Proceso abortado.");
            } finally {
                scanner.close();
            }
        }
}
