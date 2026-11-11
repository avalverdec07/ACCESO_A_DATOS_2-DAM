# Tema 2: Manejo de Conectores
## Acceso a Datos — IES Augustóbriga

## Índice

1. El desfase objeto-relacional
2. Protocolos de acceso a BD SQL
3. Conector JDBC
   - 3.1 Clases
   - 3.2 Pasos para acceder a una BD
   - 3.3 Acceso a BD MySQL
   - 3.4 Metadatos
   - 3.5 Ejecución de sentencias DML/DDL
   - 3.6 Ejecución de scripts
   - 3.7 Ejecución de rutinas almacenadas en el servidor
   - 3.8 SQLite
   - 3.9 Acceso a BD Access
4. Ejercicio 14

---

## 1 El desfase objeto-relacional

El término **desfase o impedancia** objeto-relacional se refiere a las dificultades técnicas que surgen cuando una base de datos relacional se usa en conjunto con un programa escrito en POO. Estos aspectos se pueden presentar en cuestiones como:

- **Lenguaje de programación**: el programador debe conocer el lenguaje de programación orientado a objetos (POO) y el lenguaje de acceso a datos.
- **Tipos de datos**: en las bases de datos relacionales siempre hay restricciones en el uso de tipos, mientras que la programación orientada a objetos utiliza tipos de datos más complejos.
- **Paradigma de programación**: en el proceso de diseño y construcción del software se tiene que hacer una traducción del modelo orientado a objetos de clases al modelo Entidad-Relación (E/R), puesto que el primero maneja objetos y el segundo maneja tablas y tuplas (o filas), lo que implica que se tengan que diseñar dos diagramas diferentes para el diseño de la aplicación.

## 2 Protocolos de acceso a BD SQL

Las aplicaciones usan APIs para abrir una conexión a una BD, enviar consultas, actualizaciones y resultados. Las más usadas son:

- **ODBC** (Open Database Connectivity): Es el conector de Microsoft, estándar para acceder a cualquier BD desde aplicaciones Windows. Los SGBD compatibles con ODBC proporcionan una biblioteca que se debe enlazar con el programa cliente. Cuando el programa cliente realiza una llamada a la API ODBC la biblioteca se comunica con la BD para realizar la acción solicitada y obtener los resultados. Es complejo de usar desde Java, presenta problemas de seguridad, robustez y portabilidad debido a que la API está escrita en C.
- **JDBC** (Java Database Connectivity): Librería estándar para BD relacionales. Dispone de conectores (drivers) para cada tipo de BD.

![Esquema JDBC: un programa Java accede mediante JDBC a Oracle, SQL Server, Sybase y MySQL](Tema_2_imagenes/img-000.png)

## 3 Conector JDBC

Para poder conectarse a una base de datos y lanzar consultas, una aplicación necesita tener un driver adecuado. Un conector o driver es un conjunto de clases encargadas de implementar los interfaces de la API JDBC y acceder a la base de datos.

El conector lo proporciona el fabricante de la base de datos o bien un tercero.

### 3.1 Clases

Las clases para operar con BD a través de JDBC se encuentran en el paquete **java.sql**.

A continuación, se detallan las principales clases con las que vamos a trabajar:

- **DriverManager**: Para gestionar todos los drivers instalados.
- **Connection**: Representa una conexión a una BD.
- **DatabaseMetaData**: Proporciona información (metadatos) acerca de una BD.
- **Statement**: Permite ejecutar sentencias SQL sin parámetros.
- **PreparedStatement**: Permite ejecutar sentencias SQL con parámetros.
- **CallableStatement**: Permite ejecutar sentencias SQL con parámetros de e/s como procedimientos almacenados.
- **ResultSet**: Contiene las filas resultantes de ejecutar una consulta que devuelve un conjunto de registros.
- **ResultSetMetaData**: Obtiene información (metadatos) sobre un ResultSet como el número de columnas, sus nombres, etc.

![Diagrama de clases JDBC: DriverManager devuelve Connection, que devuelve PreparedStatement, que devuelve ResultSet; aparece también SQLException](Tema_2_imagenes/img-001.png)

### 3.2 Pasos para acceder a una BD

