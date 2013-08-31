package recs.core;

import java.util.LinkedList;

import recs.core.utils.libgdx.RECSObjectMap;


/**
 * Event manager used by EntityWorld to pass messages to systems.
 *
 * @author Enrico van Oosten
 */
public final class EventManager {
	private final RECSObjectMap<Class<?>, LinkedList<EventListener<?>>> listeners;

	EventManager() {
		listeners = new RECSObjectMap<Class<?>, LinkedList<EventListener<?>>>();
	}

	void sendEvent(Event message) {
		LinkedList<EventListener<?>> listenerList = listeners.get(message.getClass());
		if (listenerList != null) {
			for (EventListener<?> listener : listenerList) {
				listener.sendMessage(message);
			}
		}
	}

	void registerListener(EventListener<? extends Event> listener, Class<? extends Event> eventClass) {
		LinkedList<EventListener<?>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			if (!listenerList.contains(listener)) {
				listenerList.add(listener);
			}
		} else {
			listenerList = new LinkedList<EventListener<?>>();
			listenerList.add(listener);
			listeners.put(eventClass, listenerList);
		}
	}

	void unregisterListener(EventListener<? extends Event> listener, Class<? extends Event> eventClass) {
		LinkedList<EventListener<?>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			listenerList.remove(listener);
		}
	}

	void clear() {
		listeners.clear();
	}
}
