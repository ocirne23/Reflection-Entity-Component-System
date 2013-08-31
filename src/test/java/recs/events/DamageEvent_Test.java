package recs.events;

import recs.Event;

public class DamageEvent_Test extends Event {
	public int entityId;
	public int damage;

	public DamageEvent_Test(int entityId, int damage) {
		this.entityId = entityId;
		this.damage = damage;
	}
}
