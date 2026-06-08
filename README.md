# Biblio-TEC Mobile

Aplicación móvil del ecosistema **Biblio-TEC**, diseñada para ofrecer una experiencia de lectura digital conectada con la API en Azure y la aplicación web.  
Este proyecto permite acceder a la biblioteca digital desde dispositivos Android y forma parte de una solución que busca sincronizar la experiencia de lectura entre distintas plataformas.

## Descripción del proyecto

Biblio-TEC Mobile es la versión Android de una plataforma de lectura de libros digitales en formato PDF.  
Su propósito es brindar una interfaz accesible y moderna para visualizar libros, gestionar el progreso de lectura y mantener una integración con los demás componentes del proyecto.

Este sistema forma parte de un ecosistema compuesto por:

- Aplicación móvil Android
- Aplicación web
- API desplegada en Azure

## Características principales

- Lectura de libros digitales en formato PDF desde el dispositivo.
- Organización de contenido por categorías, favoritos y biblioteca personal.
- Sincronización del progreso de lectura en la nube.
- Notificaciones en tiempo real mediante SignalR con servicio en segundo plano.
- Subida de libros con portada y PDF directamente desde el dispositivo.
- Gestión de perfil: nombre, usuario, correo y contraseña.
- Soporte completo para tema claro y oscuro con Material 3.
- Despliegue automatizado mediante GitHub Actions.

## Tecnologías utilizadas

- Kotlin
- Jetpack Compose (Material 3)
- Retrofit + OkHttp
- Firebase Authentication
- Microsoft SignalR
- Coil (carga de imágenes)
- Android PDF Viewer
- GitHub Actions

## Requisitos previos

Antes de ejecutar el proyecto de manera local, asegúrate de tener instalado:

- Android Studio (Hedgehog o superior)
- JDK 17
- Git
- Un dispositivo Android o emulador con API 24 o superior

## Instalación y ejecución local

### 1. Clonar el repositorio

    git clone https://github.com/Viktor1712/DesignReadAppProject

### 2. Ingresar a la carpeta del proyecto

    cd DesignReadAppProject

### 3. Abrir en Android Studio

Abre Android Studio y selecciona **Open** apuntando a la carpeta del proyecto.  
Espera a que Gradle sincronice las dependencias automáticamente.

### 4. Ejecutar la aplicación

Conecta un dispositivo Android o inicia un emulador y presiona **Run** en Android Studio, o ejecuta desde terminal:

    ./gradlew installDebug

### 5. Abrir en el dispositivo

La aplicación se instalará automáticamente en el dispositivo o emulador seleccionado.

## Comandos disponibles

### Compilar APK de desarrollo

    ./gradlew assembleDebug

### Compilar APK de producción

    ./gradlew assembleRelease

### Compilar AAB de producción (Play Store)

    ./gradlew bundleRelease

### Verificar compilación del código Kotlin

    ./gradlew compileDebugKotlin

## Build de producción

Para generar la versión lista para distribución, ejecuta:

    ./gradlew assembleRelease bundleRelease

Esto generará los archivos en:

    app/build/outputs/apk/release/
    app/build/outputs/bundle/release/

El APK y el AAB deben estar firmados con un keystore. En el flujo de CI/CD, las credenciales del keystore se configuran como secretos de GitHub.

## Despliegue mediante GitHub Actions

La aplicación está conectada a **GitHub Actions**, por lo que cada vez que se crea un tag o se lanza manualmente el workflow, se genera un APK y un AAB firmados automáticamente.

### Pasos para generar una nueva versión

1. Realizar los cambios necesarios en el proyecto.
2. Guardar los archivos modificados.
3. Agregar los cambios al control de versiones:

        git add .

4. Crear un commit con un mensaje descriptivo:

        git commit -m "Actualizacion de la aplicacion mobile"

5. Crear un tag con el número de versión:

        git tag v1.0.0

6. Subir los cambios y el tag al repositorio remoto:

        git push origin main
        git push origin v1.0.0

Después del push del tag, GitHub Actions ejecutará automáticamente el proceso de compilación, firma y publicación del release.

### Lanzamiento manual

También es posible disparar el workflow manualmente desde GitHub:

