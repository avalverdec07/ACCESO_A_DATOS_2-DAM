# UD3. Persistencia con herramientas de mapeo objeto-relacional (ORM)
## Desarrollo completo de contenidos — Módulo Acceso a Datos (0486) — 2º DAM

> **Lenguaje y tecnología de referencia:** Java + **JPA** (Jakarta Persistence API) implementada con **Hibernate**. Cuando aporte valor se indica el equivalente conceptual en C# (**Entity Framework Core**: `DbContext`, `DbSet<T>`, LINQ to Entities). SGBD de referencia: el mismo **MySQL/MariaDB** o **PostgreSQL** usado en UD2.
>
> Esta unidad retoma **exactamente el mismo dominio de datos** de UD2 (`Producto`, `Cliente`, `Pedido`, `LineaPedido`), lo que permite al alumnado comparar de forma directa el mismo caso resuelto "a pelo" con JDBC frente a resuelto con ORM.

---

## Índice de la unidad

1. Introducción y mapa de la unidad
2. Bloque 1 — Qué es un ORM y qué problema resuelve
3. Bloque 2 — Instalación y configuración de la herramienta ORM
4. Bloque 3 — Mapeo de entidades mediante anotaciones
5. Bloque 4 — Estados de un objeto en una sesión ORM
6. Bloque 5 — Operaciones CRUD sobre objetos persistentes
7. Bloque 6 — Relaciones entre entidades (1:1, 1:N, N:M)
8. Bloque 7 — Consultas: JPQL/HQL y SQL nativo
9. Bloque 8 — Gestión de transacciones con el ORM
10. Bloque 9 — JDBC vs. ORM: valoración comparada
11. Proyecto integrador de la unidad
12. Cuestionario de autoevaluación
13. Glosario de la unidad

---

## 1. Introducción y mapa de la unidad

En UD2 escribimos, sentencia a sentencia, todo el código SQL necesario para insertar un producto, actualizar el stock o recorrer un `ResultSet` transformándolo a mano en objetos `Producto`. Funciona, pero habrás notado algo: **mucho código se repite** de una entidad a otra (abrir `PreparedStatement`, fijar parámetros, recorrer el `ResultSet`, cerrar recursos...), y cada cambio en una tabla obliga a revisar SQL disperso por varias clases.

Un **ORM** (*Object-Relational Mapping*, mapeo objeto-relacional) es una herramienta que **automatiza esa traducción** entre objetos Java y filas de tablas relacionales, dejando que el programador trabaje casi exclusivamente con objetos, mientras la herramienta genera el SQL necesario por debajo.

### Analogía cotidiana inicial

En UD2 comparamos JDBC con pedirle las cosas a un bibliotecario en persona, sentencia a sentencia. Un ORM es como tener un **asistente personal bilingüe**: tú le hablas siempre en "tu idioma" (objetos Java: `guardar(producto)`, `producto.setStock(10)`), y es él quien se encarga de traducirlo al idioma del bibliotecario (SQL) y de traer la respuesta ya traducida de vuelta a tu idioma. Ya no tienes que aprender a hablar perfectamente el idioma del bibliotecario para cada gestión del día a día, aunque —como buen profesional— sigue siendo imprescindible entenderlo, porque a veces el asistente traduce de forma poco eficiente y hay que corregirle (Bloque 9).

### Mapa conceptual de la unidad

```
PERSISTENCIA CON ORM
│
├── Concepto de ORM y resolución del desfase objeto-relacional (recordar UD2, Bloque 1)
│
├── Configuración de la herramienta
│     └── persistence.xml / EntityManagerFactory
│
├── Mapeo de entidades (anotaciones JPA)
│     ├── @Entity, @Id, @GeneratedValue, @Column
│     └── Relaciones: @OneToOne, @OneToMany, @ManyToOne, @ManyToMany
│
├── Estados del objeto en la sesión
│     └── Transitorio → Persistente → Desprendido (y Eliminado)
│
├── Operaciones CRUD → EntityManager (persist, find, merge, remove)
│
├── Consultas
│     ├── JPQL/HQL (orientadas a objetos)
│     └── SQL nativo (cuando JPQL no basta)
│
└── Transacciones → EntityTransaction / @Transactional
```

---

## 2. Bloque 1 — Qué es un ORM y qué problema resuelve

### 2.1 Retomando el desfase objeto-relacional

En UD2 identificamos cuatro fricciones entre objetos y tablas: identidad, herencia, relaciones/colecciones y granularidad. Un ORM no elimina esas diferencias (siguen existiendo, porque el modelo relacional subyacente no cambia), pero **automatiza sistemáticamente su traducción**, aplicando siempre las mismas reglas de mapeo que tú defines una vez (mediante anotaciones) y reutiliza en cada operación.

### 2.2 Qué hace un ORM por ti

- Genera automáticamente el SQL de `INSERT`, `UPDATE`, `DELETE` y `SELECT` a partir de tus clases anotadas.
- Convierte cada fila de un `ResultSet` en un objeto Java completo (y viceversa), sin que tengas que escribir ese mapeo campo a campo.
- Gestiona las relaciones entre entidades como si fueran atributos normales (un `Pedido` puede tener una `List<LineaPedido>` que el ORM carga automáticamente).
- Ofrece un lenguaje de consultas orientado a objetos (JPQL/HQL) que trabaja sobre entidades y sus atributos, no directamente sobre nombres de tabla y columna.
- Gestiona internamente una **caché de primer nivel** (por sesión) para evitar consultas repetidas al mismo objeto.

### 2.3 JPA vs. Hibernate: la especificación y su implementación

Es fundamental que el alumnado entienda esta distinción, muy habitual en el ecosistema Java:

