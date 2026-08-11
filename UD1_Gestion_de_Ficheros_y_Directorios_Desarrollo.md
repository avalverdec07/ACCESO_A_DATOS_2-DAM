# UD1. Gestión de ficheros y directorios
## Desarrollo completo de contenidos — Módulo Acceso a Datos (0486) — 2º DAM

> **Lenguaje de referencia:** Java (paquetes `java.io`, `java.nio.file`, `java.util.stream`). Cuando resulte relevante se indica el equivalente conceptual en C# (`System.IO`), ya que ambos lenguajes son habituales para impartir este módulo. Los ejemplos de JSON usan la librería **Jackson**; en C# el equivalente directo sería `System.Text.Json`.

---

## Índice de la unidad

1. Introducción y mapa de la unidad
2. Bloque 1 — Tipos de fichero y formas de acceso
3. Bloque 2 — Gestión de ficheros y directorios
4. Bloque 3 — Flujos de bytes y de caracteres
5. Bloque 4 — Ficheros secuenciales y de acceso aleatorio
6. Bloque 5 — Serialización y deserialización de objetos
7. Bloque 6 — Ficheros de intercambio: XML y JSON
8. Bloque 7 — Excepciones de entrada/salida
9. Proyecto integrador de la unidad
10. Cuestionario de autoevaluación
11. Glosario de la unidad

---

## 1. Introducción y mapa de la unidad

Antes de que una aplicación pueda hablar con una base de datos relacional, con un ORM o con MongoDB (contenidos de las próximas unidades), tiene que ser capaz de hacer algo mucho más básico y, a la vez, fundamental: **leer y escribir información en el disco**. El fichero es la forma de persistencia más antigua, más universal y, en muchos casos, la única disponible: ficheros de configuración, logs, exportaciones, copias de seguridad, ficheros de intercambio entre sistemas (JSON, XML, CSV)... Todo programador, use la tecnología que use, necesita dominar el acceso a ficheros.

### Analogía cotidiana inicial

Piensa en un **archivador de oficina**. Puedes organizarlo por carpetas (directorios) y dentro de cada carpeta guardar documentos (ficheros). Puedes:
- Leer un documento entero de una vez (como abrir un PDF completo).
- Leer una ficha concreta de un fichero de fichas alfabético, sin tener que pasar por todas las anteriores (acceso aleatorio, como el fichero de un archivador de tarjetas).
- Fotocopiar (copiar), tirar (borrar) o cambiar de carpeta (mover) un documento.
- Traducir un documento de un idioma a otro sin cambiar su contenido (conversión de formato: por ejemplo, de JSON a XML).

Esta unidad recorre exactamente estas operaciones, pero programadas.

### Mapa conceptual de la unidad

```
GESTIÓN DE FICHEROS Y DIRECTORIOS
│
├── Operaciones sobre el sistema de archivos (crear, borrar, copiar, mover, listar)
│
├── Flujos (Streams)
│     ├── De bytes  → datos binarios (imágenes, ejecutables...)
│     └── De caracteres → texto
│
├── Formas de acceso
│     ├── Secuencial → se lee de principio a fin
│     └── Aleatorio  → se accede directamente a cualquier posición
│
├── Serialización de objetos → persistir el estado de un objeto Java
│
├── Ficheros de intercambio (XML / JSON)
│     ├── Parsers (DOM / SAX)
│     └── Binding (Jackson, JAXB...)
│
└── Tratamiento de excepciones de E/S
```

---

## 2. Bloque 1 — Tipos de fichero y formas de acceso

### 2.1 Fichero de texto vs. fichero binario

Un fichero, a nivel físico, es siempre una secuencia de bytes. La diferencia entre "texto" y "binario" está en **cómo interpretamos esos bytes**:

- **Fichero de texto:** los bytes se interpretan como caracteres según una codificación (*charset*) como UTF-8 o ISO-8859-1. Es legible por humanos con cualquier editor de texto.
- **Fichero binario:** los bytes codifican información en un formato propio de la aplicación (una imagen, un ejecutable, un vídeo, un objeto Java serializado). No tiene sentido "leerlo como texto".

> **💡 ¿Sabías qué...?**
> El carácter "A" en un fichero de texto UTF-8 ocupa **1 byte** (el valor 65 en decimal, `01000001` en binario). Pero un emoji como 😀 puede ocupar **4 bytes**. Por eso, cuando trabajamos con flujos de caracteres, es tan importante indicar siempre la codificación explícitamente: si el fichero se escribió en UTF-8 y se lee asumiendo ISO-8859-1 (o viceversa), aparecerán los famosos "caracteres corruptos" (mojibake), por ejemplo `Ã±` en lugar de `ñ`.

