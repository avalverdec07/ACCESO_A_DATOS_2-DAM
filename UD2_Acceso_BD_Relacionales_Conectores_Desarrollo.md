# UD2. Acceso a bases de datos relacionales mediante conectores
## Desarrollo completo de contenidos — Módulo Acceso a Datos (0486) — 2º DAM

> **Lenguaje y tecnología de referencia:** Java + **JDBC** (Java Database Connectivity). Cuando aporte valor se indica el equivalente conceptual en C# (**ADO.NET**: `SqlConnection`/`NpgsqlConnection`, `SqlCommand`, `SqlDataReader`...). SGBD de referencia: **MySQL/MariaDB** o **PostgreSQL** (independiente) y **SQLite/H2** (embebido), ambos de software libre.
>
> Esta unidad retoma el **catálogo de productos** trabajado en UD1 (ficheros) y lo traslada a una base de datos relacional, ampliándolo a un pequeño dominio de gestión de pedidos (`productos`, `clientes`, `pedidos`, `lineas_pedido`), que servirá de hilo conductor también en UD3 (ORM).

---

## Índice de la unidad

1. Introducción y mapa de la unidad
2. Bloque 1 — El desfase objeto-relacional
3. Bloque 2 — Gestores embebidos e independientes
4. Bloque 3 — El conector/driver y el establecimiento de la conexión
5. Bloque 4 — Sentencias DDL y DML: creando y modificando datos
6. Bloque 5 — Consultas y tratamiento del resultado (`ResultSet`)
7. Bloque 6 — Sentencias preparadas y seguridad (inyección SQL)
8. Bloque 7 — Procedimientos almacenados
9. Bloque 8 — Transacciones
10. Bloque 9 — Cierre de recursos y buenas prácticas
11. Proyecto integrador de la unidad
12. Cuestionario de autoevaluación
13. Glosario de la unidad

---

## 1. Introducción y mapa de la unidad

En UD1 aprendimos a persistir información en ficheros. Pero un fichero de texto o incluso un fichero de acceso aleatorio se queda corto en cuanto la aplicación necesita: consultas complejas y flexibles, acceso simultáneo de varios usuarios, garantías de integridad ante fallos, o simplemente **más de unos pocos miles de registros**. Ahí es donde entra el **sistema gestor de bases de datos (SGBD) relacional**.

Esta unidad no enseña a diseñar bases de datos (eso ya se trabajó en el módulo de 1º *Bases de datos*), sino a **programar aplicaciones que se conectan a una base de datos ya existente y operan sobre ella** desde código: abrir la conexión, enviar sentencias SQL, procesar los resultados, y hacerlo todo de forma segura y eficiente.

### Analogía cotidiana inicial

Piensa en la diferencia entre **guardar tus apuntes en una carpeta de folios sueltos** (UD1: ficheros) y **guardar tus apuntes en la biblioteca municipal**, donde hay un bibliotecario (el SGBD) al que le pides lo que necesitas mediante una petición formal (SQL), y él busca, filtra, ordena y te lo entrega; además, ese mismo bibliotecario puede atender a cientos de personas a la vez sin que se pisen unas a otras (concurrencia), y lleva un registro estricto de quién saca y devuelve qué (transacciones).

### Mapa conceptual de la unidad

```
ACCESO A BD RELACIONALES MEDIANTE CONECTORES
│
├── Desfase objeto-relacional (por qué esto no es trivial)
│
├── Tipos de gestor
│     ├── Embebido   → SQLite, H2 (vive dentro de la propia app)
│     └── Independiente → MySQL, PostgreSQL (servidor aparte)
│
├── Conector / Driver JDBC
│     └── Conexión (Connection) + Connection Pooling
│
├── Sentencias
│     ├── DDL  → CREATE, ALTER, DROP
│     ├── DML  → INSERT, UPDATE, DELETE
│     └── Consultas → SELECT + ResultSet
│
├── PreparedStatement → parámetros + seguridad (anti inyección SQL)
│
├── Procedimientos almacenados → CallableStatement
│
├── Transacciones → commit / rollback
│
└── Cierre ordenado de recursos
```

---

## 2. Bloque 1 — El desfase objeto-relacional (*object-relational impedance mismatch*)

### 2.1 ¿Qué es el desfase objeto-relacional?

Cuando programamos en Java pensamos en **objetos**: instancias de clases, con atributos, con relaciones de herencia, con colecciones (`List`, `Map`), con identidad propia en memoria. Cuando diseñamos una base de datos relacional pensamos en **tablas**: filas y columnas, claves primarias y foráneas, sin herencia nativa, sin "colecciones" como tipo de columna. Estas dos formas de modelar el mundo **no encajan de forma natural**, y a esa fricción se le llama desfase (o impedancia) objeto-relacional. Se manifiesta, entre otros, en estos problemas:

| Problema | En objetos (Java) | En relacional (SQL) |
|---|---|---|
| **Identidad** | Cada objeto tiene una identidad de memoria (referencia) | Cada fila se identifica por su clave primaria (un valor, no una referencia) |
| **Herencia** | Una clase puede heredar de otra (`Empleado extends Persona`) | No existe herencia nativa entre tablas; hay que "simularla" con varias estrategias de modelado |
| **Relaciones y colecciones** | Un objeto `Pedido` puede tener una `List<LineaPedido>` como atributo | Las relaciones se representan mediante claves foráneas entre tablas separadas, no como una colección embebida |
| **Granularidad** | Un objeto puede combinar varios valores en un tipo compuesto (p. ej. una clase `Direccion`) | Una tabla relacional tradicional prefiere valores atómicos por columna (1FN) |

### Analogía cotidiana

Es como intentar traducir un poema entre dos idiomas con estructuras gramaticales muy distintas (por ejemplo, del español al japonés): puedes traducir el significado, pero la forma nunca encaja perfectamente "palabra a palabra". Hace falta un "traductor" que sepa adaptar una estructura a la otra sin perder información. En esta unidad, ese "traductor" lo escribimos nosotros mismos, sentencia SQL a sentencia SQL; en la UD3 veremos cómo una herramienta (el ORM) automatiza gran parte de esa traducción.

