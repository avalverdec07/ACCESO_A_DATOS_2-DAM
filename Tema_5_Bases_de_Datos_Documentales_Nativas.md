# Tema 5: Bases de Datos Documentales Nativas: MongoDB
## Versión actualizada (MongoDB 8.0, driver Java 5.x, transacciones, modelado embeber/referenciar, índices, Atlas)

> Documento base: apuntes originales de IES Augustóbriga, revisados y corregidos. **Corrección importante**: el original afirma que las bases NoSQL "no permiten transacciones"; esto es obsoleto para MongoDB, que soporta transacciones ACID multi-documento desde la versión 4.0. Se han actualizado también las versiones de MongoDB y del driver Java, y se han añadido los apartados de modelado de datos, índices, seguridad y MongoDB Atlas, ausentes en el original.

---

## Índice

1. Bases de datos NoSQL
2. Clave-valor
3. Documental
4. Columnar
5. Grafo
6. Las bases de datos NoSQL y el teorema de CAP
7. Transacciones en MongoDB
8. Base de datos MongoDB
   - 8.1 Instalación local y MongoDB Atlas
   - 8.2 CRUD en Mongo (mongosh)
   - 8.3 Driver Java (Maven)
   - 8.4 Establecer conexión a Mongo
   - 8.5 Seleccionar la base de datos
   - 8.6 Operaciones con colecciones
   - 8.7 Tipos de datos BSON
   - 8.8 Insertar
   - 8.9 Nº de documentos de una colección
   - 8.10 Filtros en consultas
   - 8.11 Modificar/Reemplazar documentos
   - 8.12 Borrar documentos
   - 8.13 Recuperación
   - 8.14 CRUD en arrays de documentos embebidos
   - 8.15 Operaciones de agregación
   - 8.16 Operaciones de JOIN (`$lookup`)
   - 8.17 Trabajar con POJOs
9. Modelado de datos: embeber vs. referenciar
10. Índices
11. Seguridad y autenticación
12. Ejercicios
13. Glosario

---

## 1 Bases de datos NoSQL

Por bases de datos **NoSQL** (*Not Only SQL*) se conocen una serie de tecnologías de almacenamiento de datos que siguen modelos alternativos al clásico modelo relacional, y que fijan sus prioridades en ser capaces de **gestionar grandes volúmenes de datos de forma eficiente y flexible**, priorizando la escalabilidad y la disponibilidad frente a la atomicidad y consistencia estrictas del modelo relacional.

Surgen como respuesta a casos de uso en los que hay que gestionar, de forma rápida y eficiente, grandes volúmenes de datos que proceden de fuentes distintas y en formatos variados, permitiendo a las aplicaciones adaptarse rápidamente a los cambios.

Existen múltiples tipos de bases de datos NoSQL, agrupadas normalmente en estas categorías:

- **Clave-valor (Redis)**: cada elemento se compone de una clave identificativa única y un valor asociado. El valor puede ser de cualquier tipo, incluso ficheros. Los valores actúan como "cajas negras" para el SGBD, aunque en ciertas situaciones se pueden crear índices sobre datos incluidos en el valor.
- **Documentales (MongoDB)**: se asocia una clave identificativa única con un documento (en MongoDB, en formato **BSON**, no XML — ver apartado 3). Estos documentos pueden tener cualquier estructura, de manera que en una misma colección pueden convivir documentos con esquemas distintos.
- **Grafos (Neo4j)**: almacena información sobre nodos y conexiones entre ellos, estableciendo una estructura de red.
- **Columnares (Cassandra)**: los datos se almacenan por columnas, no por filas; cada registro puede tener un número diferente de columnas.

**Características generales compartidas**

- No ofrecen SQL como lenguaje estándar; en su lugar proporcionan una API propia para interactuar con el servidor.
- **Schemaless**: no tienen esquema rígido, lo que permite adaptabilidad frente a cambios o cuando a priori no se conoce la estructura de los datos (aunque, como se ve en el apartado 8.7, MongoDB permite *validar* un esquema de forma opcional).
- Siguen arquitecturas distribuidas que favorecen la escalabilidad horizontal frente a la vertical.
- Normalmente son de código abierto.
- Facilitan la interacción del código con la BD, al trabajar en muchos casos con el mismo tipo de estructura (objetos/documentos), reduciendo las transformaciones entre modelos.

> **⚠️ Aviso importante — "no soportan ACID ni transacciones" ya no es cierto en general**: durante años se repitió que las bases NoSQL no soportan transacciones ACID como las relacionales. Esto **ya no es así para MongoDB**: desde la versión **4.0** (2018) soporta transacciones ACID multi-documento, y desde la **4.2**, transacciones distribuidas en clústeres *sharded* (ver apartado 7). Sigue siendo cierto que, por diseño, se prioriza el rendimiento usando transacciones solo cuando es estrictamente necesario, ya que tienen un coste de rendimiento mayor que las operaciones atómicas a nivel de documento.

**Desventajas frente a los sistemas tradicionales**

- El lenguaje de consultas no es tan avanzado como en los sistemas tradicionales, y puede ser menos eficiente en consultas complejas que combinen datos de varias colecciones.
- La falta de esquema común exige disciplina en el equipo de desarrollo; si no la hay, el modelo puede volverse difícil de mantener.
- Falta de estándares, al ser tecnologías relativamente nuevas y de distintos tipos.

## 2 Clave-valor

De entre las bases NoSQL son las que tienen el modelo más sencillo. Conceptualmente se conciben como una tabla hash de dos columnas (**clave** y **valor**), donde la clave actúa como clave primaria e identifica cada elemento de forma única, y el valor puede ser cualquier tipo de objeto. Los registros clave-valor pueden agruparse lógicamente en **buckets**.

![Diagrama de buckets clave-valor: Bucket A y Bucket B, cada uno con pares Key→Value](Tema_5_imagenes/img-clave-valor-buckets.png)

Normalmente, las bases NoSQL de este tipo son de **tipo AP** (Disponibilidad y Tolerancia a Particiones) y tratan de mantener la mayor parte de la información en memoria, serializando los datos periódicamente. Así se obtiene un gran rendimiento, a costa de una consistencia eventual.

Aunque en principio las bases de datos no conocen el contenido del valor, para cierto tipo de datos sí lo hacen, permitiendo definir índices adicionales.

## 3 Documental

Las NoSQL basadas en documentos permiten almacenar los registros como **documentos**, formados por una secuencia de elementos *atributo:valor*. De entre todos los atributos, hay uno especial que ejerce de clave y que identifica cada documento de forma única.

Los documentos se agrupan en **colecciones**. Si hacemos un paralelismo con el modelo relacional, las colecciones serían tablas y los documentos, registros.

