# Guía de Configuración, Compilación y Despliegue

Este documento detalla los pasos secuenciales para validar el entorno de desarrollo y desplegar la arquitectura de microservicios de **Cobox Smart Vision** de forma local y totalmente automatizada.

## 1. Prerrequisitos: Configuración de Java 21 y Docker

El proyecto requiere **Docker Desktop** (en ejecución) y **Java 21**. Es crítico que la variable de entorno del sistema apunte a la versión 21 para que herramientas como Lombok compilen correctamente sin arrojar errores de incompatibilidad (`TypeTag :: UNKNOWN`).

Verifique la versión activa en su terminal ejecutando:

```bash
java -version
```

**Configuración del JDK (Recomendación usando IntelliJ IDEA):**

1. Instale el JDK 21 (ej. Eclipse Temurin 21) directamente a través de IntelliJ IDEA o su proveedor de preferencia.
2. Copie la ruta de instalación del JDK.
3. En Windows (PowerShell), actualice temporalmente la variable `JAVA_HOME` en su sesión para garantizar que el orquestador use la versión correcta:

```powershell
$env:JAVA_HOME = "C:\Users\TU_USUARIO\.jdks\temurin-21.0.8"
$env:Path = "$env:JAVA_HOME\bin;" + $env:Path

# Verifique nuevamente
java -version
```

---

## 2. Ejecución del Despliegue

Desde la terminal en la raíz del proyecto, ejecute el script principal:

```powershell
.\scripts\local\start-infrastructure.ps1
```

*(Opcional): Si necesita levantar el entorno generando certificados SSL reales (para entornos integrados o pruebas en dominio), agregue el flag:*
`.\scripts\start-infrastructure.ps1 -EnableSSL`

---

## 3. ¿Qué hace exactamente este pipeline?

1. **Compilación Centralizada (Maven Reactor):** El script invoca un único comando de Maven en la raíz. Gracias al archivo `pom.xml` principal (Reactor), Maven calcula automáticamente el árbol de dependencias y compila todos los `.jar` necesarios en paralelo aprovechando su CPU (`-T 1C`), garantizando que siempre se despliegue código fresco y limpio.
2. **Protección Anti-Colapsos de Docker:** Inyecta la variable `$env:COMPOSE_PARALLEL_LIMIT = "2"`, lo que obliga a Docker a construir las imágenes pesadas de Java en lotes pequeños. Esto previene el clásico error de falta de memoria o timeouts (`grpc connection closing`) en Docker Desktop para Windows.
3. **Orquestación Inteligente:** Al ejecutar `up -d --build`, Docker Compose lee los `healthchecks` declarados. No levanta todos los servicios a la vez (evitando *race conditions*), sino que despliega en cascada: primero asegura que `postgres-db` y `config-service` estén `Healthy` antes de arrancar `eureka-service`, y luego el resto de los microservicios de negocio.

---

## 4. Flujo de Desarrollo: Actualización Individual de Servicios

En esta arquitectura no es necesario reiniciar todo el entorno para reflejar un cambio de código. Para mantener la agilidad durante el desarrollo, utilice uno de los siguientes métodos según su necesidad:

### Método A: Reconstrucción Aislada (Vía Consola)

Ideal cuando se ha terminado una funcionalidad y se desea probar su integración final dentro del ecosistema Docker.

1. Compila únicamente el microservicio modificado (ej. `delivery-service`) ignorando el resto del proyecto:

```powershell
.\mvnw.cmd -q -T 1C -DskipTests -pl delivery-service -am clean package
```

2. Reconstruye y reinicia solo ese contenedor:

```powershell
docker compose -f docker-compose.yml up -d --build delivery-service
```

*Nota: Los demás servicios (PostgreSQL, Eureka, Config Server) continuarán operando sin interrupción ni pérdida de estado.*

### Método B: Modo Híbrido (Recomendado para Debugging)

El estándar recomendado para sesiones de programación activas que requieren inspección de variables y recarga en caliente (*Hot Reload*).

1. Levante toda la infraestructura base utilizando el script de la **Sección 2**.
2. Apague el contenedor del servicio específico que va a editar:

```powershell
docker compose stop delivery-service
```

3. Ejecute el microservicio directamente desde su IDE (IntelliJ, VS Code, etc.) en modo *Debug*. La aplicación local se conectará automáticamente a la base de datos y al servidor Eureka que siguen corriendo en Docker.
