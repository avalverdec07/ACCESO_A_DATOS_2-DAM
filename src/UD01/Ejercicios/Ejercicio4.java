package UD01.Ejercicios;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;

public class Ejercicio4 {
        public static void main(String[] args) {
            // Utilizamos Scanner para leer datos desde el teclado
            Scanner scanner = new Scanner(System.in);

            System.out.print("Introduce el nombre o la ruta del fichero de texto (ej. personas.txt): ");
            String rutaALeer = scanner.nextLine();

            // 1. Convertimos el String introducido por teclado en un objeto Path
            Path ruta = Paths.get(rutaALeer);

            // 2. Validamos si el fichero realmente existe antes de intentar leerlo
            if (Files.notExists(ruta)) {
                System.out.println("Error: El fichero no existe en la ruta especificada.");
                scanner.close();
                return; // Salimos del programa
            }

            // 3. Bloque try-catch porque la lectura de ficheros puede lanzar IOException
            try {
                // Files.readAllLines lee todas las líneas y las mete en una Lista de Strings
                List<String> lineas = Files.readAllLines(ruta);
                int contadorPersonas = 0;

                System.out.println("\n--- LISTADO DE PERSONAS ---");

                // Recorremos cada línea leída
                for (String linea : lineas) {
                    // Separamos la línea usando el delimitador ';'
                    String[] datos = linea.split(";");

                    // Comprobamos que la línea tiene exactamente 3 partes para evitar errores
                    if (datos.length >= 3) {
                        //.trim() elimina los espacios en blanco accidentales que el usuario haya podido dejar antes o después del punto y coma.
                        String nombre = datos[0].trim();
                        String apellidos = datos[1].trim();
                        String fechaNacimiento = datos[2].trim();

                        System.out.printf("Nombre: %-10s | Apellidos: %-20s | Fecha Nacimiento: %s%n",
                                nombre, apellidos, fechaNacimiento);
                        contadorPersonas++;
                    } else if (!linea.trim().isEmpty()){
                        // Si la línea no está vacía pero no tiene 3 partes, avisamos del mal formato
                        System.out.println("Aviso: Línea con formato incorrecto (" + linea + ")");
                    }
                }

                System.out.println("---------------------------");
                // Mostramos el recuento total
                System.out.println("Número total de personas: " + contadorPersonas);

            } catch (IOException e) {
                System.out.println("Ocurrió un error al intentar leer el fichero: " + e.getMessage());
            } finally {
                // Siempre es buena práctica cerrar el scanner
                scanner.close();
            }
        }
    }
