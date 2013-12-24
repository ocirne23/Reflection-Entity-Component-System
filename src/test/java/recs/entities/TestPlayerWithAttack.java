package recs.entities;

import recs.components.Attack0;


public class TestPlayerWithAttack extends TestPlayer {
	Attack0 attack;
	public TestPlayerWithAttack(float x, float y) {
		super(x, y);
		attack = new Attack0(2);
	}
}
