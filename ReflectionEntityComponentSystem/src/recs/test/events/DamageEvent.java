package recs.test.events;

import recs.core.Event;

public class DamageEvent extends Event {
	public int entityId;
	public int damage;

	public DamageEvent(int entityId, int damage) {
		this.entityId = entityId;
		this.damage = damage;
	}
}
