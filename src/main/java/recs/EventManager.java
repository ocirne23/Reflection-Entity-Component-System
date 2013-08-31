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

	void sendEvent(Event message) {
		LinkedList<EventListener<? extends Event>> listenerList = listeners.get(message.getClass());
		if (listenerList != null) {
			for (EventListener<? extends Event> listener : listenerList) {
				listener.sendMessage(message);
			}
		}
	}

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

	void unregisterListener(EventListener<? extends Event> listener, Class<? extends Event> eventClass) {
		LinkedList<EventListener<? extends Event>> listenerList = listeners.get(eventClass);
		if (listenerList != null) {
			listenerList.remove(listener);
		}
	}

	void clear() {
		listeners.clear();
	}
}
