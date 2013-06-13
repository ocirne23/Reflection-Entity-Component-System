package recs.test.components;

import java.util.ArrayList;


public class Position {
	public float x;
	public float y;

	public Position(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public Velocity v = new Velocity(3, 4);
	public int[] arr = {1, 2, 3, 4, 5};
	public Integer[] iarr = {new Integer(1), new Integer(2), new Integer(3)};
	public int i = 1;
	public double d = 2.011;
	public byte wa = 14;
	public short rr = 5;
	public long hurr = 14311515;
	public Attack a = new Attack(2);
	public ArrayList<Integer> list = new ArrayList<Integer>();

	public Position() {}
}
