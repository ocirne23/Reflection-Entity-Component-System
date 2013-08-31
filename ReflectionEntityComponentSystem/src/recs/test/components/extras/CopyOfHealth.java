package recs.test.components.extras;

import recs.core.Component;

public class CopyOfHealth extends Component {
	public int amount;
	public int max;

	public CopyOfHealth(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
