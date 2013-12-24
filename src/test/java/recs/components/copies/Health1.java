package recs.components.copies;

import recs.Component;

public class Health1 extends Component {
	public int amount;
	public int max;

	public Health1(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
