package UD01.Ejemplos;

import java.io.*;

public class RegistrosLongitudFija {

        // Suponemos una longitud de 20 caracteres para almacenar el nombre
        static final int LONGITUD_NOMBRE = 20;
        // A conitnuación, calculamos el tamaño completo del registro: 20 caracteres del nombre + ocupa un caracter
        // Más 2 bytes que ocupa un número entero
        static final int TAMANIO_REGISTRO = LONGITUD_NOMBRE * 2 + 4; // 2 bytes/car (UTF-16) + int edad

        public static void escribirAlumno(RandomAccessFile raf, int indice, String nombre, int edad) throws IOException {
            raf.seek((long) indice * TAMANIO_REGISTRO);
            StringBuilder sb = new StringBuilder(nombre);
            sb.setLength(LONGITUD_NOMBRE); // trunca o rellena
            raf.writeChars(sb.toString());
            raf.writeInt(edad);
        }

        public static void leerAlumno(RandomAccessFile raf, int indice) throws IOException {
            raf.seek((long) indice * TAMANIO_REGISTRO);
            char[] nombreChars = new char[LONGITUD_NOMBRE];
            for (int i = 0; i < LONGITUD_NOMBRE; i++) {
                nombreChars[i] = raf.readChar();
            }
            int edad = raf.readInt();
            System.out.println("Alumno #" + indice + " -> " + new String(nombreChars).trim() + ", " + edad);
        }

        public static void main(String[] args) throws IOException {
            try (RandomAccessFile raf = new RandomAccessFile("alumnos.dat", "rw")) {
                escribirAlumno(raf, 0, "Ana García", 19);
                escribirAlumno(raf, 1, "Luis Pérez", 20);
                leerAlumno(raf, 1); // acceso directo, sin leer el registro 0 antes
            }
        }
    }