- **JPA (Jakarta Persistence API)** es una **especificación**: un conjunto de interfaces y anotaciones estándar (`@Entity`, `EntityManager`...) que definen *qué* debe poder hacer un ORM en Java, sin implementar nada por sí misma.
- **Hibernate** es la implementación de JPA más extendida (aunque no la única; existen otras como EclipseLink). Es el motor real que genera el SQL y se comunica con el SGBD por debajo, usando JDBC internamente (es decir: **el ORM no sustituye a JDBC, se apoya en él**).

> **💡 ¿Sabías qué...?**
> Hibernate nació en 2001, **antes** de que existiera la propia especificación JPA (2006). Fue precisamente el éxito y la influencia de Hibernate (y de otros ORM similares de la época) lo que llevó a Sun Microsystems a estandarizar sus ideas en una API oficial para todo el ecosistema Java. Por eso, aunque hoy programemos "contra JPA" por portabilidad, Hibernate añade también funcionalidades propias que van más allá del estándar (y que se acceden mediante su API nativa cuando JPA se queda corta).

### Ventajas e inconvenientes generales de usar un ORM

- ✅ Ventajas: reduce drásticamente el código repetitivo (*boilerplate*); mapeo de relaciones automático; portabilidad razonable entre distintos SGBD (basta con cambiar el *dialecto* de configuración); caché de primer y segundo nivel que puede mejorar el rendimiento; consultas orientadas a objetos más mantenibles.
- ❌ Inconvenientes: curva de aprendizaje inicial (hay bastantes conceptos nuevos: sesión, estados, *fetch*...); riesgo de generar SQL poco eficiente si no se entiende bien lo que ocurre "por debajo" (el problema clásico **N+1 consultas**, que veremos en el Bloque 6); menos control fino sobre el SQL exacto ejecutado; para operaciones masivas o muy específicas, a veces sigue siendo mejor recurrir a SQL nativo o incluso a JDBC puro.

### ⚠️ Error común
> Pensar que un ORM elimina la necesidad de saber SQL o de entender el modelo relacional. Es justo lo contrario: **para usar bien un ORM hace falta entender aún mejor qué SQL se está generando por debajo**, precisamente para detectar cuándo el mapeo automático no es eficiente y corregirlo.

---

## 3. Bloque 2 — Instalación y configuración de la herramienta ORM

### 3.1 Dependencias del proyecto (Maven)

```xml
<dependencies>
    <!-- API estándar JPA -->
    <dependency>
        <groupId>jakarta.persistence</groupId>
        <artifactId>jakarta.persistence-api</artifactId>
        <version>3.1.0</version>
    </dependency>

    <!-- Implementación: Hibernate -->
    <dependency>
        <groupId>org.hibernate.orm</groupId>
        <artifactId>hibernate-core</artifactId>
        <version>6.5.2.Final</version>
    </dependency>

    <!-- Driver JDBC del SGBD (Hibernate lo usa por debajo) -->
    <dependency>
        <groupId>com.mysql</groupId>
        <artifactId>mysql-connector-j</artifactId>
        <version>8.4.0</version>
    </dependency>
</dependencies>
```

### 3.2 El fichero de configuración `persistence.xml`

Este fichero, ubicado en `src/main/resources/META-INF/persistence.xml`, define la **unidad de persistencia**: qué base de datos usar, con qué credenciales y con qué comportamiento.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<persistence xmlns="https://jakarta.ee/xml/ns/persistence" version="3.1">

    <persistence-unit name="catalogoTiendaPU" transaction-type="RESOURCE_LOCAL">
        <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>

        <properties>
            <property name="jakarta.persistence.jdbc.url"
                      value="jdbc:mysql://localhost:3306/catalogo_tienda"/>
            <property name="jakarta.persistence.jdbc.user" value="dam_alumno"/>
            <property name="jakarta.persistence.jdbc.password" value="clave_segura"/>
            <property name="jakarta.persistence.jdbc.driver" value="com.mysql.cj.jdbc.Driver"/>

            <property name="hibernate.dialect" value="org.hibernate.dialect.MySQLDialect"/>
            <property name="hibernate.hbm2ddl.auto" value="update"/>
            <property name="hibernate.show_sql" value="true"/>
            <property name="hibernate.format_sql" value="true"/>
        </properties>
    </persistence-unit>
