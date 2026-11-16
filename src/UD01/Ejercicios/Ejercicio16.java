package UD01.Ejercicios;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

    // --- CLASE PRODUCTO ---
    class Producto implements Serializable {
        private static final long serialVersionUID = 1L;

        private String codigo;
        private String nombre;
        private double precio;

        public Producto(String codigo, String nombre, double precio) {
            this.codigo = codigo;
            this.nombre = nombre;
            this.precio = precio;
        }

        public String getCodigo() { return codigo; }
        public String getNombre() { return nombre; }
        public double getPrecio() { return precio; }

        public void setNombre(String nombre) { this.nombre = nombre; }
        public void setPrecio(double precio) { this.precio = precio; }

        @Override
        public String toString() {
            return String.format("Producto [Código: %-5s | Nombre: %-20s | Precio: %8.2f €]", codigo, nombre, precio);
        }
    }

    // --- CLASE PRINCIPAL ---
    public class Ejercicio16 {

        private static final String RUTA_FICHERO = "productos.obj";

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            int opcion = 0;

            do {
                System.out.println("\n--- ALMACÉN: GESTIÓN DE PRODUCTOS ---");
                System.out.println("1. Listar todos los productos");
                System.out.println("2. Dar de alta un producto");
                System.out.println("3. Buscar por nombre");
                System.out.println("4. Modificar producto por código");
                System.out.println("5. Borrar producto por código");
                System.out.println("6. Salir");
                System.out.print("Elige una opción: ");

                try {
                    opcion = Integer.parseInt(scanner.nextLine().trim());
                    switch (opcion) {
                        case 1: listarProductos();
                                break;
                        case 2: altaProducto(scanner);
                                break;
                        case 3: buscarProducto(scanner);
                                break;
                        case 4: modificarProducto(scanner);
                                break;
                        case 5: borrarProducto(scanner);
                                break;
                        case 6: System.out.println("Saliendo del sistema...");
                                break;
                        default: System.out.println("Opción inválida.");
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Por favor, introduce un número válido.");
                }
            } while (opcion != 6);
            scanner.close();
        }

        // --- 1. LISTAR PRODUCTOS ---
        private static void listarProductos() {
            List<Producto> productos = leerFichero();
            if (productos.isEmpty()) {
                System.out.println("No hay productos en el almacén.");
            } else {
                System.out.println("\n--- INVENTARIO ACTUAL ---");
                for (Producto p : productos) {
                    System.out.println(p);
                }
            }
        }

        // --- 2. DAR DE ALTA ---
        private static void altaProducto(Scanner scanner) {
            System.out.print("Código del producto: ");
            String codigo = scanner.nextLine().trim();

            System.out.print("Nombre del producto: ");
            String nombre = scanner.nextLine().trim();

            System.out.print("Precio: ");
            double precio = Double.parseDouble(scanner.nextLine().trim());

            Producto nuevo = new Producto(codigo, nombre, precio);

            List<Producto> productos = leerFichero();

            // Pequeña validación para evitar códigos duplicados
            for (Producto p : productos) {
                if (p.getCodigo().equalsIgnoreCase(codigo)) {
                    System.out.println("Ya existe un producto con el código " + codigo);
                    return;
                }
            }

            productos.add(nuevo);
            guardarFichero(productos);

            System.out.println("Producto registrado con éxito.");
        }

        // --- 3. BUSCAR POR PARTE DEL NOMBRE ---
        private static void buscarProducto(Scanner scanner) {
            System.out.print("Introduce texto para buscar en el nombre: ");
            String busqueda = scanner.nextLine().trim().toLowerCase();

            List<Producto> productos = leerFichero();
            boolean hallado = false;

            System.out.println("\n--- RESULTADOS DE BÚSQUEDA ---");
            for (Producto p : productos) {
                if (p.getNombre().toLowerCase().contains(busqueda)) {
                    System.out.println(p);
                    hallado = true;
                }
            }

            if (!hallado) System.out.println("No se encontraron coincidencias.");
        }

        // --- 4. MODIFICAR POR CÓDIGO ---
        private static void modificarProducto(Scanner scanner) {
            System.out.print("Introduce el CÓDIGO EXACTO del producto a modificar: ");
            String codigoBuscado = scanner.nextLine().trim();

            List<Producto> productos = leerFichero();
            boolean modificado = false;

            for (Producto p : productos) {
                if (p.getCodigo().equalsIgnoreCase(codigoBuscado)) {
                    System.out.println("Producto actual: " + p);

                    System.out.print("Nuevo nombre (pulsa intro para mantener el actual): ");
                    String nuevoNombre = scanner.nextLine().trim();
                    if (!nuevoNombre.isEmpty()) {
                        p.setNombre(nuevoNombre);
                    }

                    System.out.print("Nuevo precio (0 o negativo para mantener el actual): ");
                    try {
                        double nuevoPrecio = Double.parseDouble(scanner.nextLine().trim());
                        if (nuevoPrecio > 0) {
                            p.setPrecio(nuevoPrecio);
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Formato de precio inválido. Se mantendrá el precio original.");
                    }

                    modificado = true;
                    break; // Encontramos el producto, no necesitamos seguir buscando
                }
            }

            if (modificado) {
                guardarFichero(productos);
                System.out.println("Producto actualizado correctamente.");
            } else {
                System.out.println("No se encontró ningún producto con ese código.");
            }
        }

        // --- 5. BORRAR POR CÓDIGO (SIN LAMBDAS, USANDO ITERATOR) ---
        private static void borrarProducto(Scanner scanner) {
            System.out.print("Introduce el CÓDIGO EXACTO del producto a borrar: ");
            String codigoBorrar = scanner.nextLine().trim();

            List<Producto> productos = leerFichero();
            boolean borrado = false;

            Iterator<Producto> iterador = productos.iterator();
            while (iterador.hasNext()) {
                Producto p = iterador.next();
                if (p.getCodigo().equalsIgnoreCase(codigoBorrar)) {
                    iterador.remove(); // Borrado seguro
                    borrado = true;
                    break; // Como el código es único, podemos salir del bucle una vez borrado
                }
            }

            if (borrado) {
                guardarFichero(productos);
                System.out.println("Producto borrado del fichero.");
            } else {
                System.out.println("No se encontró ningún producto con ese código.");
            }
        }

        // ==========================================
        // MÉTODOS BASE DE SERIALIZACIÓN (SIN BUCLES)
        // ==========================================

        @SuppressWarnings("unchecked")
        private static List<Producto> leerFichero() {
            File file = new File(RUTA_FICHERO);

            if (!file.exists()) {
                return new ArrayList<>();
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                return (List<Producto>) ois.readObject();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error al leer el fichero: " + e.getMessage());
                return new ArrayList<>();
            }
        }

        private static void guardarFichero(List<Producto> lista) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(RUTA_FICHERO))) {
                oos.writeObject(lista);
            } catch (IOException e) {
                System.out.println("Error al escribir en el fichero: " + e.getMessage());
            }
        }
    }