![Pasos: Cargar el driver JDBC del motor de la BD → Crear Conexión (Connection) → Crear Sentencia (Statement) → Ejecutar Sentencia → Recuperar el resultado (ResultSet) → Liberar Objetos](Tema_2_imagenes/img-007.png)

**1. Cargar el driver JDBC del motor de la BD**

```java
//Cargamos el driver MySQL
Class.forName("com.mysql.cj.jdbc.Driver");
```

**2. Crear Conexión (Connection)**

```java
//Parámetros de acceso a la BD
String url = "jdbc:mysql://localhost:3306/ies?serverTimezone=Europe/Madrid",
       usuario ="root",
       clave="root";
//Abrir conexión con la BD
Connection conexion = DriverManager.getConnection(url,usuario,clave);
```

**3. Crear Sentencia (Statement)**

```java
//Preparar la consulta
String query = "select * from alumnos";
Statement sentencia = conexion.createStatement();
```

**4. Ejecutar Sentencia**

```java
//Ejecutar la consulta
ResultSet resultado = sentencia.executeQuery(query);
```

**5. Recuperar el resultado (ResultSet)**

```java
//Procesar el resultado
//Se realiza un bucle mientras se lean registros devueltos
while(resultado.next()){
    //Recuperamos los campos de las tablas de dos formas
    //resultado.getTipo(num_columna) o
    //  resultado.getTipo(nombre_columna)
    System.out.println("ID:"+resultado.getInt(1)+"\tNOMBRE:"+resultado.getString("nombre")+"\tCURSO:"+resultado.getString(3));
}
```

**6. Liberar objetos**

```java
//Liberamos recursos
resultado.close(); sentencia.close(); conexion.close();
```

### 3.3 Acceso a BD MySQL

Vamos a preparar nuestro entorno para poder acceder desde los programas Java a BD MySQL con JDBC. Para ello debemos:

- Instalar **Servidor MySQL** (XAMPP o directamente el servidor).
- Instalar **MySQL Workbench**.
- Obtener el driver del conector JDBC:
  - Descargar el driver JDBC: debemos incluir la ruta del archivo **mysql-connector-java-XXX.jar** en el BuildPath del proyecto. El conector queda descargado en la carpeta `C:\Program Files (x86)\MySQL\Connector J 8.0\mysql-connector-java-8.0.22.jar`.
    - Añadir el driver al proyecto. Selecciona el proyecto y accede a:
      1. Project/Properties/
      2. Java BuildPath
      3. Libraries
      4. Classpath
      5. Add External Jar

![Ventana Properties del proyecto en Eclipse: Java Build Path → Libraries → Classpath → Add External JARs, con mysql-connector-java-8.0.22.jar añadido; se pulsa Apply and Close](Tema_2_imagenes/img-009.png)

  - Utilizar Maven: configuramos el archivo `pom.xml` de la siguiente forma:

```xml
<!-- https://mvnrepository.com/artifact/mysql/mysql-connector-java -->
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

> **Ejercicio 1**: Crea una clase Java que gestione el acceso a los datos de la BD alumnos. Realiza un programa en Java que muestre los datos de la tabla alumnos.

### 3.4 Metadatos

#### A nivel BD, tabla, campo

Para obtener los metadatos de la BD utilizamos la interfaz **DatabaseMetaData**.

```java
DatabaseMetaData datos = conexion.getMetaData();

