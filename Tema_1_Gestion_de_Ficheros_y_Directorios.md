---
layout: default
title: Tema 1 - Gestión de ficheros y directorios.
---

## Índice de Contenidos

* TOC
{:toc}

# Tema 1: Manejo de Ficheros — Acceso a Datos
## Versión actualizada (java.nio.file, try-with-resources, JSON/XML, excepciones modernas)

> Apuntes del tema 1, revisados e integrados con contenidos actuales de Java (paquetes `java.io`, `java.nio.file`) y con soporte de JSON además de XML.

---

## Índice

1. Introducción
2. Ficheros: tipos y formas de acceso
3. Gestión de ficheros y directorios
   - 3.1 La clase clásica `File`
   - 3.2 La API moderna `java.nio.file` (`Path` y `Files`)
   - 3.3 Ejercicios
4. Flujos o Streams
   - 4.1 Tipos de Stream
   - 4.2 Codificación de caracteres (Charset)
   - 4.3 Clases de Streams de Caracteres
   - 4.4 Clases de Streams de Bytes
   - 4.5 Ejercicios
5. Gestión de excepciones de E/S
   - 5.1 Jerarquía de excepciones
   - 5.2 `try-catch-finally` vs. `try-with-resources`
   - 5.3 Buenas prácticas
6. Ficheros de Acceso Aleatorio
   - 6.1 Tamaño de los tipos de datos
   - 6.2 Ejercicios
7. Serialización y deserialización de objetos
   - 7.1 La interfaz `Serializable`
   - 7.2 `serialVersionUID` y `transient`
   - 7.3 Ejercicios
8. Ficheros de intercambio: XML y JSON
   - 8.1 XML con DOM
   - 8.2 XML con JAXB
   - 8.3 JSON con Jackson
   - 8.4 Ejercicios
9. Glosario

---

## 1 Introducción

Interesa que los programas guarden los datos que le hemos introducido, o los resultados que dicho programa haya obtenido, de manera que, si el programa termina su ejecución, los datos no se pierdan y puedan ser recuperados posteriormente. Dicho de otro modo, interesa que los datos **persistan**.

En los primeros tiempos de la informática, los datos se guardaban en **ficheros** convencionales. Con el tiempo, y la experiencia de trabajar con ellos, se observaron sus inconvenientes (**redundancia**, **inconsistencia**, **falta de integridad**, **fuerte dependencia entre programas y datos**), y para intentar solucionarlos surgieron las **bases de datos**.

Antes de que una aplicación pueda hablar con una base de datos relacional, con un ORM o con una base de datos documental, tiene que ser capaz de hacer algo mucho más básico: **leer y escribir información en el disco**. El fichero es la forma de persistencia más antigua y universal: ficheros de configuración, logs, exportaciones, copias de seguridad, ficheros de intercambio entre sistemas (JSON, XML, CSV)... Todo programador necesita dominar el acceso a ficheros.

Las clases Java que nos permiten trabajar con ficheros se encuentran principalmente en dos paquetes:

- **`java.io`**: paquete clásico, con la clase `File` y los streams tradicionales (`FileReader`, `FileInputStream`, `RandomAccessFile`...).
- **`java.nio.file`**: paquete moderno (desde Java 7), con `Path` y la clase de utilidades `Files`. Es más potente, ofrece excepciones más informativas y es la opción recomendada en proyectos actuales.

Ambos paquetes conviven: en código nuevo se prioriza `java.nio.file` para operaciones sobre el sistema de archivos, pero los streams de `java.io` (`Reader`/`Writer`, `InputStream`/`OutputStream`) se siguen usando para leer y escribir el contenido de los ficheros.

## 2 Ficheros: tipos y formas de acceso

### 2.1 Según el contenido: texto vs. binario

Un fichero, a nivel físico, es siempre una secuencia de bytes. La diferencia entre "texto" y "binario" está en **cómo interpretamos esos bytes**:

- **Texto**: los bytes se interpretan como caracteres según una codificación (*charset*) como UTF-8. Es legible por humanos con cualquier editor de texto.
- **Binario**: los bytes codifican información en un formato propio de la aplicación (una imagen, un ejecutable, un objeto Java serializado). No tiene sentido "leerlo como texto"; solo pueden abrirlo aplicaciones que entiendan cómo están dispuestos los bytes.

> **Nota:** el carácter "A" en UTF-8 ocupa 1 byte, pero un carácter como "ñ" o un emoji puede ocupar 2, 3 o 4 bytes. Si un fichero se escribió en UTF-8 y se lee asumiendo otra codificación (o viceversa), aparecen los famosos "caracteres corruptos" (mojibake), por ejemplo `Ã±` en lugar de `ñ`. Por eso es importante indicar siempre la codificación explícitamente al trabajar con flujos de caracteres.

### 2.2 Según la forma de acceso: secuencial vs. aleatorio

| | Acceso secuencial | Acceso aleatorio (directo) |
|---|---|---|
| **Definición** | Para acceder a una posición hay que pasar por todas las anteriores | Se puede acceder a una posición concreta sin pasar por las anteriores |
| **Analogía** | Una cinta de casete: para llegar a la canción 8 hay que pasar por la 1-7 | Un CD: el láser se posiciona directamente en la pista que se quiera |
| **Clases típicas en Java** | `FileReader`, `FileInputStream`, `BufferedReader`... | `RandomAccessFile` |
| **Cuándo usarlo** | Procesar todo el contenido (logs, informes, texto en general) | Leer o modificar un registro concreto sin releer todo el fichero (registros de longitud fija, índices) |
| **Coste de acceder al registro *n*** | O(n) — proporcional a la posición | O(1) — inmediato, si se conoce el desplazamiento |