> **💡 ¿Sabías qué...?**
> El término *object-relational impedance mismatch* lo popularizó el mundo académico de bases de datos en los años 90, y sigue siendo, más de tres décadas después, una de las razones por las que existen tantas alternativas de persistencia distintas (ORM, bases de datos orientadas a documentos, bases de datos orientadas a objetos...), cada una tomando una postura diferente ante este problema. Lo verás con tus propios ojos al comparar UD2 (SQL puro), UD3 (ORM) y UD5 (documental) sobre el mismo dominio de datos.

### ⚠️ Error común
> Diseñar las clases Java "calcando" mentalmente la tabla y solo pensar en el modelo relacional al programar. Es más productivo diseñar primero el modelo de objetos que tiene sentido para la aplicación, y luego resolver conscientemente el mapeo hacia las tablas, siendo conscientes de en qué se parecen y en qué no.

---

## 3. Bloque 2 — Gestores embebidos e independientes

### 3.1 Diferencias clave

| | Gestor embebido | Gestor independiente |
|---|---|---|
| **Dónde se ejecuta** | Dentro del mismo proceso que la aplicación (misma JVM) | En un proceso/servidor aparte, normalmente en otra máquina o en otro puerto |
| **Ejemplos** | SQLite, H2, Apache Derby | MySQL, MariaDB, PostgreSQL, Oracle, SQL Server |
| **Instalación** | Ninguna instalación de servidor; el fichero de BD viaja con la app | Requiere instalar y administrar un servidor de BD |
| **Acceso concurrente multiusuario en red** | Muy limitado o inexistente | Diseñado para ello |
| **Casos de uso típicos** | Apps de escritorio/móviles, pruebas automatizadas, prototipos rápidos | Aplicaciones cliente-servidor, web, empresariales |

### Analogía cotidiana

Un gestor **embebido** es como tener una nevera pequeña en tu propia habitación: solo tú la usas, no hay que compartirla ni gestionar colas. Un gestor **independiente** es como la cocina compartida de un edificio de apartamentos: varias personas la usan a la vez, hace falta coordinación (turnos, normas) para que nadie se pise, y por eso existe alguien (el SGBD servidor) que gestiona el acceso concurrente.

### Ventajas e inconvenientes

**Gestor embebido**
- ✅ Cero configuración de servidor, arranque instantáneo, ideal para tests automatizados (H2 en memoria) y para distribuir apps que no necesitan red.
- ❌ Escalabilidad y concurrencia muy limitadas; no apto para que muchos usuarios trabajen a la vez sobre los mismos datos.

**Gestor independiente**
- ✅ Alta concurrencia, herramientas de administración avanzadas, replicación, copias de seguridad centralizadas, más robusto para producción.
- ❌ Requiere instalación, configuración y mantenimiento; añade latencia de red; mayor complejidad operativa.

> **💡 ¿Sabías qué...?**
> SQLite es probablemente el motor de bases de datos más desplegado del mundo: está integrado en cada navegador (para el almacenamiento local), en Android e iOS, y en miles de aplicaciones de escritorio. Aun así, su propia documentación oficial desaconseja su uso cuando se necesita alta concurrencia de escritura desde múltiples procesos, precisamente por su naturaleza embebida.

### 🎯 Actividad de aula 3.1 — "Embebido vs. independiente en vivo"

Instala **H2** en modo embebido (fichero local) y conéctate a un **MySQL/MariaDB** ya desplegado en el aula. Ejecuta la misma sentencia `CREATE TABLE` y una inserción de 3 filas en ambos motores desde un pequeño programa Java, y anota en un documento: tiempo de arranque, pasos de configuración necesarios y diferencias observadas en la cadena de conexión.

---

## 4. Bloque 3 — El conector/driver y el establecimiento de la conexión

### 4.1 ¿Qué es JDBC?

**JDBC** es una **API estándar de Java** (un conjunto de interfaces: `Connection`, `Statement`, `ResultSet`...) que define *cómo* un programa Java debe comunicarse con cualquier base de datos relacional, sin importar el fabricante. Cada SGBD proporciona su propia implementación de esas interfaces en forma de **driver JDBC** (una librería `.jar`), que es el "traductor" real entre las llamadas Java y el protocolo de red específico de ese SGBD.

### Analogía cotidiana

JDBC es como un **enchufe universal de viaje**: el enchufe (la API JDBC) es siempre igual desde el punto de vista del aparato que conectas (tu programa Java), pero necesitas el **adaptador correcto** (el driver) según el país (el SGBD) al que vayas: un adaptador para MySQL, otro para PostgreSQL, otro para SQLite... El programa Java, gracias a JDBC, apenas cambia entre un SGBD y otro; solo cambian la URL de conexión y el driver.

### 4.2 Añadir el driver al proyecto

```xml
<!-- Ejemplo con Maven: driver de MySQL/MariaDB -->
<dependency>
    <groupId>com.mysql</groupId>
    <artifactId>mysql-connector-j</artifactId>
    <version>8.4.0</version>
</dependency>

<!-- Ejemplo con Maven: driver de PostgreSQL -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>
```

### 4.3 Establecer la conexión

```java
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {

    private static final String URL = "jdbc:mysql://localhost:3306/catalogo_tienda";
    private static final String USUARIO = "dam_alumno";
    private static final String CONTRASENA = "clave_segura";

    public static Connection obtenerConexion() throws SQLException {
        // Desde JDBC 4.0 (Java 6+) no hace falta Class.forName() para cargar el driver,
        // se descubre automáticamente por el mecanismo ServiceLoader.
        return DriverManager.getConnection(URL, USUARIO, CONTRASENA);
    }

    public static void main(String[] args) {
        try (Connection conexion = obtenerConexion()) {
            System.out.println("Conexión establecida correctamente con: "
                    + conexion.getMetaData().getDatabaseProductName());
        } catch (SQLException e) {
            System.err.println("Error al conectar con la base de datos: " + e.getMessage());
        }
    }
}
```

