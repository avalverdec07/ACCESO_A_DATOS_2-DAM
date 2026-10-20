package UD01.Ejemplos;

import java.io.*;

public class CopiadorImagen {
    public static void main(String args[]) {
        try (
                InputStream entrada = new BufferedInputStream(new FileInputStream("src/UD01/Ejemplos/origen/logo_nombre_negro.png"));
                OutputStream salida = new BufferedOutputStream(new FileOutputStream("src/UD01/Ejemplos/destino/logo_copia.png"))) {

            byte[] buffer = new byte[4096]; // bloques de 4 KB
            int bytesLeidos;
            while ((bytesLeidos = entrada.read(buffer)) != -1) {
                salida.write(buffer, 0, bytesLeidos);
            }
            System.out.println("Imagen copiada correctamente.");

        } catch (IOException e) {
            System.err.println("Error al copiar la imagen: " + e.getMessage());
        }
    }
}
