package recs.entities;

import recs.components.Attack;


public class PlayerWithAttack extends Player {
	Attack attack;
	public PlayerWithAttack(float x, float y) {
		super(x, y);
		attack = new Attack(2);
	}
}