![Estructura de MongoDB: una base de datos contiene una o más colecciones, y cada colección contiene documentos (pares clave-valor o listas o documentos anidados)](Tema_5_imagenes/img-mongodb-estructura.png)

MongoDB representa los documentos en **BSON** (*Binary JSON*), una serialización binaria del formato JSON que añade tipos adicionales que JSON no tiene de forma nativa (fechas, enteros de 32/64 bits, `ObjectId`, binarios...).

![Ejemplo de documento BSON anotado: _id como ObjectId, nombre como texto, fecha como ISO 8601, activo como booleano, notas como array, dirección como objeto embebido](Tema_5_imagenes/img-bson-ejemplo.png)

En una base de datos documental, cada documento puede tener su propio esquema; no obstante, semánticamente, dentro de una misma colección deberíamos incluir únicamente documentos que referencien al mismo tipo de elementos.

Este tipo de bases de datos permite hacer consultas sobre cualquier campo de los documentos y definir distintos tipos de índices, además del básico por clave (apartado 10).

A la hora de modificar los datos, las actualizaciones se hacían tradicionalmente a nivel de documento, en una única sentencia. Esto sigue siendo lo más habitual y lo más eficiente, pero **ya no es una limitación técnica**: MongoDB permite agrupar varias operaciones sobre distintos documentos (incluso de distintas colecciones) dentro de una transacción real (apartado 7).

## 4 Columnar

A diferencia de los sistemas relacionales clásicos, que organizan el contenido de las tablas por filas, los sistemas columnares lo hacen por columnas: para una columna determinada se almacenan todos los valores de los distintos registros.

Además, permiten que un registro tenga un esquema variable y referencie a más o menos columnas según su naturaleza. Para leer rápidamente todas las columnas de un registro, cada valor columnar está vinculado a una clave de registro.

![Comparación row-oriented vs column-oriented: en el primero las filas se almacenan secuencialmente en un fichero; en el segundo cada columna se almacena en un fichero separado, y cada columna está en el mismo offset para una fila dada](Tema_5_imagenes/img-row-vs-column.png)

Este tipo de bases de datos se usa normalmente en **sistemas analíticos (OLAP)**. Los sistemas transaccionales (**OLTP**) usan principalmente sistemas relacionales, optimizados para trabajar a nivel de registro (dar de alta usuarios, gestionar compras, hacer reservas...). Sin embargo, operaciones típicas de sistemas analíticos, como las agregaciones, se hacen de forma mucho más eficiente en sistemas columnares, ya que requieren leer menos datos:

```sql
SELECT tipo, SUM(importe)
FROM Facturas
GROUP BY tipo;
```

Para resolver esta consulta solo hace falta leer las columnas `tipo` e `importe`, mientras que en un sistema basado en filas habría que leer todos los registros con muchos datos innecesarios. Piénsese en una tabla con cientos de millones de registros y cien columnas.

Dadas sus características, este tipo de bases de datos son adecuadas como **Data Warehouse**, ya que sobre ellas se realizan operaciones de tipo analítico, básicamente de lectura. En infraestructuras *on-premise* se puede optar por **Cassandra** o **HBase**; en la nube, por **AWS Redshift**, **Google BigQuery** o **Snowflake**.

## 5 Grafo

Este modelo permite almacenar información sobre entidades y relaciones entre ellas. Las entidades están formadas por atributos, y las relaciones entre entidades tienen también sus propios atributos y un sentido. El resultado es una estructura de red compleja, muy útil en aplicaciones de redes sociales, sistemas de mapeo y rutas, etc.

![Ejemplo de grafo: dos personas (Dan y Ann) conectadas por relaciones LOVES y LIVES WITH, y Dan conectado a un coche mediante OWNS](Tema_5_imagenes/img-grafo-ejemplo.png)

La gran fortaleza de estos sistemas es que no necesitan hacer consultas para comprobar la relación entre elementos, ya que las relaciones son intrínsecas al modelo y se pueden recorrer directamente. No son aptos para cualquier caso de uso: modificar todas o un subconjunto de entidades es costoso, y hacer búsquedas de datos no relacionados entre sí también lo es.

Este modelo es menos intuitivo que otros, con una curva de aprendizaje más alta. La base de datos de este tipo más conocida es **Neo4j** (con una versión gestionada en la nube, **Neo4j AuraDB**); otras alternativas actuales son **Amazon Neptune** y **ArangoDB**.

## 6 Las bases de datos NoSQL y el teorema de CAP

El **teorema de CAP** (o conjetura de Brewer) se aplica a sistemas de almacenamiento distribuido y establece que solo se pueden cumplir completamente dos de estas tres propiedades:

- **Consistencia (C)**: cualquier lectura devuelve la última versión del dato; si se modifica un dato, nunca se devuelve el valor anterior.
- **Disponibilidad (A)**: la base de datos siempre devuelve un valor.
- **Tolerancia a particiones (P)**: el sistema sigue funcionando aunque haya un problema puntual de comunicación entre nodos.

Según este teorema, se pueden construir sistemas **CP** (consistentes y tolerantes a particiones), sistemas **AP** (disponibles y tolerantes a particiones) y sistemas **CA** (consistentes y disponibles, pero no tolerantes a particiones). Cada base de datos NoSQL trabaja en un modo determinado:

![Triángulo del teorema de CAP: vértice Availability con categoría CA (Neo4j) donde un problema de red puede detener el sistema; vértice Partition Tolerance con categoría AP (Riak, Cassandra) donde los clientes pueden leer datos inconsistentes; vértice Consistency con categoría CP (MongoDB, Redis, HBase) donde hay riesgo de que algunos nodos queden no disponibles](Tema_5_imagenes/img-teorema-cap.png)

En un sistema **fuertemente consistente** (*strong consistency*), las escrituras son visibles en las próximas lecturas; en un sistema con **consistencia eventual** (*eventual consistency*), las escrituras no son visibles de inmediato, pudiendo devolverse valores desfasados.

Para muchas aplicaciones es imprescindible que los datos sean consistentes en todo momento; otras pueden asumir que no siempre se devuelva el último dato, a cambio de más rendimiento o disponibilidad (por ejemplo, sistemas donde básicamente se hacen lecturas y las modificaciones son escasas).

Por ejemplo, en **MongoDB**, por defecto, todas las lecturas y escrituras se hacen sobre la copia principal del dato, garantizando consistencia fuerte; pero se puede configurar para que las lecturas se hagan sobre copias secundarias, donde los datos podrían tener consistencia eventual. Es decir, se puede pasar de un sistema **CP** a un sistema **AP** según cómo se configure la preferencia de lectura (*read preference*).

