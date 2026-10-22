package UD01.Ejercicios;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Ejercicio12 {

        public static void main(String[] args) {
            // 1. Apuntamos al fichero generado en el ejercicio anterior
            Path rutaFichero = Paths.get("numero.dat");

            // 2. Comprobamos que el fichero existe antes de intentar abrirlo
            if (Files.notExists(rutaFichero)) {
                System.out.println("Error: El fichero '" + rutaFichero.getFileName() + "' no existe. Ejecuta el ejercicio anterior primero.");
                return;
            }

            System.out.println("Intentando leer el número binario desde: " + rutaFichero.toAbsolutePath());

            // 3. Abrimos flujos de lectura usando NIO.2 y try-with-resources
            try (InputStream is = Files.newInputStream(rutaFichero);
                 DataInputStream dis = new DataInputStream(is)) {

                /*
                 * El metodo readInt() lee exactamente 4 bytes del fichero
                 * y los convierte de vuelta a un número entero de 32 bits.
                 */
                int numeroRecuperado = dis.readInt();

                System.out.println("Lectura exitosa.");
                System.out.println("El número almacenado en el fichero es: " + numeroRecuperado);

            } catch (IOException e) {
                System.out.println("Ocurrió un error al leer el fichero binario: " + e.getMessage());
            }
        }
}
