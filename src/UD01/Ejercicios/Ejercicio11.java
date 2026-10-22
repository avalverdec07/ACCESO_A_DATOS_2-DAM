package UD01.Ejercicios;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

public class Ejercicio11 {

        public static void main(String[] args) {
            Scanner scanner = new Scanner(System.in);
            Path rutaFichero = Paths.get("numero.dat");

            // 1. Pedir el número entero al usuario
            System.out.print("Introduce un número entero (ej. 65 o 123456789): ");
            int numero;
            try {
                numero = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Debe introducir un número entero válido.");
                scanner.close();
                return;
            }

            // 2. ESCRIBIR el número en el fichero BINARIO
            // Usamos Files.newOutputStream (NIO.2) decorado con DataOutputStream
            try (OutputStream os = Files.newOutputStream(rutaFichero);
                 DataOutputStream dos = new DataOutputStream(os)) {

                dos.writeInt(numero); // Escribe los 4 bytes del número entero
                System.out.println("\nNúmero " + numero + " guardado en binario en: " + rutaFichero.toAbsolutePath());

            } catch (IOException e) {
                System.out.println("Error al escribir el fichero binario: " + e.getMessage());
                scanner.close();
                return;
            }

            // 3. LEER los bytes brutos y COMPARAR con UTF-8
            try {
                // Leemos todos los bytes del fichero usando NIO.2
                byte[] bytes = Files.readAllBytes(rutaFichero);

                System.out.println("\n--- ANÁLISIS DEL FICHERO BINARIO ---");
                System.out.println("Tamaño del archivo: " + bytes.length + " bytes (siempre 4 bytes para un int)");

                // Muestra 1: Representación hexadecimal y decimal byte a byte
                System.out.print("Bytes brutos (Decimal): [ ");
                for (byte b : bytes) {
                    // Byte.toUnsignedInt para ver valores de 0 a 255 claramente
                    System.out.print(Byte.toUnsignedInt(b) + " ");
                }
                System.out.println("]");

                // Muestra 2: Cómo lo interpreta UTF-8 si lo leemos como TEXTO
                String textoUTF8 = new String(bytes, StandardCharsets.UTF_8);
                System.out.println("Interpretación como texto UTF-8: \"" + textoUTF8 + "\"");

                // 4. LEERLO CORRECTAMENTE COMO BINARIO (Para demostrar cómo se recupera)
                try (InputStream is = Files.newInputStream(rutaFichero);
                     DataInputStream dis = new DataInputStream(is)) {

                    int numeroLeido = dis.readInt();
                    System.out.println("\nLectura correcta con DataInputStream.readInt(): " + numeroLeido);
                }

            } catch (IOException e) {
                System.out.println("Error al leer el fichero: " + e.getMessage());
            } finally {
                scanner.close();
            }
        }
}
/*
¿Por qué difiere de UTF-8?

Si introduces 65:

En Binario (DataOutputStream): Guarda 4 bytes [0, 0, 0, 65].
Al leerlo como UTF-8 verás tres caracteres nulos invisibles \0\0\0 y la letra A (porque 65 es 'A' en ASCII/UTF-8).

En Texto UTF-8 (Files.writeString): Guardaría 2 bytes [54, 53] (que corresponden a los caracteres del texto '6' y '5').
 */