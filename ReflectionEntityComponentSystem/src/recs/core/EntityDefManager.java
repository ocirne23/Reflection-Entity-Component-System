package recs.core;

import java.lang.reflect.Field;

import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSObjectMap;

public class EntityDefManager {
	private EntityWorld world;
	private RECSObjectMap<Class<? extends Entity>, EntityReflection> reflectionMap = new RECSObjectMap<Class<? extends Entity>, EntityReflection>();
	private RECSObjectMap<RECSBits, EntityDef> defMap = new RECSObjectMap<RECSBits, EntityDef>();

	public EntityDefManager(EntityWorld world) {
		this.world = world;
	}

	public EntityReflection getReflection(Class<? extends Entity> class1) {
		return reflectionMap.get(class1);
	}

	public void putReflection(Class<? extends Entity> class1, EntityReflection reflection) {
		reflectionMap.put(class1, reflection);
	}

	public EntityDef getDef(RECSBits componentBits) {
		return defMap.get(componentBits);
	}

	public EntityDef putDef(RECSBits componentBits, EntityDef def) {
		return defMap.put(componentBits, def);
	}

	public void clear() {
		defMap.clear();
		reflectionMap.clear();
	}

	@SuppressWarnings("unchecked")
	public EntityReflection addNewEntityReflection(Class<? extends Entity> class1) {
		Class<? extends Entity> mainClass = class1;
		RECSIntMap<Field> fieldMap = new RECSIntMap<Field>();
		RECSBits componentBits = new RECSBits();
		// Iterate all the subclasses.
		while (class1 != Entity.class) {
			// Put every field object in a map with the fields class as key.
			for (Field f : class1.getDeclaredFields()) {
				Class<?> fieldClass = f.getType();
				if (world.getComponentMapper(fieldClass) != null) {
					f.setAccessible(true);
					int componentId = world.getComponentId(fieldClass);

					componentBits.set(componentId);
					fieldMap.put(componentId, f);
				}
			}
			class1 = (Class<? extends Entity>) class1.getSuperclass();
		}

		RECSBits systemBits = world.getSystemBits(componentBits);
		EntityDef def = new EntityDef(world, componentBits, systemBits);
		putDef(componentBits, def);

		EntityReflection reflection = new EntityReflection(fieldMap, def);
		reflectionMap.put(mainClass, reflection);

		return reflection;
	}
}