</persistence>
```

> **💡 ¿Sabías qué...?**
> La propiedad `hibernate.show_sql` (junto con `format_sql`) es probablemente la herramienta didáctica más valiosa de toda la unidad: muestra en consola, con formato legible, **el SQL real** que Hibernate genera para cada operación. Se recomienda mantenerla activada durante todo el aprendizaje, para que el alumnado nunca pierda de vista qué está ocurriendo "por debajo" del código orientado a objetos que escribe.

### 3.3 Obtener el punto de entrada: `EntityManagerFactory` y `EntityManager`

```java
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class GestorPersistencia {

    private static final EntityManagerFactory FACTORY =
            Persistence.createEntityManagerFactory("catalogoTiendaPU");

    public static EntityManager obtenerEntityManager() {
        return FACTORY.createEntityManager();
    }

    public static void cerrar() {
        FACTORY.close();
    }
}
```

El `EntityManagerFactory` es **costoso de crear** (análogo conceptualmente al *pool* de conexiones de UD2) y se crea **una única vez** por aplicación. El `EntityManager`, en cambio, es **ligero** y representa una **sesión de trabajo** con la base de datos: se crea y se cierra por cada unidad de trabajo (por ejemplo, por cada operación o por cada petición en una aplicación web).

### 🎯 Actividad de aula 3.1 — "Primer arranque"

Configura un proyecto Maven con Hibernate apuntando a la misma base de datos `catalogo_tienda` usada en UD2. Activa `hibernate.show_sql` y ejecuta un `EntityManager` vacío que simplemente se abra y se cierre, comprobando en consola los mensajes de arranque de Hibernate (detección de dialecto, validación de la conexión...).

---

## 4. Bloque 3 — Mapeo de entidades mediante anotaciones

### 4.1 La anotación `@Entity`

```java
import jakarta.persistence.*;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "precio", nullable = false, precision = 8, scale = 2)
    private java.math.BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private int stock;

    // JPA EXIGE un constructor vacío (puede ser protegido)
    protected Producto() {}

    public Producto(String nombre, java.math.BigDecimal precio, int stock) {
        this.nombre = nombre;
        this.precio = precio;
        this.stock = stock;
    }

    // Getters y setters
    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public java.math.BigDecimal getPrecio() { return precio; }
    public void setPrecio(java.math.BigDecimal precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
}
```

### 4.2 Anatomía de las anotaciones principales

| Anotación | Función |
|---|---|
| `@Entity` | Marca la clase como una entidad gestionada por JPA (equivalente conceptual a una tabla) |
| `@Table(name=...)` | Indica el nombre real de la tabla, si difiere del nombre de la clase |
| `@Id` | Marca el atributo que actúa como clave primaria |
| `@GeneratedValue` | Indica que el valor de la clave primaria lo genera la base de datos (autoincremental, secuencia...) |
| `@Column` | Personaliza el mapeo de un atributo a su columna (nombre, longitud, si admite nulos...) |
| `@Transient` | Marca un atributo que **no** debe persistirse (equivalente conceptual a `transient` en la serialización de UD1) |

### Analogía cotidiana

Las anotaciones son como las **etiquetas de un armario organizado con plantillas**: en vez de que cada vez que guardas ropa tengas que decidir de nuevo dónde va cada prenda, defines una vez las etiquetas ("aquí van los calcetines", "aquí las camisetas") y, a partir de ese momento, guardar y encontrar cosas es automático y consistente.

> **💡 ¿Sabías qué...?**
> La estrategia `GenerationType.IDENTITY` delega en el mecanismo autoincremental nativo del SGBD (como el `AUTO_INCREMENT` de MySQL visto en UD2). Existen otras estrategias, como `SEQUENCE` (usa una secuencia explícita del SGBD, muy habitual en PostgreSQL/Oracle) o `TABLE` (simula una secuencia mediante una tabla auxiliar, portable entre cualquier SGBD pero menos eficiente). La elección de estrategia puede afectar al rendimiento en inserciones masivas, un matiz que conviene mencionar aunque no se profundice en el aula.

### ⚠️ Errores comunes
1. **Olvidar el constructor vacío.** JPA lo necesita internamente (vía reflexión) para reconstruir instancias al leer de la base de datos. Sin él, la aplicación falla al arrancar o al ejecutar la primera consulta.
2. **Usar tipos primitivos (`int`, `double`) para claves primarias `@GeneratedValue`.** Antes de persistir el objeto, el id no existe todavía; un tipo primitivo no puede representar "sin valor" (siempre vale al menos `0`), lo que genera confusión. Es preferible usar el tipo envolvente (`Integer`, `Long`).
3. **No anotar correctamente la precisión de tipos monetarios.** Usar `double`/`float` para precios es un error clásico (errores de redondeo en coma flotante); en el mapeo de esta unidad usamos `BigDecimal`, igual que en el `DECIMAL(8,2)` de la tabla SQL de UD2.

### 🎯 Actividad de aula 4.1 — "Del SQL al objeto"

Partiendo de las tablas `clientes` y `pedidos` creadas en UD2 (Bloque 4), diseña y anota las clases `Cliente` y `Pedido` correspondientes (sin relaciones todavía, se añadirán en el Bloque 6). Ejecuta el programa con `hibernate.hbm2ddl.auto=validate` y comprueba que Hibernate confirma que el mapeo coincide exactamente con las tablas ya existentes.

---

## 5. Bloque 4 — Estados de un objeto en una sesión ORM

### 5.1 Los cuatro estados

Un objeto de una clase anotada con `@Entity` puede encontrarse, en un momento dado, en uno de estos estados respecto a la sesión (`EntityManager`) actual:

| Estado | Descripción | Cómo se llega a él |
|---|---|---|
| **Transitorio** (*transient*) | El objeto existe solo en memoria Java; el ORM no lo conoce todavía | `new Producto(...)` |
| **Persistente** (*managed*) | El objeto está asociado a una sesión activa; cualquier cambio en sus atributos se sincroniza automáticamente con la BD | `entityManager.persist(objeto)` o al recuperarlo con `find()`/una consulta |
| **Desprendido** (*detached*) | El objeto tuvo estado persistente, pero la sesión que lo gestionaba ya se cerró; los cambios ya NO se sincronizan automáticamente | Al cerrar el `EntityManager`, o con `entityManager.detach(objeto)` |
| **Eliminado** (*removed*) | El objeto está marcado para ser borrado de la BD al confirmar la transacción | `entityManager.remove(objeto)` |

### Analogía cotidiana

Imagina que un objeto es un **paciente en un hospital**:
- **Transitorio:** una persona que aún no ha entrado en el hospital; el hospital no tiene ficha suya.
- **Persistente:** el paciente está ingresado y monitorizado en tiempo real: si cambia algo en su estado (constantes vitales), el sistema del hospital (la base de datos) se entera automáticamente y lo actualiza.
- **Desprendido:** el paciente recibe el alta. Sigue siendo la misma persona con su historial, pero el hospital ya no le está monitorizando en directo: si le pasa algo después, el hospital no se entera hasta que vuelva a "reingresar" (`merge()`).
- **Eliminado:** su ficha ha sido marcada para ser destruida definitivamente del archivo del hospital.

### 5.2 Ejemplo del ciclo de vida completo

```java
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;

