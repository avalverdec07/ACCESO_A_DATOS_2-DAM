package UD01.Ejercicios;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.Scanner;

public class Ejercicio10 {
        // Definimos la ruta como constante. Usamos una ruta relativa simple.
        private static final Path RUTA_FICHERO = Paths.get("Telefonos.dat");

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            // Aseguramos que el archivo exista al arrancar el programa
            inicializarFichero();

            do {
                System.out.println("\n--- GESTIÓN DE TELÉFONOS ---");
                System.out.println("1. Consultar contacto (por número)");
                System.out.println("2. Añadir contacto");
                System.out.println("3. Modificar contacto");
                System.out.println("4. Borrar contacto");
                System.out.println("5. Salir");
                System.out.print("Elige una opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    opcion = 0; // Si mete una letra, forzamos que caiga en el default
                }

                switch (opcion) {
                    case 1: consultar(scanner); break;
                    case 2: anadir(scanner); break;
                    case 3: modificar(scanner); break;
                    case 4: borrar(scanner); break;
                    case 5: System.out.println("Saliendo del programa... ¡Hasta pronto!"); break;
                    default: System.out.println("Opción no válida. Por favor, elige un número del 1 al 5.");
                }
            } while (opcion != 5);

            scanner.close();
        }

        // --- MÉTODOS DE OPERACIÓN (CRUD) ---

        private static void consultar(Scanner scanner) {
            System.out.print("Introduce el número a consultar: ");
            String numeroBuscar = scanner.nextLine().trim();
            boolean encontrado = false;

            try {
                for (String linea : Files.readAllLines(RUTA_FICHERO)) {
                    String[] datos = linea.split(";");
                    // Comprobamos la longitud por seguridad para evitar NullPointer si hay líneas en blanco
                    if (datos.length >= 2 && datos[0].equals(numeroBuscar)) {
                        System.out.println("Contacto encontrado -> Nombre: " + datos[1]);
                        encontrado = true;
                        break;
                    }
                }
                if (!encontrado) {
                    System.out.println("ℹNo existe ningún contacto con el número " + numeroBuscar);
                }
            } catch (IOException e) {
                System.out.println("Error al leer el fichero: " + e.getMessage());
            }
        }

        private static void anadir(Scanner scanner) {
            System.out.print("Introduce el número de teléfono: ");
            String numero = scanner.nextLine().trim();

            if (existeNumero(numero)) {
                System.out.println("Error: El número " + numero + " ya existe en la agenda.");
                return;
            }

            System.out.print("Introduce el nombre del contacto: ");
            String nombre = scanner.nextLine().trim();
            String registro = numero + ";" + nombre + System.lineSeparator();

            try {
                // Añadimos CREATE por seguridad extra
                Files.writeString(RUTA_FICHERO, registro, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                System.out.println("Contacto añadido correctamente.");
            } catch (IOException e) {
                System.out.println("Error al guardar el contacto: " + e.getMessage());
            }
        }

        private static void modificar(Scanner scanner) {
            System.out.print("Introduce el número del contacto a modificar: ");
            String numeroModificar = scanner.nextLine().trim();

            if (!existeNumero(numeroModificar)) {
                System.out.println("Error: El número no existe en la agenda.");
                return;
            }

            System.out.print("Introduce el NUEVO nombre para este número: ");
            String nuevoNombre = scanner.nextLine().trim();
            String nuevaLinea = numeroModificar + ";" + nuevoNombre;

            procesarConFicheroTemporal(numeroModificar, nuevaLinea, false);
        }

        private static void borrar(Scanner scanner) {
            System.out.print("Introduce el número del contacto a borrar: ");
            String numeroBorrar = scanner.nextLine().trim();

            if (!existeNumero(numeroBorrar)) {
                System.out.println("Error: El número no existe en la agenda.");
                return;
            }

            procesarConFicheroTemporal(numeroBorrar, null, true);
        }

        // --- MÉTODOS AUXILIARES ---

        private static void inicializarFichero() {
            try {
                if (Files.notExists(RUTA_FICHERO)) {
                    Files.createFile(RUTA_FICHERO);
                }
            } catch (IOException e) {
                System.out.println("Error crítico: No se pudo crear el fichero inicial.");
            }
        }

        private static boolean existeNumero(String numero) {
            try {
                for (String linea : Files.readAllLines(RUTA_FICHERO)) {
                    String[] datos = linea.split(";");
                    if (datos.length >= 1 && datos[0].equals(numero)) {
                        return true;
                    }
                }
            } catch (IOException e) {
                // Ignoramos error y asumimos que no existe
            }
            return false;
        }

        private static void procesarConFicheroTemporal(String numeroObjetivo, String nuevaLinea, boolean esBorrado) {
            Path rutaTemporal = null;
            try {
                // CORRECCIÓN: toAbsolutePath() asegura que getParent() no devuelva null
                Path directorioBase = RUTA_FICHERO.toAbsolutePath().getParent();
                rutaTemporal = Files.createTempFile(directorioBase, "temp_agenda_", ".dat");

                try (BufferedReader reader = Files.newBufferedReader(RUTA_FICHERO);
                     BufferedWriter writer = Files.newBufferedWriter(rutaTemporal)) {

                    String lineaActual;
                    while ((lineaActual = reader.readLine()) != null) {
                        String[] datos = lineaActual.split(";");

                        if (datos.length >= 1 && datos[0].equals(numeroObjetivo)) {
                            if (esBorrado) {
                                continue; // Skip: no escribimos nada, borrando el registro
                            } else {
                                writer.write(nuevaLinea); // Modificamos
                                writer.newLine();
                            }
                        } else {
                            writer.write(lineaActual); // Copiamos el resto de líneas tal cual
                            writer.newLine();
                        }
                    }
                }

                // Reemplazamos el fichero viejo por el nuevo
                Files.move(rutaTemporal, RUTA_FICHERO, StandardCopyOption.REPLACE_EXISTING);

                if (esBorrado) {
                    System.out.println("Contacto borrado con éxito.");
                } else {
                    System.out.println("Contacto modificado con éxito.");
                }

            } catch (IOException e) {
                System.out.println("Ocurrió un error en la modificación/borrado: " + e.getMessage());
                if (rutaTemporal != null) {
                    try {
                        Files.deleteIfExists(rutaTemporal);
                    } catch (IOException ex)
                    {
                        System.err.println(e);
                    }
                }
            }
        }
}
