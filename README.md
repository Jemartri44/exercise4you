# Exercise4You 🏋️‍♂️  
## Introducción 📍  
**Exercise4You** es una aplicación web diseñada para fisioterapeutas, que les permite gestionar pacientes y prescribir ejercicio terapéutico de manera personalizada. Esta herramienta se enmarca dentro de una tesis doctoral y combina funcionalidades clave como la gestión de usuarios, la creación de cuestionarios dinámicos, la generación de informes en formato PDF y el uso de calculadoras antropométricas. La aplicación ha sido desarrollada con tecnologías modernas como Angular, Spring Boot y servicios de AWS, asegurando escalabilidad y seguridad.  

---

## Funcionalidades principales 💡  
- **Gestión de usuarios y pacientes**: Registro, inicio de sesión y administración de perfiles.  
- **Cuestionarios dinámicos**: Personalización y configuración basadas en autómatas.  
- **Generación de informes**: Creación de documentos PDF personalizados para cada paciente.  
- **Almacenamiento seguro**: Gestión de documentos en Amazon S3 o servicios compatibles.  

---

## Requisitos del sistema 💻  
- **Node.js** (v14 o superior).  
- **Java** (JDK 17).  
- **Docker** (para despliegue y pruebas locales).  
- **MySQL** (o un servicio compatible con SQL).  
- **MongoDB** (para datos no relacionales).  
- **MinIO** o un servicio compatible con S3 para almacenamiento de archivos.

---

## Instalación y configuración 🔧  

### Clonar el repositorio  
Descarga el repositorio mediante Git:  

```bash  
git clone https://github.com/jemartri44/exercise4you 
cd exercise4you  
```

### Compilación del frontend
Navega al directorio del frontend:
```bash 
cd frontend  
npm install  
ng build --configuration production
```
Copia los archivos generados de dist/frontend/browser al directorio src/main/resources/static del backend.

### Crear la imagen Docker
Accede al directorio del backend y construye el proyecto gracias al Dockerfile:

```bash
cd backend  
docker build -t exercise4you .
```

---

## Despliegue con Docker 🐳
Inicia la aplicación en un contenedor Docker:

```bash
docker run --env-file prod.env -p 80:8080 exercise4you 
```
La aplicación estará disponible en http://localhost