# Proyecto: RosaMod

## Contexto General

Este proyecto es un **mod/plugin híbrido para Mohist 1.20.1** (Minecraft). Mohist es un servidor híbrido que combina la API de Bukkit/Spigot/Paper con la carga de mods de Forge.

- **Versión de Minecraft:** 1.20.1  
- **Plataforma:** Mohist Server  
- **Lenguaje:** Java (JDK 17 o superior compatible)  
- **Build Tool:** Gradle (con ForgeGradle o similar para la carga de mods)  
- **Artefacto:** Principalmente un mod de Forge que también puede interactuar con la capa Bukkit si es necesario, o un plugin de Spigot con acceso a Forge.

## Conocimientos Requeridos

Cuando trabajes en este proyecto, actúa como un experto en:

1. **Forge API 1.20.1:** Registro de items, bloques, entities, eventos, networking, capabilities, data generators.
2. **Bukkit/Spigot/Paper API:** Comandos, eventos del servidor,schedulers, configuraciones YAML.
3. **Mohist Especificidades:**
   - Compatibilidad entre eventos de Forge y Bukkit.
   - Manejo de entidades y worlds en ambas APIs.
   - Uso de `org.bukkit.craftbukkit.v1_20_R1` para NMS (si se requiere interaction directa).
   - Consideraciones de classloading híbrido.
4. **Estructura de Proyecto:**
   - `src/main/java/com/rosamod/RosaMod.java` como clase principal.
   - `src/main/resources/META-INF/mods.toml` para metadatos de Forge.
   - `src/main/resources/plugin.yml` si expone funcionalidad de plugin Bukkit (opcional).
   - Gradle con mappings MCP/official o Parchment para deobfuscación.

## Estilo y Convenciones

- **Código:** Java 17+, convenciones estándar de Oracle (camelCase para variables/métodos, PascalCase para clases).
- **Paquete base:** `com.rosamod`.
- **Registro:** Usar DeferredRegister o el sistema de registro apropiado para Forge 1.20.1.
- **Eventos:** Usar `@SubscribeEvent` y anotaciones de Forge; si interactúa con Bukkit, usar el PluginManager de Bukkit de forma segura verificando que Mohist está presente.
- **Configuración:** Usar configura ser de Forge (spec) para mods; `config.yml` para funcionalidad de plugin.
- **Networking:** Definir canales simples con `SimpleChannel` si se necesitan paquetes custom.

## Directrices de Diseño (Mods)

- Los items/bloques deben ser balanceados para multiplayer y no explotar el servidor.
- Las texturas y modelos deben colocarse en `src/main/resources/assets/rosamod/`.
- Usar Data Generators (si aplica) para recipes, loot tables, avances y tags ubicados en `src/main/generated`.
- Mantener compatibilidad con clientes vanilla si es un plugin puro; si es un mod Forge, asumir que los clientes también cargan el mod.

## Build y Test

- Gradle tasks clave: `build`, `runClient` (si se configura), `runServer` (usando Mohist como librería).
- El resultado del build se encuentra en `build/libs/`.
- Para probar, usar un servidor Mohist 1.20.1 local con el mod en la carpeta `mods/`.

## Notas Especiales

- **No uses Mixins a menos que sea estrictamente necesario**, preferir eventos de Forge.
- **Evita reflection innecesaria** en la capa híbrida; Mohist expone algunos métodos de forma directa.
- **Versionado:** Seguir SemVer para el mod (`1.0.0-SNAPSHOT`, etc.).
