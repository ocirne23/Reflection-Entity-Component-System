package recs.components.copies;

import recs.Component;

public class Health6 extends Component {
	public int amount;
	public int max;

	public Health6(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
