package recs.core;

import recs.core.utils.RECSBits;
import recs.core.utils.RECSObjectMap;

public class EntityDefManager {
	private RECSObjectMap<Class<? extends Entity>, EntityReflection> reflectionMap = new RECSObjectMap<Class<? extends Entity>, EntityReflection>();
	private RECSObjectMap<RECSBits, EntityDef> defMap = new RECSObjectMap<RECSBits, EntityDef>();

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
}
