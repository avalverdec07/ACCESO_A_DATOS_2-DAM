package UD01.Ejercicios;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;
public class Ejercicio18 {

        private static final String RUTA = "AgendaRandom.dat";
        private static final int TAMAÑO_REGISTRO = 73; // 4 + 8 + 60 + 1
        private static final int LONGITUD_DESC = 30;   // 30 caracteres máximo

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            do {
                System.out.println("\n--- AGENDA (ACCESO ALEATORIO) ---");
                System.out.println("1. Ver agenda completa");
                System.out.println("2. Introducir cita");
                System.out.println("3. Modificar descripción");
                System.out.println("4. Desactivar cita (Borrado lógico)");
                System.out.println("5. Borrar cita (Físico con reordenación de IDs)");
                System.out.println("6. Buscar activas por fecha");
                System.out.println("7. Salir");
                System.out.print("Elige opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                    switch (opcion) {
                        case 1: verAgenda(); break;
                        case 2: introducirCita(scanner); break;
                        case 3: modificarDescripcion(scanner); break;
                        case 4: desactivarCita(scanner); break;
                        case 5: borrarFisicoReordenando(scanner); break;
                        case 6: buscarPorFecha(scanner); break;
                        case 7: System.out.println("Saliendo..."); break;
                        default: System.out.println("Opción inválida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, introduce un número.");
                }
            } while (opcion != 7);
            scanner.close();
        }

        // --- 1. VER AGENDA ---
        private static void verAgenda() {
            try (RandomAccessFile raf = new RandomAccessFile(RUTA, "r")) {
                long numRegistros = raf.length() / TAMAÑO_REGISTRO;
                System.out.println("\nTotal registros: " + numRegistros);

                for (int i = 0; i < numRegistros; i++) {
                    raf.seek(i * TAMAÑO_REGISTRO); // Nos posicionamos

                    int id = raf.readInt();
                    long fecha = raf.readLong();
                    String desc = leerString(raf, LONGITUD_DESC);
                    boolean activo = raf.readBoolean();

                    String estado = activo ? "[ACTIVA]" : "[INACTIVA]";
                    System.out.printf("ID: %d | Fecha: %d | Desc: %-30s | %s%n", id, fecha, desc, estado);
                }
            } catch (IOException e) {
                System.out.println("ℹLa agenda está vacía o no existe aún.");
            }
        }

        // --- 2. INTRODUCIR CITA ---
        private static void introducirCita(Scanner scanner) {
            try (RandomAccessFile raf = new RandomAccessFile(RUTA, "rw")) {
                // El nuevo ID es el número total de registros actuales + 1
                int id = (int) (raf.length() / TAMAÑO_REGISTRO) + 1;

                System.out.print("Introduce fecha (formato AAAAMMDD, ej. 20261027): ");
                long fecha = Long.parseLong(scanner.nextLine().trim());

                System.out.print("Introduce descripción (máx 30 caracteres): ");
                String desc = scanner.nextLine().trim();

                raf.seek(raf.length()); // Nos vamos al final del fichero

                raf.writeInt(id);                      // 4 bytes
                raf.writeLong(fecha);                  // 8 bytes
                escribirString(raf, desc, LONGITUD_DESC); // 60 bytes
                raf.writeBoolean(true);                // 1 byte (siempre activa al crear)

                System.out.println("Cita añadida con ID " + id);
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error al guardar la cita.");
            }
        }

        // --- 3. MODIFICAR DESCRIPCIÓN ---
        private static void modificarDescripcion(Scanner scanner) {
            try {
                System.out.print("ID de la cita a modificar: ");
                int id = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA, "rw")) {
                    if (id <= 0 || id > (raf.length() / TAMAÑO_REGISTRO)) {
                        System.out.println("ID inexistente.");
                        return;
                    }

                    System.out.print("Nueva descripción: ");
                    String nuevaDesc = scanner.nextLine().trim();

                    // Nos saltamos los registros anteriores + los 12 bytes de este registro (ID(4) y Fecha(8))
                    long posicion = ((id - 1) * TAMAÑO_REGISTRO) + 12;
                    raf.seek(posicion);

                    escribirString(raf, nuevaDesc, LONGITUD_DESC);
                    System.out.println("Descripción actualizada.");
                }
            } catch (Exception e) {
                System.out.println("Error al modificar.");
            }
        }

        // --- 4. DESACTIVAR CITA ---
        private static void desactivarCita(Scanner scanner) {
            try {
                System.out.print("ID de la cita a desactivar: ");
                int id = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA, "rw")) {
                    if (id <= 0 || id > (raf.length() / TAMAÑO_REGISTRO)) {
                        System.out.println("ID inexistente.");
                        return;
                    }

                    // Posición: Registros anteriores + ID(4) + Fecha(8) + Desc(60) = +72 bytes
                    long posicion = ((id - 1) * TAMAÑO_REGISTRO) + 72;
                    raf.seek(posicion);

                    raf.writeBoolean(false); // Ponemos a false
                    System.out.println("Cita " + id + " desactivada.");
                }
            } catch (Exception e) {
                System.out.println("Error al desactivar.");
            }
        }

        // --- 5. BORRAR Y REORDENAR (LA PARTE COMPLEJA) ---
        private static void borrarFisicoReordenando(Scanner scanner) {
            try {
                System.out.print("ID de la cita a borrar FÍSICAMENTE: ");
                int idBorrar = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA, "rw")) {
                    long numRegistros = raf.length() / TAMAÑO_REGISTRO;

                    if (idBorrar <= 0 || idBorrar > numRegistros) {
                        System.out.println("ID inexistente.");
                        return;
                    }

                    /*
                     * Bucle de compresión:
                     * Leemos el registro 'N+1', le restamos 1 a su ID y lo escribimos en 'N'.
                     * Vamos moviendo todo un hueco hacia atrás.
                     */
                    for (int i = idBorrar; i < numRegistros; i++) {
                        // 1. Leer el registro posterior
                        raf.seek(i * TAMAÑO_REGISTRO);
                        int idLeido = raf.readInt();
                        long fecha = raf.readLong();
                        String desc = leerString(raf, LONGITUD_DESC);
                        boolean activo = raf.readBoolean();

                        // 2. Escribirlo en la posición anterior, restando 1 al ID
                        raf.seek((i - 1) * TAMAÑO_REGISTRO);
                        raf.writeInt(idLeido - 1);
                        raf.writeLong(fecha);
                        escribirString(raf, desc, LONGITUD_DESC);
                        raf.writeBoolean(activo);
                    }

                    // 3. Truncamos (cortamos) el fichero para eliminar el último bloque sobrante
                    raf.setLength(raf.length() - TAMAÑO_REGISTRO);
                    System.out.println("Registro borrado. IDs reordenados y fichero comprimido.");
                }
            } catch (Exception e) {
                System.out.println("Error al borrar y reordenar.");
            }
        }

        // --- 6. BUSCAR ACTIVAS POR FECHA ---
        private static void buscarPorFecha(Scanner scanner) {
            try {
                System.out.print("Introduce fecha a buscar (ej. 20261027): ");
                long fechaBuscada = Long.parseLong(scanner.nextLine().trim());
                boolean hallado = false;

                try (RandomAccessFile raf = new RandomAccessFile(RUTA, "r")) {
                    long numRegistros = raf.length() / TAMAÑO_REGISTRO;

                    System.out.println("\nCitas activas encontradas:");
                    for (int i = 0; i < numRegistros; i++) {
                        raf.seek(i * TAMAÑO_REGISTRO);

                        int id = raf.readInt();
                        long fechaLeida = raf.readLong();
                        String desc = leerString(raf, LONGITUD_DESC);
                        boolean activo = raf.readBoolean();

                        if (fechaLeida == fechaBuscada && activo) {
                            System.out.printf("ID: %d | Desc: %s%n", id, desc);
                            hallado = true;
                        }
                    }

                    if (!hallado) System.out.println("No hay citas activas para esa fecha.");
                }
            } catch (Exception e) {
                System.out.println("Error en la búsqueda.");
            }
        }

        // ==========================================
        // MÉTODOS AUXILIARES PARA LEER/ESCRIBIR STRINGS
        // ==========================================

        private static void escribirString(RandomAccessFile raf, String texto, int longitud) throws IOException {
            StringBuilder sb = new StringBuilder(texto != null ? texto : "");
            // Truncamos o rellenamos de espacios hasta llegar a la longitud exacta de 30
            sb.setLength(longitud);
            raf.writeChars(sb.toString()); // Escribe 30 * 2 = 60 bytes
        }

        private static String leerString(RandomAccessFile raf, int longitud) throws IOException {
            char[] arrayChars = new char[longitud];
            for (int i = 0; i < longitud; i++) {
                arrayChars[i] = raf.readChar();
            }
            // Construimos el String y limpiamos los caracteres nulos (espacios vacíos)
            return new String(arrayChars).replace('\0', ' ').trim();
        }
    }
