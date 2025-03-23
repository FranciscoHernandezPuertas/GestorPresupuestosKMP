# Generador de Presupuestos - TFG

## Ejecución del proyecto

Para ejecutar el proyecto, es necesario tener instalado Kobweb. Sigue estos pasos:

1. Instala Kobweb siguiendo las instrucciones en:
    - [Documentación oficial](https://kobweb.varabyte.com/docs/getting-started/gettingkobweb)
    - [Descargar binarios](https://github.com/varabyte/kobweb-cli/releases/tag/v0.9.18)

2. Añade Kobweb al PATH del sistema

3. Navega a la raíz del proyecto y ejecuta:
   ```
   kobweb run
   ```

![Kobweb Run](KobwebRun.png)

## Descripción del proyecto

Este Trabajo Fin de Grado desarrolla una aplicación multiplataforma para la generación de presupuestos, permitiendo a usuarios crear presupuestos mediante una interfaz intuitiva y a administradores gestionar precios y visualizar presupuestos generados.

La principal ventaja es que utiliza Kotlin Multiplatform (KMP) y Compose Multiplatform (CMP) junto con Kobweb, permitiendo exportar la aplicación tanto como sitio web interactivo como aplicación nativa para Android desde una única base de código.

## Tecnologías utilizadas

- **Kotlin Multiplatform**: Para compartir código entre plataformas
- **Compose Multiplatform**: Framework UI declarativo
- **Kobweb**: Framework para desarrollo web con Kotlin
- **MongoDB**: Base de datos NoSQL con driver oficial para Kotlin
- **Gradle**: Sistema de gestión de dependencias

## Arquitectura

La aplicación sigue el patrón de diseño MVVM (Model-View-ViewModel) con clara separación entre:
- **Modelos**: Definición de datos (`User`, `UserWithoutPassword`, etc.)
- **Vistas**: Componentes UI en Compose (`LoginScreen`, `SidePanel`, etc.)
- **ViewModels**: Lógica de negocio y estados

## Características principales

- **Autenticación segura**: Implementación de hashing SHA-256 para contraseñas
- **Panel de administración**: Con navegación lateral y diferentes secciones funcionales
- **Interfaz adaptativa**: Diseño responsive que se ajusta a diferentes tamaños de pantalla
- **Persistencia de sesión**: Gestión de sesiones mediante localStorage

## Estructura del proyecto

El proyecto está organizado en módulos:

- **commonMain**: Código compartido entre plataformas
- **jsMain**: Implementación específica para web usando Compose HTML
- **jvmMain**: Backend con Kobweb y MongoDB

### Componentes principales

- **Login**: Sistema de autenticación para administradores
- **SidePanel**: Panel de navegación lateral con diferentes secciones
- **MongoDB**: Integración con la base de datos para almacenamiento persistente

## Pantallas

La aplicación cuenta con las siguientes pantallas principales:

1. **Login**: Autenticación de administradores
2. **Home**: Panel principal de administración
3. **Edit**: Edición de precios y parámetros
4. **List**: Visualización de presupuestos generados

## Seguridad

El sistema implementa varias capas de seguridad:
- Hashing SHA-256 para almacenamiento seguro de contraseñas
- Verificación de sesiones mediante tokens
- Protección de rutas para usuarios no autenticados

## Dependencias principales

```
- Kotlin Multiplatform: 2.1.10
- Kobweb: 0.20.0
- MongoDB Driver: 5.3.0
- Compose: 1.7.1
- KotlinX Serialization: 1.4.1
```

## Estado del proyecto

El proyecto implementa actualmente:
- Sistema de autenticación completo
- Panel de administración con navegación
- Integración con MongoDB
- Estructura base para la generación de presupuestos

## Licencia

Proyecto desarrollado como Trabajo Fin de Grado.