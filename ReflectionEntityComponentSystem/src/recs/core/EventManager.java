package recs.core;

import java.util.LinkedList;

import recs.core.utils.RECSObjectMap;


/**
 * Event manager used by EntityWorld to pass messages to systems.
 *
 * @author Enrico van Oosten
 */
public final class EventManager {
	private final RECSObjectMap<Class<?>, LinkedList<EventListener<?>>> listeners;

	protected EventManager() {
		listeners = new RECSObjectMap<Class<?>, LinkedList<EventListener<?>>>();
	}

	protected void sendEvent(Object message) {
		LinkedList<EventListener<?>> listenerList = listeners.get(message.getClass());
		if (listenerList != null) {
			for (EventListener<?> listener : listenerList) {
				listener.sendMessage(message);
			}
		}
	}

	protected void registerListener(EventListener<?> listener, Class<?> eventClass) {
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

	protected void unregisterListener(EventListener<?> listener, Class<?> eventClass) {
		LinkedList<EventListener<?>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			listenerList.remove(listener);
		}
	}

	public void clear() {
		listeners.clear();
	}
}