public class CicloDeVidaDemo {
    public static void main(String[] args) {
        EntityManager em = GestorPersistencia.obtenerEntityManager();

        // 1. TRANSITORIO: el objeto solo existe en memoria Java
        Producto producto = new Producto("Ratón inalámbrico", new BigDecimal("19.90"), 50);

        em.getTransaction().begin();

        // 2. PERSISTENTE: a partir de aquí, el ORM lo gestiona activamente
        em.persist(producto);

        // Esta modificación se sincronizará automáticamente al confirmar la transacción,
        // ¡SIN necesidad de llamar a ningún "update" explícito!
        producto.setStock(45);

        em.getTransaction().commit();

        // 3. DESPRENDIDO: al cerrar el EntityManager, el objeto deja de estar gestionado
        em.close();

        producto.setStock(1000); // este cambio YA NO se sincroniza con la BD

        // Para "reenganchar" los cambios de un objeto desprendido:
        EntityManager em2 = GestorPersistencia.obtenerEntityManager();
        em2.getTransaction().begin();
        Producto productoActualizado = em2.merge(producto); // fusiona el estado desprendido
        em2.getTransaction().commit();
        em2.close();
    }
}
```

> **💡 ¿Sabías qué...?**
> Al mecanismo por el cual un objeto **persistente** se sincroniza automáticamente con la base de datos sin necesidad de llamar a ningún método "guardar" explícito se le llama ***dirty checking*** (literalmente, "comprobación de lo que está sucio/modificado"). Al confirmar la transacción, Hibernate compara internamente el estado actual del objeto con una "instantánea" que guardó cuando lo cargó, y genera automáticamente el `UPDATE` únicamente si detecta diferencias. Es una de las funcionalidades más potentes —y a la vez más sorprendentes para quien empieza— de trabajar con un ORM.

### ⚠️ Error común
> Modificar un objeto **desprendido** esperando que el cambio se guarde solo, olvidando que la sesión que lo gestionaba ya se cerró. Es uno de los errores conceptuales más habituales al empezar con ORM, precisamente porque el comportamiento "automático" del estado persistente hace que se dé por hecho ese comportamiento en todos los casos.

### 🎯 Actividad de aula 5.1 — "El detective de estados"

Escribe un pequeño programa que, en cada paso del ciclo de vida de un objeto `Producto` (transitorio, persistente, desprendido tras cerrar el `EntityManager`, y tras `merge()`), imprima por consola el estado en el que se encuentra usando `entityManager.contains(objeto)` (que indica si el objeto está actualmente gestionado por esa sesión), razonando en cada caso el resultado obtenido.

---

## 6. Bloque 5 — Operaciones CRUD sobre objetos persistentes

### 6.1 Crear (`persist`)

```java
public Integer crearProducto(String nombre, BigDecimal precio, int stock) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        em.getTransaction().begin();
        Producto producto = new Producto(nombre, precio, stock);
        em.persist(producto);
        em.getTransaction().commit();
        return producto.getId(); // tras el commit, el id ya está asignado
    } finally {
        em.close();
    }
}
```

### 6.2 Leer (`find` y consultas)

```java
public Producto buscarPorId(Integer id) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        return em.find(Producto.class, id); // devuelve null si no existe (no lanza excepción)
    } finally {
        em.close();
    }
}
```

### 6.3 Actualizar (aprovechando el *dirty checking*)

```java
public void actualizarStock(Integer idProducto, int nuevoStock) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        em.getTransaction().begin();
        Producto producto = em.find(Producto.class, idProducto); // queda en estado persistente
        if (producto != null) {
            producto.setStock(nuevoStock); // el ORM detecta el cambio automáticamente
        }
        em.getTransaction().commit(); // aquí se genera el UPDATE, si hubo cambios reales
    } finally {
        em.close();
    }
}
```

### 6.4 Eliminar (`remove`)

```java
public void eliminarProducto(Integer idProducto) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        em.getTransaction().begin();
        Producto producto = em.find(Producto.class, idProducto);
        if (producto != null) {
            em.remove(producto);
        }
        em.getTransaction().commit();
    } finally {
        em.close();
    }
}
```

### Comparación directa con UD2

| Operación | JDBC puro (UD2) | JPA/Hibernate (UD3) |
|---|---|---|
| Insertar | `PreparedStatement` + `setXxx()` por cada columna + `executeUpdate()` + recuperar clave generada | `em.persist(objeto)` |
| Consultar por id | `PreparedStatement` + `ResultSet` + mapeo manual columna→atributo | `em.find(Clase.class, id)` |
| Modificar | Construir `UPDATE` explícito con los campos a cambiar | Modificar el atributo del objeto; el `UPDATE` se genera solo al hacer `commit` |
| Eliminar | `PreparedStatement` con `DELETE` parametrizado | `em.remove(objeto)` |

### ⚠️ Error común
> Llamar a `em.persist()` sobre un objeto que **ya tiene** un identificador asignado manualmente (por ejemplo, si por error se ha copiado un id de otro objeto), lo que puede provocar `EntityExistsException`. Recuerda: en `persist`, el id lo genera la base de datos (con `GenerationType.IDENTITY` en nuestro ejemplo), el objeto debe llegar **sin id** todavía.

### 🎯 Actividad de aula 6.1 — "CRUD paralelo"

Reescribe con JPA/Hibernate exactamente las mismas cuatro operaciones sobre `Producto` que programaste con JDBC puro en la Actividad 10.1 de UD2 (`ProductoDAO`). Cuenta las líneas de código de cada versión y documenta en un breve informe qué operación notas que se simplifica más y por qué.

---

## 7. Bloque 6 — Relaciones entre entidades (1:1, 1:N, N:M)

### 7.1 Relación uno a muchos (`@OneToMany` / `@ManyToOne`)

Un `Pedido` tiene varias `LineaPedido`; cada `LineaPedido` pertenece a un único `Pedido`. Es la relación más habitual en el dominio de esta unidad.

```java
@Entity
@Table(name = "pedidos")
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "fecha", nullable = false)
    private java.time.LocalDate fecha;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<LineaPedido> lineas = new java.util.ArrayList<>();

    protected Pedido() {}

    public Pedido(Cliente cliente, java.time.LocalDate fecha) {
        this.cliente = cliente;
        this.fecha = fecha;
    }

    // Método de conveniencia: mantiene sincronizados ambos lados de la relación
    public void agregarLinea(LineaPedido linea) {
        lineas.add(linea);
        linea.setPedido(this);
    }

    // getters...
}
```

```java
@Entity
@Table(name = "lineas_pedido")
public class LineaPedido {

