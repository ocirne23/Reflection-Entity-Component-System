package recs.entities;

import recs.components.Attack_0;


public class PlayerWithAttack_Test extends Player_Test {
	Attack_0 attack;
	public PlayerWithAttack_Test(float x, float y) {
		super(x, y);
		attack = new Attack_0(2);
	}
}
