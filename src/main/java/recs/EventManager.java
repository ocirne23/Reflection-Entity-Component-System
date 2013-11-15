package recs;

import java.util.LinkedList;

import com.badlogic.gdx.utils.ObjectMap;



/**
 * Event manager used by EntityWorld to pass messages to systems.
 *
 * @author Enrico van Oosten
 */
public final class EventManager {
	private final ObjectMap<Class<? extends Event>, LinkedList<EventListener<? extends Event>>> listeners;

	EventManager() {
		listeners = new ObjectMap<Class<? extends Event>, LinkedList<EventListener<? extends Event>>>();
	}

	/** Send an event object to all listeners for its class */
	void sendEvent(Event message) {
		LinkedList<EventListener<? extends Event>> listenerList = listeners.get(message.getClass());
		if (listenerList != null) {
			for (EventListener<? extends Event> listener : listenerList) {
				listener.sendMessage(message);
			}
		}
	}

	/** Register a listener so it will receive the events that are sent with the matching class */
	void registerListener(EventListener<? extends Event> listener, Class<? extends Event> eventClass) {
		LinkedList<EventListener<? extends Event>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			if (!listenerList.contains(listener)) {
				listenerList.add(listener);
			}
		} else {
			listenerList = new LinkedList<EventListener<? extends Event>>();
			listenerList.add(listener);
			listeners.put(eventClass, listenerList);
		}
	}

	/** Unregister a listener so it will no longer receive events */
	void unregisterListener(EventListener<? extends Event> listener, Class<? extends Event> eventClass) {
		LinkedList<EventListener<? extends Event>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			listenerList.remove(listener);
		}
	}

	/** Wipe all the data */
	void clear() {
		listeners.clear();
	}
}
