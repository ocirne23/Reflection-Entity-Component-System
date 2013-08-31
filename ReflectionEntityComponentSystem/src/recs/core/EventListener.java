package recs.core;

import java.lang.reflect.ParameterizedType;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Used to easily retrieve events.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The type of events listened for.
 */
public class EventListener<T extends Event> {
	private final LinkedBlockingQueue<T> receivedEvents;
	private final LinkedList<T> polledEventsList;


	/**
	 * Constructor used by EntityWorld to initialize the listener after reading the field inside a system.
	 */
	protected EventListener() {
		receivedEvents = new LinkedBlockingQueue<T>();
		polledEventsList = new LinkedList<T>();
	}

	/**
	 * Constructor used to create a listener outside of an entity system, do not use this to create a listener
	 * inside an EntitySystem.
	 */
	public EventListener(EntityWorld world) {
		//Reflection hax for clean api, otherwise pass a class as parameter.
		@SuppressWarnings("unchecked")
		Class<T> genericParameter = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

		world.registerEventListener(this, genericParameter);
		receivedEvents = new LinkedBlockingQueue<T>();
		polledEventsList = new LinkedList<T>();
	}

	/**
	 * Returns all the events received and clears the list.
	 *
	 * @return A list of events.
	 */
	public LinkedList<T> pollEvents() {
		polledEventsList.clear();
		receivedEvents.drainTo(polledEventsList);
		return polledEventsList;
	}

	/**
	 * Used by EntityWorld to add events to this listener.
	 */
	@SuppressWarnings("unchecked")
	void sendMessage(Event message) {
		receivedEvents.add((T) message);
	}
}
