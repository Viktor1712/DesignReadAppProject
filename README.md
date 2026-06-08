# Biblio-TEC 📚

**Biblio-TEC** es una aplicación móvil para la lectura de libros digitales (PDF), diseñada para ofrecer una experiencia fluida y conectada. Es el cliente móvil de un ecosistema que incluye una API en Azure y una versión web.

## 🚀 Características Principales

- **Biblioteca Personal**: Visualiza, sube y organiza libros por categorías.
- **Lector PDF Integrado**: Visualización de documentos de alto rendimiento con seguimiento de progreso.
- **Progreso de Lectura en la Nube**: El progreso se sincroniza automáticamente para retomar la lectura en cualquier dispositivo.
- **Notificaciones en Tiempo Real**: Conexión SignalR con servicio en segundo plano para recibir notificaciones push aunque la app no esté activa.
- **Favoritos y Me Gusta**: Sistema para marcar y gestionar libros preferidos.
- **Autenticación Segura**: Firebase Auth con soporte para actualizar nombre, correo y contraseña desde el perfil.
- **Interfaz Adaptable**: Construida con Jetpack Compose, con soporte completo para temas claro y oscuro.
- **Subida de Libros**: Permite publicar libros con portada y PDF directamente desde el dispositivo.

## 🎨 Diseño y Planeación

El diseño de la aplicación fue planificado previamente en Figma para asegurar una experiencia de usuario intuitiva:

👉 [Diseño en Figma](https://www.figma.com/board/444ZEHDv4oxosiL4jWbU9Q/Sin-t%C3%ADtulo?node-id=0-1&t=S2Q0u9o093LViNNX-1)

## 🛠️ Arquitectura y Tecnologías

- **Lenguaje**: Kotlin
- **UI**: Jetpack Compose (Declarative UI) con Material 3
- **Networking**: Retrofit + OkHttp para la API REST en Azure
- **Tiempo Real**: SignalR (`microsoft/signalr`) con Foreground Service para persistencia en segundo plano
- **Imágenes**: Coil para la carga de portadas desde Azure Blob Storage
- **PDF**: `android-pdf-viewer` para renderizado de documentos
- **Auth**: Firebase Authentication
- **CI/CD**: GitHub Actions — genera APK y AAB firmados como artefactos `Biblio-TEC`

## 🧩 Arquitectura del Backend (Azure)

La aplicación consume una API en Azure API Management (`librosapi.azure-api.net/v1/`). Los microservicios principales son:

| Servicio | Responsabilidad |
|---|---|
| MS-1 | Autenticación y gestión de usuarios |
| MS-2 | Libros, progreso de lectura, favoritos |
| MS-3 | Notificaciones en tiempo real (SignalR) |

Los endpoints principales incluyen:
- `/books` — Lista y gestión de libros
- `/reading-progress` — Sincronización de página actual (upsert)
- `/favorites` — Libros favoritos del usuario
- `/notifications/negotiate` — Negociación SignalR

## 🔔 Notificaciones en Segundo Plano

Biblio-TEC implementa un `SignalRForegroundService` (tipo `dataSync`) que mantiene la conexión activa aunque el usuario salga de la app. Al regresar, `onResume` reconecta automáticamente si la conexión se perdió, y las notificaciones nuevas se muestran como notificaciones push del sistema.

## 🌐 Versión Web

El ecosistema incluye una versión web complementaria:

👉 [https://github.com/IsmaTEC24/libreria](https://github.com/IsmaTEC24/libreria)

---
*Desarrollado como proyecto de Diseño de Software — IV Semestre.*
