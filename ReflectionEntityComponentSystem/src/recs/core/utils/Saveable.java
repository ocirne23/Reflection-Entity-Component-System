package recs.core.utils;

public interface Saveable {
	public Object[] save();
	public void load(Object[] savedData);
}
