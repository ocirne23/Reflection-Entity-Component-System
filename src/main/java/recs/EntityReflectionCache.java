package recs;

import java.lang.reflect.Field;

import com.badlogic.gdx.utils.IntMap;


/**
 * Caches java reflection data (the fields) for a single class so they can be more efficiently
 * retreived.
 *
 * Also contains the default family for this entity so you can immediately retrieve the
 * component/system bits if no additional components have been added besides through the
 * fields of the entity class.
 *
 * @author Enrico van Oosten
 */
public final class EntityReflectionCache {
	final EntityFamily family;
	final IntMap<Field> componentFields;

	EntityReflectionCache(IntMap<Field> componentFields, EntityFamily family) {
		this.componentFields = componentFields;
		this.family = family;
	}
}
