# SIP — Sistema Intermediario de Pólizas

## 1. Descripción del proyecto

SIP (Sistema Intermediario de Pólizas) es un sistema desarrollado para consultar y gestionar información relacionada con pólizas de seguros mediante la comunicación con un Sistema Dueño externo que proporciona una interfaz REST.

El sistema permite realizar operaciones sobre clientes, pólizas y beneficiarios, utilizando una interfaz de escritorio desarrollada con JavaFX y un backend construido con Spring Boot.

**Materia:** Arquitectura de Software
**Semestre:** 2026-II
**Institución:** Universidad Autónoma de la Ciudad de México (UACM)

## 2. Arquitectura general

La solución está integrada por los siguientes componentes:

- **Interfaz de usuario:** aplicación de escritorio desarrollada con JavaFX.
- **Backend:** aplicación Spring Boot que proporciona los servicios y la comunicación entre los componentes.
- **Base de datos local:** PostgreSQL, utilizada para almacenar la información local correspondiente a clientes.
- **Sistema Dueño de pólizas:** servicio externo que proporciona operaciones mediante una API REST para consultar y actualizar información de pólizas y beneficiarios.

La interfaz JavaFX se comunica con el backend SIP, mientras que este último administra la persistencia local y las solicitudes hacia el Sistema Dueño.

## 3. Tecnologías y herramientas

| Tecnología | Versión o uso |
|---|---|
| Java JDK | 25 |
| Spring Boot | 4.1.1 |
| JavaFX | 25 |
| Maven Wrapper | Administración y ejecución del proyecto |
| PostgreSQL | Base de datos local |
| Hibernate / JPA | Persistencia y gestión del esquema |
| API REST | Comunicación con el Sistema Dueño |

## 4. Requisitos previos

Antes de ejecutar el proyecto se requiere:

1. Instalar JDK 25 y configurar la variable de entorno `JAVA_HOME`.
2. Instalar PostgreSQL y mantener activo su servicio.
3. Disponer de la base de datos `sip_db`.
4. Disponer del usuario PostgreSQL `sip_usuario` con permisos sobre la base de datos.
5. Contar con conexión a Internet para descargar dependencias Maven y comunicarse con el Sistema Dueño.

El proyecto incluye Maven Wrapper, por lo que no es necesario instalar Maven de manera independiente.

## 5. Configuración de PostgreSQL

La configuración de conexión utilizada por SIP es:

| Parámetro | Valor |
|---|---|
| Motor | PostgreSQL |
| Host | localhost |
| Puerto | 5432 |
| Base de datos | sip_db |
| Usuario | sip_usuario |
| Contraseña | Variable de entorno `SIP_DB_PASSWORD` |

En una instalación nueva, se debe crear el usuario y la base de datos desde PostgreSQL utilizando una cuenta administradora:

```sql
CREATE USER sip_usuario WITH PASSWORD 'TU_CONTRASEÑA';
CREATE DATABASE sip_db OWNER sip_usuario;
```

Estas instrucciones son únicamente para una instalación inicial. Si el usuario y la base de datos ya existen, no es necesario crearlos nuevamente.

### Variable de entorno de la contraseña

SIP utiliza la variable de entorno `SIP_DB_PASSWORD` para proporcionar la contraseña de conexión.

En PowerShell, dentro de la terminal donde se iniciará Spring Boot, ejecutar:

```powershell
$clave = Read-Host "Ingresa la contraseña de sip_usuario" -AsSecureString
$env:SIP_DB_PASSWORD = [System.Net.NetworkCredential]::new("", $clave).Password
```

Esta variable permanece disponible durante la sesión actual de PowerShell. Al abrir una terminal nueva, será necesario establecerla nuevamente.

**Importante:** no se deben publicar contraseñas ni credenciales dentro del repositorio.

## 6. Configuración de la aplicación

El archivo principal de configuración se encuentra en:

`src/main/resources/application.properties`

La conexión a PostgreSQL está configurada para utilizar:

- URL: `jdbc:postgresql://localhost:5432/sip_db`
- Usuario: `sip_usuario`
- Contraseña: variable de entorno `SIP_DB_PASSWORD`

La propiedad de Hibernate `spring.jpa.hibernate.ddl-auto=update` permite actualizar el esquema de la base de datos conforme a las entidades mapeadas por la aplicación.

La comunicación con el Sistema Dueño se configura mediante:

- URL: `http://nachintoch.mx:8080`
- Tiempo de espera de conexión: 10 segundos.
- Tiempo de espera de lectura: 180 segundos.

No es necesario modificar estos parámetros para iniciar una instalación que utilice la configuración establecida por el equipo.

## 7. Instalación y ejecución

El backend y la interfaz JavaFX se ejecutan como dos procesos independientes. Para utilizar el sistema se deben iniciar ambos componentes.

### 7.1 Obtener el proyecto

Clonar el repositorio:

```powershell
git clone https://github.com/Agustin0824/Proyecto-Arquitectura-de-Software.git
```

Ingresar al directorio del proyecto:

```powershell
cd Proyecto-Arquitectura-de-Software
```

### 7.2 Iniciar el backend Spring Boot

Abrir una primera ventana de PowerShell en el directorio raíz del proyecto.

Establecer la variable de entorno `SIP_DB_PASSWORD` siguiendo las instrucciones de la sección 5.

Iniciar el backend:

```powershell
.\mvnw.cmd spring-boot:run
```

Esperar a que Spring Boot indique que la aplicación inició correctamente.

### 7.3 Iniciar la interfaz JavaFX

Abrir una segunda ventana de PowerShell en el mismo directorio del proyecto.

Ejecutar:

```powershell
.\mvnw.cmd javafx:run
```

La aplicación de escritorio JavaFX se iniciará y podrá utilizar los servicios proporcionados por el backend SIP.

**Orden de ejecución:** primero debe iniciarse Spring Boot y posteriormente JavaFX.

## 8. Casos de uso principales

El producto implementa los siguientes ocho casos de uso principales:

1. Registrar cliente.
2. Modificar cliente.
3. Registrar póliza.
4. Consultar póliza.
5. Modificar póliza.
6. Eliminar póliza.
7. Registrar beneficiario.
8. Consultar beneficiario.

Las operaciones se realizan mediante la interfaz de usuario y los servicios correspondientes del backend.

## 9. Ejecución de pruebas

Para ejecutar las pruebas automatizadas del proyecto:

```powershell
.\mvnw.cmd test
```

Para verificar el cumplimiento de las reglas de estilo mediante Checkstyle:

```powershell
.\mvnw.cmd checkstyle:check
```

Durante la validación de la versión integrada en la rama principal `main`, el equipo obtuvo:

- 89 pruebas automatizadas ejecutadas.
- 0 pruebas fallidas.
- 0 errores.
- 0 pruebas omitidas.
- 0 violaciones de Checkstyle.

## 10. Repositorio del proyecto

Repositorio GitHub:

https://github.com/Agustin0824/Proyecto-Arquitectura-de-Software.git

La rama principal de entrega es `main`. Los cambios del proyecto se incorporaron mediante ramas de integración y solicitudes de combinación (Pull Requests).

## 11. Consideraciones de ejecución

- PostgreSQL debe estar activo antes de iniciar el backend.
- La variable `SIP_DB_PASSWORD` debe estar configurada en la terminal donde se ejecuta Spring Boot.
- La interfaz JavaFX requiere que el backend SIP esté iniciado.
- Las operaciones que dependen del Sistema Dueño requieren conectividad con su servicio REST externo.
- Los comandos de ejecución se deben realizar desde el directorio raíz del proyecto, donde se encuentra `mvnw.cmd`.