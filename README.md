# Viajes Destinos - Gestión de Destinos Turísticos ✈️🌴

Aplicación móvil desarrollada en **Android Studio** con **Kotlin** y **Jetpack Compose** para una Agencia de Viajes. Permite gestionar un catálogo de destinos turísticos con sistema de autenticación seguro, validaciones obligatorias, almacenamiento multimedia y un CRUD completo integrado con servicios de **Firebase (Auth & Firestore)** y persistencia local offline.

---

## 📋 Datos de la Entrega

- **Nombre del Alumno:** Jesus Ernesto Sanabria Sibrian
- **Enlace del Video de Defensa:** Google Drive)*
- **APK Funcional:** Generada en `app/build/outputs/apk/debug/app-debug.apk` (o mediante el menú de exportación de AI Studio).

---

## 🎨 Paleta de Colores y Temas Implementados

La aplicación implementa fielmente los colores requeridos:
- **Dark Primary Color:** `#0097A7` (Barras superiores y acentos oscuros)
- **Light Primary Color:** `#B2EBF2` (Fondos claros, contenedores y chips de filtro)
- **Primary Color:** `#00BCD4` (Botones principales, íconos y encabezados)
- **Text / Icons:** `#FFFFFF` (Texto de botones, íconos y contrastes)
- **Accent Color:** `#8BC34A` (Badges de precio, FAB de agregar y estados activos)
- **Primary Text:** `#212121` (Títulos y texto principal de alta legibilidad)
- **Secondary Text:** `#757575` (Subtítulos, contadores y descripciones)
- **Divider Color:** `#BDBDBD` (Divisores de tarjetas y bordes)

Todos los textos de la interfaz gráfica están completamente centralizados en `app/src/main/res/values/strings.xml`.

---

## ✅ Cumplimiento de Requerimientos

### 1. Autenticación (Firebase Auth: Email & Cuentas de Google)
- Login y Registro de usuarios con Firebase Auth (`com.google.firebase:firebase-auth`).
- Inicio de sesión directo con Cuentas de Google (`GoogleAuthProvider` + Credential Manager).
- Validación de formato de correo electrónico y contraseña mínima de 6 caracteres.
- Muestra de advertencias y errores en tiempo real en la pantalla.
- Acceso seguro para agentes de viajes (sin accesos de demostración no autorizados).

### 2. Base de Datos (Firebase Firestore & Sincronización Local)
- Conexión a Firebase Firestore (`destinations`) con listener en tiempo real.
- Sincronización con almacenamiento local para garantizar funcionamiento offline y disponibilidad continua.

### 3. Almacenamiento Multimedia (Storage Local)
- Selector de imágenes desde la galería del dispositivo mediante Android Photo Picker (`ActivityResultContracts.PickVisualMedia()`).
- Almacenamiento durable en el directorio interno de la aplicación (`context.filesDir/destinations_media/`), garantizando persistencia segura y sin necesidad de permisos invasivos.
- Opciones de fotografías sugeridas integradas para facilitar pruebas directas.

### 4. Carga de Imágenes
- Implementación de **Coil** con soporte tanto en Jetpack Compose (`AsyncImage`) como en el `DestinationAdapter` de **RecyclerView / ImageView** tradicional.

### 5. CRUD Completo y Validaciones Obligatorias
1. **Create (Registro de Destinos):**
   - Nombre del destino (no vacío).
   - País mediante **Spinner** (con selección rápida de países).
   - Precio del paquete con formato numérico estricto y **obligatoriamente mayor a 0**.
   - Descripción del viaje con validación de **mínimo 20 caracteres** y contador en vivo.
   - Fotografía obligatoria asociada para poder guardar.
2. **Read (Catálogo Turístico con RecyclerView XML Tradicional):**
   - Implementación con **`RecyclerView` + `DestinationAdapter` + `CardView` en XML** (`layout_destinations_recycler.xml` e `item_destination_card.xml`), cumpliendo con la exigencia académica del docente.
   - Incluye selector para alternar con la vista moderna en Compose.
   - Tarjetas con fotografía, nombre, precio destacado, país y descripción.
   - Barra de búsqueda y filtros rápidos por país.
3. **Update (Edición):**
   - Modificación de cualquier campo del paquete turístico, incluyendo la actualización o cambio de la imagen.
4. **Delete (Eliminación con Confirmación Previa):**
   - Borrado de destinos con diálogo de confirmación emergente que previene eliminaciones accidentales.

---

## 📜 Historial Sugerido de Commits (Mínimo 10 con verbos)

Para cumplir con el requerimiento de mínimo 10 commits con verbos válidos (en español o inglés):

1. `implement`: implementar estructura base del proyecto Android y configuración de dependencias
2. `add`: agregar paleta de colores requerida y recursos en strings.xml
3. `add`: agregar modelo de datos Destination y helper de almacenamiento multimedia local
4. `implement`: implementar repositorio con sincronización Firebase Firestore y Auth
5. `add`: agregar componente Spinner de países para selección en formularios
6. `implement`: implementar pantalla de Login y Registro con validaciones en pantalla
7. `implement`: implementar pantalla de Catálogo con RecyclerView, CardView y filtros
8. `implement`: implementar formulario de registro y edición con validaciones obligatorias
9. `add`: agregar diálogo de confirmación previa para eliminación de destinos
10. `update`: actualizar icono adaptativo personalizado de la aplicación
11. `test`: probar modelos de datos y pruebas unitarias con Robolectric
12. `docs`: documentar proyecto y guía de entrega en README.md

---

## 🚀 Instrucciones de Compilación y Ejecución

1. Clonar el repositorio:
   ```bash
   git clone https://github.com/jess300605/TravelApp)
   ```
2. Abrir el proyecto en **Android Studio**.
3. (Opcional para Firebase): Descargar `google-services.json` desde Firebase Console y colocarlo en el directorio `/app`. Si no se incluye, la aplicación funciona de forma automática en modo local garantizando que no se interrumpa la evaluación.
4. Compilar el APK de prueba:
   ```bash
   ./gradlew assembleDebug
   ```
   o
   ```bash
   ./gradlew assembleRelease
   ```
5. Ejecutar en un dispositivo físico o emulador con Android 7.0 (API 24) o superior.
