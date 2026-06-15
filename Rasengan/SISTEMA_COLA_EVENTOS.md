# Sistema de Cola de Eventos - Guía Genérica

Lógica para evitar que múltiples eventos se ejecuten simultáneamente sobre un mismo objetivo.

---

## Concepto

Cuando se recibe una solicitud de evento, verificar si ya hay uno activo:
- **Sí**: Encolar la solicitud
- **No**: Ejecutar inmediatamente

Al terminar un evento, verificar si hay más en cola y ejecutarlos en orden.

---

## Estructura de Datos

```java
private Map<UUID, Queue<EventExecutor>> eventQueues = new HashMap<>();
private Map<UUID, EventExecutor> activeEvents = new HashMap<>();
```

| Variable | Tipo | Propósito |
|----------|------|-----------|
| `eventQueues` | Map<UUID, Queue> | Cola de espera por objetivo |
| `activeEvents` | Map<UUID, EventExecutor> | Eventos actualmente ejecutándose |

---

## Flujo

```
Solicitud → ¿activeEvents.containsKey(id)?
                │
       ┌────────┴────────┐
       ▼                 ▼
   Ejecutar          Encolar
   (remove queue)    (add to queue)
       │                 │
       └────────┬────────┘
                │
          (evento termina)
                │
                ▼
    ┌─────────────────────┐
    │ ¿queue.isEmpty()?   │
    └─────────────────────┘
                │
       ┌────────┴────────┐
       ▼                 ▼
   Siguiente         Fin
   (poll + start)   (remove from active)
```

---

## Implementación Genérica

### 1. Clase Principal

```java
public class MiPlugin extends JavaPlugin {

    private Map<UUID, Queue<EventExecutor>> eventQueues = new HashMap<>();
    private Map<UUID, EventExecutor> activeEvents = new HashMap<>();
    private MiFreezeListener freezeListener;

    @Override
    public void onEnable() {
        freezeListener = new MiFreezeListener();
        getServer().getPluginManager().registerEvents(freezeListener, this);
    }

    // Método para agregar evento con cola automática
    public void queueEvent(Player target, MiFreezeListener freezeListener) {
        UUID id = target.getUniqueId();
        EventExecutor evento = new EventExecutor(this, target, freezeListener);

        if (activeEvents.containsKey(id)) {
            // Ya hay evento activo → encolar
            eventQueues.computeIfAbsent(id, k -> new LinkedList<>()).add(evento);
        } else {
            // Sin evento activo → ejecutar
            activeEvents.put(id, evento);
            evento.start();
        }
    }

    // Callback obligatorio cuando evento termina
    public void onEventCompleted(UUID id) {
        activeEvents.remove(id);

        Queue<EventExecutor> queue = eventQueues.get(id);
        if (queue != null && !queue.isEmpty()) {
            EventExecutor next = queue.poll();
            activeEvents.put(id, next);
            next.start();
        }
    }

    @Override
    public void onDisable() {
        for (EventExecutor e : activeEvents.values()) {
            e.cancel();
        }
        activeEvents.clear();
        eventQueues.clear();
    }
}
```

### 2. EventExecutor

```java
public class EventExecutor extends BukkitRunnable {

    private MiPlugin plugin;
    private Player target;
    private MiFreezeListener freezeListener;
    private int duration = 20; // segundos

    public EventExecutor(MiPlugin plugin, Player target, MiFreezeListener freezeListener) {
        this.plugin = plugin;
        this.target = target;
        this.freezeListener = freezeListener;
    }

    public void start() {
        // Iniciar efectos del evento
        freezeListener.freeze(target);
        this.runTaskTimer(plugin, 0, 20); // cada segundo
    }

    @Override
    public void run() {
        // Lógica del evento
        // ...
        
        // Terminar evento
        if (tiempoAgotado) {
            this.cancel();
            freezeListener.unfreeze(target);
            plugin.onEventCompleted(target.getUniqueId()); // ← IMPORTANTE
        }
    }
}
```

---

## Puntos Clave

1. **ID único**: Usar UUID del objetivo
2. **LinkedList**: Cola FIFO (First In, First Out)
3. **computeIfAbsent**: Crear cola solo si no existe
4. **poll()**: Extraer y.remove primer elemento
5. **Callback**: EventExecutor debe notificar al terminar
6. **Limpieza**: Cancelar todo en onDisable

---

## Notas

- Un objetivo puede tener múltiples eventos en cola
- Múltiples objetivos pueden ejecutar eventos simultáneamente
- Los eventos de un mismo objetivo NUNCA se cruzan
- El orden de ejecución es FIFO (orden de llegada)