System.out.println(""+datos.getDatabaseProductName());
ResultSet tablas = datos.getTables(null, null, null, null);
while(tablas.next()) {
    System.out.println(tablas.getString(3));
}
ResultSet campos = datos.getColumns(null, null, "libros", null);
while(campos.next()) {
    System.out.println("---"+campos.getString(4));
}
```

Los métodos más usados son:

- `getDatabaseProductName()`
- `getDriverName()`
- `getUrl()`
- `getUserName()`
- `getTables()`
- `getColumns()`
- `getPrimaryKeys()`
- `getImportedKeys()`
- `getProcedures()`

#### A nivel de resultado de consulta

Para obtener los metadatos sobre los datos devueltos en una consulta utilizamos la interfaz **ResultSetMetaData**.

```java
Statement consulta = conexion.createStatement();
ResultSet resultado = consulta.executeQuery("select * from libros");
ResultSetMetaData datosConsulta = resultado.getMetaData();
for(int i=1; i<=datosConsulta.getColumnCount(); i++) {
    System.out.println("Campo:"+datosConsulta.getColumnName(i));
    System.out.println("Tipo:"+datosConsulta.getColumnTypeName(i));
}
```

Los métodos más usados son:

- `getColumnCount()`
- `getColumnName()`
- `getColumnTypeName()`
- `isNullable()`

> **Ejercicio 2**: Utilizando la clase Alumno, realiza un programa que muestre los siguientes metadatos:
> - Gestor de base de datos
> - Nombre del driver
> - URL
> - Usuario
> - Tablas: para cada tabla
>   - Nombre de la tabla
>   - Clave primaria
>   - Claves externas
>   - Información del campo
>     - Nombre
>     - Tipo

> **Ejercicio 3**: Hacer un programa que muestre los datos de la tabla asignatura de la siguiente forma:

![Salida esperada del Ejercicio 3: número de columnas, columnas con tipo y si admiten nulos, datos de las asignaturas y número de registros](Tema_2_imagenes/img-010.png)

```
Número de columnas:2

Columna Tipo    Admite Nulos
nombreC VARCHAR 0
nombreL VARCHAR 1

Datos
AD Acceso a datos
BD Bases de datos
LM Lenguajes de Marcas
Número de registros:3
```

### 3.5 Ejecución de sentencias DML/DDL

Para ejecutar una sentencia DML o DDL debemos:

| Paso | Instrucción |
|---|---|
| **Crear un objeto Sentencia** | `Statement conexion.createStatement();` |
| **Ejecutar la sentencia.** La forma de ejecutar la sentencia depende de si la consulta ejecutada devuelve un conjunto de filas o no. | |
| Si la sentencia devuelve un conjunto de filas. | `ResultSet executeQuery(String)` |
| Si la sentencia no devuelve un conjunto de filas. Para sentencias DDL (create, drop, alter) devuelve 0 y para DML (insert, update, delete) devuelve el número de registros afectados. | `int executeUpdate(String)` |
| Para cualquier tipo de sentencia. Devuelve *true* si se obtiene un ResultSet y *false* en caso contrario. | `Boolean execute(String)` |
| **Recuperar el ResultSet de una sentencia.** Si la ejecución se hace con el método *execute*: recuperar el ResultSet, o el número de registros tratados si devuelve false. | `sentencia.getResultSet` / `sentencia.getUpdateCount` |
| Para acceder a los datos de un registro devuelto en un ResultSet | `while(ResultSet.next()){ ResultSet.getTipo(numColumna) ResultSet.getTipo(nombreColum) }` |
| **Sentencias con parámetros.** Interfaz **PreparedStatement** en vez de **Statement**. | `String consulta = "select * from alumnos where id = ? and asig=?";` |

`PreparedStatement` permite construir una sentencia con marcadores (representados por `?`) que serán sustituidos por datos cuando se ejecute la sentencia. Cada marcador tiene un número de orden empezando en 1.

Los marcadores sólo se pueden usar para los datos de columnas y no para nombres de tablas, o de columnas.

Para rellenar los marcadores de la sentencia se utilizan los métodos `sentencia.setTipo(nº orden parámetro, valor)` en el orden en el que se definen los marcadores.

Los métodos para ejecutar estas sentencias son los mismos que los de `Statement`, pero no hace falta pasar el String con la consulta, ya que este se fija en la declaración de la sentencia.

```java
PreparedStatement sentencia = conexion.prepareStatement(consulta);
sentencia.setInt(1, 1);
sentencia.setString(2, "AD");
//Ejecutamos la sentencia
ResultSet resultado = sentencia.executeQuery();
```

> **Ejercicio 4**: Realiza un programa que muestre un menú con las siguientes opciones:
>
> 1. **Alumnos por asignatura**: Mostrar los nombres de alumnos, de asignatura y la nota de los alumnos de una asignatura que se pide por teclado. No usar consultas con parámetros.
> 2. **Notas de alumno**: Mostrar los nombres de asignatura y la nota de un alumno cuyo id se pide por teclado. Usar consultas con parámetros.
> 3. **Alta de alumno**: Insértate como alumno de 2DAM.
> 4. **Añadir cp**: Añadir a la tabla alumno el campo cp de tipo entero.
> 5. **Rellenar cp**: Rellena el cp de los alumnos con 10300.
> 6. **Añadir fecha de nacimiento**: Añade a la tabla alumno el campo fecha de nacimiento.
> 7. **Rellenar fecha de nacimiento**: Rellena la fecha de nacimiento de un alumno cuyo id se pide por teclado con la fecha que también se pide por teclado.

### 3.6 Ejecución de scripts

Para poder ejecutar un script SQL en el servidor de BD hay que indicarlo en la cadena de conexión añadiendo el modificador `allowMultiQueries=true` de la siguiente forma:

```java
Connection conexion =
    DriverManager.getConnection("jdbc:mysql://localhost/alumnos?allowMultiQueries=true","root","rosa");
