package recs.components.copies;

import recs.Component;

public class Health7 extends Component {
	public int amount;
	public int max;

	public Health7(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
