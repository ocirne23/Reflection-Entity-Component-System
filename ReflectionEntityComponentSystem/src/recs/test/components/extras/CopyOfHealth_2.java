package recs.test.components.extras;

import recs.core.Component;

public class CopyOfHealth_2 extends Component {
	public int amount;
	public int max;

	public CopyOfHealth_2(int health, int maxHealth) {
		this.amount = health;
		this.max = maxHealth;
	}
}