Anatomía de la URL de conexión: `jdbc:<subprotocolo>://<host>:<puerto>/<nombre_bd>?<parámetros_opcionales>`. Por ejemplo, para PostgreSQL sería `jdbc:postgresql://localhost:5432/catalogo_tienda`, y para SQLite embebido (sin host ni puerto, apunta a un fichero) `jdbc:sqlite:catalogo.db`.

> **💡 ¿Sabías qué...?**
> Antes de JDBC 4.0 (anterior a Java 6, año 2006) era obligatorio escribir `Class.forName("com.mysql.cj.jdbc.Driver")` al principio del programa para forzar la carga de la clase del driver en memoria. Hoy en día ya no es necesario porque los drivers modernos se registran automáticamente mediante el mecanismo `ServiceLoader` de Java, pero **todavía verás esa línea en mucho código y en muchos tutoriales antiguos**; si aparece, no es un error, simplemente es una precaución que ya no hace falta.

### 4.4 *Connection pooling*

Abrir una conexión a un SGBD independiente es una operación **relativamente costosa** (negociación de red, autenticación...). Si una aplicación abre y cierra una conexión nueva por cada operación, el rendimiento se resiente mucho, especialmente en aplicaciones web con muchas peticiones concurrentes. La solución es un **pool de conexiones**: un conjunto de conexiones ya abiertas y "listas para usar" que se reutilizan entre distintas operaciones, en lugar de crearlas y destruirlas constantemente.

```java
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class PoolConexiones {

    private static final HikariDataSource dataSource;

    static {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/catalogo_tienda");
        config.setUsername("dam_alumno");
        config.setPassword("clave_segura");
        config.setMaximumPoolSize(10); // máximo de conexiones simultáneas en el pool
        dataSource = new HikariDataSource(config);
    }

    public static Connection obtenerConexion() throws SQLException {
        return dataSource.getConnection(); // "presta" una conexión del pool
    }
}
```

Nota didáctica: **HikariCP** es actualmente el pool de conexiones de referencia en el ecosistema Java (usado por defecto en Spring Boot), por lo que es una buena elección para introducir el concepto de forma realista, aunque para el alumnado que empieza puede bastar con explicar el concepto y hacer una demostración, dejando la implementación completa como contenido de ampliación.

### Analogía cotidiana del *pooling*

Es como una **flota de taxis en una parada**, en vez de fabricar un taxi nuevo cada vez que alguien necesita ir a algún sitio (crear una conexión nueva) y "desguazarlo" al llegar (cerrarla): los taxis (conexiones) esperan en la parada (el pool), se cogen cuando se necesitan, y al terminar el trayecto vuelven a la parada para que los use el siguiente cliente, en lugar de destruirse.

### ⚠️ Error común
> No cerrar nunca las conexiones obtenidas de un pool (`connection.close()`). Ojo: cuando se usa un *pool*, `close()` **no destruye realmente la conexión física**, sino que la devuelve al pool para que otro la reutilice. Si el programador olvida llamar a `close()`, la conexión queda "prestada" indefinidamente y el pool acaba agotándose (`Connection is not available, request timed out`), aunque parezca que "no se ha cerrado nada".

### 🎯 Actividad de aula 4.1 — "Primer contacto"

Crea un pequeño programa que se conecte a la base de datos de referencia de la unidad (`catalogo_tienda`) y muestre por consola: el nombre y versión del SGBD, el catálogo/esquema activo, y el listado de nombres de todas las tablas existentes (pista: `connection.getMetaData().getTables(...)`).

---

## 5. Bloque 4 — Sentencias DDL y DML: creando y modificando datos

### 5.1 Base de datos de referencia de la unidad

```sql
CREATE TABLE clientes (
    id_cliente   INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    email        VARCHAR(150) UNIQUE NOT NULL
);

CREATE TABLE productos (
    id_producto  INT AUTO_INCREMENT PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    precio       DECIMAL(8,2) NOT NULL,
    stock        INT NOT NULL DEFAULT 0
);

CREATE TABLE pedidos (
    id_pedido    INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente   INT NOT NULL,
    fecha        DATE NOT NULL,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente)
);

CREATE TABLE lineas_pedido (
    id_pedido    INT NOT NULL,
    id_producto  INT NOT NULL,
    cantidad     INT NOT NULL,
    PRIMARY KEY (id_pedido, id_producto),
    FOREIGN KEY (id_pedido) REFERENCES pedidos(id_pedido),
    FOREIGN KEY (id_producto) REFERENCES productos(id_producto)
);
```

### 5.2 Ejecutar DDL desde Java con `Statement`

```java
import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class CreadorEsquema {
    public static void crearTablaProductos(Connection conexion) throws SQLException {
        String sql = """
            CREATE TABLE IF NOT EXISTS productos (
                id_producto  INT AUTO_INCREMENT PRIMARY KEY,
                nombre       VARCHAR(100) NOT NULL,
                precio       DECIMAL(8,2) NOT NULL,
                stock        INT NOT NULL DEFAULT 0
            )
            """;
        try (Statement statement = conexion.createStatement()) {
            statement.executeUpdate(sql);
        }
    }
}
```

`Statement` sirve para sentencias **sin parámetros variables**, típicamente DDL. Para DML con datos que varían (lo habitual: insertar el producto que ha escrito el usuario) se debe usar `PreparedStatement` (Bloque 6), tanto por seguridad como por rendimiento.

### 5.3 Insertar, actualizar y borrar (DML) con `PreparedStatement`