### 2.2 Formas de acceso: secuencial vs. aleatorio

| | Acceso secuencial | Acceso aleatorio (directo) |
|---|---|---|
| **Definición** | Se lee/escribe empezando por el principio, registro a registro, en orden | Se puede saltar directamente a cualquier posición del fichero sin recorrer las anteriores |
| **Analogía** | Una cinta de casete: para llegar a la canción 8 tienes que pasar por la 1-7 | Un disco de vinilo o un CD: bajas la aguja/el láser directamente en la pista que quieras |
| **Clase típica en Java** | `FileReader`, `FileInputStream`, `BufferedReader`... | `RandomAccessFile` |
| **Cuándo usarlo** | Procesar todo el contenido (logs, informes, ficheros de texto en general) | Necesitas leer o modificar un registro concreto sin releer todo el fichero (ficheros de registros de longitud fija, índices) |
| **Coste de acceder al registro *n*** | O(n) — proporcional a la posición | O(1) — inmediato, si conocemos el desplazamiento |

### Ventajas e inconvenientes

**Acceso secuencial**
- ✅ Ventajas: sencillo de programar, eficiente para procesar todo el fichero, ficheros de texto (fáciles de depurar a simple vista), buena compresión.
- ❌ Inconvenientes: muy ineficiente si solo necesitamos un registro concreto de un fichero grande; no permite actualizar un registro intermedio sin reescribir el fichero (salvo que uses técnicas de sustitución con fichero temporal).

**Acceso aleatorio**
- ✅ Ventajas: acceso y modificación puntual muy rápidos; ideal para ficheros de registros de longitud fija (tipo "base de datos casera").
- ❌ Inconvenientes: exige que los registros tengan **longitud fija** (o una estructura de índices adicional); el fichero no es directamente legible como texto; más complejidad de programación (cálculo de desplazamientos).

### ⚠️ Error común
> Confundir "fichero binario" con "fichero de acceso aleatorio". No son sinónimos: se puede tener un fichero binario de acceso secuencial (como una imagen JPEG, que se lee de un tirón) y, en teoría, un fichero de texto de acceso aleatorio (aunque es poco habitual, porque los caracteres en UTF-8 pueden ocupar un número variable de bytes, lo que complica calcular desplazamientos fijos).

---

## 3. Bloque 2 — Gestión de ficheros y directorios

En Java, la API moderna para gestionar el sistema de archivos es `java.nio.file` (desde Java 7), con las clases `Path`, `Paths`/`Path.of()` y, sobre todo, la clase de utilidades `Files`. La clase clásica `java.io.File` sigue existiendo y se ve mucho en proyectos antiguos, pero **en un ciclo de Grado Superior en 2026 se debe enseñar y priorizar `java.nio.file`**, ya que es más potente, más segura (lanza excepciones informativas) y es la recomendada en la documentación oficial.

### 3.1 Crear directorios y ficheros

