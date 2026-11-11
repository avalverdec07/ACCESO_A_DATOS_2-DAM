# ACCESO A DATOS 2ºDAM

Repositorio con los apuntes y ejercicios del módulo de **Acceso a Datos** del Ciclo Formativo de Grado Superior de Desarrollo de Aplicaciones Multiplataforma (DAM).

> **Profesor:** Andrés Valverde Consentino  
> **Curso:** 2º DAM

---

## 📚 Unidades Didácticas

<div class="grid cards" markdown>

-   :material-folder-file:{ .lg .middle } __UD 1: Gestión de Ficheros y Directorios__

    ---

    Clases `java.io` y `java.nio`, flujos de entrada/salida (I/O), serialización de objetos y formatos XML / JSON.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_1_Gestion_de_Ficheros_y_Directorios.md){ .md-button .md-button--primary }

-   :material-database-connect:{ .lg .middle } __UD 2: Acceso a BD Relacionales (Conectores)__

    ---

    Arquitectura JDBC, ejecución de sentencias SQL, gestión de transacciones y pools de conexiones.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_2_Acceso_BD_Relacionales_Conectores.md){ .md-button .md-button--primary }

-   :material-database-sync:{ .lg .middle } __UD 3: Herramientas ORM (Hibernate)__

    ---

    Mapeo de entidades con Hibernate/JPA, estados de objetos, consultas HQL/JPQL y relaciones 1:N / N:M.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_3_Herramientas_ORM.md){ .md-button .md-button--primary }

-   :material-database-cog:{ .lg .middle } __UD 4: BD Objeto-Relacionales y OO__

    ---

    Instalación de PostgreSQL, JDBC, almacenamiento JSON/JSONB y persistencia de objetos con DB4O.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_4_BD_OR_y_OO.md){ .md-button .md-button--primary }

-   :material-leaf:{ .lg .middle } __UD 5: BD Documentales Nativas (MongoDB)__

    ---

    Bases de datos NoSQL, arquitectura MongoDB, operaciones CRUD, mapeo POJO y modelado de documentos.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_5_Bases_de_Datos_Documentales_Nativas.md){ .md-button .md-button--primary }

-   :material-cube-outline:{ .lg .middle } __UD 6: Creación de Componentes de AD__

    ---

    Tecnologías de desarrollo de componentes, especificación JavaBeans, empaquetado de librerías y JakartaEE.

    [:octicons-arrow-right-24: Ver Unidad Didáctica](Tema_6_Creacion_de_Componentes_de_AD.md){ .md-button .md-button--primary }

</div>

<!-- Ajuste visual de las tarjetas para integrarse perfectamente con el tema Material -->
<style>
.md-typeset .grid.cards > ul > li {
  border: 1px solid var(--md-default-fore-color--lightest);
  border-radius: 8px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.04);
  transition: transform 0.2s ease, box-shadow 0.2s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.md-typeset .grid.cards > ul > li:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0,0,0,0.1);
  border-color: var(--md-primary-bg-color);
}

.md-typeset .grid.cards hr {
  margin: 0.8em 0;
}

.md-typeset .md-button--primary {
  background-color: var(--md-primary-bg-color);
  color: var(--md-primary-fg-color) !important;
  border: none;
  margin-top: 1em;
  width: 100%;
  text-align: center;
}

.md-typeset .md-button--primary:hover {
  background-color: var(--md-accent-fg-color);
}
</style>