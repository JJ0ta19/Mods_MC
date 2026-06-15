# Cómo compilar el mod a JAR

Instrucciones para compilar un mod de Forge 1.20.1 usando Gradle.

---

## Requisitos

| Requisito | Versión |
|-----------|---------|
| Java JDK | 17 o superior |
| Gradle | 8.1.1 (incluido en gradlew.bat) |
| Git | Opcional |

Verifica Java:
```
java -version
```
Debe mostrar `openjdk version "17.0.x"` o superior.

---

## Compilar el JAR

### Paso 1: Abre una terminal en la carpeta del proyecto

```
cd D:\ruta\a\tu\mod
```

### Paso 2: Ejecuta Gradle (primera vez)

```batch
.\gradlew.bat
```

Esto descarga Gradle y las dependencias de Forge. La primera vez tarda varios minutos.

### Paso 3: Compila el mod

```batch
.\gradlew.bat build
```

### Paso 4: Encuentra el JAR

El JAR compilado estará en:

```
build/libs/<modid>-1.0.0.jar
```

Ejemplo: `build/libs/rasengan-1.0.0.jar`

---

## Instalar el mod

1. Copia el archivo `.jar` a la carpeta `mods/` de tu instancia de Minecraft
2. La carpeta `mods/` está en:
   - **Vanilla Launcher**: `%appdata%\.minecraft\mods`
   - **CurseForge**: `instances\<nombre>\mods`
   - **MultiMC/Prism**: `instances\<nombre>\.minecraft\mods`
3. Asegúrate de tener Forge 1.20.1 instalado en el perfil
4. Inicia Minecraft

---

## Comandos útiles de Gradle

| Comando | Qué hace |
|---------|----------|
| `.\gradlew.bat build` | Compila el JAR |
| `.\gradlew.bat clean` | Borra build/ (compilación limpia) |
| `.\gradlew.bat runClient` | Ejecuta Minecraft directamente |
| `.\gradlew.bat runServer` | Ejecuta servidor directamente |
| `.\gradlew.bat genIntellijRuns` | Genera configs para IntelliJ IDEA |
| `.\gradlew.bat genEclipseRuns` | Genera configs para Eclipse |
| `.\gradlew.bat --stop` | Detiene el daemon de Gradle |

---

## Solución de problemas

### "Java not found" o error de JDK

Asegúrate de tener JAVA_HOME configurado:

```batch
set JAVA_HOME=C:\Program Files\Java\jdk-17
```

O temporalmente:

```batch
.\gradlew.bat build -Dorg.gradle.java.home="C:\Program Files\Java\jdk-17"
```

### "Out of memory" / Heap space

Abre `gradle.properties` y aumenta la memoria:

```properties
org.gradle.jvmargs=-Xmx4G
```

### "BUILD FAILED" por errores de código

Revisa los mensajes de error en la terminal. Los errores de compilación
muestran el archivo y línea exacta. Corrige y ejecuta `build` otra vez.

### El JAR se genera pero no funciona en Minecraft

Causas comunes:
- El `mods.toml` tiene mal el `modId`
- La clase `@Mod` no existe o tiene mal el MODID
- Faltan dependencias en `mods.toml`
- El JAR está dañado (corrupto) — ejecuta `.\gradlew.bat clean build`

---

## Flujo de trabajo recomendado

```batch
# 1. Limpiar compilacion anterior
.\gradlew.bat clean

# 2. Compilar
.\gradlew.bat build

# 3. (Opcional) Ejecutar cliente para probar
.\gradlew.bat runClient
```

Para desarrollo rápido, usa `runClient` directamente y solo compila el JAR
cuando quieras distribuir el mod.