**Ventajas e inconvenientes del acceso secuencial**
- Sencillo de programar, eficiente para procesar todo el fichero, fácil de depurar si es texto.
- Muy ineficiente si solo se necesita un registro concreto de un fichero grande; no permite actualizar un registro intermedio sin reescribir el fichero (salvo usando un fichero temporal).

**Ventajas e inconvenientes del acceso aleatorio**
- Acceso y modificación puntual muy rápidos; ideal para registros de longitud fija.
- Exige que los registros tengan longitud fija (o una estructura de índices adicional); el fichero no es directamente legible como texto; mayor complejidad de programación (cálculo de desplazamientos).

> **Error común:** confundir "fichero binario" con "fichero de acceso aleatorio". No son sinónimos: se puede tener un fichero binario de acceso secuencial (una imagen, que se lee de un tirón) y, en teoría, un fichero de texto de acceso aleatorio, aunque es poco habitual porque los caracteres en UTF-8 pueden ocupar un número variable de bytes.

## 3 Gestión de ficheros y directorios

### 3.1 La clase clásica `File`

La clase **File** (paquete `java.io`) proporciona información sobre archivos/carpetas y permite operaciones básicas (creación, renombrado, borrado). Las instancias de `File` representan *nombres* de archivo, no su contenido.

**Elementos más usados de la clase File**

| Categoría | Miembros |
|---|---|
| Constante | `File.pathSeparator` |
| Constructor | `File(String rutaFichero)` |
| Nombre | `getName()`, `getParent()`, `getAbsolutePath()`, `renameTo(File nuevoNombre)` |
| Predicados | `exists()`, `canWrite()`, `canRead()`, `isFile()`, `isDirectory()` |
| Información | `lastModified()`, `length()` |
| Borrar | `delete()` |
| Crear | `createNewFile()`, `mkdir()`, `mkdirs()` |
| Listar | `list()`, `list(FilenameFilter)`, `listFiles()`, `listFiles(FilenameFilter)` |

`File` sigue apareciendo mucho en proyectos y ejemplos antiguos, y algunas APIs (como `RandomAccessFile` o los constructores de `FileReader`/`FileWriter`) todavía la aceptan como parámetro. Pero **para código nuevo se recomienda `java.nio.file`**, más potente y con mejor gestión de errores.

### 3.2 La API moderna `java.nio.file` (`Path` y `Files`)

Desde Java 7, la forma recomendada de gestionar el sistema de archivos es mediante la clase **`Path`** (que representa una ruta, sustituyendo conceptualmente a `File`) y la clase de utilidades **`Files`**, que concentra prácticamente todas las operaciones: crear, copiar, mover, borrar, listar, comprobar existencia, leer atributos, etc.

**Crear directorios y ficheros**

```java
import java.nio.file.*;
import java.io.IOException;

public class GestionDirectorios {
    public static void main(String[] args) {
        Path carpetaCatalogo = Path.of("catalogo");
        Path subcarpetaImagenes = carpetaCatalogo.resolve("imagenes");

        try {
            // Crea el directorio "catalogo" si no existe, y también los padres necesarios
            Files.createDirectories(subcarpetaImagenes);
            System.out.println("Directorios creados correctamente.");

            Path ficheroConfig = carpetaCatalogo.resolve("config.txt");
            if (!Files.exists(ficheroConfig)) {
                Files.createFile(ficheroConfig);
            }
        } catch (IOException e) {
            System.err.println("Error al crear la estructura de directorios: " + e.getMessage());
        }
    }
}
```

> **Nota:** `Files.createDirectory()` (singular) lanza excepción si el directorio padre no existe. `Files.createDirectories()` (plural) crea toda la cadena de directorios intermedios, igual que `mkdir -p` en Linux.

**Copiar, mover y borrar**

```java
import java.nio.file.*;
import java.io.IOException;

public class OperacionesFichero {
    public static void main(String[] args) throws IOException {
        Path origen  = Path.of("catalogo/config.txt");
        Path copia   = Path.of("catalogo/config_copia.txt");
        Path destino = Path.of("catalogo/backup/config.txt");

        // Copiar (sobrescribiendo si ya existe)
        Files.copy(origen, copia, StandardCopyOption.REPLACE_EXISTING);

        // Mover / renombrar
        Files.createDirectories(destino.getParent());
        Files.move(copia, destino, StandardCopyOption.REPLACE_EXISTING);

        // Borrar de forma segura
        boolean borrado = Files.deleteIfExists(Path.of("catalogo/fichero_temporal.tmp"));
        System.out.println("¿Se borró el fichero temporal? " + borrado);
    }
}
```

> **Error común:** usar `Files.delete()` a secas sobre un fichero que puede no existir. Lanza `NoSuchFileException` y detiene el programa si no se controla. La alternativa segura es `Files.deleteIfExists()`, que devuelve `true`/`false` en lugar de lanzar excepción.

**Listar y recorrer directorios**

```java
import java.nio.file.*;
import java.io.IOException;
import java.util.stream.Stream;

public class ExploradorFicheros {
    public static void main(String[] args) throws IOException {
        Path raiz = Path.of("catalogo");

        // Listar solo el contenido directo (no recursivo)
        try (Stream<Path> listado = Files.list(raiz)) {
            listado.forEach(System.out::println);
        }

        // Recorrer TODO el árbol de subdirectorios (recursivo)
        try (Stream<Path> arbol = Files.walk(raiz)) {
            arbol.filter(Files::isRegularFile)
                 .forEach(p -> System.out.println("Fichero encontrado: " + p));
        }
    }
}
```

`Files.walk()` sustituye a la recursividad manual que se hacía antes con `File.listFiles()` (recorrer con un bucle y llamar recursivamente a la función por cada subdirectorio).

