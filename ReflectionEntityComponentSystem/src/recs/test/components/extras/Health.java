package recs.test.components.extras;

import recs.core.Component;

public class Health extends Component {
	public int amount;
	public int max;

	public Health(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