```java
import java.nio.file.*;
import java.io.IOException;

public class GestionDirectorios {
    public static void main(String[] args) {
        Path carpetaCatalogo = Path.of("catalogo");
        Path subcarpetaImagenes = carpetaCatalogo.resolve("imagenes");

        try {
            // Crea el directorio "catalogo" si no existe
            Files.createDirectories(subcarpetaImagenes); // crea también los padres necesarios
            System.out.println("Directorios creados correctamente.");

            // Crear un fichero vacío dentro
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

> **💡 ¿Sabías qué...?**
> `Files.createDirectory()` (en singular) lanza excepción si el directorio padre no existe. `Files.createDirectories()` (en plural) crea toda la cadena de directorios intermedios necesarios, igual que el comando `mkdir -p` en Linux o `md` con la opción recursiva en Windows.

### 3.2 Copiar, mover y borrar

```java
import java.nio.file.*;
import java.nio.file.StandardCopyOption;
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

        // Borrar
        boolean borrado = Files.deleteIfExists(Path.of("catalogo/fichero_temporal.tmp"));
        System.out.println("¿Se borró el fichero temporal? " + borrado);
    }
}
```

### ⚠️ Error común
> Usar `Files.delete()` a secas sobre un fichero que puede no existir. Esto lanza `NoSuchFileException` y detiene el programa si no se controla. La alternativa segura es `Files.deleteIfExists()`, que devuelve `true`/`false` en lugar de lanzar excepción cuando el fichero no está.

### 3.3 Listar y recorrer directorios

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

Este método `Files.walk()` sustituye a la típica recursividad manual que se hacía con `File.listFiles()` en versiones antiguas de Java (recorrer con un bucle y llamar recursivamente a la función por cada subdirectorio).

### 🎯 Actividad de aula 2.1 — "El explorador programado"

**Objetivo:** afianzar las operaciones básicas del sistema de archivos.

Crea una clase `ExploradorFicheros` que, recibiendo una ruta por parámetro, sea capaz de:
1. Mostrar el número total de ficheros y de directorios que contiene (recursivamente).
2. Calcular el tamaño total ocupado en bytes (pista: `Files.size(path)`).
3. Listar únicamente los ficheros con una extensión concreta (por ejemplo, todos los `.txt`).
4. Crear una carpeta `backup` y copiar dentro todos los ficheros `.txt` encontrados.

**Ampliación (para quien termine antes):** que el programa acepte la extensión a filtrar como argumento por consola y muestre además la fecha de última modificación de cada fichero (`Files.getLastModifiedTime()`).

---

## 4. Bloque 3 — Flujos de bytes y de caracteres

### 4.1 ¿Qué es un flujo (stream)?

Un **flujo** es una abstracción que representa un canal de comunicación por el que fluyen datos, byte a byte o carácter a carácter, entre el programa y un origen/destino (un fichero, la red, la consola...).

### Analogía cotidiana

Piensa en una **tubería de agua**. El agua (los datos) fluye en una dirección: de la fuente (el fichero) al grifo (tu programa) cuando lees, o del grifo a la fuente cuando escribes. No puedes "teletransportarte" al medio de la tubería: el agua fluye en orden, gota a gota (byte a byte o carácter a carácter), salvo que uses técnicas específicas de acceso aleatorio (Bloque 4).

### 4.2 La jerarquía de clases de `java.io`

Java diferencia claramente entre flujos **de bytes** (para datos binarios) y flujos **de caracteres** (para texto), lo que evita mezclar por error datos binarios con texto:

| | Flujos de bytes | Flujos de caracteres |
|---|---|---|
| **Clases base (abstractas)** | `InputStream` / `OutputStream` | `Reader` / `Writer` |
| **Para ficheros** | `FileInputStream` / `FileOutputStream` | `FileReader` / `FileWriter` |
| **Con buffer (recomendado)** | `BufferedInputStream` / `BufferedOutputStream` | `BufferedReader` / `BufferedWriter` |
| **Se usan para...** | Imágenes, audio, vídeo, ejecutables, objetos serializados | Ficheros de texto: `.txt`, `.csv`, `.json`, `.xml`... |

> **💡 ¿Sabías qué...?**
> `FileReader` y `FileWriter`, por defecto, usan la codificación por defecto de la JVM/del sistema operativo, lo que puede variar entre un Windows en español y un servidor Linux. Desde Java 11 existen constructores que permiten indicar explícitamente el `Charset` (p. ej. `new FileReader(fichero, StandardCharsets.UTF_8)`), y es una **buena práctica profesional usarlos siempre**, para que el programa se comporte igual en cualquier equipo.

### 4.3 Leer un fichero de texto línea a línea (flujo de caracteres, con buffer)

```java
import java.io.*;
import java.nio.charset.StandardCharsets;