```

Para cargar el script, hay que formar un String con el contenido del fichero. La forma de ejecutar la sentencia es la misma que la de las sentencias DDL.

Debemos tener cuidado ya que hay BD que no soportan la ejecución de scripts.

> **Ejercicio 5**: Realiza un script que añada a la base de datos la tabla profesor y rellénala con algunos profesores. Además, debe añadir también la tabla imparte donde se refleje qué profesor imparte cada asignatura.

### 3.7 Ejecución de rutinas almacenadas en el servidor

JDBC permite también ejecutar rutinas almacenadas en el servidor, ya sean funciones o procedimientos. La forma de ejecutar una rutina es diferente según el tipo de rutina, ya que las funciones tienen un retorno y los procedimientos no.

Los pasos que hay que dar son:

- **Rellenar un String con el nombre de la rutina y los parámetros necesarios.**
  - Procedimientos:
    ```java
    String consulta = "{ call nombre_proc}"
    String consulta = "{ call nombre_proc(?,?,…)}"
    ```
  - Funciones:
    ```java
    String consulta = "{? = call nombre_func}"
    String consulta = "{? = call nombre_func(?,?,…)}"
    ```
- **Crear la sentencia.** La sentencia será un objeto de la clase `CallableStatement` y se crea con el método `prepareCall(String consulta)` de la conexión.
- **Rellenar los parámetros de entrada.** Se rellenan del mismo modo que los parámetros de las sentencias preparadas.
- **Registrar los parámetros de salida** para funciones y procedimientos. Los parámetros de salida deben registrarse antes de ejecutar la sentencia, indicando de qué tipo es el parámetro. Tipo es una constante definida en la clase `java.sql.Types`: `Types.INTEGER`, `Types.VARCHAR`, …
  ```java
  sentencia.registerOutParameter(int indice, int tipo);
  ```
- **Ejecutar la rutina.** La sentencia se ejecuta con el método `executeQuery`/`executeUpdate`, según lo que devuelva la rutina. Normalmente `executeQuery` en procedimientos y `executeUpdate` en funciones.
- **Recuperar el valor devuelto** en función o en procedimiento con: `sentencia.getTipo(indice);`

> **Ejercicio 6**: Realiza un programa Java que ejecute un script que cree en MySQL las siguientes rutinas almacenadas:
> - una función llamada `chequear_alumno(idAlumno)` que devuelva true si un id de alumno que se pasa por parámetro existe y false en caso contrario.
> - un procedimiento que inserte una nota a un alumno cuyo id, asignatura y nota se pasa por parámetro. La rutina debe devolver un select de las notas del alumno si se ha insertado correctamente.

> **Ejercicio 7**: Realiza un programa que pida un número de alumno por teclado y muestre si existe o no llamando a la función `chequear_alumno`.

> **Ejercicio 9**: Realiza un programa que pida un id de alumno, una asignatura y una nota e inserte el registro en la tabla notas llamando al procedimiento `notas_Asig`.

> **Ejercicio 10: Desfase Objeto-Relacional.** A partir del siguiente diagrama de clases, realiza un programa que permita:
> - Crear biblioteca
> - Mostrar libros
> - Alta libro
> - Modificar libro
> - Borrar libro

![Diagrama de clases del Ejercicio 10: Libro (id, titulo, numEjemplares) con 0..N libros por Biblioteca (mostrarLibros, altaLibro, modificarLibro, borrarLibro); BDBiblioteca (conexión, bdBiblioteca, obtenerLibros, altaLibro, modificarLibro, borrarLibro); ambas relacionadas 1 a 1 con la clase Ejercicio 10](Tema_2_imagenes/img-011.png)

### 3.8 SQLite

JDBC permite conectarnos con diferentes SGBD. La forma de trabajar con las BD es independiente del SGBD.

#### Instalación del SGBD SQLite

- Descarga de SQLite (versión 3.15.1) de https://sqlite.org/download.html: `sqlite-tools-win32-x86-3300000.zip`
- Descomprime el zip.
- Crear una BD en modo comando: ejecutar `sqlite3 ruta_BD`. Si no existe la BD, se crea.

#### Instalación driver JDBC de SQLite

- Descarga: https://bitbucket.org/xerial/sqlite-jdbc/downloads/ → `sqlite-jdbc-3.27.2.1.jar`
- Añadir la librería al proyecto.
- Cargar la clase: `Class.forName("org.sqlite.JDBC");`

#### Configuración Maven para driver JDBC SQLite

```xml
<!-- https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.28.0</version>
</dependency>
```

#### Establecer la conexión

- ```java
  Connection conexion =
      DriverManager.getConnection("jdbc:sqlite:c:\\SQLITE\\alumnos.db");
  ```
- Admite la ejecución de múltiples consultas sin indicarlo en la cadena de conexión.

Para trabajar con el servidor utilizaremos el programa `sqlite3` de la carpeta descomprimida.

Para crear una BD en modo comando debemos ejecutar `sqlite3 ruta_BD`. Si no existe la BD, se crea.

Otros comandos de `sqlite3` son:

![Tabla de comandos de sqlite3](Tema_2_imagenes/img-013.png)

| Comando | Descripción |
|---|---|
| `.show` | Muestra los valores actuales de varios parámetros |
| `.databases` | Proporciona nombres de bases de datos y archivos |
| `.quit` | Salir del programa sqlite3 |
| `.tables` | Mostrar tablas actuales |
| `.schema` | Pantalla de esquema de la tabla |
| `.header` | Mostrar u ocultar el encabezado de la tabla de salida |
| `.mode` | Selecciona el modo de la tabla de salida |
| `.dump` | Base de datos de volcado en formato de texto SQL |

> **Ejercicio 11**: Crea la BD alumnos.

> **Ejercicio 12**: Realiza un programa en Java que permita realizar las siguientes acciones:
> - Crear BD alumnos: borrar tablas y volverlas a crear
> - Ver datos
> - Introducir alumnos
> - Introducir asignaturas
> - Introducir notas

### 3.9 Acceso a BD Access

#### Instalación de drivers Access

- Descargar los drivers: https://sourceforge.net/projects/ucanaccess/files/
- Descomprimir drivers.
- Añadir librerías al proyecto:

![Librerías necesarias: commons-lang-2.6.jar, commons-logging-1.1.1.jar, hsqldb.jar, jackcess-2.1.3.jar y ucanaccess-3.0.7.jar](Tema_2_imagenes/img-015.png)
![ucanaccess-3.0.7.jar](Tema_2_imagenes/img-016.png)

- `commons-lang-2.6.jar`
- `commons-logging-1.1.1.jar`
- `hsqldb.jar`
- `jackcess-2.1.3.jar`
- `ucanaccess-3.0.7.jar`

#### Configuración Maven

```xml
<!-- https://mvnrepository.com/artifact/net.sf.ucanaccess/ucanaccess -->
<dependency>
    <groupId>net.sf.ucanaccess</groupId>
    <artifactId>ucanaccess</artifactId>
    <version>4.0.4</version>