**Equivalencia entre `File` y `java.nio.file`**

| Operación | `java.io.File` (clásico) | `java.nio.file` (moderno) |
|---|---|---|
| Crear directorio | `mkdir()` / `mkdirs()` | `Files.createDirectory()` / `Files.createDirectories()` |
| Crear fichero | `createNewFile()` | `Files.createFile()` |
| Comprobar existencia | `exists()` | `Files.exists(path)` |
| Borrar | `delete()` | `Files.delete(path)` / `Files.deleteIfExists(path)` |
| Copiar | (no incluido, había que programarlo con streams) | `Files.copy(origen, destino, opciones)` |
| Mover / renombrar | `renameTo()` | `Files.move(origen, destino, opciones)` |
| Listar directorio | `list()` / `listFiles()` | `Files.list(path)` |
| Recorrer árbol recursivamente | Recursividad manual | `Files.walk(path)` |
| Tamaño | `length()` | `Files.size(path)` |
| Fecha de modificación | `lastModified()` | `Files.getLastModifiedTime(path)` |

### 3.3 Ejercicios

**Ejercicio 0**: Realiza un programa (usando `Path`/`Files`) que implemente las siguientes acciones:

1. Mostrar la ruta absoluta de la carpeta actual.
2. Pedir por teclado una ruta de fichero o carpeta y mostrar si lo introducido existe, si es un fichero o una carpeta, la fecha de modificación y el tamaño.
3. Mostrar el contenido de una carpeta cuya ruta se pide por teclado, comprobando que existe y que es una carpeta.
4. Crear una carpeta cuyo nombre se pide por teclado en la ruta por defecto, comprobando antes que no existe.
5. Crear un fichero cuyo nombre se pide por teclado, comprobando antes que no existe.
6. Pedir por teclado una ruta de fichero y un nuevo nombre, y renombrar el fichero original comprobando que existe y que el nuevo nombre no está ya en uso.

**Ejercicio 1**: Realiza un programa que reciba por parámetro nombres de ficheros y para cada uno de ellos muestre: si existe, si es fichero o carpeta, el nombre sin ruta, la ruta absoluta, si se puede leer/escribir/ejecutar, el tamaño y la fecha de última modificación.

**Ejercicio 2**: Realiza un programa que, usando `Files.walk()`, pida por teclado una ruta de carpeta y, si existe, muestre su contenido y el de todos sus subdirectorios (directos e indirectos).

**Ejercicio 3**: Realiza un programa que borre un fichero pasado por parámetro, comprobando antes que existe y pidiendo confirmación antes de borrarlo (usa `Files.deleteIfExists()`).

**Ejercicio 3b (nuevo)**: Crea una clase `ExploradorFicheros` que, recibiendo una ruta por parámetro, sea capaz de: mostrar el número total de ficheros y directorios (recursivamente), calcular el tamaño total ocupado en bytes (`Files.size()`), listar únicamente los ficheros con una extensión concreta, y crear una carpeta `backup` copiando dentro todos los ficheros de esa extensión.

## 4 Flujos o Streams

Un **flujo** o **stream** es un canal de comunicación entre un fichero y un programa Java, por el que circula información que puede leerse o escribirse de forma secuencial. Actúan de intermediario entre un programa y un fichero (o la memoria, o una operación de E/S). La vinculación del flujo con el dispositivo físico la hace el sistema de E/S de Java; las clases son independientes del dispositivo con el que se esté trabajando.

### 4.1 Tipos de Stream

Java diferencia claramente entre flujos **de bytes** (datos binarios) y flujos **de caracteres** (texto), evitando mezclar por error datos binarios con texto:

| | Flujos de bytes | Flujos de caracteres |
|---|---|---|
| **Tamaño** | 8 bits | 16 bits (Unicode) |
| **Clases base (abstractas)** | `InputStream` / `OutputStream` | `Reader` / `Writer` |
| **Para ficheros** | `FileInputStream` / `FileOutputStream` | `FileReader` / `FileWriter` |
| **Con buffer (recomendado)** | `BufferedInputStream` / `BufferedOutputStream` | `BufferedReader` / `BufferedWriter` |
| **Se usan para** | Imágenes, audio, vídeo, ejecutables, objetos serializados | Ficheros `.txt`, `.csv`, `.json`, `.xml`... |

La forma de trabajar con streams secuenciales es siempre la misma:

**Lectura**: 1) se abre el stream, 2) se va leyendo carácter a carácter, byte a byte o línea a línea, 3) se cierra el stream.

**Escritura (añadir)**: 1) se abre el stream (si no existe se crea), 2) se escribe la información, 3) se cierra el stream.

**Escritura (modificar o borrar)**: al no poder insertar/eliminar en medio de un fichero secuencial, hay que: crear un flujo de lectura sobre el original y otro de escritura sobre un fichero temporal; copiar los datos leídos al temporal tal cual hasta llegar al dato a modificar/borrar; escribir la versión nueva (o nada, si se borra); seguir copiando el resto; cerrar ambos ficheros; borrar el original y renombrar el temporal con su nombre.

### 4.2 Codificación de caracteres (Charset)

`FileReader` y `FileWriter`, si no se indica lo contrario, usan la codificación por defecto de la JVM/del sistema operativo, que puede variar entre un Windows en español y un servidor Linux. Desde Java 11 existen constructores que permiten indicar explícitamente el `Charset` (por ejemplo `new FileReader(fichero, StandardCharsets.UTF_8)`), y es una **buena práctica usarlos siempre**, para que el programa se comporte igual en cualquier equipo.

### 4.3 Clases de Streams de Caracteres

**Jerarquía:**