    @EmbeddedId
    private LineaPedidoId id; // clave primaria compuesta, ver nota más abajo

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idPedido")
    @JoinColumn(name = "id_pedido")
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("idProducto")
    @JoinColumn(name = "id_producto")
    private Producto producto;

    @Column(name = "cantidad", nullable = false)
    private int cantidad;

    protected LineaPedido() {}

    public LineaPedido(Producto producto, int cantidad) {
        this.producto = producto;
        this.cantidad = cantidad;
    }

    public void setPedido(Pedido pedido) { this.pedido = pedido; }
    // getters...
}
```

> Nota didáctica: la clave primaria compuesta (`id_pedido` + `id_producto`, igual que en la tabla `lineas_pedido` de UD2) es uno de los puntos más avanzados de la unidad. Para una primera aproximación en el aula, es razonable simplificar añadiendo una clave `id` autogenerada propia a `LineaPedido` (una columna extra respecto a UD2) y dejar la clave compuesta con `@EmbeddedId` como contenido de ampliación.

### 7.2 Relación muchos a muchos (`@ManyToMany`)

Ejemplo con un caso nuevo, útil para completar el dominio: un `Producto` puede pertenecer a varias `Categoria`, y una `Categoria` agrupa varios `Producto`.

```java
@Entity
public class Producto {
    // ...atributos anteriores...

    @ManyToMany
    @JoinTable(
        name = "producto_categoria",
        joinColumns = @JoinColumn(name = "id_producto"),
        inverseJoinColumns = @JoinColumn(name = "id_categoria")
    )
    private java.util.Set<Categoria> categorias = new java.util.HashSet<>();
}
```

### 7.3 El problema N+1 y la estrategia de carga (*fetch*)

Uno de los errores de rendimiento más frecuentes al trabajar con ORM es el llamado **problema N+1**: si cargas una lista de *N* pedidos y, para cada uno, accedes a su lista de líneas con `FetchType.EAGER` mal gestionado (o iterando sin cuidado con `LAZY`), el ORM puede acabar ejecutando **1 consulta para los pedidos + N consultas adicionales**, una por cada pedido, para traer sus líneas, en lugar de una única consulta optimizada con `JOIN`.

| Estrategia | Comportamiento | Cuándo usarla |
|---|---|---|
| `FetchType.LAZY` (perezosa) | La relación **no** se carga hasta que se accede a ella explícitamente | Por defecto para colecciones (`@OneToMany`, `@ManyToMany`); evita cargar datos innecesarios |
| `FetchType.EAGER` (ansiosa) | La relación se carga **siempre**, junto con la entidad principal | Solo cuando se sabe que casi siempre se va a necesitar ese dato asociado |

> **💡 ¿Sabías qué...?**
> El valor por defecto de `fetch` en JPA es distinto según el tipo de relación: `@ManyToOne` y `@OneToOne` son **EAGER** por defecto, mientras que `@OneToMany` y `@ManyToMany` son **LAZY** por defecto. Este detalle se pregunta con frecuencia en exámenes de certificación y es una fuente habitual de sorpresas en el rendimiento de aplicaciones reales si no se conoce.

### Ventajas e inconvenientes del mapeo automático de relaciones

- ✅ Ventajas: navegar de un objeto a sus relacionados es tan simple como acceder a un atributo (`pedido.getLineas()`), sin escribir ningún `JOIN` manual; Hibernate gestiona automáticamente los `INSERT`/`UPDATE`/`DELETE` en cascada según la configuración (`CascadeType`).
- ❌ Inconvenientes: si no se entiende bien `LAZY`/`EAGER`, es fácil generar consultas ineficientes (problema N+1) o incluso excepciones (`LazyInitializationException`, al intentar acceder a una relación LAZY después de cerrar la sesión); el mapeo de relaciones complejas (claves compuestas, relaciones N:M con atributos propios) añade complejidad de configuración considerable.

### ⚠️ Errores comunes
1. Acceder a una colección `LAZY` (por ejemplo, `pedido.getLineas()`) **después** de haber cerrado el `EntityManager` que gestionaba el objeto: lanza `LazyInitializationException`, porque ya no hay sesión activa con la que ir a buscar esos datos a la base de datos.
2. Olvidar `cascade = CascadeType.ALL` en una relación de "composición" (como `Pedido`→`LineaPedido`, donde las líneas no tienen sentido sin su pedido), lo que obliga a persistir manualmente cada línea por separado.
3. No mantener sincronizados ambos lados de una relación bidireccional (olvidar el `linea.setPedido(this)` del método `agregarLinea()`), lo que puede dejar el grafo de objetos en memoria inconsistente aunque la base de datos, tras el `commit`, acabe siendo correcta.

### 🎯 Actividad de aula 7.1 — "Cazando el problema N+1"

Con `hibernate.show_sql` activado (Bloque 2), carga una lista de 5 pedidos con `FetchType.LAZY` en sus líneas y recorre cada pedido accediendo a `getLineas().size()`. Cuenta cuántas sentencias `SELECT` aparecen en consola. Después, reescribe la consulta inicial usando `JOIN FETCH` en JPQL (Bloque 7) para traer pedidos y líneas en una sola consulta, y comprueba la diferencia en el número de sentencias generadas.

---

## 8. Bloque 7 — Consultas: JPQL/HQL y SQL nativo

### 8.1 JPQL: un lenguaje de consultas orientado a objetos

**JPQL** (*Jakarta Persistence Query Language*, y su equivalente propio de Hibernate, **HQL**) se parece a SQL en la sintaxis, pero **opera sobre entidades y sus atributos Java, no sobre tablas y columnas**.

```java
import jakarta.persistence.TypedQuery;
import java.util.List;