1. Ingresar al repositorio en GitHub.
2. Abrir la pestaña **Actions**.
3. Seleccionar el workflow **Android Release**.
4. Hacer clic en **Run workflow** e ingresar el número de versión.

## Verificación del despliegue

Después de ejecutar el workflow, se recomienda:

1. Ingresar al repositorio en GitHub.
2. Abrir la pestaña **Actions** y verificar que el workflow haya finalizado correctamente.
3. Ir a la sección **Releases** del repositorio.
4. Verificar que el release **Biblio-TEC v{version}** aparezca publicado con los archivos `Biblio-TEC-v{version}.apk` y `Biblio-TEC-v{version}.aab`.

## Secretos de GitHub requeridos

Para que el workflow de CI/CD funcione correctamente, deben configurarse los siguientes secretos en el repositorio:

| Secreto | Descripción |
|---|---|
| `KEYSTORE_BASE64` | Keystore codificado en Base64 para firmar el APK |
| `KEYSTORE_PASSWORD` | Contraseña del keystore |
| `KEY_ALIAS` | Alias de la clave dentro del keystore |
| `KEY_PASSWORD` | Contraseña de la clave |

## Enlaces del proyecto

### Repositorio de la aplicación móvil

https://github.com/Viktor1712/DesignReadAppProject

### Aplicación web desplegada

https://brave-sea-03b672010.2.azurestaticapps.net

### Repositorio de la aplicación web

https://github.com/IsmaTEC24/libreria

## Estructura general del proyecto

    DesignReadAppProject/
    ├── app/
    │   ├── src/main/
    │   │   ├── java/com/design/readerapp/
    │   │   │   ├── MainActivity.kt
    │   │   │   ├── BooksService.kt
    │   │   │   ├── SignalRManager.kt
    │   │   │   ├── SignalRForegroundService.kt
    │   │   │   ├── Models.kt
    │   │   │   ├── ReaderState.kt
    │   │   │   ├── PdfUtils.kt
    │   │   │   └── ui/theme/
    │   │   │       ├── LoginScreen.kt
    │   │   │       ├── HomeScreen.kt
    │   │   │       ├── GroupScreen.kt
    │   │   │       ├── ReaderScreen.kt
    │   │   │       ├── UploadScreen.kt
    │   │   │       ├── EditBookScreen.kt
    │   │   │       ├── ProfileScreen.kt
    │   │   │       ├── NotificationsScreen.kt
    │   │   │       ├── ForumScreen.kt
    │   │   │       ├── Theme.kt
    │   │   │       ├── Color.kt
    │   │   │       └── Type.kt
    │   │   ├── res/
    │   │   └── AndroidManifest.xml
    │   └── build.gradle.kts
    ├── .github/workflows/
    │   └── release.yml
    └── gradle/
        └── libs.versions.toml

## Consideraciones importantes

- La aplicación utiliza **Jetpack Compose**, por lo que requiere Android API 24 o superior.
- Las variables sensibles (claves de API, credenciales del keystore) no deben subirse directamente al repositorio. Deben configurarse como secretos de GitHub o en un archivo `local.properties` ignorado por Git.
- El progreso de lectura se sincroniza con el backend mediante un patrón **upsert**: si ya existe un registro para el libro, se actualiza; si no, se crea uno nuevo.
- Las notificaciones en tiempo real utilizan un **Foreground Service** (`SignalRForegroundService`) para mantener la conexión activa aunque la app esté en segundo plano.
- Si se modifican las URLs base de la API, deben actualizarse en `BooksService.kt`.
- Se recomienda mantener una estructura clara de commits para facilitar el seguimiento del proyecto.

## Relación con el ecosistema Biblio-TEC

Este proyecto móvil complementa la aplicación web y la API en Azure, permitiendo que la solución funcione como un ecosistema unificado de lectura digital.  
La intención del proyecto es ofrecer una experiencia consistente entre plataformas y centralizar el acceso a la biblioteca digital.

## Estado del proyecto

Proyecto académico en desarrollo.

## Autores

Desarrollado como parte del proyecto **Biblio-TEC** — Diseño de Software, IV Semestre.
