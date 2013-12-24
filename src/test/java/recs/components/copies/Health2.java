package recs.components.copies;

import recs.Component;

public class Health2 extends Component {
	public int amount;
	public int max;

	public Health2(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
