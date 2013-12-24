package recs.components.copies;

import recs.Component;

public class Health4 extends Component {
	public int amount;
	public int max;

	public Health4(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
