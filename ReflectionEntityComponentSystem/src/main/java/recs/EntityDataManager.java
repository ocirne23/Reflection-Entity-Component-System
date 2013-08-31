package main.java.recs;

import java.lang.reflect.Field;

import main.java.recs.utils.RECSBits;
import main.java.recs.utils.libgdx.RECSIntMap;
import main.java.recs.utils.libgdx.RECSObjectMap;


public final class EntityDataManager {
	private EntityWorld world;
	private RECSObjectMap<Class<? extends Entity>, EntityReflection> reflectionMap = new RECSObjectMap<Class<? extends Entity>, EntityReflection>();
	private RECSObjectMap<RECSBits, EntityData> entityDataMap = new RECSObjectMap<RECSBits, EntityData>();

	EntityDataManager(EntityWorld world) {
		this.world = world;
	}

	EntityReflection getReflection(Class<? extends Entity> class1) {
		EntityReflection reflectionData = reflectionMap.get(class1);
		if (reflectionData == null)
			reflectionData = createNewEntityReflection(class1);

		return reflectionData;
	}

	void putReflection(Class<? extends Entity> class1, EntityReflection reflection) {
		reflectionMap.put(class1, reflection);
	}

	/**
	 * Retrieve an EntityData object matching the set of components
	 */
	EntityData getEntityData(RECSBits componentBits) {
		return entityDataMap.get(componentBits);
	}

	EntityData putEntityData(EntityData data) {
		return entityDataMap.put(data.componentBits, data);
	}

	void removeSystem(int id) {
		for(EntityData data: entityDataMap.values()) {
			data.systemBits.clear(id);
		}
	}

	void clear() {
		entityDataMap.clear();
		reflectionMap.clear();
	}

	/**
	 * Create an EntityReflection containing the reflection data of a class
	 * by scanning its fields.
	 */
	@SuppressWarnings("unchecked")
	private EntityReflection createNewEntityReflection(Class<? extends Entity> class1) {
		Class<? extends Entity> mainClass = class1;
		RECSIntMap<Field> fieldMap = new RECSIntMap<Field>();
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

		RECSBits systemBits = world.getSystemBits(componentBits);
		EntityData data = new EntityData(world, componentBits, systemBits);
		putEntityData(data);

		EntityReflection reflection = new EntityReflection(fieldMap, data);
		reflectionMap.put(mainClass, reflection);

		return reflection;
	}
}
