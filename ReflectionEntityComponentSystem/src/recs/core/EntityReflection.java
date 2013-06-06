package recs.core;

import java.lang.reflect.Field;

import recs.core.utils.RECSIntMap;

/**
 * Representation of a single Entity class, used by the EntityWorld to remember reflection data.
 * Has a map holding the component fields in that class, and a list of usable systems.
 *
 * @author Enrico van Oosten
 */
public final class EntityReflection {
	protected EntityDef def;
	/**
	 * Map of components, retrievable by using the class of the component.
	 */
	protected RECSIntMap<Field> componentFields;
	/**
	 * List of systems this entity class can use.
	 */

	protected EntityReflection(RECSIntMap<Field> componentFields, EntityDef def) {
		this.componentFields = componentFields;
		this.def = def;
	}
}
