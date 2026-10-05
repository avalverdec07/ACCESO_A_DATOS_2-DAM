package UD01.Ejercicios;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class Ejercicio0 {
    static void main(String[] args) {

        Scanner teclado = new Scanner(System.in);

        /*
        1. MOSTRAR LA RUTA ABSOLUTA DE LA CARPETA ACTUAL.
         */
        Path raiz = Path.of(".");
        Path rutaAbsoluta = raiz.toAbsolutePath();
        System.out.println("La ruta absoluta es: " + rutaAbsoluta);

        /*
        2. Pedir por teclado una ruta de fichero o carpeta y mostrar si lo introducido existe,
            si es un fichero o una carpeta, la fecha de modificación y el tamaño.
         */
        System.out.println("Introduce una ruta de fichero o carpeta: ");
        String directorio = teclado.nextLine();

        Path ruta = Path.of(directorio);

        if (Files.exists(ruta)) {
            System.out.println("La ruta existe.");
            if (Files.isDirectory(ruta)) {
                System.out.println("Es un directorio.");
            } else {
                System.out.println("Es un fichero.");
            }
                try {
                    System.out.println("El tamaño del fichero es: " + Files.size(ruta));
                    System.out.println("La fecha de modificación es. " + Files.getLastModifiedTime(ruta));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }



        }
    }
}
