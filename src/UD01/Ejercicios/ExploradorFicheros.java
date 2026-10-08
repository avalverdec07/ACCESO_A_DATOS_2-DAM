package UD01.Ejercicios;
/*
    EJERCICIO 3b
 */
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class ExploradorFicheros {

    private Path rutaBase;

    // Constructor que valida la ruta inicial
    public ExploradorFicheros(String ruta) {
        this.rutaBase = Paths.get(ruta);

        if (Files.notExists(this.rutaBase)) {
            System.out.println("Error: La ruta proporcionada no existe.");
            this.rutaBase = null;
        } else if (!Files.isDirectory(this.rutaBase)) {
            System.out.println("Error: La ruta proporcionada no es un directorio.");
            this.rutaBase = null;
        }
    }

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Uso: java ExploradorFicheros <ruta_carpeta> [extension]");
            System.out.println("Ejemplo 1: java ExploradorFicheros C:\\MisDocumentos");
            System.out.println("Ejemplo 2: java ExploradorFicheros C:\\Proyectos .java");
            return;
        }

        String ruta = args[0];
        String extension = (args.length > 1) ? args[1] : null;

        // Instanciamos el explorador
        ExploradorFicheros explorador = new ExploradorFicheros(ruta);

        // Si la ruta no era válida, el constructor ya avisó y rutaBase es null
        if (explorador.rutaBase == null) return;

        System.out.println("=== EXPLORADOR DE FICHEROS ===");
        System.out.println("Ruta base: " + explorador.rutaBase.toAbsolutePath() + "\n");

        try {
            // 1. Estadísticas generales
            long[] stats = explorador.calcularEstadisticas();
            System.out.println("1. ESTADÍSTICAS GENERALES:");
            System.out.println("   - Total ficheros: " + stats[0]);
            System.out.println("   - Total directorios: " + stats[1]);
            System.out.println("   - Tamaño total: " + stats[2] + " bytes (" + (stats[2] / 1024 / 1024) + " MB)\n");

            // 2. Filtrado por extensión (si se proporcionó)
            if (extension != null) {
                System.out.println("2. FICHEROS CON EXTENSIÓN '" + extension + "':");
                List<Path> ficherosFiltrados = explorador.listarPorExtension(extension);
                System.out.println("   - Cantidad encontrados: " + ficherosFiltrados.size());

                // Mostramos los primeros 10 para no saturar consola
                int limite = Math.min(ficherosFiltrados.size(), 10);
                for (int i = 0; i < limite; i++) {
                    System.out.println("     * " + ficherosFiltrados.get(i).getFileName());
                }
                if (ficherosFiltrados.size() > 10) {
                    System.out.println("     ... y " + (ficherosFiltrados.size() - 10) + " más.");
                }

                // 3. Crear Backup
                System.out.println("\n3. CREANDO BACKUP...");
                explorador.crearBackup(ficherosFiltrados, extension);
            } else {
                System.out.println("2. No se especificó extensión. Saltando filtrado y backup.");
            }

        } catch (IOException e) {
            System.out.println("Error durante el proceso: " + e.getMessage());
        }
    }

    // --- MÉTODOS DE FUNCIONALIDAD ---

    /**
     * Recorre todo el árbol y devuelve:
     * [0] = numFicheros, [1] = numDirectorios, [2] = tamañoTotalBytes
     */
    private long[] calcularEstadisticas() throws IOException {
        long numFicheros = 0;
        long numDirectorios = 0;
        long tamañoTotal = 0;

        try (Stream<Path> stream = Files.walk(rutaBase)) {
            for (Path path : stream.toList()) { // toList() para evitar cerrar stream dentro del loop si usáramos forEach
                // Nota: walk incluye la raíz, así que contamos directorios con cuidado
                if (Files.isDirectory(path)) {
                    numDirectorios++;
                } else if (Files.isRegularFile(path)) {
                    numFicheros++;
                    try {
                        tamañoTotal += Files.size(path);
                    } catch (IOException e) {
                        // Ignoramos errores de tamaño en archivos protegidos
                    }
                }
            }
        }
        // Restamos 1 a directorios porque walk cuenta la propia rutaBase como directorio
        return new long[]{numFicheros, numDirectorios - 1, tamañoTotal};
    }

    /**
     * Devuelve una lista con todos los ficheros que terminan en la extensión dada
     */
    private List<Path> listarPorExtension(String extension) throws IOException {
        List<Path> resultados = new ArrayList<>();

        try (Stream<Path> stream = Files.walk(rutaBase)) {
            stream.filter(path -> Files.isRegularFile(path)) // Solo ficheros
                    .filter(path -> path.toString().endsWith(extension)) // Solo la extensión
                    .forEach(resultados::add);
        }

        return resultados;
    }

    /**
     * Crea una carpeta 'backup_<extension>' y copia dentro todos los ficheros de la lista
     * Mantiene la estructura plana (solo copia los archivos, no las subcarpetas)
     */
    private void crearBackup(List<Path> ficheros, String extension) throws IOException {
        if (ficheros.isEmpty()) {
            System.out.println("   - No hay ficheros para copiar.");
            return;
        }

        // Nombre de la carpeta backup: ej: backup_.java
        String nombreBackup = "backup_" + extension.replace(".", "");
        Path rutaBackup = rutaBase.resolve(nombreBackup);

        // 1. Crear carpeta de backup si no existe
        if (Files.notExists(rutaBackup)) {
            Files.createDirectory(rutaBackup);
            System.out.println("   - Carpeta de backup creada: " + rutaBackup.getFileName());
        } else {
            System.out.println("   - Usando carpeta de backup existente: " + rutaBackup.getFileName());
        }

        // 2. Copiar ficheros
        int copiados = 0;
        int errores = 0;

        for (Path origen : ficheros) {
            try {
                // Destino: mismo nombre de archivo dentro de la carpeta backup
                Path destino = rutaBackup.resolve(origen.getFileName());

                // Si hay dos archivos con el mismo nombre en distintas subcarpetas,
                // el segundo sobrescribirá al primero. Para evitarlo podríamos añadir un sufijo único.
                if (Files.exists(destino)) {
                    // Opción: Generar nombre único si hay colisión
                    destino = rutaBackup.resolve(System.nanoTime() + "_" + origen.getFileName());
                }

                // Copia estándar (REPLACE_EXISTING por seguridad si decidimos sobrescribir)
                Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
                copiados++;

            } catch (IOException e) {
                errores++;
                System.out.println("   - Error copiando " + origen.getFileName() + ": " + e.getMessage());
            }
        }

        System.out.println("   - BACKUP FINALIZADO: " + copiados + " ficheros copiados, " + errores + " errores.");
    }
}