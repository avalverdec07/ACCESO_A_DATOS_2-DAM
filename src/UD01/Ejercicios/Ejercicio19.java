package UD01.Ejercicios;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class Ejercicio19 {

        private static final String RUTA_FICHERO = "ConsultasMedicas.dat";
        private static final int TAMANO_REGISTRO = 152; // 4 + 40 + 8 + 100
        private static final int LONGITUD_NOMBRE = 20;
        private static final int LONGITUD_DIAGNOSTICO = 50;

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            do {
                System.out.println("\n--- CLÍNICA: GESTIÓN DE CONSULTAS ---");
                System.out.println("1. Mostrar todas las consultas");
                System.out.println("2. Introducir nueva consulta");
                System.out.println("3. Añadir/Modificar diagnóstico");
                System.out.println("4. Buscar consulta por nombre de paciente");
                System.out.println("5. Buscar consulta por fecha");
                System.out.println("6. Salir");
                System.out.print("Elige una opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                    switch (opcion) {
                        case 1: mostrarTodas(); break;
                        case 2: introducirConsulta(scanner); break;
                        case 3: anadirDiagnostico(scanner); break;
                        case 4: buscarPorNombre(scanner); break;
                        case 5: buscarPorFecha(scanner); break;
                        case 6: System.out.println("Saliendo del sistema..."); break;
                        default: System.out.println("Opción inválida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, introduce un número válido.");
                }
            } while (opcion != 6);
            scanner.close();
        }

        // --- 1. MOSTRAR TODAS ---
        private static void mostrarTodas() {
            try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "r")) {
                long numRegistros = raf.length() / TAMANO_REGISTRO;
                if (numRegistros == 0) {
                    System.out.println("No hay consultas registradas.");
                    return;
                }

                System.out.println("\n--- LISTADO DE CONSULTAS ---");
                for (int i = 0; i < numRegistros; i++) {
                    raf.seek(i * TAMANO_REGISTRO);
                    imprimirConsultaLeida(raf);
                }
            } catch (IOException e) {
                System.out.println("El fichero de consultas aún no existe.");
            }
        }

        // --- 2. INTRODUCIR CONSULTA ---
        private static void introducirConsulta(Scanner scanner) {
            try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "rw")) {
                int id = (int) (raf.length() / TAMANO_REGISTRO) + 1; // ID Autonumérico

                System.out.print("Nombre del paciente (máx 20 caracteres): ");
                String nombre = scanner.nextLine().trim();

                System.out.print("Fecha de la consulta (AAAAMMDD, ej. 20261029): ");
                long fecha = Long.parseLong(scanner.nextLine().trim());

                System.out.print("Diagnóstico inicial (intro para dejar en blanco, máx 50 carácteres): ");
                String diagnostico = scanner.nextLine().trim();

                raf.seek(raf.length()); // Posicionarnos al final

                raf.writeInt(id);                                      // 4 bytes
                escribirString(raf, nombre, LONGITUD_NOMBRE);          // 40 bytes
                raf.writeLong(fecha);                                  // 8 bytes
                escribirString(raf, diagnostico, LONGITUD_DIAGNOSTICO);// 100 bytes

                System.out.println("Consulta registrada correctamente con el ID: " + id);
            } catch (IOException | NumberFormatException e) {
                System.out.println("Error al guardar la consulta.");
            }
        }

        // --- 3. AÑADIR O MODIFICAR DIAGNÓSTICO ---
        private static void anadirDiagnostico(Scanner scanner) {
            try {
                System.out.print("Introduce el ID de la consulta a diagnosticar: ");
                int id = Integer.parseInt(scanner.nextLine().trim());

                try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "rw")) {
                    if (id <= 0 || id > (raf.length() / TAMANO_REGISTRO)) {
                        System.out.println("ID de consulta inexistente.");
                        return;
                    }

                    System.out.print("Nuevo diagnóstico (máx 50 caracteres): ");
                    String nuevoDiagnostico = scanner.nextLine().trim();

                    // Cálculo del desplazamiento (offset) exacto del diagnóstico:
                    // Registros anteriores + ID(4) + Nombre(40) + Fecha(8) = 52 bytes
                    long posicionDiagnostico = ((id - 1) * TAMANO_REGISTRO) + 52;

                    raf.seek(posicionDiagnostico);
                    escribirString(raf, nuevoDiagnostico, LONGITUD_DIAGNOSTICO);

                    System.out.println("Diagnóstico actualizado con éxito.");
                }
            } catch (Exception e) {
                System.out.println("Error al actualizar el diagnóstico.");
            }
        }

        // --- 4. BUSCAR POR NOMBRE (Coincidencia parcial) ---
        private static void buscarPorNombre(Scanner scanner) {
            try {
                System.out.print("Introduce el nombre o parte del nombre a buscar: ");
                String busqueda = scanner.nextLine().trim().toLowerCase();
                boolean hallado = false;

                try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "r")) {
                    long numRegistros = raf.length() / TAMANO_REGISTRO;

                    System.out.println("\n--- RESULTADOS DE LA BÚSQUEDA ---");
                    for (int i = 0; i < numRegistros; i++) {
                        raf.seek(i * TAMANO_REGISTRO);

                        int id = raf.readInt();
                        String nombre = leerString(raf, LONGITUD_NOMBRE);
                        long fecha = raf.readLong();
                        String diagnostico = leerString(raf, LONGITUD_DIAGNOSTICO);

                        // Comprobamos si el nombre contiene la cadena buscada (ignorando mayúsculas)
                        if (nombre.toLowerCase().contains(busqueda)) {
                            System.out.printf("ID: %d | Fecha: %d | Paciente: %-20s | Diag: %s%n",
                                    id, fecha, nombre, diagnostico);
                            hallado = true;
                        }
                    }
                    if (!hallado) System.out.println("No se encontraron pacientes con ese nombre.");
                }
            } catch (IOException e) {
                System.out.println("Error de lectura en el fichero.");
            }
        }

        // --- 5. BUSCAR POR FECHA ---
        private static void buscarPorFecha(Scanner scanner) {
            try {
                System.out.print("Introduce la fecha a buscar (AAAAMMDD): ");
                long fechaBuscada = Long.parseLong(scanner.nextLine().trim());
                boolean hallado = false;

                try (RandomAccessFile raf = new RandomAccessFile(RUTA_FICHERO, "r")) {
                    long numRegistros = raf.length() / TAMANO_REGISTRO;

                    System.out.println("\n--- CONSULTAS DEL DÍA " + fechaBuscada + " ---");
                    for (int i = 0; i < numRegistros; i++) {
                        raf.seek(i * TAMANO_REGISTRO);

                        int id = raf.readInt();
                        String nombre = leerString(raf, LONGITUD_NOMBRE);
                        long fecha = raf.readLong();
                        String diagnostico = leerString(raf, LONGITUD_DIAGNOSTICO);

                        if (fecha == fechaBuscada) {
                            System.out.printf("ID: %d | Paciente: %-20s | Diag: %s%n",
                                    id, nombre, diagnostico);
                            hallado = true;
                        }
                    }
                    if (!hallado) System.out.println("No hay consultas registradas en esa fecha.");
                }
            } catch (Exception e) {
                System.out.println("Error durante la búsqueda.");
            }
        }

        // ==========================================
        // MÉTODOS AUXILIARES Y LECTURA/ESCRITURA
        // ==========================================

        private static void imprimirConsultaLeida(RandomAccessFile raf) throws IOException {
            int id = raf.readInt();
            String nombre = leerString(raf, LONGITUD_NOMBRE);
            long fecha = raf.readLong();
            String diagnostico = leerString(raf, LONGITUD_DIAGNOSTICO);
            System.out.printf("ID: %d | Fecha: %d | Paciente: %-20s | Diagnóstico: %s%n",
                    id, fecha, nombre, diagnostico);
        }

        private static void escribirString(RandomAccessFile raf, String texto, int longitud) throws IOException {
            StringBuilder sb = new StringBuilder(texto != null ? texto : "");
            sb.setLength(longitud); // Trunca si es muy largo, rellena de caracteres nulos si es corto
            raf.writeChars(sb.toString()); // El tamaño real escrito será longitud * 2
        }

        private static String leerString(RandomAccessFile raf, int longitud) throws IOException {
            char[] arrayChars = new char[longitud];
            for (int i = 0; i < longitud; i++) {
                arrayChars[i] = raf.readChar();
            }
            // Construimos el string y eliminamos los caracteres vacíos ('\0') y espacios sobrantes
            return new String(arrayChars).replace('\0', ' ').trim();
        }
}
