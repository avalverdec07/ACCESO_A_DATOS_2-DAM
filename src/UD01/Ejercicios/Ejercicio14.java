package UD01.Ejercicios;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Scanner;

public class Ejercicio14 {

        private static final Path RUTA_FICHERO = Paths.get("notas_alumnos.dat");

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);

            if (Files.notExists(RUTA_FICHERO)) {
                System.out.println("El fichero no existe. Debes ejecutar el ejercicio anterior primero.");
                return;
            }

            int opcion = 0;
            do {
                System.out.println("\n--- GESTIÓN DE NOTAS (BINARIO) ---");
                System.out.println("1. Consultar alumno");
                System.out.println("2. Modificar nota");
                System.out.println("3. Borrar alumno");
                System.out.println("4. Salir");
                System.out.print("Opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                } catch (NumberFormatException e) {
                    opcion = 0;
                }

                switch (opcion) {
                    case 1:
                        System.out.print("Expediente a consultar: ");
                        consultar(Integer.parseInt(scanner.nextLine().trim()));
                        break;
                    case 2:
                        System.out.print("Expediente a modificar: ");
                        int expModificar = Integer.parseInt(scanner.nextLine().trim());
                        System.out.print("Nueva nota: ");
                        double nuevaNota = Double.parseDouble(scanner.nextLine().trim());
                        procesarConFicheroTemporal(expModificar, nuevaNota, false);
                        break;
                    case 3:
                        System.out.print("Expediente a borrar: ");
                        int expBorrar = Integer.parseInt(scanner.nextLine().trim());
                        procesarConFicheroTemporal(expBorrar, null, true);
                        break;
                    case 4:
                        System.out.println("Saliendo...");
                        break;
                    default:
                        System.out.println("Opción inválida.");
                }
            } while (opcion != 4);

            scanner.close();
        }

        // --- MÉTODOS CRUD ---

        private static void consultar(int expedienteBuscado) {
            boolean encontrado = false;
            try (InputStream is = Files.newInputStream(RUTA_FICHERO);
                 DataInputStream dis = new DataInputStream(is)) {

                while (true) {
                    // Leemos los datos en el MISMO ORDEN en que se escribieron
                    int expediente = dis.readInt();
                    double nota = dis.readDouble();
                    String nombre = leerNombre(dis);

                    if (expediente == expedienteBuscado) {
                        System.out.println("ALUMNO ENCONTRADO:");
                        System.out.println("Nombre: " + nombre + " | Nota: " + nota);
                        encontrado = true;
                        break;
                    }
                }
            } catch (EOFException e) {
                // EOFException es el comportamiento normal al terminar un fichero binario
                if (!encontrado) System.out.println("ℹNo se encontró el expediente.");
            } catch (IOException e) {
                System.out.println("Error de lectura: " + e.getMessage());
            }
        }

        private static void procesarConFicheroTemporal(int expObjetivo, Double nuevaNota, boolean esBorrado) {
            Path rutaTemporal = null;
            boolean modificado = false;

            try {
                Path directorioBase = RUTA_FICHERO.toAbsolutePath().getParent();
                rutaTemporal = Files.createTempFile(directorioBase, "temp_notas_", ".dat");

                try (InputStream is = Files.newInputStream(RUTA_FICHERO);
                     DataInputStream dis = new DataInputStream(is);
                     OutputStream os = Files.newOutputStream(rutaTemporal);
                     DataOutputStream dos = new DataOutputStream(os)) {

                    while (true) {
                        int expediente = dis.readInt();
                        double nota = dis.readDouble();
                        String nombre = leerNombre(dis);

                        if (expediente == expObjetivo) {
                            modificado = true;
                            if (esBorrado) {
                                continue; // Skip: no escribimos nada en el temporal
                            } else {
                                // Modificar: escribimos el mismo exp/nombre, pero nota nueva
                                escribirRegistro(dos, expediente, nuevaNota, nombre);
                            }
                        } else {
                            // Copiar tal cual
                            escribirRegistro(dos, expediente, nota, nombre);
                        }
                    }
                }
            } catch (EOFException e) {
                // Fin de fichero alcanzado correctamente
                try {
                    if (modificado) {
                        Files.move(rutaTemporal, RUTA_FICHERO, StandardCopyOption.REPLACE_EXISTING);
                        System.out.println(esBorrado ? "Alumno borrado." : "Nota modificada.");
                    } else {
                        Files.deleteIfExists(rutaTemporal);
                        System.out.println("ℹNo se encontró el expediente. No se realizaron cambios.");
                    }
                } catch (IOException ex) {
                    System.out.println("Error al reemplazar el fichero: " + ex.getMessage());
                }
            } catch (IOException e) {
                System.out.println("Error de E/S: " + e.getMessage());
                if (rutaTemporal != null) {
                    try { Files.deleteIfExists(rutaTemporal); } catch (IOException ignored) {}
                }
            }
        }

        // --- MÉTODOS AUXILIARES ---

        /**
         * Lee byte a byte hasta encontrar el salto de línea '\n' y devuelve el String
         */
        private static String leerNombre(DataInputStream dis) throws IOException {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int b;
            while ((b = dis.read()) != -1) {
                if (b == '\n') break; // Encontramos el delimitador
                baos.write(b);
            }
            return new String(baos.toByteArray(), StandardCharsets.UTF_8);
        }

        /**
         * Centraliza la lógica de escritura de un registro completo
         */
        private static void escribirRegistro(DataOutputStream dos, int expediente, double nota, String nombre) throws IOException {
            dos.writeInt(expediente);
            dos.writeDouble(nota);
            dos.write(nombre.getBytes(StandardCharsets.UTF_8));
            dos.writeByte('\n');
        }
    }
}
