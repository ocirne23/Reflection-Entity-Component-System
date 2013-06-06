package recs.core;

import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Used to easily retrieve events.
 *
 * @author Enrico van Oosten
 * @param <T>
 *            The type of events listened for.
 */
public class EventListener<T> {
	private final LinkedBlockingQueue<T> receivedEvents;
	private final LinkedList<T> polledEventsList;

	protected EventListener() {
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
	 *
	 * @param message
	 */
	@SuppressWarnings("unchecked")
	protected void sendMessage(Object message) {
		receivedEvents.add((T) message);
	}
}
