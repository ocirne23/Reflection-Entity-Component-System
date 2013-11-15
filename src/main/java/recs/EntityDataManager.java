package recs;

import java.lang.reflect.Field;

import recs.utils.RECSBits;

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Manages reflection data and families of the entities so they can be more efficiently retrieved.
 * @author Enrico van Oosten
 */
public final class EntityDataManager {
	private EntityWorld world;
	/** Maps class to reflection data of that class */
	private ObjectMap<Class<? extends Entity>, EntityReflectionCache> reflectionMap = new ObjectMap<Class<? extends Entity>, EntityReflectionCache>();
	/** Maps component bits to family */
	private ObjectMap<RECSBits, EntityFamily> entityFamilyMap = new ObjectMap<RECSBits, EntityFamily>();

	EntityDataManager(EntityWorld world) {
		this.world = world;
	}

	/** Get the reflection data from the given class. */
	EntityReflectionCache getReflection(Class<? extends Entity> class1) {
		EntityReflectionCache reflectionData = reflectionMap.get(class1);
		if (reflectionData == null)
			reflectionData = createNewEntityReflection(class1);

		return reflectionData;
	}

	/** Retrieve an EntityFamily object matching the set of components. */
	EntityFamily getEntityFamily(RECSBits componentBits) {
		EntityFamily data = entityFamilyMap.get(componentBits);
		if (data == null) {
			RECSBits systemBits = world.getSystemBits(componentBits);
			data = new EntityFamily(world, componentBits, systemBits);
			entityFamilyMap.put(componentBits, data);
		}
		return data;
	}

	/** Is called on removeSystem to remove the matching system bit from all entities */
	void removeSystem(int id) {
		for(EntityFamily data: entityFamilyMap.values()) {
			data.systemBits.clear(id);
		}
	}

	/** Wipes all the data */
	void clear() {
		entityFamilyMap.clear();
		reflectionMap.clear();
	}

	/**
	 * Create an EntityReflection containing the reflection data of a class
	 * by scanning its fields.
	 */
	@SuppressWarnings("unchecked")
	private EntityReflectionCache createNewEntityReflection(Class<? extends Entity> class1) {
		Class<? extends Entity> mainClass = class1;
		IntMap<Field> fieldMap = new IntMap<Field>();
		RECSBits componentBits = new RECSBits();
		// Iterate all the subclasses.
		while (class1 != Entity.class) {
			// Put every field object in a map with the fields class as key.
			for (Field f : class1.getDeclaredFields()) {
				Class<?> fieldClass = f.getType();
				if (fieldClass.getSuperclass() == Component.class) {
					f.setAccessible(true);
					int componentId = world.getComponentId((Class<? extends Component>) fieldClass);

					componentBits.set(componentId);
					fieldMap.put(componentId, f);
				}
			}
			class1 = (Class<? extends Entity>) class1.getSuperclass();
		}

		EntityFamily data = getEntityFamily(componentBits);

		EntityReflectionCache reflection = new EntityReflectionCache(fieldMap, data);
		reflectionMap.put(mainClass, reflection);

		return reflection;
	}
}