```java
import java.sql.*;

public class RepositorioProductos {

    public int insertarProducto(Connection conexion, String nombre, double precio, int stock) throws SQLException {
        String sql = "INSERT INTO productos (nombre, precio, stock) VALUES (?, ?, ?)";

        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, nombre);
            ps.setDouble(2, precio);
            ps.setInt(3, stock);

            int filasAfectadas = ps.executeUpdate();

            if (filasAfectadas == 0) {
                throw new SQLException("La inserción del producto no afectó a ninguna fila.");
            }

            try (ResultSet clavesGeneradas = ps.getGeneratedKeys()) {
                if (clavesGeneradas.next()) {
                    return clavesGeneradas.getInt(1); // id_producto autogenerado
                } else {
                    throw new SQLException("No se pudo obtener el id generado.");
                }
            }
        }
    }

    public void actualizarStock(Connection conexion, int idProducto, int nuevoStock) throws SQLException {
        String sql = "UPDATE productos SET stock = ? WHERE id_producto = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, nuevoStock);
            ps.setInt(2, idProducto);
            ps.executeUpdate();
        }
    }

    public void eliminarProducto(Connection conexion, int idProducto) throws SQLException {
        String sql = "DELETE FROM productos WHERE id_producto = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, idProducto);
            int filas = ps.executeUpdate();
            System.out.println(filas == 1 ? "Producto eliminado." : "No se encontró el producto.");
        }
    }
}
```

> **💡 ¿Sabías qué...?**
> `executeUpdate()` no es solo para `UPDATE`: su nombre puede despistar. Se usa para **cualquier sentencia que modifique datos o estructura** (`INSERT`, `UPDATE`, `DELETE`, `CREATE TABLE`...) y devuelve el número de filas afectadas (o 0 para DDL). El método `executeQuery()`, en cambio, se reserva exclusivamente para `SELECT` y devuelve un `ResultSet`. Si intentas usar `executeQuery()` con un `INSERT`, obtendrás una excepción.

### ⚠️ Error común
> Concatenar directamente los valores en la cadena SQL en vez de usar parámetros (`"INSERT INTO productos VALUES ('" + nombre + "', ...)"`). Además del riesgo de inyección SQL (Bloque 6), esto rompe con datos que contienen comillas simples (por ejemplo, un nombre de producto como `"Funda 6.1'' para móvil"`), provocando errores de sintaxis SQL en pleno funcionamiento.

### 🎯 Actividad de aula 5.1 — "Altas por consola"

Implementa un menú por consola que permita dar de alta clientes y productos en la base de datos de referencia, validando que el email del cliente no esté ya usado (captura la excepción `SQLIntegrityConstraintViolationException` que lanza la restricción `UNIQUE` y muestra un mensaje claro en vez de dejar que se propague la traza de pila).

---

## 6. Bloque 5 — Consultas y tratamiento del resultado (`ResultSet`)

### 6.1 El objeto `ResultSet`

Un `ResultSet` representa la **tabla de resultados** devuelta por una consulta `SELECT`. Se comporta como un **cursor**: apunta inicialmente *antes* de la primera fila, y hay que llamar a `next()` para avanzar fila a fila (por eso decimos que, por defecto, es de recorrido **secuencial hacia adelante** — es el mismo concepto de "acceso secuencial" que vimos con ficheros en UD1, aplicado ahora a un conjunto de resultados).

```java
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultasProductos {

    public List<Producto> listarProductosConStockBajo(Connection conexion, int umbral) throws SQLException {
        String sql = "SELECT id_producto, nombre, precio, stock FROM productos WHERE stock < ? ORDER BY stock";
        List<Producto> resultado = new ArrayList<>();

        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, umbral);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Producto p = new Producto(
                            rs.getInt("id_producto"),
                            rs.getString("nombre"),
                            rs.getDouble("precio"),
                            rs.getInt("stock")
                    );
                    resultado.add(p);
                }
            }
        }
        return resultado;
    }
}
```

### 6.2 Consultas con `JOIN` y resultados agregados

```java
public void mostrarPedidosDeCliente(Connection conexion, int idCliente) throws SQLException {
    String sql = """
        SELECT pe.id_pedido, pe.fecha, pr.nombre, lp.cantidad, pr.precio
        FROM pedidos pe
        JOIN lineas_pedido lp ON pe.id_pedido = lp.id_pedido
        JOIN productos pr ON lp.id_producto = pr.id_producto
        WHERE pe.id_cliente = ?
        ORDER BY pe.fecha DESC
        """;

    try (PreparedStatement ps = conexion.prepareStatement(sql)) {
        ps.setInt(1, idCliente);
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                double subtotal = rs.getInt("cantidad") * rs.getDouble("precio");
                System.out.printf("Pedido #%d (%s): %d x %s = %.2f €%n",
                        rs.getInt("id_pedido"), rs.getDate("fecha"),
                        rs.getInt("cantidad"), rs.getString("nombre"), subtotal);
            }
        }
    }
}
```

### Analogía cotidiana del `ResultSet`

Es como una **cinta transportadora en una fábrica de embalaje**: los productos (filas) pasan uno a uno frente a ti; puedes coger e inspeccionar el que tienes delante (`rs.getXxx()`), pero para ver el siguiente tienes que "hacer avanzar la cinta" (`rs.next()`) — no puedes ver dos productos a la vez ni, en el modo por defecto, retroceder la cinta hacia atrás.

> **💡 ¿Sabías qué...?**
> Es posible crear un `ResultSet` **desplazable** (*scrollable*) y hasta actualizable directamente, indicándolo al crear el `Statement`/`PreparedStatement`: `conexion.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)`. Esto permite moverse con `rs.previous()`, `rs.first()`, `rs.absolute(n)` o incluso modificar una fila directamente con `rs.updateString(...)` seguido de `rs.updateRow()`. En la práctica profesional actual se usa poco (se prefiere una sentencia `UPDATE` explícita), pero es interesante que el alumnado sepa que existe, ya que recuerda conceptualmente al acceso aleatorio de UD1.

### ⚠️ Errores comunes
1. Llamar a un `getXxx()` **antes** de la primera llamada a `next()`, o después de que `next()` haya devuelto `false`: lanza `SQLException`.
2. Acceder a una columna por un índice numérico incorrecto (recordar que en JDBC los índices de columnas **empiezan en 1, no en 0**, a diferencia de la mayoría de estructuras Java).
3. Usar el nombre de columna con una capitalización que no coincide y asumir que siempre es sensible a mayúsculas (depende del SGBD); es más seguro fijarse en el *alias* real devuelto y probarlo.
4. Intentar usar el `ResultSet` **después de cerrar** la conexión o el `Statement` que lo generó: al cerrar el recurso "padre" con `try-with-resources`, el `ResultSet` deja de ser válido.

