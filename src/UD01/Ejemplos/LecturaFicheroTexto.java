package UD01.Ejemplos;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;


public class LecturaFicheroTexto {
        public static void main(String[] args) {
            Path fichero = Path.of("catalogo/config.txt");

            try (BufferedReader br = new BufferedReader(
                    new FileReader(fichero.toFile(), StandardCharsets.UTF_8))) {

                String linea;
                int numeroLinea = 1;
                while ((linea = br.readLine()) != null) {
                    System.out.println(numeroLinea + ": " + linea);
                    numeroLinea++;
                }
            } catch (IOException e) {
                System.err.println("Error de lectura: " + e.getMessage());
            }
        }
    }
