package UD01.Ejercicios;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;


public class Ejercicio15 {


    // --- CLASE EMPLEADO ---
    class Empleado implements Serializable {
        private static final long serialVersionUID = 1L;

        private String nombre;
        private double sueldo;

        public Empleado(String nombre, double sueldo) {
            this.nombre = nombre;
            this.sueldo = sueldo;
        }

        public String getNombre() { return nombre; }
        public double getSueldo() { return sueldo; }
        public void setSueldo(double sueldo) { this.sueldo = sueldo; }

        @Override
        public String toString() {
            return String.format("Empleado [Nombre: %-15s | Sueldo: %.2f €]", nombre, sueldo);
        }
    }

    // --- CLASE PRINCIPAL ---
    public class GestionEmpleados {

        private static final String RUTA_FICHERO = "empleados.obj";

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            do {
                System.out.println("\n--- GESTIÓN DE EMPLEADOS (SERIALIZACIÓN DE LISTA COMPLETA) ---");
                System.out.println("1. Listar empleados");
                System.out.println("2. Dar de alta empleado");
                System.out.println("3. Borrar empleado por nombre");
                System.out.println("4. Buscar empleado por nombre");
                System.out.println("5. Subir el sueldo a todos los empleados");
                System.out.println("6. Salir");
                System.out.print("Elige una opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                    switch (opcion) {
                        case 1: listarEmpleados(); break;
                        case 2: altaEmpleado(scanner); break;
                        case 3: borrarEmpleado(scanner); break;
                        case 4: buscarEmpleado(scanner); break;
                        case 5: subirSueldoTodos(scanner); break;
                        case 6: System.out.println("Saliendo del sistema..."); break;
                        default: System.out.println("Opción no válida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, introduce un número válido.");
                }
            } while (opcion != 6);
            scanner.close();
        }

        // --- 1. LISTAR EMPLEADOS ---
        private static void listarEmpleados() {
            List<Empleado> empleados = leerFichero();
            if (empleados.isEmpty()) {
                System.out.println("No hay empleados registrados.");
            } else {
                System.out.println("\n--- PLANTILLA ACTUAL ---");
                for (Empleado e : empleados) {
                    System.out.println(e);
                }
            }
        }

        // --- 2. DAR DE ALTA ---
        private static void altaEmpleado(Scanner scanner) {
            System.out.print("Nombre del empleado: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("Sueldo: ");
            double sueldo = Double.parseDouble(scanner.nextLine().trim());

            Empleado nuevo = new Empleado(nombre, sueldo);

            List<Empleado> empleados = leerFichero();
            empleados.add(nuevo);
            guardarFichero(empleados);

            System.out.println("Empleado guardado con éxito.");
        }

        // --- 3. BORRAR POR NOMBRE (SIN LAMBDAS, USANDO ITERATOR) ---
        private static void borrarEmpleado(Scanner scanner) {
            System.out.print("Introduce el nombre EXACTO del empleado a borrar: ");
            String nombreBorrar = scanner.nextLine().trim();

            List<Empleado> empleados = leerFichero();
            boolean borrado = false;

            // Usamos un Iterator explícito para evitar ConcurrentModificationException
            Iterator<Empleado> iterador = empleados.iterator();
            while (iterador.hasNext()) {
                Empleado e = iterador.next();
                if (e.getNombre().equalsIgnoreCase(nombreBorrar)) {
                    iterador.remove(); // Borra el elemento actual de la lista de forma segura
                    borrado = true;
                }
            }

            if (borrado) {
                guardarFichero(empleados);
                System.out.println("Empleado borrado del fichero.");
            } else {
                System.out.println("No se encontró ningún empleado con ese nombre.");
            }
        }

        // --- 4. BUSCAR POR PARTE DEL NOMBRE ---
        private static void buscarEmpleado(Scanner scanner) {
            System.out.print("Introduce texto para buscar en el nombre: ");
            String busqueda = scanner.nextLine().trim().toLowerCase();

            List<Empleado> empleados = leerFichero();
            boolean hallado = false;

            System.out.println("\n--- RESULTADOS DE BÚSQUEDA ---");
            for (Empleado e : empleados) {
                if (e.getNombre().toLowerCase().contains(busqueda)) {
                    System.out.println(e);
                    hallado = true;
                }
            }

            if (!hallado) System.out.println("No se encontraron coincidencias.");
        }

        // --- 5. SUBIR SUELDO A TODOS ---
        private static void subirSueldoTodos(Scanner scanner) {
            System.out.print("Introduce el porcentaje de subida (ej. 10 para 10%): ");
            double porcentaje = Double.parseDouble(scanner.nextLine().trim());

            List<Empleado> empleados = leerFichero();
            if (empleados.isEmpty()) {
                System.out.println("No hay empleados a los que subir el sueldo.");
                return;
            }

            for (Empleado e : empleados) {
                double nuevoSueldo = e.getSueldo() * (1 + (porcentaje / 100));
                e.setSueldo(nuevoSueldo);
            }

            guardarFichero(empleados);
            System.out.println("Sueldos actualizados a toda la plantilla.");
        }

        // ===============================
        // MÉTODOS BASE DE SERIALIZACIÓN
        // ===============================

        @SuppressWarnings("unchecked")
        private static List<Empleado> leerFichero() {
            File file = new File(RUTA_FICHERO);

            // Si el archivo no existe, devolvemos una lista nueva vacía
            if (!file.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                // Leemos la lista completa de un solo golpe
                return (List<Empleado>) ois.readObject();

            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error al leer el fichero: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        private static void guardarFichero(List<Empleado> lista) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_FICHERO))) {
                // Escribimos la lista completa de un solo golpe
                oos.writeObject(lista);

            } catch (IOException e) {
                System.out.println("Error al escribir en el fichero: " + e.getMessage());
            }
        }
    }
}
