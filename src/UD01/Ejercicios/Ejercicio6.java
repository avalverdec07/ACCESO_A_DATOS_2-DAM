package UD01.Ejercicios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Ejercicio6 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            // 1. Pedimos el nombre del fichero
            System.out.print("Introduce la ruta o nombre del fichero (ej. personas.txt): ");
            String nombreFichero = scanner.nextLine().trim();

            Path ruta = Paths.get(nombreFichero);

            // Informamos al usuario
            if (Files.exists(ruta)) {
                System.out.println("ℹEl fichero ya existe. Se añadirán los nuevos registros al final.");
            } else {
                System.out.println("ℹEl fichero no existe. Se creará uno nuevo.");
            }

            System.out.println("\n--- ALTA DE PERSONAS ---");

            // Variable centinela para controlar el bucle de forma limpia
            boolean continuar = true;

            do {
                System.out.print("\nNombre (o 'salir' para terminar): ");
                String nombre = scanner.nextLine().trim();

                // Evaluamos si el usuario quiere terminar antes de pedir el resto de datos
                if (nombre.equalsIgnoreCase("salir")) {
                    continuar = false;
                } else {
                    System.out.print("Apellidos: ");
                    String apellidos = scanner.nextLine().trim();

                    System.out.print("Teléfono: ");
                    String telefono = scanner.nextLine().trim();

                    // Formateamos la línea con el delimitador ';'
                    String registro = nombre + ";" + apellidos + ";" + telefono + System.lineSeparator();

                    // Escribimos la persona en el fichero usando NIO.2
                    try {
                        Files.writeString(ruta, registro, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        System.out.println("Persona guardada correctamente.");
                    } catch (IOException e) {
                        System.out.println("Error al escribir en el fichero: " + e.getMessage());
                    }
                }

            } while (continuar); // La condición de parada es clara y transparente

            System.out.println("\nProceso finalizado. ¡Gracias!");
            scanner.close();
        }
    }
