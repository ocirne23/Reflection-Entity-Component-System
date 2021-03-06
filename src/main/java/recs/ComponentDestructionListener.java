package recs;

import java.lang.reflect.ParameterizedType;

public abstract class ComponentDestructionListener<T extends Component> {

	/**
	 * Class that will receive notifications through destroyed() whenever a component
	 * matching the generic type is removed from the world.
	 */
	public ComponentDestructionListener(EntityWorld world) {
		//Reflection hax for clean api, other option is passing a class as parameter.
		@SuppressWarnings("unchecked")
		Class<T> genericParameter = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];

		world.registerDestuctionListener(this, genericParameter);
	}

	/**
	 * Called when a component of the same type as the type parameter is destroyed.
	 */
	public abstract void destroyed(T component);
}
