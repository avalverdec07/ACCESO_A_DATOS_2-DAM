package UD01.Ejercicios;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.io.IOException;
public class Ejercicio5 {

        public static void main(String[] args) {
            // 1. Comprobamos que el usuario ha pasado los dos parámetros necesarios
            if (args.length < 2) {
                System.out.println("Error. Uso correcto: java AnadirTexto <ruta_fichero> \"<texto_a_añadir>\"");
                return; // Salimos si no hay parámetros suficientes
            }

            // 2. Recogemos los parámetros
            String rutaFichero = args[0];
            String textoAAnadir = args[1];

            // 3. Creamos el objeto Path
            Path ruta = Paths.get(rutaFichero);

            // 4. Escribimos en el fichero
            try {
                /*
                 * Files.writeString (disponible desde Java 11) es la forma más limpia en NIO.2
                 * Le pasamos el Path, el texto (le añado un salto de línea para que no se pegue
                 * con el texto anterior) y las opciones de apertura.
                 *
                 * - CREATE: Si el fichero no existe, lo crea.
                 * - APPEND: Si el fichero ya existe, añade el texto al final (no lo sobrescribe).
                 * ¡Esto es el equivalente exacto a FileWriter en modo append!
                 */
                Files.writeString(ruta,
                        textoAAnadir + System.lineSeparator(),
                        StandardOpenOption.CREATE,
                        StandardOpenOption.APPEND);

                System.out.println("Texto añadido correctamente en: " + ruta.toAbsolutePath());

            } catch (IOException e) {
                System.out.println("Error al intentar escribir en el fichero: " + e.getMessage());
            }
        }
    }
