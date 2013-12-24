package recs.components.copies;

import recs.Component;

public class Health5 extends Component {
	public int amount;
	public int max;

	public Health5(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