</dependency>
```

#### Conexión

- Cargar la clase: `Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");`
- Crear la conexión: `DriverManager.getConnection("jdbc:ucanaccess://C:\\...biblioteca.mdb")`

> **Ejercicio 13**: Realiza un programa que:
> - Añada un libro a la BD `biblioteca.mdb` del tema 1 (Comedia) de la editorial 1 (Planeta). El campo autonumérico no se puede pasar. Las fechas hay que introducirlas con el formato `yyyy-mm-dd hh:mm:ss`.
> - Muestre la tabla libros de la BD `biblioteca.mdb`.

## 4 Ejercicio 14

Se proporciona el script `gimnasio.sql`.

Realiza una aplicación que permita gestionar el funcionamiento de un gimnasio. La aplicación contará con dos tipos de usuarios: clientes y administradores. Las funciones que puede hacer cada tipo de usuario son:

- **Clientes:**
  - Ver las actividades en las que está inscrito.
  - Inscribirse en una actividad.
  - Borrarse de una actividad.
  - Ver los recibos que se le han emitido, pudiendo filtrar entre recibos pagados, no pagados y todos.
- **Administradores:**
  - Gestionar los clientes, con la posibilidad de modificar, añadir y dar de baja clientes.
  - Gestionar las actividades, con la posibilidad de modificar, añadir y dar de baja actividades.
  - Generar los recibos de un mes.
  - Marcar recibo como pagado cuando el cliente pague.
  - Mostrar recibos: realiza un informe que muestre los recibos realizados en un año que se pasa como parámetro. El informe debe mostrar lo que se ha cobrado a cada cliente en ese año. Al final del informe se mostrará lo que ha cobrado el gimnasio a todos los clientes.