```
Reader
├── BufferedReader → LineNumberReader
├── CharArrayReader
├── InputStreamReader → FileReader
├── FilterReader → PushbackReader
├── PipedReader
└── StringReader

Writer
├── BufferedWriter
├── CharArrayWriter
├── OutputStreamWriter → FileWriter
├── FilterWriter
├── PipedWriter
├── StringWriter
└── PrintWriter
```

**FileReader / FileWriter**: cada lectura o escritura se hace físicamente en el disco duro. Si se leen o escriben pocos caracteres cada vez, el proceso es costoso y lento por los muchos accesos a disco.

**BufferedReader / BufferedWriter**: añaden un buffer intermedio que agrupa los accesos a disco, haciendo el programa más rápido. Además, `BufferedReader` permite leer línea a línea con `readLine()`.

**Ejemplo: leer un fichero de texto línea a línea, con buffer, codificación explícita y `try-with-resources`**

```java
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public class LectorTexto {
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
```

**Ejemplo: escribir en un fichero de texto, con buffer**

```java
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EscritorTexto {
    public static void main(String[] args) {
        List<String> productos = List.of("Teclado mecánico", "Ratón inalámbrico", "Monitor 27\"");

        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("catalogo/productos.txt", StandardCharsets.UTF_8))) {

            for (String producto : productos) {
                bw.write(producto);
                bw.newLine(); // salto de línea multiplataforma
            }
        } catch (IOException e) {
            System.err.println("Error de escritura: " + e.getMessage());
        }
    }
}
```

**Constructores y métodos principales**

- `FileReader(String ruta)` / `FileReader(File fichero)` / `FileReader(File fichero, Charset cs)`: lanza `FileNotFoundException` si no se puede abrir. Métodos: `read()` (devuelve -1 al llegar al final), `close()`.
- `BufferedReader(Reader in)`: método `readLine()`, que devuelve `null` al llegar al final.
- `FileWriter(String ruta, [boolean añadir])`: si `añadir` es `false` (o se omite), se sobrescribe; si es `true`, se añade al final. Si el fichero no existe, se crea. Métodos: `write(String s)`, `append(String texto)`, `flush()`, `close()`, `newLine()` (en `BufferedWriter`).

### 4.4 Clases de Streams de Bytes

**Jerarquía:**

```
InputStream
├── FileInputStream
├── PipedInputStream
├── FilterInputStream → BufferedInputStream, DataInputStream, PushbackInputStream
├── ByteArrayInputStream
├── SequenceInputStream
└── ObjectInputStream

OutputStream
├── FileOutputStream
├── PipedOutputStream
├── FilterOutputStream → BufferedOutputStream, DataOutputStream, PushbackOutputStream
├── ByteArrayOutputStream
└── ObjectOutputStream
```

Los archivos binarios guardan datos codificados en binario, sin caracteres reconocibles.

- **FileInputStream / FileOutputStream**: leen/escriben bytes de un fichero. `FileOutputStream` borra el contenido existente al abrir para escritura, salvo que se use el constructor con `append=true`.
- **DataInputStream / DataOutputStream**: leen/escriben tipos primitivos (`readInt`, `readChar`, `writeFloat`...) de forma independiente de la máquina. Al leer hasta el final se produce `EOFException`.
- **ObjectInputStream / ObjectOutputStream**: convierten objetos en bytes y viceversa (ver apartado de serialización).

**Ejemplo: copiar un fichero binario con buffer y `try-with-resources`**

```java
import java.io.*;

public class CopiadorImagen {
    public static void main(String[] args) {
        try (InputStream entrada = new BufferedInputStream(new FileInputStream("origen/logo.png"));
             OutputStream salida  = new BufferedOutputStream(new FileOutputStream("destino/logo_copia.png"))) {

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
```

> **Nota:** existe una forma mucho más corta de copiar ficheros: `Files.copy(origen, destino)` (apartado 3.2). Entonces, ¿por qué aprender a hacerlo "a mano" con flujos? Porque en el mundo real no siempre se copia un fichero tal cual: a menudo hace falta **procesar los datos mientras se leen o escriben** (cifrarlos, comprimirlos, convertir su formato, filtrar líneas...), y para eso hace falta dominar el manejo directo de flujos.

**Errores comunes con flujos**

1. **No cerrar los flujos**: cada flujo abierto consume un recurso del sistema operativo. Si no se cierra, se puede agotar el número de ficheros abiertos permitidos. Solución: usar siempre `try-with-resources`.
2. **Mezclar flujos de bytes y de caracteres sobre un mismo fichero de texto sin necesidad**: usar `FileInputStream` para leer un `.txt` en vez de `FileReader` obliga a convertir bytes a `String` manualmente sin gestionar bien la codificación.
3. **No usar buffer**: leer/escribir byte a byte o carácter a carácter directamente sobre el fichero es muy lento, porque cada operación implica una llamada al sistema operativo.
4. **Olvidar el `flush()`** en escenarios donde no se use `try-with-resources` ni se cierre correctamente el flujo.

### 4.5 Ejercicios

**Ejercicio 4**: Lee y muestra un fichero de texto con nombre, apellidos y fecha de nacimiento de personas separados por `;`. Muestra también el número de personas. El nombre del fichero se pide por teclado.

**Ejercicio 5**: Añade un texto al final de un fichero pasado por parámetro, creándolo si no existe (usa el modo `append` de `FileWriter`).

**Ejercicio 6**: Crea un fichero de personas (nombre, apellidos, teléfono) pidiendo los datos por teclado; si el fichero existe, añade registros.

**Ejercicio 7**: Modifica una línea concreta de un fichero de texto (pide el número de línea y los nuevos datos), reescribiendo el fichero con la técnica de fichero temporal.

**Ejercicio 8**: Igual que el anterior, pero para borrar una línea en vez de modificarla.

