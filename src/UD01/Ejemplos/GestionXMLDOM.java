package UD01.Ejemplos;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.File;

public class GestionXMLDOM {

    private static final String RUTA_ARCHIVO = "src/UD01/Ejemplos/instituto.xml";

    public static void main(String[] args) {
        System.out.println("--- INICIANDO CREACIÓN DE XML ---");
        escribirXML();

        System.out.println("\n--- INICIANDO LECTURA DE XML ---");
        leerXML();
    }

    // 1. MÉTODO PARA CREAR Y ESCRIBIR EL XML
    public static void escribirXML() {
        try {
            // Fábrica para construir el documento DOM
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();

            // 1. Crear el elemento raíz <instituto>
            Element instituto = document.createElement("instituto");
            document.appendChild(instituto);

            // 2. Crear el elemento <alumno> y su atributo
            Element alumno = document.createElement("alumno");
            alumno.setAttribute("id", "101");
            instituto.appendChild(alumno);

            // 3. Crear elementos hijos de <alumno>
            Element nombre = document.createElement("nombre");
            nombre.setTextContent("Laura");
            alumno.appendChild(nombre);

            Element curso = document.createElement("curso");
            curso.setTextContent("2 DAM");
            alumno.appendChild(curso);

            Element modulos = document.createElement("modulos");
            alumno.appendChild(modulos);

            // 4. Crear elementos de la lista de módulos
            Element modulo1 = document.createElement("modulo");
            modulo1.setTextContent("Acceso a Datos");
            modulos.appendChild(modulo1);

            Element modulo2 = document.createElement("modulo");
            modulo2.setTextContent("Programación Multimedia");
            modulos.appendChild(modulo2);

            // 5. Transformar el árbol DOM en memoria a un archivo físico
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();

            // Configurar el formato para que quede tabulado y legible (Pretty Print)
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            // Origen de datos (el documento en memoria) y destino (el archivo físico)
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(RUTA_ARCHIVO));

            // Ejecutar la transformación
            transformer.transform(source, result);
            System.out.println("Archivo '" + RUTA_ARCHIVO + "' creado correctamente.");

        } catch (Exception e) {
            System.err.println("Error al escribir el XML: " + e.getMessage());
        }
    }

    // 2. MÉTODO PARA LEER EL XML
    public static void leerXML() {
        try {
            File archivoXML = new File(RUTA_ARCHIVO);

            // Cargar el documento de forma similar a la escritura
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(archivoXML);

            // Normalizar el documento (elimina nodos de texto vacíos y unifica la estructura)
            // Es una buena práctica siempre que se lee un DOM
            document.getDocumentElement().normalize();

            System.out.println("Elemento raíz: " + document.getDocumentElement().getNodeName());

            // Obtener una lista con todos los nodos <alumno>
            NodeList listaAlumnos = document.getElementsByTagName("alumno");

            for (int i = 0; i < listaAlumnos.getLength(); i++) {
                Node nodo = listaAlumnos.item(i);

                // Comprobar que el nodo es realmente un Elemento (y no un salto de línea o texto)
                if (nodo.getNodeType() == Node.ELEMENT_NODE) {
                    Element elementoAlumno = (Element) nodo;

                    // Leer atributo
                    String id = elementoAlumno.getAttribute("id");

                    // Leer sub-elementos. getElementsByTagName devuelve una lista, cogemos el item(0)
                    String nombre = elementoAlumno.getElementsByTagName("nombre").item(0).getTextContent();
                    String curso = elementoAlumno.getElementsByTagName("curso").item(0).getTextContent();

                    System.out.println("\n--- Alumno ---");
                    System.out.println("ID: " + id);
                    System.out.println("Nombre: " + nombre);
                    System.out.println("Curso: " + curso);
                    System.out.println("Módulos:");

                    // Leer la lista interna de módulos
                    NodeList listaModulos = elementoAlumno.getElementsByTagName("modulo");
                    for (int j = 0; j < listaModulos.getLength(); j++) {
                        System.out.println("  - " + listaModulos.item(j).getTextContent());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error al leer el XML: " + e.getMessage());
        }
    }
}