### 🎯 Actividad de aula 6.1 — "El informe de ventas"

Escribe una consulta y su correspondiente código Java que calcule, para cada producto, la cantidad total vendida (suma de `cantidad` en `lineas_pedido`) y el importe total generado (`cantidad * precio`), ordenado de mayor a menor importe. Muestra el resultado formateado en una tabla por consola.

---

## 7. Bloque 6 — Sentencias preparadas y seguridad (inyección SQL)

### 7.1 El problema: inyección SQL

La **inyección SQL** es una de las vulnerabilidades más conocidas y peligrosas en aplicaciones que acceden a bases de datos. Ocurre cuando se construye una sentencia SQL **concatenando directamente** una entrada proporcionada por el usuario, permitiendo que ese usuario "inyecte" código SQL no previsto.

```java
// ❌ CÓDIGO VULNERABLE — NUNCA HACER ESTO
String nombreBuscado = entradaUsuario; // por ejemplo, viene de un formulario web
String sql = "SELECT * FROM clientes WHERE nombre = '" + nombreBuscado + "'";
Statement st = conexion.createStatement();
ResultSet rs = st.executeQuery(sql);
```

Si un usuario malicioso introduce como "nombre" el valor:

```
' OR '1'='1
```

la sentencia final ejecutada sería:

```sql
SELECT * FROM clientes WHERE nombre = '' OR '1'='1'
```

Esta condición es **siempre verdadera**, por lo que la consulta devolvería **todos los clientes** de la tabla, saltándose por completo el filtro previsto. Con técnicas más avanzadas (`; DROP TABLE clientes; --`), un atacante podría llegar a borrar tablas completas o extraer datos confidenciales.

### Analogía cotidiana

Es como si en un formulario de "escribe tu nombre" para entrar a una fiesta privada, alguien escribiera una frase que, leída por el portero de una determinada manera automática, sonara como *"...o déjame pasar a cualquiera"*. El portero (la base de datos) simplemente obedece literalmente la orden que recibe si no distingue entre "el nombre del invitado" y "una instrucción".

### 7.2 La solución: `PreparedStatement`

```java
// ✅ CÓDIGO SEGURO
String sql = "SELECT * FROM clientes WHERE nombre = ?";
try (PreparedStatement ps = conexion.prepareStatement(sql)) {
    ps.setString(1, nombreBuscado); // el valor SIEMPRE se trata como dato, nunca como código SQL
    try (ResultSet rs = ps.executeQuery()) {
        // ...
    }
}
```

Con `PreparedStatement`, la sentencia SQL se envía **precompilada** al SGBD con "huecos" (`?`) reservados para los parámetros. El motor de base de datos sabe, desde el principio, cuál es la estructura exacta de la consulta, y trata cualquier valor proporcionado después como **un dato literal**, nunca como código SQL ejecutable, sin importar lo que ese valor contenga.

### Ventajas e inconvenientes: `Statement` vs. `PreparedStatement`

| | `Statement` | `PreparedStatement` |
|---|---|---|
| **Seguridad frente a inyección SQL** | Ninguna si se concatenan datos | Total, por diseño |
| **Rendimiento en ejecuciones repetidas** | Se recompila cada vez | El SGBD puede reutilizar el plan de ejecución compilado |
| **Legibilidad del código** | Puede ser más directo para SQL fijo sin parámetros | Ligeramente más verboso (hay que fijar cada parámetro) |
| **Uso recomendado** | Solo DDL fijo sin datos de usuario | **Siempre** que intervenga cualquier valor variable (y, en la práctica profesional, casi siempre) |

> **💡 ¿Sabías qué...?**
> Según los informes anuales del **OWASP Top 10** (el catálogo de referencia mundial de riesgos de seguridad en aplicaciones web), los fallos de inyección (SQL entre ellos) llevan más de una década apareciendo sistemáticamente entre las categorías de riesgo más críticas. No es una amenaza "de laboratorio": ha sido la causa de fugas de datos masivas en empresas reales. Aprender a usar `PreparedStatement` de forma sistemática no es un capricho académico, es una competencia profesional de seguridad básica.

### ⚠️ Error común
> Pensar que basta con "escapar" manualmente las comillas del texto introducido por el usuario (por ejemplo, sustituyendo `'` por `''`) en vez de usar `PreparedStatement`. Es una técnica frágil, propensa a errores, y no cubre todos los vectores de ataque posibles (codificaciones alternativas, otros caracteres especiales según el SGBD). `PreparedStatement` no es "una opción más segura", es **la** solución correcta y estándar.

### 🎯 Actividad de aula 7.1 — "Laboratorio de inyección SQL controlado"

En un entorno de práctica aislado (una base de datos de pruebas, nunca en un sistema real), reproduce el ejemplo vulnerable del apartado 7.1 con una tabla de clientes de prueba, y comprueba en primera persona cómo `' OR '1'='1` devuelve todos los registros. A continuación, reescribe el mismo buscador con `PreparedStatement` y demuestra que el mismo valor ya no tiene ningún efecto especial: se busca literalmente un cliente llamado `' OR '1'='1`. Documenta ambas capturas de pantalla en un informe breve.

---

## 8. Bloque 7 — Procedimientos almacenados

### 8.1 Concepto

Un **procedimiento almacenado** es un bloque de código SQL (con lógica, variables, control de flujo) que se define y se guarda **dentro del propio SGBD**, y que se puede invocar por su nombre desde la aplicación, en lugar de enviar cada vez la sentencia completa.

### Ejemplo de procedimiento en MySQL: registrar una venta y descontar stock

```sql
DELIMITER //

CREATE PROCEDURE registrar_venta (
    IN p_id_producto INT,
    IN p_cantidad INT,
    OUT p_resultado VARCHAR(100)
)
BEGIN
    DECLARE stock_actual INT;

    SELECT stock INTO stock_actual FROM productos WHERE id_producto = p_id_producto;

    IF stock_actual >= p_cantidad THEN
        UPDATE productos SET stock = stock - p_cantidad WHERE id_producto = p_id_producto;
        SET p_resultado = 'OK';
    ELSE
        SET p_resultado = 'STOCK_INSUFICIENTE';
    END IF;
END //

DELIMITER ;
```