La aplicación pedirá el usuario y la contraseña y, en función del tipo de usuario, se le mostrarán las operaciones que puede hacer. El script crea el usuario `admin` con clave `admin`; este será el usuario administrador.

Las contraseñas en la BD se almacenan encriptadas con la función de MySQL `sha2('clave', longitud)`; longitud puede ser 224, 256, 384, 512, o 0 (equivalente a 256). Para comprobar si se introduce la contraseña correcta, se deberá chequear que las claves encriptadas coinciden.

Cuando se dé de alta un cliente hay que hacer un insert en dos tablas ⇒ **¡¡USAR TRANSACCIONES!!**

- `dbConnection.setAutoCommit(false)` antes del primer insert.
- `dbConnection.commit()` después del segundo insert.
- Si se produce algún error entre los dos insert: `dbConnection.rollback()`.

Cada vez que se añadan registros deberá informarse de si se ha insertado el registro o no. Los mensajes mostrarán la mayor información posible sin saturar al usuario. Para recuperar el último número asignado por MySQL en un campo auto-increment debemos:

```java
numero = stmt.executeUpdate(query, Statement.RETURN_GENERATED_KEYS);
ResultSet rs = stmt.getGeneratedKeys();
if (rs.next()){
    resultado = rs.getInt(1);
}
```

Para gestionar los recibos se pedirá el mes y el año. Si se han generado los recibos para esta fecha se mostrarán, si no, se generarán. Para generar los recibos puedes usar la rutina `generar_recibos()`.

Cuando un cliente paga un recibo, la fecha de pago se rellena automáticamente con la fecha del día en que se paga.

**Modelo entidad-relación de la BD del gimnasio:**

![Diagrama E/R del gimnasio: tablas cliente (id, usuario, dni, apellidos, nombre, tfno_contacto, baja), actividad (id, nombre, coste_mensual, activa), participa (actividad_id, cliente_id), recibo (cliente_id, fecha_emision, fecha_pago, cuantia, pagado) y usuarios (usuario, clave, tipo)](Tema_2_imagenes/img-017.png)

| Tabla | Campos |
|---|---|
| **cliente** | `id` INT(11) (PK), `usuario` VARCHAR(10), `dni` VARCHAR(10), `apellidos` VARCHAR(40), `nombre` VARCHAR(20), `tfno_contacto` VARCHAR(9), `baja` TINYINT(1) |
| **actividad** | `id` INT(11) (PK), `nombre` VARCHAR(100), `coste_mensual` FLOAT, `activa` ENUM(...) |
| **participa** | `actividad_id` INT(11), `cliente_id` INT(11) |
| **recibo** | `cliente_id` INT(11), `fecha_emision` DATE (PK compuesta), `fecha_pago` DATE, `cuantia` FLOAT, `pagado` TINYINT(1) |
| **usuarios** | `usuario` VARCHAR(10) (PK), `clave` BLOB, `tipo` ENUM('C','A') |