**Ejercicio 9**: Genera un fichero igual a uno dado, pero con las mayúsculas y minúsculas intercambiadas (`Hola` → `hOLA`).

**Ejercicio 10**: Gestiona una lista de teléfonos (número y nombre) en un fichero `Telefonos.dat`, permitiendo consultar, añadir (sin repeticiones), modificar y borrar por número.

**Ejercicio 11**: Genera un fichero binario que almacene un número entero, comparando el resultado con la tabla UTF-8.

**Ejercicio 12**: Muestra el número del fichero generado en el ejercicio anterior.

**Ejercicio 13**: Genera un fichero con notas de alumnos (expediente `int`, nota `double`, nombre `String` terminado en `\n`).

**Ejercicio 14**: Menú para consultar, modificar una nota y borrar un alumno del fichero del ejercicio anterior.

**Ejercicio 14b (nuevo)**: Lee un fichero de texto (`entrada.txt`) línea a línea y genera `salida.txt` con el texto en mayúsculas y el número de línea al principio de cada línea, usando flujos de caracteres con buffer y codificación UTF-8 explícita.

**Ejercicio 14c (nuevo)**: Implementa dos versiones de un programa que copie un fichero grande: una sin buffer (byte a byte) y otra con buffer. Mide el tiempo de cada una con `System.nanoTime()` y compara los resultados.

## 5 Gestión de excepciones de E/S

Prácticamente todas las operaciones de E/S en Java pueden fallar por causas externas al programa: el fichero no existe, no hay permisos, el disco está lleno... Por eso, los métodos de E/S declaran `throws IOException` (una excepción **comprobada**, *checked exception*), obligando a gestionarla.

### 5.1 Jerarquía de excepciones

```
Exception
 └── IOException
       ├── FileNotFoundException
       ├── EOFException
       ├── UncheckedIOException (envuelve IOException en streams/lambdas)
       └── (en java.nio.file) NoSuchFileException, FileAlreadyExistsException, AccessDeniedException...
```

### 5.2 `try-catch-finally` vs. `try-with-resources`

```java
// Estilo antiguo (antes de Java 7) — evitar en código nuevo
BufferedReader br = null;
try {
    br = new BufferedReader(new FileReader("datos.txt"));
    System.out.println(br.readLine());
} catch (IOException e) {
    System.err.println("Error: " + e.getMessage());
} finally {
    if (br != null) {
        try {
            br.close();
        } catch (IOException e) {
            System.err.println("Error al cerrar: " + e.getMessage());
        }
    }
}
```

```java
// Estilo moderno y recomendado: try-with-resources
try (BufferedReader br = new BufferedReader(new FileReader("datos.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    System.err.println("Error: " + e.getMessage());
}
// El recurso se cierra automáticamente al salir del try, incluso si hay excepción
```

`try-with-resources` funciona con cualquier clase que implemente `AutoCloseable` (todos los flujos de `java.io` la implementan), y puede gestionar varios recursos a la vez, separados por punto y coma, cerrándolos en orden inverso al de apertura.

### 5.3 Buenas prácticas

- Capturar la excepción más específica posible primero (`FileNotFoundException` antes que `IOException`), para dar mensajes de error más útiles.
- No "tragarse" la excepción con un `catch` vacío: como mínimo, registrar el error.
- Distinguir entre errores recuperables (reintentar, pedir otra ruta) y errores que deben propagarse.
- No usar excepciones para controlar el flujo normal: por ejemplo, no usar `FileNotFoundException` para comprobar si un fichero existe; para eso está `Files.exists()`.

> **Errores comunes**: capturar `Exception` a secas en vez de `IOException` (ocultando errores de programación como `NullPointerException`); dejar un `catch` vacío; reintentar una operación en bucle infinito sin límite ante un fallo persistente.

## 6 Ficheros de Acceso Aleatorio

Java proporciona la clase **RandomAccessFile** para este tipo de entrada/salida. Permite posicionarse en una posición concreta del fichero sin pasar por los datos anteriores, avanzar y retroceder. Es útil para ficheros cuyos **registros tienen un tamaño fijo**.

Permite leer y escribir con la misma clase (no hace falta una clase de lectura y otra de escritura).

**Constructores**

- `RandomAccessFile(String nombre, String modoAcceso)`
- `RandomAccessFile(File fichero, String modoAcceso)`

`modoAcceso` puede ser `"r"` (solo lectura, el fichero debe existir) o `"rw"` (lectura y escritura; si no existe, se crea).

Para leer y escribir se usan los métodos `readXXX`/`writeXXX` de `DataInputStream`/`DataOutputStream`. La clase maneja un puntero de posición (apunta al principio al abrir) que se desplaza con cada lectura/escritura según los bytes leídos/escritos. Al llegar al final se produce `EOFException`.

Métodos de gestión del puntero:

- `long getFilePointer()`: posición actual.
- `void seek(long posicion)`: mueve el puntero.
- `long length()`: tamaño del fichero en bytes.
- `int skipBytes(int desplazamiento)`: desplaza el puntero desde la posición actual.

### 6.1 Tamaño de los tipos de datos

| Tipo | Tamaño en bytes |
|---|---|
| char | 2 (Unicode) |
| int | 4 |
| double | 8 |
| short | 2 |
| byte | 1 |
| long | 8 |
| boolean | 1 |
| float | 4 |

> **Error común:** calcular mal el desplazamiento (`offset`) al mezclar tipos de distinto tamaño. Si el cálculo del tamaño de registro no es exacto, `seek()` apuntará a mitad de otro registro y la lectura devolverá basura. Conviene dibujar el "mapa de bytes" del registro antes de programar.

**Ejemplo: fichero de registros de longitud fija**

