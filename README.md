TurisMap

Descripción General
TurisMap es una aplicación móvil multiplataforma (Android e iOS) diseñada para promover y gestionar el ecoturismo en la Ruta de las Flores, El Salvador. El sistema, desarrollado por TurisLabs, opera como una guía interactiva e informativa para la exploración de los municipios de Concepción de Ataco, Apaneca, Juayúa, Salcoatitán y Nahuizalco.

La aplicación proporciona a los usuarios un mapa interactivo con puntos de interés categorizados (establecimientos gastronómicos, miradores, reservas naturales), gestión de perfiles de usuario y un módulo de reseñas en tiempo real. El proyecto está construido sobre una arquitectura de código compartido para ambas plataformas, optimizando el ciclo de desarrollo y mantenimiento.



Dependencias y Tecnologías
El presente proyecto está desarrollado mediante Kotlin Multiplatform (KMP) e implementa las siguientes tecnologías y bibliotecas fundamentales:

* Lenguaje de Programación:** Kotlin
* Framework de Interfaz de Usuario:** Compose Multiplatform (Jetpack Compose para Android/iOS)
* Base de Datos y Backend:** Firebase Cloud Firestore (NoSQL)
* Autenticación:** Firebase Authentication (Credenciales de correo/contraseña y Google Sign-in)
* Servicios de Cartografía y Geolocalización:** SDK de Google Maps API (`maps-compose`)
* Enrutamiento y Navegación:** Navigation Compose
* Programación Asíncrona:** Kotlin Coroutines y StateFlow
* Inyección de Dependencias:** Hilt / Dagger (Módulos nativos)



 Instrucciones de Instalación

Requisitos del Sistema
1. Entorno de desarrollo [Android Studio](https://developer.android.com/studio) (Se requiere versión Iguana o superior).
2. Para la compilación del binario en iOS, se requiere el sistema operativo macOS con el entorno **Xcode** instalado.
3. Herramienta de línea de comandos [KDoctor](https://github.com/Kotlin/kdoctor) para la verificación y validación del entorno Multiplatform.