## 7 Transacciones en MongoDB

Como se ha señalado en el apartado 1, **MongoDB soporta transacciones ACID multi-documento** desde la versión 4.0, y transacciones distribuidas (entre varios *shards*) desde la 4.2. Esto permite agrupar varias operaciones —incluso sobre distintas colecciones— como una unidad atómica, igual que se hace con `EntityTransaction` en JPA (Tema 3 - ORM) o con `conexion.commit()`/`rollback()` en JDBC (Tema 2).

```java
try (ClientSession session = mongoClient.startSession()) {
    session.startTransaction();
    try {
        MongoCollection<Document> alumnos = database.getCollection("alumno");
        MongoCollection<Document> auditoria = database.getCollection("auditoria");

        alumnos.updateOne(session, Filters.eq("dni", "1A"),
                Updates.set("famNum", true));

        auditoria.insertOne(session, new Document()
                .append("accion", "modificacion_alumno")
                .append("fecha", new Date()));

        session.commitTransaction();
    } catch (Exception e) {
        session.abortTransaction();
        System.err.println("Transacción deshecha: " + e.getMessage());
    }
}
```

> **Dato importante**: aunque la capacidad existe, usar transacciones en MongoDB tiene un coste de rendimiento mayor que las operaciones atómicas habituales a nivel de documento (cada operación sobre un único documento ya es atómica por diseño, sin necesidad de transacción explícita). La recomendación general de MongoDB es **modelar los datos para minimizar la necesidad de transacciones** (ver apartado 9, embeber vs. referenciar) y reservarlas para los casos en los que de verdad hace falta atomicidad entre documentos distintos.

## 8 Base de datos MongoDB

### 8.1 Instalación local y MongoDB Atlas

