package recs.core;

import java.lang.reflect.ParameterizedType;

public abstract class ComponentDestructionListener<T> {

	public ComponentDestructionListener(EntityWorld world) {
		//Reflection hax for clean api, otherwise pass a class as parameter.
		@SuppressWarnings("unchecked")
		Class<T> genericParameter = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

		world.registerDestuctionListener(this, genericParameter);
	}

	/**
	 * Called when a component of the same type as the type parameter is destroyed.
	 */
	public abstract void destroyed(T component);
}