### 8.2 Invocar el procedimiento desde Java con `CallableStatement`

```java
import java.sql.*;

public class RegistradorVentas {

    public String registrarVenta(Connection conexion, int idProducto, int cantidad) throws SQLException {
        String llamada = "{call registrar_venta(?, ?, ?)}";

        try (CallableStatement cs = conexion.prepareCall(llamada)) {
            cs.setInt(1, idProducto);          // parámetro de entrada (IN)
            cs.setInt(2, cantidad);            // parámetro de entrada (IN)
            cs.registerOutParameter(3, Types.VARCHAR); // parámetro de salida (OUT)

            cs.execute();

            return cs.getString(3); // recuperamos el resultado devuelto por el procedimiento
        }
    }
}
```

### Ventajas e inconvenientes de los procedimientos almacenados

- ✅ Ventajas: la lógica se ejecuta **dentro** del SGBD, evitando ir y venir de datos innecesarios por la red (importante si se procesan muchos registros); se puede reutilizar desde distintas aplicaciones/lenguajes sin duplicar lógica; permite centralizar reglas de negocio críticas de integridad.
- ❌ Inconvenientes: la lógica queda "escondida" fuera del código de la aplicación y del control de versiones habitual del proyecto (salvo que se gestione explícitamente); el lenguaje procedural varía de un SGBD a otro (no es portable: un procedimiento en MySQL no funciona tal cual en PostgreSQL u Oracle); dificulta las pruebas unitarias automatizadas típicas del desarrollo de aplicaciones.

### ⚠️ Error común
> Olvidar registrar el tipo del parámetro de salida con `registerOutParameter()` **antes** de ejecutar el procedimiento, o intentar leer el valor de salida con `getString()`/`getInt()` **antes** de llamar a `execute()`.

### 🎯 Actividad de aula 8.1 — "Automatizando reglas de negocio"

Crea un procedimiento almacenado `calcular_total_pedido(IN id_pedido INT, OUT total DECIMAL(10,2))` que sume el importe de todas las líneas de un pedido, y su correspondiente invocación desde Java mostrando el resultado formateado.

---

## 9. Bloque 8 — Transacciones

### 9.1 ¿Qué es una transacción?

Una **transacción** es un conjunto de una o más operaciones sobre la base de datos que se ejecutan como **una unidad indivisible**: o se completan todas, o no se completa ninguna. Este comportamiento se resume habitualmente en las propiedades **ACID** (Atomicidad, Consistencia, Aislamiento, Durabilidad).

### Analogía cotidiana

Piensa en una **transferencia bancaria**: quitar 100 € de tu cuenta y añadir 100 € a la cuenta de otra persona son, en realidad, **dos operaciones**. Si el sistema falla justo después de restarte el dinero pero antes de sumárselo al destinatario, el dinero "desaparecería". Una transacción garantiza que, ante ese fallo, **ninguna de las dos operaciones se queda a medias**: o se hacen las dos, o se deshacen las dos (como si nunca hubiera ocurrido nada).

### 9.2 Gestión de transacciones en JDBC

Por defecto, JDBC funciona en modo `autocommit = true`: **cada sentencia se confirma automáticamente** en cuanto se ejecuta, como si fuera su propia mini-transacción. Para agrupar varias operaciones en una sola transacción, hay que desactivar ese modo:

```java
import java.sql.*;

public class ServicioPedidos {

    public void crearPedidoConLineas(Connection conexion, int idCliente, java.util.List<LineaPedido> lineas) {
        try {
            conexion.setAutoCommit(false); // iniciamos la transacción manualmente

            // 1. Insertar la cabecera del pedido
            String sqlPedido = "INSERT INTO pedidos (id_cliente, fecha) VALUES (?, CURRENT_DATE)";
            int idPedido;
            try (PreparedStatement ps = conexion.prepareStatement(sqlPedido, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idCliente);
                ps.executeUpdate();
                try (ResultSet claves = ps.getGeneratedKeys()) {
                    claves.next();
                    idPedido = claves.getInt(1);
                }
            }

            // 2. Insertar cada línea y descontar stock
            String sqlLinea = "INSERT INTO lineas_pedido (id_pedido, id_producto, cantidad) VALUES (?, ?, ?)";
            String sqlStock = "UPDATE productos SET stock = stock - ? WHERE id_producto = ? AND stock >= ?";

            for (LineaPedido linea : lineas) {
                try (PreparedStatement psLinea = conexion.prepareStatement(sqlLinea)) {
                    psLinea.setInt(1, idPedido);
                    psLinea.setInt(2, linea.idProducto());
                    psLinea.setInt(3, linea.cantidad());
                    psLinea.executeUpdate();
                }

                try (PreparedStatement psStock = conexion.prepareStatement(sqlStock)) {
                    psStock.setInt(1, linea.cantidad());
                    psStock.setInt(2, linea.idProducto());
                    psStock.setInt(3, linea.cantidad());
                    int filasActualizadas = psStock.executeUpdate();

                    if (filasActualizadas == 0) {
                        // No había stock suficiente: forzamos deshacer TODO el pedido
                        throw new SQLException("Stock insuficiente para el producto " + linea.idProducto());
                    }
                }
            }

            conexion.commit(); // confirma TODAS las operaciones anteriores
            System.out.println("Pedido #" + idPedido + " registrado correctamente.");

        } catch (SQLException e) {
            try {
                conexion.rollback(); // deshace TODO lo hecho desde el último commit
                System.err.println("Transacción deshecha. Motivo: " + e.getMessage());
            } catch (SQLException rollbackEx) {
                System.err.println("Error crítico al hacer rollback: " + rollbackEx.getMessage());
            }
        } finally {
            try {
                conexion.setAutoCommit(true); // devolvemos la conexión a su estado habitual
            } catch (SQLException e) {
                System.err.println("Error al restaurar autocommit: " + e.getMessage());
            }
        }
    }
}
```

*(Se asume un `record LineaPedido(int idProducto, int cantidad) {}` como tipo auxiliar.)*

