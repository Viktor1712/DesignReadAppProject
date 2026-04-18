# ReadFlow 📚

**ReadFlow** es una aplicación móvil avanzada para la lectura de libros digitales (PDF), diseñada para ofrecer una experiencia fluida y conectada. Esta aplicación es el cliente móvil de un ecosistema que incluye una API robusta en Azure y una versión web.

## 🚀 Características Principales

- **Gestión de Biblioteca**: Visualiza libros organizados por categorías obtenidas dinámicamente desde una API externa.
- **Sincronización en Tiempo Real**: El progreso de lectura (página actual) se guarda automáticamente en la nube, permitiéndote retomar la lectura exactamente donde la dejaste.
- **Lectura Offline Inteligente**: Los libros se descargan y cachean localmente para garantizar una lectura fluida sin depender constantemente de la conexión a internet.
- **Favoritos**: Sistema para marcar y gestionar tus libros preferidos.
- **Autenticación**: Integración con Firebase para un acceso seguro de los usuarios.
- **Interfaz Moderna**: Construida totalmente con **Jetpack Compose**, soportando temas claros y oscuros.

## 🎨 Diseño y Planeación

El diseño de la aplicación, incluyendo wireframes y bocetos (mockups), fue planificado previamente para asegurar una experiencia de usuario intuitiva. Puedes ver el tablero de diseño en Figma aquí:
👉 [Diseño en Figma](https://www.figma.com/board/444ZEHDv4oxosiL4jWbU9Q/Sin-t%C3%ADtulo?node-id=0-1&t=S2Q0u9o093LViNNX-1)

## 🛠️ Arquitectura y Tecnologías

El proyecto sigue las mejores prácticas de desarrollo Android moderno:

- **Lenguaje**: Kotlin.
- **UI**: Jetpack Compose (Declarative UI).
- **Networking**: Retrofit para el consumo de la API REST en Azure.
- **Imágenes**: Coil para la carga eficiente de portadas de libros desde URLs externas.
- **PDF Core**: `android-pdf-viewer` para una visualización de documentos de alto rendimiento.
- **Backend-as-a-Service**: Firebase Auth para la gestión de usuarios.

## 🧩 Lógica del Proyecto

### 1. Conectividad con la API de Azure
La aplicación se comunica con `https://librosapi.azure-api.net/v1/`. Utiliza una clave de suscripción (`Ocp-Apim-Subscription-Key`) para autenticar las peticiones. Los endpoints principales incluyen:
- `/categories`: Obtiene la estructura de la biblioteca.
- `/books`: Lista de libros disponibles con sus metadatos (título, autor, portada, URL del PDF).
- `/reading-progress`: Sincroniza la página actual del usuario.
- `/favorites`: Gestiona la lista de libros marcados por el usuario.

### 2. Flujo de Lectura
Cuando un usuario selecciona un libro:
1. La aplicación verifica si el PDF es remoto (URL).
2. Se inicia una descarga asíncrona al almacenamiento interno del dispositivo (caché).
3. Una vez disponible localmente, el `PDFView` renderiza el documento.
4. Cada vez que el usuario pasa una página, se dispara un evento que actualiza el progreso en la API de Azure de forma transparente.

### 3. Persistencia Híbrida
ReadFlow utiliza un sistema de persistencia doble:
- **Nube**: Para la sincronización entre dispositivos.
- **Local (SharedPreferences)**: Como backup rápido para una carga instantánea mientras se sincronizan los datos de red.

## 🌐 Versión Web

Puedes encontrar la versión web de este ecosistema en el siguiente repositorio:
👉 [https://github.com/IsmaTEC24/libreria](https://github.com/IsmaTEC24/libreria)

---
*Desarrollado como parte del proyecto de diseño de software para lectura digital.*