public class LectorTexto {
    public static void main(String[] args) {
        Path fichero = Path.of("catalogo/config.txt");

        // try-with-resources: cierra el flujo automáticamente aunque haya excepción
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

Nota: en el ejemplo se usa `Path` importado como en el bloque anterior, ajusta el `import java.nio.file.Path;` si lo copias como fichero independiente.

### 4.4 Escribir en un fichero de texto (con buffer)

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

### 4.5 Copiar un fichero binario (imagen) con flujos de bytes

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

> **💡 ¿Sabías qué...?**
> Existe una forma mucho más corta de copiar ficheros: `Files.copy(origen, destino)` (visto en el Bloque 2). Entonces, ¿por qué aprender a hacerlo "a mano" con flujos? Porque en el mundo real no siempre copias un fichero tal cual: a menudo necesitas **procesar los datos mientras los lees o escribes** (cifrarlos, comprimirlos, convertir su formato, filtrar líneas...), y para eso necesitas dominar el manejo directo de flujos.

### ⚠️ Errores comunes con flujos

1. **No cerrar los flujos.** Cada flujo abierto consume un recurso del sistema operativo (un *file handle*). Si no se cierra, se puede llegar a agotar el número de ficheros abiertos permitidos (`Too many open files`). **Solución:** usar siempre `try-with-resources`, que garantiza el cierre automático incluso si se produce una excepción.
2. **Mezclar flujos de bytes y de caracteres sobre el mismo fichero de texto sin necesidad.** Es un error de diseño habitual en quien empieza: usar `FileInputStream` para leer un `.txt` en vez de `FileReader`, obligándote a convertir bytes a `String` manualmente sin gestionar bien la codificación.
3. **No usar buffer.** Leer o escribir byte a byte / carácter a carácter directamente sobre el fichero (sin `BufferedReader`/`BufferedInputStream`) es extremadamente lento, porque cada operación de E/S implica una llamada al sistema operativo. El buffer agrupa esas operaciones.
4. **Olvidar el `flush()` en flujos sin buffer automático o antes de cerrar en ciertos escenarios.** Aunque `close()` normalmente hace *flush* implícito, en flujos compuestos manualmente conviene tenerlo presente.

### 🎯 Actividad de aula 4.1 — "El traductor de mayúsculas"

Escribe un programa que lea un fichero de texto (`entrada.txt`) línea a línea y genere un fichero nuevo (`salida.txt`) con todo el texto convertido a mayúsculas, usando flujos de caracteres con buffer. Añade el número de línea al principio de cada línea del fichero de salida.

### 🎯 Actividad de aula 4.2 — "Comparador de tiempos"

Implementa dos versiones del mismo programa que copie un fichero de 50 MB: una **sin** buffer (leyendo byte a byte con `FileInputStream`/`FileOutputStream`) y otra **con** buffer (`BufferedInputStream`/`BufferedOutputStream`). Mide el tiempo de ejecución de cada una con `System.currentTimeMillis()` o `System.nanoTime()` y compara los resultados en un pequeño informe. **Objetivo pedagógico:** entender de forma tangible por qué el buffer es tan importante en rendimiento.

---

## 5. Bloque 4 — Ficheros secuenciales y de acceso aleatorio

### 5.1 La clase `RandomAccessFile`

`RandomAccessFile` permite moverse libremente dentro de un fichero (leer y escribir en cualquier posición) mediante un **puntero de posición** que se controla con el método `seek(long posicion)`.

Para que el acceso aleatorio funcione de forma predecible, **los registros deben tener longitud fija en bytes**. Si un registro ocupa, por ejemplo, 64 bytes, el registro número *n* (empezando en 0) empieza siempre en la posición `n * 64`.

### 5.2 Ejemplo: fichero de registros de longitud fija (ficha de un alumno)

Vamos a construir un fichero donde cada "ficha" ocupa exactamente 40 bytes: 20 caracteres para el nombre (rellenado con espacios) y un `long` (8 bytes) para el DNI/NIE numérico, más un `int` (4 bytes) para la edad, más 8 bytes de margen de seguridad para futuras ampliaciones... En este ejemplo simplificamos a: 20 caracteres de nombre (40 bytes en UTF-16, que es como Java escribe `writeChars`) + un `int` para la edad (4 bytes).

```java
import java.io.*;

public class FicheroAleatorioAlumnos {

    static final int LONGITUD_NOMBRE = 20;         // caracteres
    static final int TAMANIO_REGISTRO = LONGITUD_NOMBRE * 2 + 4; // 2 bytes/car. (UTF-16) + int edad

    public static void escribirAlumno(RandomAccessFile raf, int indice, String nombre, int edad) throws IOException {
        raf.seek((long) indice * TAMANIO_REGISTRO);

        // Rellenamos o truncamos el nombre a longitud fija
        StringBuilder sb = new StringBuilder(nombre);
        sb.setLength(LONGITUD_NOMBRE); // trunca o rellena con caracteres nulos
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

        String nombre = new String(nombreChars).trim();
        System.out.println("Alumno #" + indice + " -> Nombre: " + nombre + ", Edad: " + edad);
    }

    public static void main(String[] args) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile("alumnos.dat", "rw")) {
            escribirAlumno(raf, 0, "Ana García", 19);
            escribirAlumno(raf, 1, "Luis Pérez", 20);
            escribirAlumno(raf, 2, "Marta Ruiz", 18);

            // Acceso DIRECTO al registro 1, sin leer el 0 antes
            leerAlumno(raf, 1);

            // Actualizamos solo la edad del alumno 2 (registro índice 2)
            raf.seek((long) 2 * TAMANIO_REGISTRO + LONGITUD_NOMBRE * 2); // saltamos el nombre
            raf.writeInt(19); // cumpleaños: ahora tiene 19

            leerAlumno(raf, 2);
        }
    }
}
```

### Ventajas e inconvenientes de `RandomAccessFile`

- ✅ Ventajas: acceso y modificación de un registro concreto en tiempo constante O(1); no hace falta reescribir el fichero completo para modificar un dato; útil para simular un "mini motor de base de datos" o ficheros de índices.
- ❌ Inconvenientes: exige diseñar cuidadosamente el formato del registro (longitud fija); si un dato variable (como el nombre) se trunca mal se pierde información; el fichero deja de ser legible directamente como texto; borrar un registro intermedio no "encoge" el fichero (hay que marcarlo como libre o compactar).

### ⚠️ Error común
> Calcular mal el desplazamiento (`offset`) al mezclar tipos de distinto tamaño en bytes: un `char` en Java ocupa 2 bytes (UTF-16), un `int` ocupa 4, un `long` ocupa 8, un `double` ocupa 8... Si el cálculo de `TAMANIO_REGISTRO` no es exacto, `seek()` apuntará a mitad de otro registro y la lectura devolverá basura. **Consejo docente:** dibujar en la pizarra el "mapa de bytes" del registro antes de programar.

### 🎯 Actividad de aula 5.1 — "Agenda de contactos con acceso directo"

Diseña un fichero de acceso aleatorio para una agenda de contactos donde cada registro tenga: nombre (15 caracteres), teléfono (9 caracteres) y un `int` con el número de veces que se ha llamado a ese contacto. Implementa un menú por consola con las opciones: añadir contacto, consultar contacto por posición, incrementar el contador de llamadas de un contacto sin reescribir los demás.

---

## 6. Bloque 5 — Serialización y deserialización de objetos

### 6.1 Concepto

**Serializar** un objeto es convertir su estado (los valores de sus atributos) en una secuencia de bytes que se puede guardar en un fichero (o enviar por red). **Deserializar** es el proceso inverso: reconstruir el objeto en memoria a partir de esos bytes.

### Analogía cotidiana

Es como **hacer las maletas para mudarte de casa**. "Serializar" tu salón es meter cada mueble (cada atributo del objeto) en una caja etiquetada; "deserializar" en la casa nueva es sacar cada mueble de su caja y volver a montarlo exactamente igual que estaba.

### 6.2 La interfaz `Serializable`

En Java, para que un objeto se pueda serializar con los mecanismos estándar, su clase debe implementar la interfaz marcadora `java.io.Serializable` (no declara métodos, simplemente "marca" la clase como serializable).

```java
import java.io.Serializable;

public class Producto implements Serializable {
    private static final long serialVersionUID = 1L; // ver "Sabías qué" más abajo

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
    // getters / setters omitidos por brevedad
}
```

### 6.3 Serializar (`ObjectOutputStream`) y deserializar (`ObjectInputStream`)

```java
import java.io.*;
import java.util.ArrayList;
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

    public static void main(String[] args) throws Exception {
        List<Producto> catalogo = new ArrayList<>();
        catalogo.add(new Producto("Teclado mecánico", 59.90));
        catalogo.add(new Producto("Monitor 27\"", 249.00));

        guardarCatalogo(catalogo, "catalogo.ser");

        List<Producto> catalogoRecuperado = cargarCatalogo("catalogo.ser");
        catalogoRecuperado.forEach(System.out::println);
    }
}
```

> **💡 ¿Sabías qué...?**
> El campo `serialVersionUID` identifica la "versión" de la clase para el mecanismo de serialización. Si más adelante modificas la clase `Producto` (por ejemplo, añades un atributo nuevo) y no cambias este identificador, Java seguirá aceptando deserializar ficheros antiguos siempre que sea compatible; pero si lo omites, Java lo calcula automáticamente a partir de los detalles internos de la clase, y **cualquier cambio mínimo en la clase puede invalidar todos los ficheros serializados anteriormente**, lanzando `InvalidClassException`. Por eso es una buena práctica profesional declararlo siempre explícitamente.

### Ventajas e inconvenientes de la serialización nativa de Java

- ✅ Ventajas: muy sencilla de usar (una línea de código para persistir un grafo completo de objetos, incluidas listas, mapas, referencias entre objetos...); no hay que escribir manualmente el "mapeo" campo a campo.
- ❌ Inconvenientes: el fichero resultante **solo lo puede leer Java** (no es interoperable con aplicaciones en otros lenguajes, a diferencia de JSON/XML); es sensible a cambios en la clase (ver aviso anterior); no es legible por humanos; puede suponer un riesgo de seguridad si se deserializan datos de origen no confiable (ataques de deserialización).

### ⚠️ Errores comunes
1. Olvidar que un atributo de tipo objeto (por ejemplo, una referencia a otra clase) **también debe implementar `Serializable`**, o se producirá `NotSerializableException` en tiempo de ejecución.
2. Marcar como `transient` un campo imprescindible sin darse cuenta de que, al deserializar, ese campo volverá a su valor por defecto (`null`, `0`, `false`), y no al valor que tenía antes de guardarlo.
3. Confundir serialización con clonación: serializar/deserializar es una forma de crear una copia profunda, pero no es su propósito principal ni la forma más eficiente si solo se necesita clonar en memoria.

### 🎯 Actividad de aula 6.1 — "Guardián de partidas"

Diseña una clase `Personaje` (nombre, nivel, puntos de vida, inventario como `List<String>`) que implemente `Serializable`. Crea un pequeño "juego" por consola que permita guardar el estado del personaje al salir y recuperarlo exactamente igual al volver a arrancar el programa.

---

## 7. Bloque 6 — Ficheros de intercambio de datos: XML y JSON

### 7.1 ¿Por qué formatos de intercambio?

La serialización nativa de Java (Bloque 5) es cómoda, pero **no interoperable**: un sistema en Python o un frontend en JavaScript no pueden leer un `.ser`. Para intercambiar información entre sistemas distintos se usan formatos de texto estandarizados, siendo los dos más habituales:

- **XML** (eXtensible Markup Language): basado en etiquetas anidadas, muy usado en configuración, servicios SOAP, documentos.
- **JSON** (JavaScript Object Notation): más ligero, basado en pares clave-valor, es el estándar de facto en APIs REST modernas.

### Ejemplo comparativo del mismo dato en ambos formatos

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

### Ventajas e inconvenientes: JSON vs. XML

| | JSON | XML |
|---|---|---|
| **Legibilidad** | Alta, muy compacto | Alta, pero más verboso |
| **Tamaño del fichero** | Menor | Mayor (etiquetas de apertura/cierre) |
| **Soporte de metadatos (atributos, namespaces, esquemas robustos como XSD)** | Limitado | Muy completo |
| **Uso típico actual** | APIs REST, configuración, ficheros de intercambio ligeros | Documentos empresariales, SOAP, sistemas heredados, cuando se necesita validación estricta con XSD |
| **Soporte nativo en JavaScript** | Total (es su propio formato) | Requiere parseo adicional |

### 7.2 Dos estrategias: *parsing* manual vs. *binding* automático

- **Parser (DOM/SAX en XML, o parsers JSON de bajo nivel):** tú recorres el árbol del documento "a mano", nodo a nodo. Tienes control total, pero es más código.
- **Binding (mapeo automático objeto-documento):** una librería (Jackson para JSON, JAXB para XML) convierte automáticamente entre tus objetos Java (POJOs) y el documento, mediante anotaciones o convención de nombres. Es mucho más productivo y es el enfoque que se usa en la inmensa mayoría del código profesional actual.

### 7.3 JSON con *binding* automático (Jackson)

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.File;
import java.util.List;

public class GestorJsonProductos {

    public static void guardarJson(List<Producto> productos, String ruta) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // JSON "bonito" y legible
        mapper.writeValue(new File(ruta), productos);
    }

    public static List<Producto> leerJson(String ruta) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(ruta), mapper.getTypeFactory()
                .constructCollectionType(List.class, Producto.class));
    }
}
```

> Nota: para que Jackson funcione "sin fricción" con nuestra clase `Producto` del Bloque 5, esta necesita un constructor vacío o los getters/setters estándar (o bien anotarse con `@JsonCreator`/`@JsonProperty` en el constructor).

### 7.4 XML con *parsing* mediante DOM (control manual)

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

### 7.5 Conversión entre formatos (JSON → XML)

Una tarea profesional muy habitual: recibir datos en un formato y entregarlos en otro. Con *binding*, la conversión se reduce a **leer con un mapper y escribir con otro**, sin tocar la lógica de negocio:

```java
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import java.util.List;