### Ventajas e inconvenientes de trabajar con transacciones explícitas

- ✅ Ventajas: garantiza la integridad de operaciones compuestas por varios pasos; evita estados intermedios inconsistentes visibles para otros usuarios; permite deshacer de forma segura ante errores.
- ❌ Inconvenientes: mientras una transacción está abierta, puede retener bloqueos sobre filas/tablas, afectando a la concurrencia si se hacen demasiado largas; añade complejidad de código (hay que acordarse de `commit`/`rollback`/restaurar `autocommit`); un mal diseño puede provocar interbloqueos (*deadlocks*) entre transacciones concurrentes.

> **💡 ¿Sabías qué...?**
> Además de `commit`/`rollback` completos, JDBC permite definir **puntos de guardado** (`Savepoint`) dentro de una transacción con `conexion.setSavepoint()`, de forma que se pueda deshacer solo una parte de las operaciones realizadas (`conexion.rollback(savepoint)`) sin perder todo el trabajo previo de la transacción. Es una herramienta avanzada, útil en transacciones largas con varios sub-pasos independientes entre sí.

### ⚠️ Errores comunes
1. Olvidar `conexion.setAutoCommit(false)` y pensar que se está trabajando "en transacción" cuando en realidad cada sentencia se confirma sola.
2. No restaurar `autocommit = true` (o no cerrar bien la conexión) después de terminar, dejando la conexión en un estado inesperado si se reutiliza (especialmente grave si la conexión procede de un *pool*, ver Bloque 3).
3. Hacer el `commit()` demasiado pronto (antes de comprobar todas las condiciones de negocio necesarias) o el `rollback()` demasiado tarde.

### 🎯 Actividad de aula 9.1 — "Transferencia entre cuentas simulada"

Crea una tabla `cuentas (id_cuenta INT, saldo DECIMAL)` y programa una operación `transferir(idOrigen, idDestino, importe)` que reste el importe de una cuenta y lo sume a otra dentro de una única transacción. Provoca deliberadamente un error a mitad del proceso (por ejemplo, lanzando una excepción manual tras la primera actualización) y comprueba que, gracias al `rollback`, el saldo total del sistema permanece exactamente igual que al principio.

---

## 10. Bloque 9 — Cierre de recursos y buenas prácticas

### 10.1 La jerarquía de recursos a cerrar

En una operación JDBC típica intervienen hasta tres recursos que implementan `AutoCloseable` y que deben cerrarse, **en orden inverso al de apertura**: `ResultSet` → `Statement`/`PreparedStatement` → `Connection`. Cerrar un recurso "padre" (por ejemplo, la conexión) cierra automáticamente los recursos "hijos" abiertos sobre él, pero **no es buena práctica depender de eso**: hay que cerrarlos explícitamente, y `try-with-resources` (visto en UD1) es la herramienta ideal para ello.

```java
public List<Producto> listarTodos(Connection conexion) throws SQLException {
    String sql = "SELECT id_producto, nombre, precio, stock FROM productos";
    List<Producto> productos = new ArrayList<>();

    try (PreparedStatement ps = conexion.prepareStatement(sql);
         ResultSet rs = ps.executeQuery()) {

        while (rs.next()) {
            productos.add(new Producto(
                    rs.getInt("id_producto"), rs.getString("nombre"),
                    rs.getDouble("precio"), rs.getInt("stock")));
        }
    }
    return productos;
}
```

Fíjate en que **la propia `Connection` no se cierra aquí**: siguiendo el patrón habitual, la conexión se abre en una capa superior (o se obtiene del pool) y se pasa como parámetro a los métodos de acceso a datos, cerrándose en el nivel que la abrió. Es una decisión de diseño importante para poder reutilizar la misma conexión en varias operaciones dentro de una transacción (Bloque 8).

### 10.2 Patrón recomendado: separar la capa de acceso a datos (DAO)

Aunque el patrón **DAO** (*Data Access Object*) se formaliza más adelante en el módulo (UD6, componentes de acceso a datos), conviene introducirlo ya desde UD2 como buena práctica: encapsular todo el código JDBC de una entidad (por ejemplo, `Producto`) dentro de una única clase (`ProductoDAO` o `ProductoRepositorio`), de forma que el resto de la aplicación nunca escriba SQL directamente, sino que llame a métodos con nombre claro (`insertar`, `buscarPorId`, `listarConStockBajo`...).

### Ventajas e inconvenientes de programar con conectores "a pelo" (JDBC puro)

Cerramos la unidad con una valoración global, que servirá de contraste directo con la UD3 (ORM):

- ✅ Ventajas: control total y explícito sobre el SQL ejecutado (importante para optimizar consultas críticas); sin "magia" oculta, todo el comportamiento es predecible; no añade dependencias pesadas al proyecto; rendimiento máximo posible al no existir capas de abstracción intermedias.
- ❌ Inconvenientes: mucho código repetitivo (*boilerplate*) para tareas comunes (mapear cada columna a cada atributo a mano, por ejemplo); fácil cometer errores de cierre de recursos o de gestión de excepciones si no se es disciplinado; cualquier cambio en el esquema de la tabla obliga a revisar manualmente el SQL disperso por el código.

### ⚠️ Error común
> Cerrar la conexión dentro de un método de "listar" o "buscar" genérico que en realidad recibe la conexión como parámetro desde fuera. Esto rompe cualquier operación posterior que intente reutilizar esa misma conexión (por ejemplo, dentro de una transacción con varios pasos, como en el Bloque 8), provocando errores de tipo "conexión cerrada" muy difíciles de depurar si no se entiende bien qué capa es responsable de abrir y cerrar cada recurso.

### 🎯 Actividad de aula 10.1 — "Mi primer DAO"

Refactoriza todo el código de consultas y modificaciones sobre `productos` escrito a lo largo de la unidad en una única clase `ProductoDAO`, con métodos: `insertar`, `actualizarStock`, `eliminar`, `buscarPorId`, `listarTodos`, `listarConStockBajo`. La clase debe recibir la `Connection` (o, en su versión avanzada, un `DataSource`/pool) por constructor, nunca crearla ella misma.

