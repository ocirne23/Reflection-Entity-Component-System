package recs.events;

import recs.Event;

public class TestDamageEvent extends Event {
	public int entityId;
	public int damage;

	public TestDamageEvent(int entityId, int damage) {
		this.entityId = entityId;
		this.damage = damage;
	}
}