public class ConversorJsonAXml {
    public static void main(String[] args) throws Exception {
        ObjectMapper jsonMapper = new ObjectMapper();
        List<Producto> productos = jsonMapper.readValue(
                new java.io.File("productos.json"),
                jsonMapper.getTypeFactory().constructCollectionType(List.class, Producto.class));

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.writeValue(new java.io.File("productos_convertido.xml"), productos);
    }
}
```

> **💡 ¿Sabías qué...?**
> Este patrón de "leer con un formato y escribir con otro usando el mismo modelo de objetos intermedio" es exactamente lo que hacen por dentro muchas herramientas ETL (Extract-Transform-Load) empresariales, y es la base conceptual de cómo un ORM (que veremos en la próxima unidad) traduce entre objetos Java y filas de una tabla relacional.

### ⚠️ Errores comunes con XML/JSON
1. **No cerrar el flujo subyacente** cuando se trabaja con parsers de bajo nivel que reciben un `InputStream` en vez de un `File`.
2. **Asumir que el documento siempre tendrá una estructura válida.** Un JSON o XML mal formado (una coma de más, una etiqueta sin cerrar) lanza excepciones de parseo (`JsonParseException`, `SAXException`) que hay que prever.
3. **Confundir claves opcionales con obligatorias** al mapear a POJOs: si un campo JSON no existe y el atributo Java es un tipo primitivo (`int`, `double`) en lugar de su clase envolvente (`Integer`, `Double`), puede fallar el mapeo o quedar con valores por defecto engañosos (`0` en vez de "sin dato").
4. Olvidar la codificación del fichero al leer/escribir XML (los ficheros XML declaran su codificación en la cabecera `<?xml version="1.0" encoding="UTF-8"?>`, pero si el fichero real no coincide, aparecerán errores o caracteres corruptos).

### 🎯 Actividad de aula 7.1 — "El traductor universal de catálogos"

Amplía el catálogo de productos (Bloque 5/6) para que el programa ofrezca un menú:
1. Cargar catálogo desde JSON.
2. Cargar catálogo desde XML.
3. Guardar catálogo actual en JSON.
4. Guardar catálogo actual en XML.
5. Mostrar catálogo por pantalla.

El objetivo es que el alumnado compruebe que, gracias al *binding*, la lógica interna (la lista de objetos `Producto`) es independiente del formato externo.

---

## 8. Bloque 7 — Excepciones de entrada/salida

### 8.1 La jerarquía de `IOException`

Prácticamente todas las operaciones de E/S en Java pueden fallar por causas externas al propio programa: el fichero no existe, no hay permisos, el disco está lleno, la red se corta... Por eso, los métodos de E/S declaran `throws IOException` (una excepción **comprobada**, *checked exception*), obligando al programador a gestionarla.

Jerarquía simplificada relevante para esta unidad:

```
Exception
 └── IOException
       ├── FileNotFoundException
       ├── EOFException
       ├── UncheckedIOException (envuelve IOException en contextos de streams/lambdas)
       └── (en java.nio) NoSuchFileException, FileAlreadyExistsException, AccessDeniedException...