public List<Producto> listarConStockBajo(int umbral) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        String jpql = "SELECT p FROM Producto p WHERE p.stock < :umbral ORDER BY p.stock";
        TypedQuery<Producto> query = em.createQuery(jpql, Producto.class);
        query.setParameter("umbral", umbral);
        return query.getResultList();
    } finally {
        em.close();
    }
}
```

Observa las diferencias clave respecto al SQL de UD2: `FROM Producto p` (el nombre de la **clase** Java, no de la tabla), `p.stock` (el **atributo**, no la columna `stock`), y el parámetro con nombre `:umbral` en lugar del `?` posicional de JDBC.

### 8.2 `JOIN FETCH`: resolviendo el problema N+1

```java
public List<Pedido> listarPedidosConLineas() {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        String jpql = "SELECT DISTINCT p FROM Pedido p JOIN FETCH p.lineas";
        return em.createQuery(jpql, Pedido.class).getResultList();
    } finally {
        em.close();
    }
}
```

`JOIN FETCH` le indica explícitamente a Hibernate que traiga la entidad principal **y** su colección asociada **en una única consulta SQL**, evitando el problema N+1 descrito en el Bloque 6.

### 8.3 Consultas con nombre (*named queries*)

Para consultas que se reutilizan mucho, es buena práctica declararlas una única vez junto a la entidad:

```java
@Entity
@NamedQuery(
    name = "Producto.buscarPorNombreParcial",
    query = "SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))"
)
public class Producto {
    // ...
}
```

```java
List<Producto> resultado = em.createNamedQuery("Producto.buscarPorNombreParcial", Producto.class)
        .setParameter("texto", "teclado")
        .getResultList();