```java
import java.io.*;

public class FicheroAleatorioAlumnos {

    static final int LONGITUD_NOMBRE = 20;
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
```

### 6.2 Ejercicios

**Ejercicio 18**: Gestiona una agenda con ficheros de acceso aleatorio. Cada registro: índice (4 bytes), fecha (long, 8 bytes), descripción (String de 30 caracteres, 60 bytes), activo (boolean, 1 byte). El programa debe permitir ver la agenda, introducir cita (id autonumérico, siempre activa), modificar la descripción, borrar (reordenando ids), buscar citas activas por fecha, y desactivar una cita por id.

**Ejercicio 19**: Gestiona consultas de un doctor (id, nombre de paciente de 20 caracteres, fecha, diagnóstico de 50 caracteres) con acceso aleatorio: mostrar todas, introducir consulta, añadir diagnóstico, buscar por nombre (o parte) y por fecha.

**Ejercicio 19b (nuevo)**: Diseña un fichero de acceso aleatorio para una agenda de contactos (nombre 15 caracteres, teléfono 9 caracteres, contador de llamadas `int`). Implementa un menú: añadir contacto, consultar por posición, incrementar el contador de llamadas de un contacto sin reescribir los demás registros.

## 7 Serialización y deserialización de objetos

### 7.1 La interfaz `Serializable`

**Serializar** un objeto consiste en convertirlo en una secuencia de bits que pueden ser restaurados posteriormente para regenerar el objeto original. **Deserializar** es el proceso inverso.

La clase del objeto que se desea guardar/leer debe implementar la interfaz marcadora `java.io.Serializable` (no declara métodos, solo "marca" la clase).

```java
import java.io.Serializable;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private double precio;
    private transient String cacheTemporal; // "transient": NO se serializa

    public Producto(String nombre, double precio) {
        this.nombre = nombre;
        this.precio = precio;
    }

    @Override
    public String toString() {
        return "Producto{nombre='" + nombre + "', precio=" + precio + "}";
    }
    // getters / setters omitidos
}
```

Los métodos para guardar/leer objetos son:

- `void writeObject(Object obj)` (en `ObjectOutputStream`)
- `Object readObject()` (en `ObjectInputStream`) — al llegar al final del fichero se produce `EOFException`.

```java
import java.io.*;
import java.util.List;

public class SerializadorCatalogo {

    public static void guardarCatalogo(List<Producto> productos, String rutaFichero) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(rutaFichero))) {
            oos.writeObject(productos); // se puede serializar una colección entera
        }
    }

    @SuppressWarnings("unchecked")
    public static List<Producto> cargarCatalogo(String rutaFichero) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(rutaFichero))) {
            return (List<Producto>) ois.readObject();
        }
    }
}
```

**Problema al guardar varios objetos en un fichero**: cada vez que se abre el fichero para añadir un objeto se escribe una cabecera, lo que provoca error al leer (solo se espera una cabecera al principio). Para solucionarlo se puede redefinir el método `writeStreamHeader()` de `ObjectOutputStream` para que no haga nada, y usarlo solo a partir de la segunda escritura.

### 7.2 `serialVersionUID` y `transient`

El campo `serialVersionUID` identifica la "versión" de la clase para el mecanismo de serialización. Si se modifica la clase más adelante (por ejemplo, se añade un atributo) y no se cambia este identificador, Java seguirá aceptando deserializar ficheros antiguos si es compatible; pero si se omite, Java lo calcula automáticamente a partir de detalles internos de la clase, y **cualquier cambio mínimo puede invalidar todos los ficheros serializados anteriormente**, lanzando `InvalidClassException`. Por eso es buena práctica declararlo siempre explícitamente.

Un atributo marcado como `transient` no se serializa; al deserializar, recupera su valor por defecto (`null`, `0`, `false`).

**Ventajas e inconvenientes de la serialización nativa de Java**

- Muy sencilla de usar (una línea de código persiste un grafo completo de objetos, listas, mapas, referencias...).
- El fichero resultante solo lo puede leer Java (no interoperable con otros lenguajes, a diferencia de JSON/XML); es sensible a cambios en la clase; no es legible por humanos; puede suponer un riesgo de seguridad si se deserializan datos de origen no confiable.

**Errores comunes**

1. Olvidar que un atributo de tipo objeto también debe implementar `Serializable`, o se produce `NotSerializableException`.
2. Marcar como `transient` un campo imprescindible sin ser consciente de que, al deserializar, volverá a su valor por defecto.
3. Confundir serialización con clonación: sirve para crear una copia, pero no es la forma más eficiente si solo se necesita clonar en memoria.

### 7.3 Ejercicios

**Ejercicio 15**: Implementa la clase `Empleado` (nombre, sueldo) como `Serializable`. Gestiona un fichero de empleados: listar, dar de alta, borrar por nombre, buscar por parte del nombre, subir el sueldo a todos.

**Ejercicio 16**: Gestiona productos (código, nombre, precio) en un fichero binario de objetos: alta, listar todos, buscar por parte del nombre, modificar por código, borrar por código.

**Ejercicio 17**: Gestiona notas de alumnos (expediente, nombre, nota) como objetos: mostrar, añadir, poner nota, mostrar estadística (suspensos, aprobados, nota media), borrar.

**Ejercicio 17b (nuevo)**: Diseña una clase `Personaje` (nombre, nivel, puntos de vida, inventario como `List<String>`) `Serializable`, con `serialVersionUID` explícito. Crea un pequeño programa que guarde el estado del personaje al salir y lo recupere exactamente igual al volver a arrancar.

## 8 Ficheros de intercambio: XML y JSON

Java permite desarrollar aplicaciones portables. Los formatos de texto estandarizados permiten el intercambio de información entre sistemas no compatibles. Los dos más habituales son:

- **XML** (eXtensible Markup Language): basado en etiquetas anidadas; muy usado en configuración, documentos, sistemas heredados y cuando se necesita validación estricta con esquemas (XSD).
- **JSON** (JavaScript Object Notation): más ligero, basado en pares clave-valor; es el estándar de facto en APIs REST modernas.

**El mismo dato en ambos formatos**

```json
{
  "nombre": "Teclado mecánico",
  "precio": 59.90,
  "disponible": true,
  "categorias": ["periféricos", "oficina"]
}
```

```xml
<producto>
    <nombre>Teclado mecánico</nombre>
    <precio>59.90</precio>
    <disponible>true</disponible>
    <categorias>
        <categoria>periféricos</categoria>
        <categoria>oficina</categoria>
    </categorias>
</producto>
```

**JSON vs. XML**

| | JSON | XML |
|---|---|---|
| Legibilidad | Alta, muy compacto | Alta, pero más verboso |
| Tamaño del fichero | Menor | Mayor (etiquetas de apertura/cierre) |
| Metadatos (atributos, namespaces, esquemas) | Limitado | Muy completo |
| Uso típico actual | APIs REST, configuración | Documentos empresariales, SOAP, sistemas heredados |
| Soporte nativo en JavaScript | Total | Requiere parseo adicional |

Existen dos estrategias para trabajar con estos formatos:

- **Parser (DOM/SAX en XML)**: se recorre el árbol del documento "a mano", nodo a nodo. Control total, más código.
- **Binding (mapeo automático)**: una librería (JAXB para XML, Jackson para JSON) convierte automáticamente entre objetos Java (POJOs) y el documento. Mucho más productivo; es el enfoque habitual en código profesional actual.

### 8.1 XML con DOM

La tecnología **DOM (Document Object Model)** es una interfaz de programación que permite analizar y manipular dinámicamente el contenido, el estilo y la estructura de un documento. El documento se carga en memoria en forma de árbol de nodos; si el fichero XML es muy grande, esta tecnología es menos eficiente por el consumo de memoria. Las clases están en los paquetes **javax.xml** y **org.w3c.dom**.

**Pasos en el uso de DOM**

```java
DocumentBuilderFactory factoria = DocumentBuilderFactory.newInstance();
DocumentBuilder db = factoria.newDocumentBuilder();
Document doc = db.parse("direcciones.xml");
```

**Métodos habituales para recorrer un árbol DOM**

- `documento.getDocumentElement()`: nodo raíz.
- `elemento.hasAttributes()`, `elemento.getAttributes()`, `elemento.getAttribute("nombre")`.
- `elemento.getChildNodes()`, `nodo.getNodeType()`.
- `doc.getElementsByTagName("nombre")`.
- `elemento.getParentNode()`, `eltoTexto.getTextContent()`, `eltoTexto.setNodeValue("texto")`.

**Ejemplo: leer un XML de productos con DOM**

```java
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;

public class LectorXmlDom {
    public static void main(String[] args) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document documento = builder.parse(new File("productos.xml"));

        documento.getDocumentElement().normalize();
        NodeList listaProductos = documento.getElementsByTagName("producto");

        for (int i = 0; i < listaProductos.getLength(); i++) {
            Element producto = (Element) listaProductos.item(i);
            String nombre = producto.getElementsByTagName("nombre").item(0).getTextContent();
            String precio = producto.getElementsByTagName("precio").item(0).getTextContent();
            System.out.println("Producto: " + nombre + " - " + precio + " €");
        }
    }
}
```

**Crear un fichero XML a partir de un DOM**: crear una fuente (`new DOMSource(doc)`), un destino (`new StreamResult(new File("fichero.xml"))`), un transformador (`TransformerFactory.newInstance().newTransformer()`) y transformar (`t.transform(fuente, destino)`).

### 8.2 XML con JAXB

**JAXB** (Java Architecture for XML Binding) es una tecnología de *binding* que, a diferencia de DOM, permite además validar un esquema asociado al documento XML. Trabaja sobre JavaBeans (clases con getters/setters y constructores) anotados con `@XmlRootElement`, `@XmlType`, `@XmlElement`, `@XmlElementWrapper` y `@XmlAttribute`.

> **Aviso importante**: `javax.xml.bind` (JAXB) formaba parte del JDK hasta Java 8, pero **se eliminó del JDK a partir de Java 11** (pasó a ser un módulo opcional de Java EE, ya deprecado). En proyectos con Java 11 o superior es imprescindible añadir las dependencias correspondientes (por ejemplo, vía Maven: `jakarta.xml.bind-api` y una implementación como `jaxb-runtime`) para poder usarlo. En muchos proyectos actuales, JAXB se sustituye directamente por **Jackson con su módulo XML** (`jackson-dataformat-xml`), que permite usar el mismo modelo de anotaciones tanto para JSON como para XML.

**Marshal (Java → XML)**

```java
JAXBContext contexto = JAXBContext.newInstance(Libreria.class);
Marshaller m = contexto.createMarshaller();
m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
m.marshal(lib, new File("libreria.xml"));
```

**Unmarshal (XML → Java)**

```java
JAXBContext contexto = JAXBContext.newInstance(Libreria.class);
Unmarshaller um = contexto.createUnmarshaller();
Libreria lib = (Libreria) um.unmarshal(new File("libreria.xml"));
```

### 8.3 JSON con Jackson

**Jackson** es la librería de *binding* JSON más usada en el ecosistema Java. Su clase central es `ObjectMapper`, que convierte automáticamente entre POJOs y JSON.

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.util.List;

public class GestorJsonProductos {