---

## 11. Proyecto integrador de la unidad — "Gestión de pedidos con base de datos relacional"

Este proyecto retoma y amplía el catálogo de productos trabajado en UD1 (ficheros), trasladándolo ahora a una base de datos relacional real, y añadiendo el dominio de clientes y pedidos que se seguirá utilizando en la UD3 (ORM).

### Especificación

Desarrolla una aplicación de consola con menú que permita:

1. **Gestión de productos:** alta, baja, modificación y listado (reutilizando y ampliando el `ProductoDAO` de la Actividad 10.1).
2. **Gestión de clientes:** alta y listado, validando que el email sea único (gestionando la excepción de restricción `UNIQUE` con un mensaje claro).
3. **Registro de pedidos:** un cliente puede realizar un pedido con una o varias líneas (producto + cantidad). La creación del pedido completo debe ser **transaccional**: si algún producto no tiene stock suficiente, no se registra ninguna línea del pedido y se informa al usuario.
4. **Informe de ventas:** consulta que muestre, para cada producto, unidades vendidas e importe total generado (como en la Actividad 6.1), usando `JOIN` entre `pedidos`, `lineas_pedido` y `productos`.
5. **Procedimiento almacenado:** implementa el cálculo del total de un pedido como procedimiento almacenado (Bloque 7) e invócalo desde la opción de "Ver detalle de pedido" del menú.
6. Toda la conexión a base de datos debe realizarse a través de un *pool* de conexiones (aunque sea con una configuración mínima) y todos los recursos JDBC deben cerrarse mediante `try-with-resources`.
7. **(Ampliación opcional)** Añade una opción para exportar el listado de productos a JSON reutilizando el código de la UD1, cerrando así el círculo entre ambas unidades y evidenciando que la capa de exportación es independiente de dónde vengan realmente los datos (fichero o base de datos).

### Rúbrica orientativa de evaluación del proyecto

| Criterio | Insuficiente | Adecuado | Excelente |
|---|---|---|---|
| Conexión y cierre de recursos | Conexiones sin cerrar o `Statement` simple con datos concatenados | Uso correcto de `PreparedStatement` y `try-with-resources` | Además usa *pool* de conexiones configurado correctamente |
| CRUD completo | Falta alguna operación básica | Implementa alta/baja/modificación/consulta de productos y clientes | Además con validaciones de integridad bien gestionadas |
| Transacciones | No usa transacciones donde son necesarias | `commit`/`rollback` correctos en el registro de pedidos | Además contempla casos límite (stock exacto, pedidos vacíos, fallos parciales) |
| Procedimientos almacenados | No implementado | Procedimiento simple invocado correctamente | Procedimiento con lógica de negocio relevante y bien documentado |
| Seguridad | Alguna sentencia con concatenación de datos de usuario | Todas las sentencias con datos variables usan `PreparedStatement` | Además el alumnado puede explicar y demostrar por qué es seguro |

---

## 12. Cuestionario de autoevaluación

1. Explica con tus propias palabras qué es el desfase objeto-relacional y pon un ejemplo distinto a los vistos en clase.
2. ¿Qué diferencia fundamental hay entre un SGBD embebido y uno independiente? Cita un caso de uso real para cada uno.
3. ¿Por qué ya no es necesario, en general, escribir `Class.forName(...)` para cargar un driver JDBC moderno?
4. ¿Qué problema resuelve un *pool* de conexiones y qué puede ocurrir si una aplicación "olvida" devolver conexiones al pool?
5. Explica la diferencia entre `executeQuery()` y `executeUpdate()`. ¿Cuándo usarías cada uno?
6. Describe, paso a paso, cómo se produciría un ataque de inyección SQL sobre una consulta mal construida, y cómo lo evita exactamente `PreparedStatement`.
7. ¿Qué significa que un `ResultSet` sea, por defecto, de recorrido secuencial hacia adelante? Relaciónalo con lo aprendido sobre acceso a ficheros en la UD1.
8. ¿Qué ventaja aporta un procedimiento almacenado frente a ejecutar la misma lógica en Java? ¿Y qué inconveniente importante tiene?
9. En una transacción JDBC, ¿qué ocurre si se llama a `commit()` sin haber llamado antes a `setAutoCommit(false)`?
10. Cita dos ventajas y dos inconvenientes de trabajar con JDBC "puro" frente a lo que intuyes que podría ofrecer una herramienta ORM (lo veremos en profundidad en la UD3).

---

## 13. Glosario de la unidad

- **ACID:** conjunto de propiedades (Atomicidad, Consistencia, Aislamiento, Durabilidad) que garantiza una transacción bien gestionada.
- **Autocommit:** modo por defecto de JDBC en el que cada sentencia se confirma automáticamente al ejecutarse.
- **Connection pooling:** técnica de reutilización de un conjunto de conexiones ya abiertas para evitar el coste de crear y destruir conexiones constantemente.
- **DAO (Data Access Object):** patrón de diseño que encapsula todo el acceso a datos de una entidad en una única clase.
- **Desfase objeto-relacional:** fricción conceptual entre el modelo de objetos de un lenguaje de programación y el modelo de tablas de una base de datos relacional.
- **Driver JDBC:** implementación concreta, proporcionada por cada fabricante de SGBD, de la API estándar JDBC.
- **Inyección SQL:** vulnerabilidad que permite a un atacante alterar el significado de una sentencia SQL insertando código no previsto a través de una entrada de datos.
- **PreparedStatement:** sentencia SQL precompilada con parámetros, que trata cualquier valor proporcionado como dato literal, nunca como código ejecutable.
- **Procedimiento almacenado:** bloque de lógica SQL guardado y ejecutado dentro del propio SGBD, invocable por su nombre.
- **ResultSet:** objeto que representa, como un cursor de recorrido secuencial, el conjunto de filas devuelto por una consulta `SELECT`.
- **Rollback:** operación que deshace todas las modificaciones realizadas desde el último `commit` de una transacción.
- **Savepoint:** punto de guardado intermedio dentro de una transacción hasta el cual se puede deshacer parcialmente el trabajo realizado.
