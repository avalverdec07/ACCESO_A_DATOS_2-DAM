package UD01.Ejercicios;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Ejercicio19b {

        private static final String RUTA_FICHERO = "Contactos.dat";
        private static final int TAMANO_REGISTRO = 52; // 30 + 18 + 4
        private static final int LONGITUD_NOMBRE = 15;
        private static final int LONGITUD_TELEFONO = 9;

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            do {
                System.out.println("\n--- AGENDA DE CONTACTOS ---");
                System.out.println("1. Añadir contacto");
                System.out.println("2. Consultar contacto por posición");
                System.out.println("3. Incrementar llamadas de un contacto");
                System.out.println("4. Salir");
                System.out.print("Elige una opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                    switch (opcion) {
                        case 1: anadirContacto(scanner); break;
                        case 2: consultarPorPosicion(scanner); break;
                        case 3: incrementarLlamadas(scanner); break;
                        case 4: System.out.println("Saliendo..."); break;
                        default: System.out.println("Opción inválida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Introduce un número válido.");
                }
            } while (opcion != 4);
            scanner.close();
        }

        // --- 1. AÑADIR CONTACTO ---
        private static void anadirContacto(Scanner scanner) {
            try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "rw")) {
                System.out.print("Nombre (máx 15 caracteres): ");
                String nombre = scanner.nextLine().trim();

                System.out.print("Teléfono (máx 9 caracteres): ");
                String telefono = scanner.nextLine().trim();

                raf.seek(raf.length()); // Nos posicionamos al final del archivo

                escribirString(raf, nombre, LONGITUD_NOMBRE);      // 30 bytes
                escribirString(raf, telefono, LONGITUD_TELEFONO);  // 18 bytes
                raf.writeInt(0);                                   // 4 bytes (contador inicial a 0)

                long posicionInsertada = raf.length() / TAMANO_REGISTRO;
                System.out.println("Contacto guardado en la posición: " + posicionInsertada);
            } catch (IOException e) {
                System.out.println("Error al guardar el contacto.");
            }
        }

        // --- 2. CONSULTAR POR POSICIÓN ---
        private static void consultarPorPosicion(Scanner scanner) {
            try {
                System.out.print("Introduce la posición del contacto (1 en adelante): ");
                int posicion = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "r")) {
                    if (posicion <= 0 || posicion > (raf.length() / TAMANO_REGISTRO)) {
                        System.out.println("Posición inexistente.");
                        return;
                    }

                    raf.seek((posicion - 1) * TAMANO_REGISTRO); // Salto exacto al registro

                    String nombre = leerString(raf, LONGITUD_NOMBRE);
                    String telefono = leerString(raf, LONGITUD_TELEFONO);
                    int llamadas = raf.readInt();

                    System.out.println("\n--- DATOS DEL CONTACTO ---");
                    System.out.printf("Posición: %d | Nombre: %-15s | Teléfono: %-9s | Llamadas: %d%n",
                            posicion, nombre, telefono, llamadas);
                }
            } catch (Exception e) {
                System.out.println("Error al consultar el contacto.");
            }
        }

        // --- 3. INCREMENTAR LLAMADAS (SIN REESCRIBIR EL RESTO) ---
        private static void incrementarLlamadas(Scanner scanner) {
            try {
                System.out.print("Introduce la posición del contacto al que has llamado: ");
                int posicion = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "rw")) {
                    if (posicion <= 0 || posicion > (raf.length() / TAMANO_REGISTRO)) {
                        System.out.println("Posición inexistente.");
                        return;
                    }

                    // Cálculo del desplazamiento:
                    // Registros anteriores + Nombre(30 bytes) + Teléfono(18 bytes) = +48 bytes
                    long posicionContador = ((posicion - 1) * TAMANO_REGISTRO) + 48;

                    // 1. Nos posicionamos sobre el contador
                    raf.seek(posicionContador);

                    // 2. Leemos el valor actual (al leer un int, el puntero avanza 4 bytes automáticamente)
                    int llamadasActuales = raf.readInt();

                    // 3. Volvemos a retroceder esos 4 bytes para sobrescribir el valor
                    raf.seek(posicionContador);

                    // 4. Escribimos el valor incrementado
                    raf.writeInt(llamadasActuales + 1);

                    System.out.println("Llamada registrada. Total de llamadas ahora: " + (llamadasActuales + 1));
                }
            } catch (Exception e) {
                System.out.println("Error al actualizar las llamadas.");
            }
        }

        // ==========================================
        // MÉTODOS AUXILIARES LECTURA/ESCRITURA
        // ==========================================

        private static void escribirString(RandomAccessFile raf, String texto, int longitud) throws IOException {
            StringBuilder sb = new StringBuilder(texto != null ? texto : "");
            sb.setLength(longitud);
            raf.writeChars(sb.toString());
        }

        private static String leerString(RandomAccessFile raf, int longitud) throws IOException {
            char[] arrayChars = new char[longitud];
            for (int i = 0; i < longitud; i++) {
                arrayChars[i] = raf.readChar();
            }
            return new String(arrayChars).replace('\0', ' ').trim();
        }
 }
