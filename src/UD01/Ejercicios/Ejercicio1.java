package UD01.Ejercicios;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.ZonedDateTime;

        public class Ejercicio1 {

            public static void main(String[] args) {
                // 1. Comprobación inicial: ¿El usuario pasó argumentos?
                if (args.length == 0) {
                    System.out.println("Uso: java Ejercicio1 <ruta1> <ruta2> ...");
                    System.out.println("Ejemplo: java Ejercicio1 datos.txt C:\\Windows");
                    return; // Terminamos el programa
                }

                System.out.println("=== ANALIZANDO " + args.length + " RUTAS ===\n");

                // 2. Bucle for-each para recorrer cada argumento recibido
                for (String rutaString : args) {
                    analizarRuta(rutaString);
                    System.out.println("------------------------------------------");
                }
            }

            private static void analizarRuta(String rutaString) {
                Path ruta = Paths.get(rutaString);

                System.out.println("Analizando: " + rutaString);

                try {
                    // --- A. EXISTENCIA Y TIPO ---
                    if (Files.notExists(ruta)) {
                        System.out.println(">> ERROR: La ruta NO existe.");
                        return;
                    }

                    boolean esFichero = Files.isRegularFile(ruta);
                    boolean esCarpeta = Files.isDirectory(ruta);
                    System.out.println(">> Existe: SÍ");
                    System.out.println(">> Tipo: " + (esFichero ? "Fichero" : (esCarpeta ? "Carpeta" : "Otro (enlace, etc.)")));

                    // --- B. NOMBRE Y RUTA ---
                    System.out.println(">> Nombre (sin ruta): " + ruta.getFileName());
                    System.out.println(">> Ruta absoluta: " + ruta.toAbsolutePath());

                    // --- C. PERMISOS (Lectura/Escritura/Ejecución) ---
                    // Nota: Files.isReadable/Writable/Executable comprueban permisos reales del SO
                    System.out.println(">> Se puede leer: " + Files.isReadable(ruta));
                    System.out.println(">> Se puede escribir: " + Files.isWritable(ruta));
                    System.out.println(">> Se puede ejecutar: " + Files.isExecutable(ruta));

                    // --- D. TAMAÑO Y FECHA (Usando Atributos) ---
                    BasicFileAttributes atributos = Files.readAttributes(ruta, BasicFileAttributes.class);

                    System.out.println(">> Tamaño: " + atributos.size() + " bytes");

                    // Formatear la fecha para que sea legible
                    ZonedDateTime fechaMod = atributos.lastModifiedTime().toInstant()
                            .atZone(ZoneId.systemDefault());
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

                    System.out.println(">> Última modificación: " + fechaMod.format(formatter));

                } catch (IOException e) {
                    // Si hay un error de E/S (ej: permisos denegados al leer atributos)
                    System.out.println(">> Error de E/S: " + e.getMessage());
                } catch (Exception e) {
                    // Captura de seguridad para otros errores (ej: ruta inválida)
                    System.out.println(">> Error inesperado: " + e.getMessage());
                }
            }
        }
    }
}
