# ACCESO A DATOS 2ºDAM
## Repositorio con apuntes y ejercicios del módulo de Acceso a Datos de 2º de DAM
### Profesor: Andrés Valverde Consentino.

<h3>UNIDADES DIDÁCTICAS</h3>

<div class="grid-container">

  <div class="card">
    <div class="card-header">
      <span class="badge">UD 1</span>
    </div>
    <h4>Gestión de Ficheros y Directorios</h4>
    <p>Clases java.io y java.nio, flujos de entrada/salida (I/O), serialización de objetos y formatos XML / JSON.</p>
    <a href="Tema_1_Gestion_de_Ficheros_y_Directorios.html" class="btn">Ver Unidad Didáctica →</a>
  </div>

  <div class="card">
    <div class="card-header">
      <span class="badge">UD 2</span>
    </div>
    <h4>Acceso a BD Relacionales (Conectores)</h4>
    <p>Arquitectura JDBC, ejecución de sentencias SQL, gestión de transacciones y pools de conexiones.</p>
    <a href="Tema_2_Acceso_BD_Relacionales_Conectores.html" class="btn">Ver Unidad Didáctica →</a>
  </div>

  <div class="card">
    <div class="card-header">
      <span class="badge">UD 3</span>
    </div>
    <h4>Herramientas de Mapeo Objeto Relacional (ORM)</h4>
    <p>Mapeo de entidades con Hibernate/JPA, estados de objetos, consultas HQL/JPQL y relaciones 1:N / N:M.</p>
    <a href="Tema_3_Herramientas_ORM.html" class="btn">Ver Unidad Didáctica →</a>
  </div>

</div>

<!-- Estilos para crear las tarjetas visuales -->
<style>
.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.card {
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 20px;
  background-color: #ffffff;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 15px rgba(0, 0, 0, 0.1);
}

.badge {
  background-color: #159957;
  color: white;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 0.8rem;
  font-weight: bold;
}

.card h4 {
  margin: 12px 0 8px 0;
  color: #159957;
}

.card p {
  font-size: 0.9rem;
  color: #606f7b;
  flex-grow: 1;
}

.btn {
  display: inline-block;
  margin-top: 15px;
  padding: 8px 14px;
  background-color: #159957;
  color: white !important;
  text-decoration: none;
  border-radius: 5px;
  font-size: 0.9rem;
  font-weight: bold;
  text-align: center;
}

.btn:hover {
  background-color: #117a45;
}
</style>