    public static void guardarJson(List<Producto> productos, String ruta) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON legible
        mapper.writeValue(new File(ruta), productos);
    }

    public static List<Producto> leerJson(String ruta) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(ruta), mapper.getTypeFactory()
                .constructCollectionType(List.class, Producto.class));
    }
}
```

> **Nota**: para que Jackson funcione sin fricción con una clase como `Producto`, esta necesita un constructor vacío o los getters/setters estándar (o anotarse con `@JsonCreator`/`@JsonProperty`).

**Conversión entre formatos (JSON → XML)**: gracias al *binding*, convertir de un formato a otro se reduce a leer con un mapper y escribir con otro, sin tocar la lógica de negocio:

```java
ObjectMapper jsonMapper = new ObjectMapper();
List<Producto> productos = jsonMapper.readValue(new File("productos.json"),
        jsonMapper.getTypeFactory().constructCollectionType(List.class, Producto.class));

XmlMapper xmlMapper = new XmlMapper();
xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
xmlMapper.writeValue(new File("productos_convertido.xml"), productos);
```

**Errores comunes con XML/JSON**

1. No cerrar el flujo subyacente cuando se trabaja con parsers de bajo nivel sobre un `InputStream`.
2. Asumir que el documento siempre tendrá una estructura válida: un JSON o XML mal formado lanza excepciones de parseo (`JsonParseException`, `SAXException`) que hay que prever.
3. Confundir claves opcionales con obligatorias al mapear a POJOs: si un campo no existe y el atributo Java es un tipo primitivo (`int`) en vez de su clase envolvente (`Integer`), el mapeo puede fallar o dar valores por defecto engañosos.
4. Olvidar la codificación del fichero al leer/escribir XML (la cabecera `<?xml version="1.0" encoding="UTF-8"?>` debe coincidir con la codificación real del fichero).

### 8.4 Ejercicios

**Ejercicio 20**: Muestra en consola el contenido de `direcciones.xml` recorriendo el árbol DOM de forma recursiva, sin conocer previamente su estructura.

**Ejercicio 21**: Crea un documento XML a partir de los datos del fichero `agenda.bin` del Ejercicio 18.

**Ejercicio 22**: A partir del XML del ejercicio anterior, ofrece un menú para añadir cita, mostrar citas activas, mostrar citas de un día y borrar (desactivar) una cita, actualizando el fichero en cada modificación.

**Ejercicio 23**: Gestiona un fichero XML de libros (isbn, título, número de ejemplares): añadir, mostrar, modificar y borrar por isbn.

**Ejercicio 24**: Con JAXB, crea un fichero XML de una librería (nombre, lugar, CP y lista de libros con autor, nombre, editorial e isbn).

**Ejercicio 25**: Con JAXB, muestra por consola el contenido del XML de la librería del ejercicio anterior con un formato legible.

**Ejercicio 26**: Con JAXB, gestiona los jugadores de un club de fútbol (club: nombre, dirección, número RFEF, lista de jugadores; jugador: NIF, nombre, posición, dorsal). Permite ver datos del club, ver jugadores, añadir (sin NIF duplicados), modificar y borrar por NIF.

**Ejercicio 27 (nuevo)**: Amplía el catálogo de productos (ejercicios 15/16) para que el programa ofrezca un menú: cargar catálogo desde JSON, cargar desde XML, guardar en JSON, guardar en XML, mostrar catálogo por pantalla. El objetivo es comprobar que, gracias al *binding*, la lógica interna (la lista de objetos `Producto`) es independiente del formato externo.

**Ejercicio 28 (nuevo, proyecto integrador)**: Desarrolla una aplicación de consola que gestione un catálogo de productos (`nombre`, `precio`, `stock`, `categoría`):

1. Menú: alta de producto, listar catálogo, buscar por nombre, guardar catálogo, cargar catálogo, salir.
2. El catálogo se mantiene en memoria como `List<Producto>` mientras se ejecuta el programa.
3. Al guardar, se puede elegir formato: JSON, XML o serialización nativa Java (`.ser`).
4. Al cargar, el programa detecta el formato según la extensión y reconstruye la lista de objetos.
5. Cada alta, baja o modificación se añade como línea a un fichero `operaciones.log` (acceso secuencial, flujo de caracteres, modo *append*).
6. Toda operación de E/S debe usar `try-with-resources` y mostrar mensajes de error comprensibles.
7. **(Ampliación opcional)**: añade un fichero de acceso aleatorio `indice.dat` que guarde, para cada producto, su nombre y el offset de su registro completo en un fichero de registros de longitud fija, para poder recuperar un producto por su nombre sin leer todo el fichero.

## 9 Glosario

- **Buffer**: zona de memoria intermedia que agrupa varias operaciones de E/S en una sola llamada al sistema operativo, mejorando el rendimiento.
- **Charset (codificación)**: conjunto de reglas que asocia caracteres a secuencias de bytes (UTF-8, ISO-8859-1...).
- **Binding**: mapeo automático entre un documento (JSON/XML) y objetos del lenguaje de programación, sin parseo manual.
- **Deserializar**: reconstruir un objeto en memoria a partir de su representación en bytes.
- **Flujo (stream)**: canal por el que fluyen datos, byte a byte o carácter a carácter, entre el programa y un origen/destino.
- **Offset (desplazamiento)**: posición, en bytes, dentro de un fichero, a partir de la cual se realiza una operación de lectura/escritura.
- **Parser**: componente que analiza sintácticamente un documento (XML/JSON) y permite recorrer su estructura.
- **Serializar**: convertir el estado de un objeto en una secuencia de bytes para poder almacenarlo o transmitirlo.
- **try-with-resources**: construcción de Java que garantiza el cierre automático de los recursos (`AutoCloseable`) usados en un bloque `try`.
