package UD01.Ejemplos;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;


public class EscrituraFicheroTexto {
    public static void main(String[] args) {
            List<String> productos = List.of("Teclado mecánico", "Ratón inalámbrico", "Monitor 27\"");

            try (BufferedWriter bw = new BufferedWriter(
                    new FileWriter("catalogo/productos.txt", StandardCharsets.UTF_8))) {

                for (String producto : productos) {
                    bw.write(producto);
                    bw.newLine(); // salto de línea multiplataforma
                }
                String frase;
                Scanner teclado = new Scanner(System.in);

                do {
                    System.out.println(("Introduce una frase para escribir en el fichero. \"Salir\" para terminar"));
                    frase = teclado.nextLine();
                    bw.write(frase);
                    bw.newLine();
                } while (!frase.equalsIgnoreCase("salir"));



            } catch (IOException e) {
                System.err.println("Error de escritura: " + e.getMessage());
            }
        }
    }
