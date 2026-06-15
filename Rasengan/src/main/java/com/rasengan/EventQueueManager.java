package com.rasengan;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class EventQueueManager {

    private final Map<UUID, Queue<Runnable>> eventQueues = new HashMap<>();
    private final Map<UUID, Runnable> activeEvents = new HashMap<>();

    /**
     * Queue or execute an event for a given target.
     * If the target already has an active event, the new one is queued (FIFO).
     * Otherwise, it executes immediately.
     */
    public void queueOrExecute(UUID targetId, Runnable event) {
        if (activeEvents.containsKey(targetId)) {
            eventQueues.computeIfAbsent(targetId, k -> new LinkedList<>()).add(event);
        } else {
            activeEvents.put(targetId, event);
            event.run();
        }
    }

    /**
     * Must be called when the currently active event finishes.
     * Removes it from active and starts the next queued event (if any).
     */
    public void onEventCompleted(UUID targetId) {
        activeEvents.remove(targetId);

        Queue<Runnable> queue = eventQueues.get(targetId);
        if (queue != null && !queue.isEmpty()) {
            Runnable next = queue.poll();
            activeEvents.put(targetId, next);
            next.run();
        }
    }

    public boolean hasActiveEvent(UUID targetId) {
        return activeEvents.containsKey(targetId);
    }

    public void clearAll() {
        activeEvents.clear();
        eventQueues.clear();
    }
}
