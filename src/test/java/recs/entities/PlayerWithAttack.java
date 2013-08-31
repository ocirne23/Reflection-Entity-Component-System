package recs.entities;

import recs.components.Attack_0;


public class PlayerWithAttack extends Player {
	Attack_0 attack;
	public PlayerWithAttack(float x, float y) {
		super(x, y);
		attack = new Attack_0(2);
	}
}
