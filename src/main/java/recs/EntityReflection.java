package recs;

import java.lang.reflect.Field;

import com.badlogic.gdx.utils.IntMap;


/**
 * Representation of a single Entity class, used by the EntityWorld to remember reflection data.
 * Has a map holding the component fields in that class so the values of the fields can be easily
 * retieved should another instance of the class be added.
 *
 * @author Enrico van Oosten
 */
public final class EntityReflection {
	final EntityData data;
	final IntMap<Field> componentFields;

	EntityReflection(IntMap<Field> componentFields, EntityData data) {
		this.componentFields = componentFields;
		this.data = data;
	}
}