**Instalación local**: ejecutar el instalador de MongoDB Community Server (versión **8.0** o superior; https://www.mongodb.com/try/download/community), que incluye el servidor y, opcionalmente, el cliente gráfico **MongoDB Compass**. Por defecto está configurado para acceder al servidor sin usuarios y solo se aceptan conexiones desde `localhost` (ver apartado 11 sobre cómo activar autenticación).

**MongoDB Atlas**: en la práctica profesional actual, la mayoría de los despliegues de MongoDB se hacen en **Atlas** (https://www.mongodb.com/atlas), el servicio de base de datos gestionado en la nube de MongoDB, con un nivel gratuito (M0) adecuado para aprendizaje y prototipos. En vez de instalar un servidor local, se crea un clúster en Atlas y se obtiene una cadena de conexión del tipo:

```
mongodb+srv://usuario:clave@nombrecluster.xxxxx.mongodb.net/?retryWrites=true&w=majority
```

Todo el código de este tema funciona igual trabajando en local o contra Atlas; lo único que cambia es la cadena de conexión que se pasa a `MongoClients.create(...)` (apartado 8.4).

### 8.2 CRUD en Mongo (mongosh)

**mongosh** es el shell interactivo de MongoDB (se instala junto al servidor, o se puede descargar aparte desde https://www.mongodb.com/try/download/shell).

| Acción | Comando |
|---|---|
| Limpiar la consola | `Ctrl+L` |
| Listar las bases de datos | `show dbs` |
| Cambiarse de base de datos | `use <nombreBD>` |
| Listar las colecciones de una BD | `show collections` / `show tables` |
| Mostrar el nombre de la BD actual | `db.getName()` / `db` |

**Insertar uno**

```javascript
db.alumno.insertOne({
    "dni": "1A",
    "nombre": "Pedro",
    "fechaN": "2000-01-01",
    "famNum": false,
    "curso": ["1ºDAM", "1ºDAW"],
    "notas": [
        {"asignatura": "LM", "fecha": "2023-11-01", "nota": 7},
        {"asignatura": "AD", "fecha": "2023-12-01", "nota": 8}
    ]
});
```

**Insertar varios**

```javascript
db.alumno.insertMany([
    {
        "dni": "1A", "nombre": "Pedro", "fechaN": "2000-01-01", "famNum": false,
        "curso": ["1ºDAM", "1ºDAW"],
        "notas": [
            {"asignatura": "LM", "fecha": "2023-11-01", "nota": 7},
            {"asignatura": "AD", "fecha": "2023-12-01", "nota": 8}
        ]
    },
    {
        "dni": "1B", "nombre": "María", "fechaN": "2000-06-01", "famNum": true,
        "curso": ["2ºDAM"],
        "notas": [
            {"asignatura": "LM", "fecha": "2023-11-01", "nota": 4},
            {"asignatura": "AD", "fecha": "2023-12-01", "nota": 9}
        ]
    }
]);
```

**Borrar**

```javascript
// Borrar uno: indicar filtro
db.alumno.deleteOne({"dni": "1A"})

// Borrar varios: indicar filtro
db.alumno.deleteMany({"famNum": true})
```

**Modificar**

```javascript
// Modificar uno: indicar filtro y modificaciones
db.alumno.updateOne(
    { "dni": "1A" },
    { $set: { "nombre": "Ana", "famNum": true } }
)

// Modificar varios: indicar filtro y modificaciones
db.alumno.updateMany(
    { "dni": "1A" },
    { $set: { "famNum": true } }
)
```

**Reemplazar**

```javascript
// Reemplazar: indicar filtro y nuevo documento completo
db.alumno.replaceOne(
    { "dni": "1A" },
    {
        "dni": "1A", "nombre": "Pedro", "fechaN": "2000-01-01", "famNum": false,
        "curso": ["1ºDAM", "1ºDAW"],
        "notas": [
            {"asignatura": "LM", "fecha": "2023-11-01", "nota": 7},
            {"asignatura": "AD", "fecha": "2023-12-01", "nota": 8}
        ]
    }
)
```

**Consultas**

```javascript
// Todos los documentos de la colección
db.alumno.find()

// Filtro: Y lógico (ambas condiciones)
db.alumno.find({ "dni": "1A", "famNum": false })

// Filtro: O lógico
db.alumno.find({ $or: [ {"dni": "1A"}, {"famNum": false} ] })

// Mayor que
db.alumno.find({ "notas.nota": { $gt: 8 } })

// Entre (rango)
db.alumno.find({ "notas.nota": { $gte: 3, $lte: 5 } })
```

**Consultas sobre arrays**

```javascript
// Elemento incluido en el array
db.alumno.find({ "curso": "1ºDAM" })

// Todos los elementos indicados están en el array (en cualquier orden)
db.alumno.find({ "curso": { $all: ["1ºDAW", "2ºDAM"] } })
db.alumno.find({ "curso": { $all: ["1ºDAW", "1ºDAM"] } })

// Comparar el array completo (mismo orden y mismos elementos)
db.alumno.find({ "curso": ["1ºDAW", "1ºDAM"] })
db.alumno.find({ "curso": ["1ºDAM", "1ºDAW"] })
```

### 8.3 Driver Java (Maven)

Documentación oficial: https://www.mongodb.com/docs/drivers/java/sync/current/quick-start/

Los paquetes con los que trabajaremos son **com.mongodb** y **org.bson**.

```xml
<dependencies>
    <!-- https://mvnrepository.com/artifact/org.mongodb/mongodb-driver-sync -->
    <dependency>
        <groupId>org.mongodb</groupId>
        <artifactId>mongodb-driver-sync</artifactId>
        <version>5.2.0</version>
    </dependency>
</dependencies>
```

### 8.4 Establecer conexión a Mongo

```java
// Conexión en local
MongoClient cliente = MongoClients.create("mongodb://localhost:27017");
```

```java
// Conexión a MongoDB Atlas (ver apartado 8.1)
String connectionString =
    "mongodb+srv://usuario:clave@nombrecluster.xxxxx.mongodb.net/?retryWrites=true&w=majority";
MongoClient cliente = MongoClients.create(connectionString);
```

> **Error común — no cerrar el `MongoClient`**: `MongoClient` implementa `AutoCloseable`, igual que los *streams* del Tema 1 o la `Connection` de JDBC en el Tema 2. No cerrarlo deja conexiones abiertas en el *pool* interno del driver. Usa siempre `try-with-resources`:
> ```java
> try (MongoClient cliente = MongoClients.create("mongodb://localhost:27017")) {
>     MongoDatabase database = cliente.getDatabase("IES");
>     // ... operaciones ...
> } // el cliente se cierra automáticamente aquí
> ```

### 8.5 Seleccionar la base de datos

Se puede seleccionar una base de datos aunque no exista todavía: se creará en el momento en que se cree el primer documento.

```java
MongoDatabase database = cliente.getDatabase("IES");
```

### 8.6 Operaciones con colecciones

```java
// Mostrar las colecciones existentes
for (String name : database.listCollectionNames()) {
    System.out.println(name);
}

// Seleccionar/crear colección
MongoCollection<Document> collection = database.getCollection("aulas");
```

### 8.7 Tipos de datos BSON

| Tipo BSON | Tipo de la librería Java BSON |
|---|---|
| Array | `org.bson.BsonArray` |
| Binary | `org.bson.BsonBinary` |
| Boolean | `org.bson.Boolean` |
| Date (long value) | `org.bson.BsonDateTime` |
| Document | `org.bson.BsonDocument` |
| Double | `org.bson.BsonDouble` |
| Int32 | `org.bson.BsonInt32` |
| Int64 | `org.bson.BsonInt64` |
| Null | `org.bson.BsonNull` |
| ObjectId | `org.bson.BsonObjectId` |
| String | `org.bson.BsonString` |

> **Dato importante — validación de esquema**: aunque MongoDB es *schemaless* por diseño, desde la versión 3.6 permite definir una **validación de esquema** opcional con `$jsonSchema`, para que una colección rechace documentos que no cumplan ciertas reglas (tipos de campo, campos obligatorios...) sin perder la flexibilidad de añadir campos nuevos libremente:
> ```javascript
> db.createCollection("alumno", {
>    validator: {
>       $jsonSchema: {
>          bsonType: "object",
>          required: ["dni", "nombre"],
>          properties: {
>             dni: { bsonType: "string" },
>             nombre: { bsonType: "string" },
>             famNum: { bsonType: "bool" }
>          }
>       }
>    }
> });
> ```

### 8.8 Insertar

**Insertar 1 documento**

```java
// Array con el material
ArrayList<String> material = new ArrayList<>();
material.add("Ordenador");
material.add("Pizarra Digital");
material.add("Cañón");

// Objeto revisión (fecha, incidencias)
Document rev = new Document()
        .append("fecha", new Date(new Date().getTime() + (int)(Math.random()*1000)))
        .append("observacion", "superada");

// Insertar 1 documento
InsertOneResult result = collection.insertOne(
        new Document().append("_id", new ObjectId())       // Tipo ObjectId
                .append("codigo", "INF1")                    // String
                .append("planta", "Baja")
                .append("wifi", true)                         // Boolean
                .append("metros2", Math.random()*100)         // Float
                .append("material", material)                 // Array
                .append("fechaCreacion", new Date())          // Date
                .append("extintor", null)
                .append("revisiones", rev));                  // Object

System.out.println("Id documento generado: " + result.getInsertedId());
```

**Insertar múltiples documentos**

```java
ArrayList<Document> docs = new ArrayList<>();
for (int i = 2; i < 13; i++) {
    Document d = new Document()
            .append("_id", new ObjectId())
            .append("codigo", "INF" + i)
            .append("planta", (i % 2 == 0 ? "Alta" : "Baja"))
            .append("wifi", (i % 2 == 0 ? true : false))
            .append("metros2", Math.random()*100)
            .append("material", material)
            .append("fechaCreacion", new Date())
            .append("extintor", (i % 2 == 0 ? null : true))
            .append("revisiones", rev);
    docs.add(d);
}
InsertManyResult r = collection.insertMany(docs);
System.out.println("Inserted document ids: " + r.getInsertedIds());

// Recorrer ids generados
for (Map.Entry<Integer, BsonValue> dato : r.getInsertedIds().entrySet()) {
    System.out.println("Pos: " + dato.getKey() + " Value: " + dato.getValue());
}
```

### 8.9 Nº de documentos de una colección

```java
System.out.println("Nº de docs: " + collection.countDocuments());
```

### 8.10 Filtros en consultas

- Operadores de consulta: https://www.mongodb.com/docs/manual/reference/operator/query/
- Documentos de filtro en el driver Java: https://www.mongodb.com/docs/drivers/java/sync/current/fundamentals/crud/query-document/

Permiten especificar los criterios que deben cumplir los documentos en las operaciones CRUD.

| Comparación | Lógicos | Array | Elementos |
|---|---|---|---|
| `eq` `gt` `gte` `lt` `lte` `ne` | `and` `not` `nor` `or` | `all` `elemMatch` `size` `in` `nin` | `exists` `type` |

```java
// Modificar un campo
Bson condicion = Filters.eq("dni", "1A");
Bson modif = Updates.set("nombre", "Mónica");
UpdateResult r = col.updateOne(condicion, modif);
if (r.getModifiedCount() == 1) {
    System.out.println("ok");
}

// Modificar más de un campo
modif = Updates.combine(
        Updates.set("nombre", "Elsa"),
        Updates.inc("numModif", 1));
r = col.updateOne(condicion, modif);

// Añadir elemento a un array (sin duplicados)
modif = Updates.addToSet("curso", "1ºSMR");
r = col.updateOne(condicion, modif);

// Eliminar elemento de un array
modif = Updates.pull("curso", "1ºSMR");
r = col.updateOne(condicion, modif);

// Añadir campo de tipo documento
modif = Updates.set("aula", new Document().append("nombre", "B1").append("planta", "baja"));
r = col.updateOne(condicion, modif);

// Modificar campo de un documento embebido
modif = Updates.set("aula.nombre", "B5");
r = col.updateOne(condicion, modif);
```

```java
// Varias condiciones combinadas con AND, y varias modificaciones combinadas
Bson condiciones = Filters.and(Filters.eq("planta", "Alta"), Filters.gt("metros2", 50), Filters.eq("ex", null));

Bson camposModif = Updates.combine(
        Updates.set("ex", true),                           // modificar un campo
        Updates.addToSet("material", "Extintor"),            // añadir elemento a un array
        Updates.set("revisiones.fecha", new Date()));

UpdateResult result = collection.updateMany(condiciones, camposModif);
System.out.println("Nº de documentos modificados: " + result.getModifiedCount());
```

### 8.11 Modificar/Reemplazar documentos

La diferencia entre modificar y reemplazar es que el reemplazo borra todos los campos del documento, excepto el `_id`, y deja solamente los campos del documento de reemplazo.

```java
// Modificar
Bson condicion = Filters.eq("codigo", "INF1");
Document reemplazar = new Document()
        .append("wifi", false)
        .append("codigo", "INF1");

UpdateResult result = collection.replaceOne(condicion, reemplazar);
System.out.println("Nº de documentos modificados: " + result.getModifiedCount());
```

### 8.12 Borrar documentos

```java
// Borrar un documento
Bson condicion = Filters.gt("metros2", 50);
try {
    DeleteResult result = collection.deleteOne(condicion);
    System.out.println("Deleted document count: " + result.getDeletedCount());
} catch (MongoException me) {
    System.err.println("Unable to delete due to an error: " + me);
}
```

```java
// Borrar múltiples documentos
Bson condicion = Filters.gt("metros2", 50);
try {
    DeleteResult result = collection.deleteMany(condicion);
    System.out.println("Deleted document count: " + result.getDeletedCount());
} catch (MongoException me) {
    System.err.println("Unable to delete due to an error: " + me);
}
```

### 8.13 Recuperación

**Recuperar todos los documentos de una colección**

```java
// Recuperar todos los documentos
MongoCursor<Document> r = col.find().iterator();
while (r.hasNext()) {
    Document d = r.next();

    // Mostrar en formato JSON
    System.out.println(d.toJson());

    // Transformar cada documento en objeto Java
    Alumno a = new Alumno();
    a.setDni(d.getString("dni"));
    a.setNombre(d.getString("nombre"));
    // En este ejemplo, la fecha se trata como un texto
    a.setFechaN(new SimpleDateFormat("yyyy-MM-dd").parse(d.getString("fechaN")));
    a.setFamNum(d.getBoolean("famNum"));
    // Obtener un Array de String
    a.setCurso((ArrayList<String>) d.get("curso"));
    // Obtener un Array de documentos embebidos
    ArrayList<Document> notas = (ArrayList<Document>) d.get("notas");
    for (Document de : notas) {
        Nota n = new Nota(de.getString("asignatura"), de.getInteger("nota"));
        a.getNotas().add(n);
    }
    System.out.println(a);
}
```

**Recuperar documentos con filtro, orden y proyección**

```java
// Filtro
Bson filtro = Filters.eq("famNum", true);

// Proyección: campos a recuperar
// Recuperar solamente el dni y las notas (sin ids de documentos)
Bson campos = Projections.fields(Projections.include("dni", "notas"), Projections.excludeId());

// Orden: descendente por fechaN y ascendente por dni y nombre
Bson orden = Sorts.orderBy(Sorts.descending("fechaN"), Sorts.ascending("dni", "nombre"));

// Recuperar todos los documentos
MongoCursor<Document> r = col.find().projection(campos).sort(orden).iterator();
while (r.hasNext()) {
    Document d = r.next();
    System.out.println(d.toJson());
}
```

> **Enunciados de práctica (del documento original)**:
> - Obtener el dni y la nota de todos los alumnos que sean familia numerosa, ordenados por fecha de nacimiento de forma descendente, y por dni y nombre de forma ascendente.
> - Obtener el código, los metros cuadrados y si hay extintor en las aulas de la planta alta de más de 50 metros cuadrados que no tengan extintor, ordenadas por tamaño de aula de forma descendente.

```java
// Obtener aulas de la planta alta con más de 50m2 y con extintor, ordenadas por tamaño
Bson campos = Projections.fields(
        Projections.include("codigo", "metros2", "extintor"),
        Projections.excludeId());

Bson filtro = Filters.and(Filters.eq("planta", "Alta"), Filters.gt("metros2", 50), Filters.eq("ex", null));
MongoCursor<Document> cursor = collection.find(filtro).projection(campos)
        .sort(Sorts.descending("metros2"))  // import com.mongodb.client.model.Sorts;
        .iterator();
while (cursor.hasNext()) {
    System.out.println(cursor.next().toJson());
}
cursor.close();
```

**Recuperar un solo documento**

```java
// Obtener un documento
// Obtener el aula de la planta alta ordenada por tamaño (la primera)
Bson campos = Projections.fields(
        Projections.include("codigo", "metros2", "material", "extintor", "fechaCreacion", "revisiones"),
        Projections.excludeId());

Document doc = collection.find(Filters.eq("planta", "Alta"))
        .projection(campos)
        .sort(Sorts.descending("metros2"))  // import com.mongodb.client.model.Sorts;
        .first();

if (doc == null) {
    System.out.println("No encontrado");
} else {
    // Mostrar con formato JSON
    System.out.println(doc.toJson());

    // Acceder a cada elemento
    String codigo = doc.getString("codigo");
    System.out.println("Código:" + codigo);

    double m = doc.getDouble("metros2");
    System.out.println("M2:" + m);

    if (doc.getBoolean("extintor") != null) {
        boolean e = doc.getBoolean("extintor");
        System.out.println("Extintor:" + e);
    } else {
        System.out.println("Extintor:null");
    }

    Date fechaC = doc.getDate("fechaCreacion");
    System.out.println("Fecha Creación:" + new SimpleDateFormat("dd/MM/yyyy").format(fechaC));

    ArrayList<String> material = (ArrayList<String>) doc.get("material");
    System.out.print("Materiales:");
    for (String mat : material) {
        System.out.print(mat + ",");
    }
    System.out.println();

    Document d = (Document) doc.get("revisiones");
    System.out.println("Revisión:" + new SimpleDateFormat("dd/MM/yyyy").format(d.getDate("fecha")) +
            "\tObservación:" + d.getString("observacion"));
}
```

### 8.14 CRUD en arrays de documentos embebidos

#### Recuperar documentos embebidos de un array que cumplan una condición (`$elemMatch`)

Las condiciones de un filtro afectan a todo el documento, aunque la condición se evalúe sobre un campo de un documento embebido en un array. Así, el siguiente ejemplo recupera todos los alumnos que tengan al menos una nota > 7.5, y muestra **todas** sus notas:

```java
// FILTRO: recupera documentos (alumnos) en los que al menos haya una nota > 7.5
// pero recupera todo el array notas
Bson filtro = Filters.gt("notas.nota", 7.5);

// PROYECCIÓN: campos a recuperar
// Recuperar solamente el dni y las notas (sin ids de documentos)
Bson campos = Projections.fields(Projections.include("dni", "notas"), Projections.excludeId());

MongoCursor<Document> r = col.find().projection(campos).iterator();
while (r.hasNext()) {
    Document d = r.next();
    System.out.println(d.toJson());
}
```

Si queremos mostrar solamente las notas que cumplan la condición > 7.5, hay que hacer el filtro en la proyección con `$elemMatch`:

```java
// FILTRO: recupera documentos (alumnos) en los que al menos haya una nota > 7.5
// pero recupera solamente las notas que sean > 7.5
Bson campos = Projections.fields(
        Projections.include("dni", "notas"),
        Projections.excludeId(),
        Projections.elemMatch("notas", Filters.gt("nota", 7.5)));

MongoCursor<Document> r = col.find().projection(campos).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}
```

#### Insertar documento embebido en array (`$addToSet`)

```java
// FILTRO
Bson filtro = Filters.eq("dni", "1A");
Bson modif = Updates.addToSet("notas",
        new Document().append("asignatura", "BD")
                .append("nota", 6).append("fecha", new Date()));
UpdateResult r = collection.updateOne(filtro, modif);
if (r.getModifiedCount() == 1) {
    System.out.println("Correcto");
}
```

#### Modificar documento embebido de un array

```java
// Subir 1 a las notas de BD entre 9 y 10
Bson filtro = Filters.and(Filters.eq("notas.asignatura", "BD"), Filters.gte("notas.nota", 9), Filters.lt("notas.nota", 10));
// Filtro de los documentos embebidos a modificar
// (docE es el nombre con el que se va a referenciar el documento embebido)
Bson filtroDocEmbebidos = Filters.and(Filters.eq("docE.asignatura", "BD"), Filters.gte("docE.nota", 9), Filters.lt("docE.nota", 10));
// Modificaciones
Bson modif = Updates.inc("$[docE].nota", 1);
UpdateOptions filtroModif = new UpdateOptions().arrayFilters(Arrays.asList(filtroDocEmbebidos));
UpdateResult r = collection.updateMany(filtro, modif, filtroModif);
if (r.getModifiedCount() >= 1) {
    System.out.println("Correcto: " + r.getModifiedCount());
}
```

#### Borrar documento embebido de un array

```java
// Borrar las notas de BD entre 9 y 10
Bson filtro = Filters.and(Filters.eq("notas.asignatura", "BD"),
        Filters.gte("notas.nota", 9), Filters.lt("notas.nota", 10));

// Definir condiciones de documentos a borrar
Document patronDoc = new Document().append("asignatura", "BD")
        .append("nota", new Document().append("$gte", 9)
                .append("$lte", 10));

Bson modif = Updates.pull("notas", patronDoc);
UpdateResult r = collection.updateMany(filtro, modif);
if (r.getModifiedCount() >= 1) {
    System.out.println("Correcto: " + r.getModifiedCount());
}
```

### 8.15 Operaciones de agregación

Las operaciones de agregación procesan grupos de documentos y generan documentos resumen para cada grupo.

Se realizan en una serie de etapas, a través de lo que se conoce como un **pipeline** (tubería), donde cada etapa trabaja sobre unos documentos de entrada y genera documentos de salida que sirven de entrada a la siguiente etapa.

El método encargado de ejecutarlo es `.aggregate()`. Documentación de los tipos de etapas: https://www.mongodb.com/docs/manual/reference/operator/aggregation/

Las etapas más usadas son:

- **`match`**: aplica un filtro. Equivale al `WHERE`/`HAVING` de SQL, según el orden en que se aplique en el pipeline.
- **`group`**: agrupa los documentos de entrada por el criterio indicado, aplicando una o varias funciones acumuladoras sobre cada grupo. Equivale al `GROUP BY` + funciones resumen del `SELECT` en SQL. Hay que importar `com.mongodb.client.model.Accumulators`. Las funciones más frecuentes son: `count`, `max`, `maxN`, `min`, `minN`, `sum`, `avg`.
- **`sort`**: reordena los documentos por el criterio indicado.
- **`count`**: equivale a `COUNT` de SQL.
- **`lookup`**: enlaza con documentos de otra colección. Equivale al `JOIN` de SQL (apartado 8.16).
- **`addFields`**: añade nuevos campos al documento de salida (todos los de entrada más los nuevos). Equivale a los campos calculados de SQL.
- **`project`**: define qué campos de los documentos de entrada van a estar en el documento de salida.
- **`limit`**: se queda únicamente con un número de documentos. Equivale al `LIMIT` de SQL.
- **`unwind`**: deconstruye un array, generando tantos documentos como elementos tenga. Los campos de los nuevos documentos son los mismos, pero el array se transforma en cada uno de los objetos deconstruidos.

```java
// Contar el nº de alumnos
MongoCursor<Document> r = col.aggregate(Arrays.asList(
        Aggregates.count("dni"))).iterator();

// Mostrar por separado las notas de cada alumno
r = col.aggregate(Arrays.asList(
        Aggregates.unwind("$notas")
)).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}
```

```java
// Igual que lo anterior, pero mostrando solamente el dni y el documento nota
r = col.aggregate(Arrays.asList(
        Aggregates.unwind("$notas"),
        Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("dni", "notas")))
)).iterator();
```

```java
// Mostrar las notas de un alumno y el nombre de la asignatura
MongoCursor<Document> r = col.aggregate(Arrays.asList(
        Aggregates.match(Filters.eq("dni", "1A")),
        Aggregates.unwind("$notas"),
        Aggregates.sort(Sorts.orderBy(Sorts.ascending("notas.asignatura"))),
        Aggregates.lookup("asignatura", "notas.asignatura", "codigo", "nombreAsig"),
        Aggregates.project(Projections.fields(Projections.excludeId(), Projections.include("dni", "nombreAsig.nombre", "notas.nota")))
)).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}

// Mostrar, para cada asignatura, el nombre, el nº de notas, la nota más alta, la más baja y la media
r = col.aggregate(Arrays.asList(
        Aggregates.unwind("$notas"),
        Aggregates.lookup("asignatura", "notas.asignatura", "codigo", "nombreAsig"),
        Aggregates.group("$nombreAsig.nombre", Accumulators.sum("numNotas", 1),
                Accumulators.min("NotaMin", "$notas.nota"),
                Accumulators.max("NotaMax", "$notas.nota"),
                Accumulators.avg("NotaMedia", "$notas.nota")),
        Aggregates.addFields(new Field("nombreAsig", new Field<>("$_id", "$_id"))), // campo nuevo
        Aggregates.project(Projections.fields(Projections.excludeId()))
)).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}
```

```java
// Mostrar el nº de alumnos, el nº de notas, la fecha de nacimiento del alumno más
// mayor y el del más joven, y la nota media, según si los alumnos son familia
// numerosa o no.
MongoCursor<Document> r = col.aggregate(Arrays.asList(
        Aggregates.group("$famNum",
                Accumulators.sum("numAlumnos", 1),
                Accumulators.sum("numNotas", new Document("$size", "$notas")),
                Accumulators.min("FechaNacMin", "$fechaN"),
                Accumulators.max("FechaNacMax", "$fechaN"),
                Accumulators.avg("NotaMedia", new Document("$avg", "$notas.nota")))
)).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}

// Ídem a la anterior, pero filtrando por notaMedia mayor que 7
r = col.aggregate(Arrays.asList(
        Aggregates.group("$famNum",
                Accumulators.sum("numAlumnos", 1),
                Accumulators.sum("numNotas", new Document("$size", "$notas")),
                Accumulators.min("FechaNacMin", "$fechaN"),
                Accumulators.max("FechaNacMax", "$fechaN"),
                Accumulators.avg("NotaMedia", new Document("$avg", "$notas.nota"))),
        Aggregates.sort(Sorts.ascending("NotaMedia")),
        Aggregates.match(Filters.gte("NotaMedia", 7))
)).iterator();
while (r.hasNext()) {
    System.out.println(r.next().toJson());
}
```

### 8.16 Operaciones de JOIN (`$lookup`)

Oficialmente no hay *joins* en MongoDB, aunque sí hay métodos para implementarlos.

El operador **`$lookup`** en las operaciones de agregación es el elemento con el que se hace. Tiene 4 campos principales:

- **`from`**: nombre de la colección con la que queremos hacer el *join*.
- **`localField`**: campo de la colección que invoca el `$lookup`, usado para el *join*.
- **`foreignField`**: campo de la colección indicada en `from`, usado para el *join*.
- **`as`**: nombre del campo que se añade a la colección de entrada, donde se almacenará el resultado de los documentos enlazados de la otra colección.

En realidad, `$lookup` hace un **LEFT OUTER JOIN**: los documentos de la primera colección que no se vinculan con ningún documento de la derecha también aparecen en el resultado, pero con el campo indicado en `as` vacío.

### 8.17 Trabajar con POJOs

Mongo permite almacenar y recuperar datos usando **POJOs** (*Plain Old Java Objects*): objetos Java con todos los atributos privados, que garantizan el principio de encapsulación y separan la lógica de la aplicación de la representación de los datos.

**Configuración de la conexión**

```java
import static com.mongodb.MongoClientSettings.getDefaultCodecRegistry;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
```

```java
// String de conexión
ConnectionString connectionString = new ConnectionString(
        "mongodb+srv://rosa:eEWEFKwNveeKF560@accesodatos.c38r0x1.mongodb.net/?retryWrites=true&w=majority");

// Conectar con la bd
MongoClient mongoClient = MongoClients.create(connectionString);

// Configuración para trabajar con POJOs
CodecProvider pojoCodecProvider = PojoCodecProvider.builder().automatic(true).build();

// Registrar pojoCodecProvider
CodecRegistry pojoCodecRegistry = fromRegistries(getDefaultCodecRegistry(), fromProviders(pojoCodecProvider));

// Acceder a la bd con el pojoCodecProvider registrado
MongoDatabase database = mongoClient.getDatabase("IES").withCodecRegistry(pojoCodecRegistry);

// Seleccionar/crear colección indicando la clase POJO
MongoCollection<Aula> collection = database.getCollection("aulas", Aula.class);
```

**Crear documentos**

```java
// Insertar varios
ArrayList<Aula> docs = new ArrayList<>();
for (int i = 1; i < 13; i++) {
    ArrayList<String> material = new ArrayList<>();
    material.add("Ordenador");
    material.add("Pizarra Digital");
    material.add("Cañón");

    Aula a = new Aula();
    a.setCodigo("INF" + i);
    a.setPlanta(i % 2 == 0 ? "Alta" : "Baja");
    a.setExtintor(i % 2 == 0 ? false : true);
    a.setMetros2((float) Math.random()*100);
    a.setMaterial(material);
    a.setFechaCreacion(new Date());
    a.setWifi(i % 2 == 0 ? false : true);
    a.setRevisiones(new Revision(new Date(new Date().getTime() + (int)(Math.random()*1000)), "superada"));
    docs.add(a);
}
collection.insertMany(docs);
```

**Recuperar documentos**

```java
ArrayList<Aula> aulas = new ArrayList<>();
collection.find().into(aulas);
for (Aula a : aulas) {
    System.out.println(a);
}
```

> **Ejercicio 14 (del documento original)**: realiza una aplicación completa de gestión de un centro educativo (aulas, alumnos, asignaturas) sobre MongoDB, combinando los apartados anteriores: CRUD, agregaciones, *joins* con `$lookup` y trabajo con POJOs.

## 9 Modelado de datos: embeber vs. referenciar

A diferencia del modelo relacional (Temas 1-3), donde la normalización es la norma y las relaciones casi siempre se resuelven con claves foráneas entre tablas, en MongoDB la decisión de diseño más importante es elegir, para cada relación entre dos tipos de datos, si se **embebe** (se guarda el documento relacionado dentro del documento principal, como se ha hecho con `notas` dentro de `alumno`) o si se **referencia** (se guarda solo el identificador del documento relacionado en otra colección, y se recupera con `$lookup` cuando hace falta, como con `asignatura`/`notas.asignatura`).

| | Embeber | Referenciar |
|---|---|---|
| **Cuándo usarlo** | Relación 1 a pocos, los datos se leen casi siempre juntos, el subdocumento no crece sin límite | Relación 1 a muchos/muchos a muchos con colecciones grandes, el documento relacionado se consulta también de forma independiente, o el array podría superar el límite de tamaño |
| **Ventaja principal** | Una sola lectura trae todos los datos relacionados; es atómico por diseño sin necesidad de transacción | Evita duplicar datos; los documentos relacionados pueden crecer sin límite y actualizarse de forma independiente |
| **Inconveniente principal** | Si el array crece sin control, el documento puede acercarse al límite de **16 MB** por documento; duplica datos si el mismo subdocumento se usa en varios sitios | Necesita un `$lookup` (más coste) o varias consultas; ya no es atómico por sí solo (puede necesitar una transacción, apartado 7) |

> **Dato importante**: esta decisión no es una regla fija, sino un compromiso según el patrón de acceso de la aplicación: la misma relación "alumno-notas" podría embeberse (como en este tema, porque las notas casi siempre se consultan junto al alumno y no crecen indefinidamente) o referenciarse (si hubiera que consultar las notas de forma masiva e independiente del alumno, por ejemplo para estadísticas globales del centro).

## 10 Índices

Igual que en una base de datos relacional, los índices son estructuras adicionales que aceleran las búsquedas a costa de espacio en disco y de un pequeño coste extra en cada escritura.

```java
// Índice simple ascendente sobre el campo dni
collection.createIndex(Indexes.ascending("dni"));

// Índice único (no permite dos documentos con el mismo dni)
collection.createIndex(Indexes.ascending("dni"), new IndexOptions().unique(true));

// Índice compuesto: por planta y, dentro de cada planta, por metros2 descendente
collection.createIndex(Indexes.compoundIndex(Indexes.ascending("planta"), Indexes.descending("metros2")));

// Ver los índices de una colección
for (Document idx : collection.listIndexes()) {
    System.out.println(idx.toJson());
}
```

> **Dato importante**: toda colección tiene, por defecto, un índice único sobre `_id`. Conviene crear un índice sobre cualquier campo que se use habitualmente en un filtro (`Filters.eq`, `Filters.gt`...) o en un orden (`Sorts`), especialmente en colecciones grandes — igual que en SQL, una consulta sin índice adecuado obliga a MongoDB a recorrer la colección entera (*collection scan*).

## 11 Seguridad y autenticación

Por defecto, una instalación local de MongoDB no exige usuario ni contraseña y solo acepta conexiones desde `localhost`. Antes de exponer un servidor MongoDB fuera de un entorno de pruebas aislado, hay que **activar la autenticación**:

1. Crear un usuario administrador desde `mongosh`:
   ```javascript
   use admin
   db.createUser({
       user: "admin",
       pwd: "claveSegura",
       roles: [ { role: "userAdminAnyDatabase", db: "admin" } ]
   })
   ```
2. Arrancar el servidor con autenticación activada (`mongod --auth`, o la opción equivalente en el fichero de configuración `mongod.conf`: `security.authorization: enabled`).
3. Conectar indicando usuario y contraseña en la cadena de conexión:
   ```
   mongodb://admin:claveSegura@localhost:27017/?authSource=admin
   ```

En **MongoDB Atlas** (apartado 8.1) la autenticación está activada por defecto: se crea el usuario de base de datos desde el panel de Atlas, y la propia cadena de conexión que proporciona Atlas ya incluye usuario y contraseña.

## 12 Ejercicios

**Ejercicio 1**: Instala MongoDB en local (o crea un clúster gratuito en Atlas) y, usando `mongosh`, crea una colección `alumno` con al menos 5 documentos siguiendo la estructura usada en este tema (dni, nombre, fechaN, famNum, curso, notas).

**Ejercicio 2**: Desde Java, conéctate a la base de datos usando `try-with-resources` y muestra por consola todos los documentos de la colección `alumno` en formato JSON.

**Ejercicio 3**: Inserta una nueva aula con los campos usados en el tema (código, planta, wifi, metros2, material, fechaCreacion, extintor, revisiones), comprobando antes que no existe otra aula con el mismo código (usa un índice único, apartado 10).

**Ejercicio 4**: Realiza un programa que, dado un dni por parámetro, muestre el nombre del alumno y la nota media de todas sus asignaturas, usando el pipeline de agregación.

**Ejercicio 5**: Implementa, con una transacción (apartado 7), una operación que traslade a un alumno de un curso a otro y registre el cambio en una colección `auditoria`, deshaciendo ambas operaciones si alguna falla.

**Ejercicio 6**: Añade validación de esquema (`$jsonSchema`, apartado 8.7) a la colección `alumno`, exigiendo que `dni` y `nombre` sean obligatorios y de tipo texto. Comprueba que un `insertOne` que no cumpla el esquema es rechazado.

**Ejercicio 7**: Reflexiona (y documenta en un párrafo) si la relación entre `aula` y sus `revisiones` debería embeberse o referenciarse (apartado 9) si cada aula pudiera llegar a tener miles de revisiones a lo largo de los años.

## 13 Glosario

- **Atlas**: servicio de base de datos MongoDB gestionado en la nube, alternativa habitual a instalar un servidor local.
- **BSON**: *Binary JSON*, formato de serialización binaria usado por MongoDB para almacenar documentos; añade tipos que JSON no tiene de forma nativa (fechas, `ObjectId`, enteros de distinta precisión...).
- **Colección**: conjunto de documentos en MongoDB, equivalente conceptual a una tabla en el modelo relacional.
- **Documento**: unidad de almacenamiento en una base de datos documental, equivalente conceptual a un registro/fila.
- **Embeber**: estrategia de modelado que guarda un documento relacionado dentro de otro, en vez de en una colección aparte.
- **mongosh**: shell interactivo de línea de comandos para MongoDB.
- **Pipeline de agregación**: secuencia de etapas (`match`, `group`, `sort`...) que procesan documentos de entrada y generan documentos de salida, usada para consultas complejas y resúmenes.
- **POJO**: *Plain Old Java Object*, objeto Java sencillo con atributos privados y *getters*/*setters*, usado por el driver de MongoDB para mapear documentos automáticamente.
- **Referenciar**: estrategia de modelado que guarda solo el identificador de un documento relacionado, recuperándolo por separado (normalmente con `$lookup`) cuando hace falta.
- **Teorema de CAP**: principio que establece que un sistema distribuido solo puede garantizar completamente dos de estas tres propiedades: consistencia, disponibilidad y tolerancia a particiones.
- **Transacción multi-documento**: conjunto de operaciones sobre varios documentos (incluso de colecciones distintas) que se ejecutan como una unidad atómica, disponible en MongoDB desde la versión 4.0.
