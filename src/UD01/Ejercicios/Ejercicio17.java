package UD01.Ejercicios;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

// --- CLASE ALUMNO ---
// La dejamos fuera de la clase principal para evitar problemas de "static"
class Alumno implements Serializable {
    private static final long serialVersionUID = 1L;

    private String expediente;
    private String nombre;
    private double nota;

    public Alumno(String expediente, String nombre, double nota) {
        this.expediente = expediente;
        this.nombre = nombre;
        this.nota = nota;
    }

    public String getExpediente() {
        return expediente;
    }
    public String getNombre() {
        return nombre;
    }
    public double getNota() {
        return nota;
    }

    public void setNota(double nota) { this.nota = nota; }

    @Override
    public String toString() {
        return String.format("Alumno [Expediente: %-8s | Nombre: %-20s | Nota: %5.2f ]", expediente, nombre, nota);
    }
}

// --- CLASE PRINCIPAL ---
public class Ejercicio17 {

    private static final String RUTA_FICHERO = "alumnos.obj";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        do {
            System.out.println("\n--- GESTIÓN ACADÉMICA DE ALUMNOS ---");
            System.out.println("1. Mostrar todos los alumnos");
            System.out.println("2. Añadir un alumno");
            System.out.println("3. Poner/Modificar nota");
            System.out.println("4. Mostrar estadísticas");
            System.out.println("5. Borrar alumno");
            System.out.println("6. Salir");
            System.out.print("Elige una opción: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());
                switch (opcion) {
                    case 1: mostrarAlumnos(); break;
                    case 2: añadirAlumno(scanner); break;
                    case 3: ponerNota(scanner); break;
                    case 4: mostrarEstadistica(); break;
                    case 5: borrarAlumno(scanner); break;
                    case 6: System.out.println("Saliendo del sistema..."); break;
                    default: System.out.println("Opción inválida.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, introduce un número válido.");
            }
        } while (opcion != 6);
        scanner.close();
    }

    // --- 1. MOSTRAR TODOS ---
    private static void mostrarAlumnos() {
        List<Alumno> alumnos = leerFichero();
        if (alumnos.isEmpty()) {
            System.out.println("No hay alumnos matriculados.");
        } else {
            System.out.println("\n--- LISTADO DE ALUMNOS ---");
            for (Alumno a : alumnos) {
                System.out.println(a);
            }
        }
    }

    // --- 2. AÑADIR ALUMNO ---
    private static void añadirAlumno(Scanner scanner) {
        System.out.print("Número de expediente: ");
        String expediente = scanner.nextLine().trim();

        List<Alumno> alumnos = leerFichero();

        // Comprobamos si el expediente ya existe
        for (Alumno a : alumnos) {
            if (a.getExpediente().equalsIgnoreCase(expediente)) {
                System.out.println("Error: Ya existe un alumno con el expediente " + expediente);
                return;
            }
        }

        System.out.print("Nombre del alumno: ");
        String nombre = scanner.nextLine().trim();

        System.out.print("Nota actual (si no tiene, pon 0): ");
        double nota = Double.parseDouble(scanner.nextLine().trim());

        Alumno nuevo = new Alumno(expediente, nombre, nota);
        alumnos.add(nuevo);
        guardarFichero(alumnos);

        System.out.println("Alumno matriculado con éxito.");
    }

    // --- 3. PONER / MODIFICAR NOTA ---
    private static void ponerNota(Scanner scanner) {
        System.out.print("Introduce el EXPEDIENTE del alumno: ");
        String expedienteBuscado = scanner.nextLine().trim();

        List<Alumno> alumnos = leerFichero();
        boolean encontrado = false;

        for (Alumno a : alumnos) {
            if (a.getExpediente().equalsIgnoreCase(expedienteBuscado)) {
                System.out.println("Alumno actual: " + a);
                System.out.print("Introduce la nueva nota: ");

                double nuevaNota = Double.parseDouble(scanner.nextLine().trim());
                a.setNota(nuevaNota);

                encontrado = true;
                break; // Salimos del bucle porque ya lo hemos modificado
            }
        }

        if (encontrado) {
            guardarFichero(alumnos);
            System.out.println("Nota actualizada correctamente.");
        } else {
            System.out.println("No se encontró ningún alumno con ese expediente.");
        }
    }

    // --- 4. MOSTRAR ESTADÍSTICA ---
    private static void mostrarEstadistica() {
        List<Alumno> alumnos = leerFichero();

        if (alumnos.isEmpty()) {
            System.out.println("No hay datos para generar estadísticas.");
            return;
        }

        int aprobados = 0;
        int suspensos = 0;
        double sumaNotas = 0.0;

        // Recorremos los alumnos una sola vez para calcular todo
        for (Alumno a : alumnos) {
            sumaNotas += a.getNota();
            if (a.getNota() >= 5.0) {
                aprobados++;
            } else {
                suspensos++;
            }
        }

        double notaMedia = sumaNotas / alumnos.size();

        System.out.println("\n--- ESTADÍSTICAS GLOBALES ---");
        System.out.println("Total de alumnos : " + alumnos.size());
        System.out.println("Aprobados        : " + aprobados);
        System.out.println("Suspensos        : " + suspensos);
        System.out.printf("Nota Media       : %.2f\n", notaMedia);
    }

    // --- 5. BORRAR ALUMNO ---
    private static void borrarAlumno(Scanner scanner) {
        System.out.print("Introduce el EXPEDIENTE del alumno a dar de baja: ");
        String expedienteBorrar = scanner.nextLine().trim();

        List<Alumno> alumnos = leerFichero();
        boolean borrado = false;

        // Usamos el Iterator para borrar con seguridad
        Iterator<Alumno> iterador = alumnos.iterator();
        while (iterador.hasNext()) {
            Alumno a = iterador.next();
            if (a.getExpediente().equalsIgnoreCase(expedienteBorrar)) {
                iterador.remove();
                borrado = true;
                break;
            }
        }

        if (borrado) {
            guardarFichero(alumnos);
            System.out.println("Alumno borrado del registro.");
        } else {
            System.out.println("No se encontró ningún alumno con ese expediente.");
        }
    }

    // ==========================================
    // MÉTODOS BASE DE SERIALIZACIÓN
    // ==========================================

    @SuppressWarnings("unchecked")
    private static List<Alumno> leerFichero() {
        File file = new File(RUTA_FICHERO);

        if (!file.exists()) {
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            return (List<Alumno>) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error al leer el fichero: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void guardarFichero(List<Alumno> lista) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_FICHERO))) {
            oos.writeObject(lista);
        } catch (IOException e) {
            System.out.println("Error al escribir en el fichero: " + e.getMessage());
        }
    }
}