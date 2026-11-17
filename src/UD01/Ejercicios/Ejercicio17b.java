package UD01.Ejercicios;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

// --- CLASE PERSONAJE ---
// Implementa Serializable y tiene su serialVersionUID explícito
class Personaje implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private int nivel;
    private int puntosVida;
    private List<String> inventario;

    public Personaje(String nombre) {
        this.nombre = nombre;
        this.nivel = 1;          // Nivel inicial por defecto
        this.puntosVida = 100;   // Vida inicial por defecto
        this.inventario = new ArrayList<>();
    }

    // Getters y Setters básicos
    public String getNombre() {
        return nombre;
    }

    public int getNivel() {
        return nivel;
    }
    public void subirNivel() {
        this.nivel++;
    }

    public int getPuntosVida() {
        return puntosVida;
    }

    public void setPuntosVida(int puntosVida) {
        this.puntosVida = puntosVida;
    }

    public List<String> getInventario() {
        return inventario;
    }

    public void anadirObjeto(String objeto) {
        this.inventario.add(objeto);
    }

    @Override
    public String toString() {
        return "\n=== ESTADO DEL PERSONAJE ===" +
                "\nNombre    : " + nombre +
                "\nNivel     : " + nivel +
                "\nVida (HP) : " + puntosVida +
                "\nInventario: " + (inventario.isEmpty() ? "Vacío" : inventario) +
                "\n============================";
    }
}

// --- CLASE PRINCIPAL ---
public class Ejercicio17b {

    private static final String ARCHIVO_GUARDADO = "partida.sav";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Personaje miPersonaje = cargarPartida();

        // Si es la primera vez que jugamos (no hay archivo), creamos el personaje
        if (miPersonaje == null) {
            System.out.println("¡Bienvenido a tu nueva aventura!");
            System.out.print("Introduce el nombre de tu héroe: ");
            String nombre = scanner.nextLine().trim();
            miPersonaje = new Personaje(nombre);
        } else {
            System.out.println("Partida cargada con éxito. ¡Bienvenido de nuevo, " + miPersonaje.getNombre() + "!");
        }

        int opcion = 0;

        do {
            System.out.println("\n--- MENÚ DE JUEGO ---");
            System.out.println("1. Ver estado del personaje");
            System.out.println("2. Subir de nivel");
            System.out.println("3. Recibir daño (-10 HP)");
            System.out.println("4. Curar (+20 HP)");
            System.out.println("5. Encontrar objeto (Añadir al inventario)");
            System.out.println("6. Guardar partida y Salir");
            System.out.print("¿Qué deseas hacer?: ");

            try {
                opcion = Integer.parseInt(scanner.nextLine().trim());

                switch (opcion) {
                    case 1:
                        System.out.println(miPersonaje);
                        break;
                    case 2:
                        miPersonaje.subirNivel();
                        System.out.println("¡Has subido al nivel " + miPersonaje.getNivel() + "!");
                        break;
                    case 3:
                        miPersonaje.setPuntosVida(miPersonaje.getPuntosVida() - 10);
                        System.out.println("¡Ouch! Has recibido daño. HP actual: " + miPersonaje.getPuntosVida());
                        break;
                    case 4:
                        miPersonaje.setPuntosVida(miPersonaje.getPuntosVida() + 20);
                        System.out.println("Te has curado. HP actual: " + miPersonaje.getPuntosVida());
                        break;
                    case 5:
                        System.out.print("¿Qué objeto has encontrado?: ");
                        String objeto = scanner.nextLine().trim();
                        miPersonaje.anadirObjeto(objeto);
                        System.out.println(" " + objeto + " añadido al inventario.");
                        break;
                    case 6:
                        guardarPartida(miPersonaje);
                        System.out.println(" Partida guardada. ¡Hasta la próxima!");
                        break;
                    default:
                        System.out.println(" Acción no permitida.");
                }
            } catch (NumberFormatException e) {
                System.out.println(" Por favor, elige un número del menú.");
            }

        } while (opcion != 6);

        scanner.close();
    }

    // ==========================================
    // SISTEMA DE GUARDADO Y CARGA
    // ==========================================

    private static Personaje cargarPartida() {
        File archivo = new File(ARCHIVO_GUARDADO);

        if (!archivo.exists()) {
            return null; // No hay partida guardada
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            // Leemos el objeto y lo convertimos (cast) a Personaje
            return (Personaje) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(" Error al cargar la partida corrupta: " + e.getMessage());
            return null;
        }
    }

    private static void guardarPartida(Personaje p) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(ARCHIVO_GUARDADO))) {
            // Escribimos el objeto personaje completo (incluye sus stats y su lista de inventario)
            oos.writeObject(p);
        } catch (IOException e) {
            System.out.println(" Error crítico al guardar la partida: " + e.getMessage());
        }
    }
}
