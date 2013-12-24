package recs.components;

import recs.Component;

public class Health0 extends Component {
	public int amount;
	public int max;

	public Health0(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