```

### 8.2 `try-catch-finally` clásico vs. `try-with-resources`

```java
// Estilo ANTIGUO (antes de Java 7) — evitar en código nuevo
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
// Estilo MODERNO y recomendado: try-with-resources
try (BufferedReader br = new BufferedReader(new FileReader("datos.txt"))) {
    System.out.println(br.readLine());
} catch (IOException e) {
    System.err.println("Error: " + e.getMessage());
}
// El recurso se cierra automáticamente al salir del bloque try, incluso si hay excepción
```

El `try-with-resources` funciona con cualquier clase que implemente la interfaz `AutoCloseable` (todos los flujos de `java.io` la implementan), y puede gestionar **varios recursos a la vez**, separados por punto y coma, cerrándolos en orden inverso al de apertura.

### 8.3 Buenas prácticas de gestión de excepciones en E/S

- Capturar la excepción **más específica posible** primero (`FileNotFoundException` antes que `IOException`), para dar mensajes de error más útiles al usuario.
- No "tragarse" la excepción con un `catch` vacío: como mínimo, registrar (log) el error.
- Distinguir entre errores recuperables (reintentar, pedir otra ruta) y errores que deben propagarse hacia arriba.
- No usar excepciones para controlar el flujo normal del programa (por ejemplo, no uses `FileNotFoundException` para "comprobar" si un fichero existe: usa `Files.exists()` para eso).

### ⚠️ Errores comunes
1. Capturar `Exception` a secas en vez de `IOException`, ocultando errores de programación (como `NullPointerException`) que deberían solucionarse en el código, no "silenciarse".
2. Dejar un `catch` vacío ("tragarse" la excepción), lo que convierte el debugging en una pesadilla porque el error desaparece sin dejar rastro.
3. Volver a intentar una operación de E/S en bucle infinito sin límite ni espera, ante un fallo persistente (por ejemplo, un disco desconectado), bloqueando la aplicación.

### 🎯 Actividad de aula 8.1 — "El programa a prueba de balas"

Retoma el programa de la Actividad 2.1 (el explorador de ficheros) y refactorízalo para que:
- Use `try-with-resources` en todos los flujos.
- Distinga y trate de forma diferenciada al menos tres tipos de fallo: ruta inexistente, sin permisos de lectura, y ruta que en realidad es un fichero y no un directorio.
- Muestre mensajes de error claros y orientados al usuario final (no trazas de pila crudas).

---

## 9. Proyecto integrador de la unidad — "Gestor de catálogo multiformato"

Este proyecto cierra la UD1 integrando todos los bloques trabajados, y sienta las bases del "hilo conductor" (el catálogo de productos) que se reutilizará en las unidades siguientes del módulo (bases de datos relacionales, ORM, documentales...).

### Especificación

Desarrolla una aplicación de consola que gestione un catálogo de productos (`nombre`, `precio`, `stock`, `categoría`) con las siguientes funcionalidades:

1. **Menú principal** con las opciones: Alta de producto, Listar catálogo, Buscar producto por nombre, Guardar catálogo, Cargar catálogo, Salir.
2. El catálogo se mantiene en memoria como una `List<Producto>` mientras el programa se ejecuta.
3. Al elegir "Guardar catálogo", el usuario puede elegir el formato de exportación: JSON, XML o serialización nativa Java (`.ser`).
4. Al elegir "Cargar catálogo", el programa detecta automáticamente el formato según la extensión del fichero indicado y reconstruye la lista de objetos.
5. Se debe incluir una opción de "Exportar log de operaciones": cada alta, baja o modificación se añade como una línea a un fichero `operaciones.log` (acceso secuencial, flujo de caracteres, en modo *append*).
6. Toda operación de E/S debe estar protegida con `try-with-resources` y mostrar mensajes de error comprensibles.
7. **(Ampliación opcional)** Implementa además un fichero de acceso aleatorio `indice.dat` que guarde, para cada producto, su nombre y la posición (offset) en la que se encuentra su registro completo dentro de un fichero de registros de longitud fija, de forma que se pueda recuperar un producto por su nombre sin leer todo el fichero.

### Rúbrica orientativa de evaluación del proyecto

| Criterio | Insuficiente | Adecuado | Excelente |
|---|---|---|---|
| Gestión de ficheros/directorios | No usa `Files`/`Path` o falla en operaciones básicas | Usa correctamente crear/copiar/borrar | Además gestiona errores de forma robusta y con mensajes claros |
| Flujos de texto/binarios | Mezcla flujos de forma incorrecta | Usa el flujo adecuado en cada caso | Usa buffer y codificación explícita en todos los casos |
| Serialización/JSON/XML | Solo implementa un formato | Implementa al menos dos formatos correctamente | Implementa los tres formatos e interconversión |
| Tratamiento de excepciones | `catch` genéricos o vacíos | `try-with-resources` y captura específica | Además diferencia tipos de error y registra un log |
| Documentación del código | Sin comentarios | Comentarios básicos | Javadoc completo y README de uso |

---

## 10. Cuestionario de autoevaluación

1. ¿Cuál es la diferencia esencial entre un flujo de bytes y uno de caracteres? Pon un ejemplo de cuándo usarías cada uno.
2. ¿Por qué `Files.deleteIfExists()` es preferible a `Files.delete()` en la mayoría de los casos?
3. Explica con tus palabras qué significa que el acceso aleatorio tenga coste O(1) y el secuencial O(n).
4. ¿Qué ocurre si serializas un objeto Java, modificas la clase añadiendo un atributo nuevo, y luego intentas deserializar un fichero antiguo sin haber definido `serialVersionUID`?
5. Cita dos ventajas de JSON frente a XML y dos ventajas de XML frente a JSON.
6. ¿Qué diferencia hay entre un *parser* (DOM/SAX) y una herramienta de *binding* como Jackson?
7. ¿Por qué es recomendable usar siempre `try-with-resources` en vez de `try-catch-finally` clásico al trabajar con ficheros?
8. Diseña (en papel, sin código) el formato de un registro de longitud fija para almacenar una "reserva de hotel" (nombre del huésped, número de habitación, número de noches). Indica cuántos bytes ocuparía cada campo y el total del registro.

---

## 11. Glosario de la unidad

- **Buffer:** zona de memoria intermedia que agrupa varias operaciones de E/S en una sola llamada al sistema operativo, mejorando el rendimiento.
- **Charset (codificación):** conjunto de reglas que asocia caracteres a secuencias de bytes (UTF-8, ISO-8859-1...).
- **Binding:** mapeo automático entre un documento (JSON/XML) y objetos del lenguaje de programación, sin parseo manual.
- **Deserializar:** reconstruir un objeto en memoria a partir de su representación en bytes.
- **Flujo (stream):** canal por el que fluyen datos, byte a byte o carácter a carácter, entre el programa y un origen/destino.
- **Offset (desplazamiento):** posición, en bytes, dentro de un fichero, a partir de la cual se realiza una operación de lectura/escritura.
- **Parser:** componente que analiza sintácticamente un documento (XML/JSON) y permite recorrer su estructura.
- **Serializar:** convertir el estado de un objeto en una secuencia de bytes para poder almacenarlo o transmitirlo.
- **try-with-resources:** construcción de Java que garantiza el cierre automático de los recursos (`AutoCloseable`) usados en un bloque `try`.