```

### 8.4 Cuándo recurrir a SQL nativo

A veces JPQL no basta: funciones muy específicas del SGBD, consultas de rendimiento crítico muy optimizadas a mano, o sentencias que combinan tablas sin mapeo directo a entidades. JPA permite ejecutar SQL nativo sin salir del `EntityManager`:

```java
public List<Object[]> informeVentasPorProducto() {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    try {
        String sql = """
            SELECT pr.nombre, SUM(lp.cantidad), SUM(lp.cantidad * pr.precio)
            FROM productos pr
            JOIN lineas_pedido lp ON pr.id_producto = lp.id_producto
            GROUP BY pr.nombre
            ORDER BY SUM(lp.cantidad * pr.precio) DESC
            """;
        return em.createNativeQuery(sql).getResultList();
    } finally {
        em.close();
    }
}
```

Aquí sí volvemos a escribir SQL puro sobre nombres de tabla y columna reales, exactamente como en UD2; la diferencia es que seguimos ejecutándolo a través del mismo `EntityManager`, dentro de la misma gestión de transacciones y conexión que el resto de la aplicación.

> **💡 ¿Sabías qué...?**
> Esta capacidad de mezclar JPQL y SQL nativo dentro de la misma aplicación es, precisamente, uno de los criterios de evaluación explícitos del RD 405/2023 para esta unidad ("desarrolla aplicaciones que realizan consultas usando el lenguaje SQL"): un buen profesional no elige "ORM o SQL", sino que usa JPQL para el 90 % de las consultas habituales y SQL nativo cuando el caso lo justifica, con criterio.

### Ventajas e inconvenientes: JPQL vs. SQL nativo

| | JPQL/HQL | SQL nativo (vía JPA) |
|---|---|---|
| **Portabilidad entre SGBD** | Alta (Hibernate traduce al dialecto configurado) | Baja (sintaxis específica del SGBD) |
| **Orientación a objetos** | Total: opera sobre entidades y sus relaciones | Ninguna: opera sobre tablas y columnas reales |
| **Control fino de rendimiento** | Bueno, pero limitado por lo que el ORM sabe generar | Total |
| **Curva de aprendizaje si ya se sabe SQL** | Ligera (sintaxis muy parecida) | Ninguna, es SQL puro |

### ⚠️ Error común
> Usar `getSingleResult()` cuando la consulta puede devolver cero resultados. A diferencia de `find()` (que devuelve `null` si no encuentra nada), `getSingleResult()` **lanza `NoResultException`** si no hay ningún resultado, y `NonUniqueResultException` si hay más de uno. Hay que capturar estas excepciones específicas o usar `getResultList()` y comprobar si está vacía.

### 🎯 Actividad de aula 8.1 — "El mismo informe, dos caminos"

Reescribe el informe de ventas de la Actividad 6.1 de UD2 (unidades vendidas e importe total por producto) primero en JPQL puro (agregando con `SUM` y `GROUP BY` sobre entidades) y después, si no es directamente expresable de forma cómoda, en SQL nativo. Documenta si conseguiste expresarlo completamente en JPQL o tuviste que recurrir a SQL nativo, y por qué.

---

## 9. Bloque 8 — Gestión de transacciones con el ORM

### 9.1 `EntityTransaction`

En un entorno `RESOURCE_LOCAL` (el habitual en una aplicación de escritorio o de consola, como las de esta programación), las transacciones se gestionan a través del objeto `EntityTransaction` obtenido del `EntityManager`, con una API deliberadamente parecida a la de JDBC vista en UD2:

```java
public void crearPedidoConLineas(Integer idCliente, List<LineaPedidoDTO> lineasSolicitadas) {
    EntityManager em = GestorPersistencia.obtenerEntityManager();
    EntityTransaction tx = em.getTransaction();

    try {
        tx.begin();

        Cliente cliente = em.find(Cliente.class, idCliente);
        Pedido pedido = new Pedido(cliente, java.time.LocalDate.now());

        for (LineaPedidoDTO dto : lineasSolicitadas) {
            Producto producto = em.find(Producto.class, dto.idProducto());

            if (producto.getStock() < dto.cantidad()) {
                throw new IllegalStateException("Stock insuficiente para " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - dto.cantidad()); // dirty checking

            pedido.agregarLinea(new LineaPedido(producto, dto.cantidad()));
        }

        em.persist(pedido); // cascade = ALL persiste también las líneas automáticamente

        tx.commit();
        System.out.println("Pedido #" + pedido.getId() + " registrado correctamente.");

    } catch (Exception e) {
        if (tx.isActive()) {
            tx.rollback();
        }
        System.err.println("Transacción deshecha. Motivo: " + e.getMessage());
    } finally {
        em.close();
    }
}
```

*(Se asume un `record LineaPedidoDTO(Integer idProducto, int cantidad) {}` como tipo auxiliar para los datos de entrada.)*

### 9.2 Comparación directa con UD2 (Bloque 8)

| | JDBC (UD2) | JPA/Hibernate (UD3) |
|---|---|---|
| Iniciar transacción | `conexion.setAutoCommit(false)` | `em.getTransaction().begin()` |
| Confirmar | `conexion.commit()` | `em.getTransaction().commit()` |
| Deshacer | `conexion.rollback()` | `em.getTransaction().rollback()` |
| ¿Qué se deshace? | Las sentencias SQL ya enviadas | Todos los cambios detectados en los objetos gestionados, y las sentencias SQL generadas a partir de ellos |

La lógica es la misma que en UD2 (Bloque 8), pero fíjate en un matiz importante: en el ejemplo anterior **no hemos escrito ni un `UPDATE` ni un `INSERT` explícitos** para el descuento de stock ni para las líneas del pedido; ha sido el mecanismo de *dirty checking* y de `cascade` (Bloque 6) el que ha generado automáticamente el SQL necesario al hacer `commit()`.

> **💡 ¿Sabías qué...?**
> En aplicaciones basadas en frameworks como Spring, este patrón de `try/begin/commit/rollback/finally` casi nunca se escribe a mano: se delega en la anotación `@Transactional`, que envuelve automáticamente el método con la gestión de transacciones. No se trabajará en profundidad en este módulo por no formar parte del currículo oficial de FP, pero conviene que el alumnado sepa que este patrón manual que están aprendiendo es exactamente lo que ese tipo de anotaciones automatizan por debajo en entornos profesionales más avanzados.

### ⚠️ Error común
> Olvidar comprobar `tx.isActive()` antes de llamar a `rollback()` en el bloque `catch`. Si la excepción se produjo, por ejemplo, durante el propio `commit()` (y no antes), la transacción podría ya no estar activa, y llamar a `rollback()` sobre una transacción inactiva lanza una nueva excepción que además "tapa" el motivo real del primer fallo.

### 🎯 Actividad de aula 9.1 — "Rollback con ORM"

Reproduce la Actividad 9.1 de UD2 (transferencia entre cuentas con `rollback` forzado) pero ahora con entidades JPA. Comprueba que, al provocar un error a mitad del proceso, ningún cambio realizado sobre los objetos en memoria (ni siquiera los que el *dirty checking* ya había "detectado" como modificados) llega finalmente a la base de datos.

---

## 10. Bloque 9 — JDBC vs. ORM: valoración comparada

### 10.1 Reto de comparación directa

Esta unidad se cierra, como estaba previsto desde la planificación general del módulo, con un ejercicio explícito de comparación entre el mismo caso resuelto con conectores (UD2) y resuelto con ORM (UD3), para que el alumnado interiorice de forma crítica cuándo conviene cada enfoque.

| Criterio | JDBC puro (UD2) | JPA/Hibernate (UD3) |
|---|---|---|
| Líneas de código para un CRUD completo | Alto (mapeo manual, gestión explícita de recursos) | Bajo (persist/find/remove + dirty checking) |
| Control exacto sobre el SQL ejecutado | Total | Parcial (aunque se puede inspeccionar y ajustar) |
| Riesgo de inyección SQL | Alto si no se usa `PreparedStatement` sistemáticamente | Bajo por diseño (JPQL parametrizado) |
| Rendimiento en operaciones muy simples | Máximo posible | Ligero *overhead* por la capa de abstracción |
| Rendimiento en operaciones complejas mal entendidas | Depende solo de la pericia del programador | Riesgo de problema N+1 si no se domina *fetch*/`JOIN FETCH` |
| Mantenibilidad ante cambios de esquema | SQL disperso, revisión manual | Cambios centralizados en el mapeo de la entidad |
| Portabilidad entre SGBD | Baja (SQL específico) | Alta (JPQL + cambio de dialecto) |
| Curva de aprendizaje | Menor si ya se sabe SQL | Mayor al principio (estados, *fetch*, caché...) |

### 10.2 Cuándo elegir cada enfoque en la vida profesional

No es una elección "para siempre": muchas aplicaciones profesionales reales combinan ambos enfoques. Un criterio orientativo razonable:

- Usa **ORM (JPQL)** por defecto, para la inmensa mayoría de las operaciones CRUD y consultas habituales de la aplicación.
- Recurre a **SQL nativo dentro del propio ORM** (Bloque 7) cuando la consulta sea muy específica del SGBD o de rendimiento crítico.
- Considera **JDBC puro** (o herramientas ligeras intermedias, como MyBatis, fuera del alcance de esta unidad) para procesos de carga masiva de datos, informes muy pesados, o sistemas donde el control absoluto del SQL sea un requisito no negociable.

### 🎯 Actividad de aula 10.1 — "Debate técnico"

Organiza un pequeño debate/coloquio en el aula: divide a la clase en dos grupos, uno defiende "usar siempre JDBC puro" y el otro "usar siempre ORM", cada uno con argumentos técnicos concretos basados en lo aprendido en UD2 y UD3. Cierra la actividad con una puesta en común de la postura intermedia y matizada (la que se recoge en el apartado 10.2).

---

## 11. Proyecto integrador de la unidad — "Migración del sistema de pedidos a ORM"

Este proyecto reutiliza literalmente el mismo dominio de datos y el mismo caso de uso del proyecto integrador de UD2, pero resuelto ahora con JPA/Hibernate, cerrando el ciclo comparativo de la unidad.

### Especificación

1. **Mapea con anotaciones** las cuatro entidades del dominio: `Cliente`, `Producto`, `Pedido`, `LineaPedido`, incluyendo correctamente las relaciones `@ManyToOne`/`@OneToMany` entre `Pedido` y `LineaPedido`, y `@ManyToOne` entre `Pedido` y `Cliente`.
2. **Reimplementa el CRUD** de productos y clientes (alta, baja, modificación, listado) usando `EntityManager`, reutilizando el patrón DAO ya conocido de UD2 pero ahora como un `ProductoRepositorioJPA`.
3. **Reimplementa el registro de pedidos transaccional**, comprobando el stock disponible antes de confirmar, exactamente con la misma regla de negocio que en UD2, pero ahora apoyándote en `dirty checking` y `cascade` en lugar de sentencias `INSERT`/`UPDATE` manuales.
4. **Reimplementa el informe de ventas** (Bloque 7) tanto en JPQL con `JOIN FETCH` como en SQL nativo, y compara el SQL generado por Hibernate (con `show_sql` activo) en ambos casos.
5. **Detecta y corrige un problema N+1 intencionado**: el enunciado debe incluir, a propósito, un primer listado de pedidos con sus líneas mal resuelto (sin `JOIN FETCH`), y pedir al alumnado que lo identifique en consola y lo optimice.
6. **(Ampliación opcional)** Añade la relación `@ManyToMany` entre `Producto` y una nueva entidad `Categoria`, y una consulta JPQL que liste los productos de una categoría concreta.

### Rúbrica orientativa de evaluación del proyecto

| Criterio | Insuficiente | Adecuado | Excelente |
|---|---|---|---|
| Mapeo de entidades y relaciones | Entidades sin relaciones correctas o sin constructor vacío | Relaciones 1:N bien mapeadas y funcionales | Además usa `cascade`/`orphanRemoval` con criterio justificado |
| Operaciones CRUD | Falta alguna operación básica | CRUD completo correcto con `EntityManager` | Además aprovecha `dirty checking` en vez de `UPDATE` manuales innecesarios |
| Consultas JPQL/SQL nativo | Solo usa `find()`, sin consultas propias | Consultas JPQL parametrizadas correctas | Además distingue con criterio cuándo usar SQL nativo |
| Gestión de transacciones | No usa transacciones donde son necesarias | `begin`/`commit`/`rollback` correctos | Además contempla y prueba explícitamente casos de fallo |
| Rendimiento (N+1) | No detecta el problema planteado | Detecta el problema N+1 | Además lo corrige con `JOIN FETCH` y lo demuestra con `show_sql` |

---

## 12. Cuestionario de autoevaluación

1. Explica la diferencia entre JPA y Hibernate. ¿Por qué se dice que Hibernate "usa JDBC por debajo"?
2. ¿Qué es el *dirty checking* y en qué estado debe estar un objeto para que funcione?
3. Describe, en tus propias palabras, los cuatro estados de un objeto en una sesión ORM, con un ejemplo distinto al de la analogía del hospital.
4. ¿Qué diferencia hay entre `em.find()` y una consulta JPQL con `getSingleResult()` cuando el objeto buscado no existe?
5. ¿Qué es el problema N+1 y qué herramienta de JPQL permite evitarlo?
6. ¿Por qué `@ManyToOne` es `EAGER` por defecto y `@OneToMany` es `LAZY` por defecto? ¿Qué problema práctico intenta evitar esa elección de diseño?
7. Cita dos situaciones en las que, aun trabajando con un ORM, seguiría siendo razonable recurrir a SQL nativo.
8. Compara cómo se gestiona una transacción en JDBC (UD2) y en JPA (UD3): ¿qué tienen en común y qué cambia realmente "por debajo"?
9. ¿Qué excepción se produce si accedes a una colección `LAZY` después de cerrar el `EntityManager` que gestionaba el objeto? ¿Por qué ocurre?
10. Después de haber resuelto el mismo caso con JDBC y con ORM, argumenta con tus propias palabras en qué tipo de proyecto profesional elegirías cada enfoque.

---

## 13. Glosario de la unidad

- **Cascade (cascada):** configuración que propaga automáticamente operaciones (persistir, eliminar...) desde una entidad "padre" a sus entidades relacionadas.
- **Dirty checking:** mecanismo por el cual el ORM detecta automáticamente qué atributos de un objeto persistente han cambiado, generando el `UPDATE` correspondiente solo cuando es necesario.
- **EntityManager:** objeto que representa una sesión de trabajo con la base de datos en JPA; gestiona el ciclo de vida de las entidades.
- **EntityManagerFactory:** fábrica, costosa de crear y reutilizada durante toda la aplicación, encargada de producir `EntityManager`.
- **Estado desprendido (detached):** estado de un objeto que fue persistente pero cuya sesión ya se cerró; sus cambios no se sincronizan automáticamente.
- **Fetch (LAZY/EAGER):** estrategia que determina si una relación se carga de forma perezosa (bajo demanda) o inmediata (junto con la entidad principal).
- **JPA (Jakarta Persistence API):** especificación estándar de Java para el mapeo objeto-relacional.
- **JPQL/HQL:** lenguaje de consultas orientado a objetos que opera sobre entidades y atributos Java, no sobre tablas y columnas.
- **N+1 (problema de las):** patrón de rendimiento ineficiente en el que se ejecuta una consulta adicional por cada elemento de una colección, en vez de una única consulta optimizada.
- **ORM (Object-Relational Mapping):** técnica y herramienta que automatiza la traducción entre objetos de un lenguaje de programación y filas de tablas relacionales.
- **Persistence unit (unidad de persistencia):** configuración nombrada, definida en `persistence.xml`, que agrupa los parámetros de conexión y comportamiento de un `EntityManagerFactory`.
