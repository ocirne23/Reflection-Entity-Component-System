package recs.core;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;

import recs.core.utils.BlockingThreadPoolExecutor;
import recs.core.utils.RECSBits;
import recs.core.utils.RECSIntMap;
import recs.core.utils.RECSIntMap.Keys;
import recs.core.utils.RECSObjectIntMap;
import recs.core.utils.RECSObjectMap;

/**
 * The main world class containing all the logic. Add Entities with components and systems to this class and call the process method.
 * 
 * @author Enrico van Oosten
 */
public final class EntityWorld {
    private static EntityDefManager defManager = new EntityDefManager();
    private static int systemIdCounter = 0;

    public static int createEntity(Entity e) {
        Class<? extends Entity> entityClass = e.getClass();
        EntityReflection reflection = entityReflections.get(entityClass);
        if (reflection == null) {
            reflection = addNewEntityReflection(entityClass);
        }
        e.def = reflection.def;

        return getEntityId();
    }

    public static void addComponent(Entity e, Object... components) {
        // if has existing def, get components from it.
        RECSBits componentBits;
        RECSBits systemBits;
        EntityDef def = e.def;
        if (def != null) {
            componentBits = new RECSBits();
            componentBits.copy(def.componentBits);
            for (Object component : components) {
                int componentId = getComponentId(component.getClass());
                componentBits.set(componentId);
            }
            EntityDef newDef = defManager.getDef(componentBits);
            if (newDef != null) {
                e.def = newDef;
                return;
            }
            systemBits = getSystemBits(componentBits);
            // else get components from reflection.
        } else {
            EntityReflection reflection = entityReflections.get(e.getClass());
            componentBits = new RECSBits();
            Keys keys = reflection.componentFields.keys();
            while (keys.hasNext) {
                componentBits.set(keys.next());
            }
            for (Object component : components) {
                componentBits.set(getComponentId(component.getClass()));
            }
            EntityDef newDef = defManager.getDef(componentBits);
            if (newDef != null) {
                e.def = newDef;
                return;
            }
            systemBits = getSystemBits(componentBits);
        }
        def = new EntityDef(componentBits, systemBits);
        defManager.putDef(componentBits, def);
        e.def = def;
    }

    public static void removeComponent(Entity e, Object... components) {
        // TODO:
    }

    private static RECSBits getSystemBits(RECSBits componentBits) {
        System.out.println("componentBits: " + componentBits.toString());

        RECSBits systemBits = new RECSBits();
        for (EntitySystem s : systems) {
            if (s.getComponentBits().contains(componentBits)) {
                System.out.println("getting system: " + s.getClass().getName() + ":" + s.id);
                systemBits.set(s.id);
            }
        }
        return systemBits;
    }

    public static int getSystemId() {
        return ++systemIdCounter;
    }

    /**
     * Process all the systems.
     * 
     * @param deltaInSec
     *            The time passed in seconds since the last update. EntityTaskSystems are updated independantly of this delta.
     */
    public static void process(float deltaInSec) {
        for (EntitySystem system : systems) {
            if (system.isEnabled()) {
                system.processSystem(deltaInSec);
            }
        }
    }

    // /////////////////////////////////////////////ENTITIES/////////////////////////////////////////////
    /**
     * Collection of EntityManagers, managers are retrievable by using the class they represent.
     */
    private static RECSObjectMap<Class<? extends Entity>, EntityReflection> entityReflections = new RECSObjectMap<Class<? extends Entity>, EntityReflection>();
    /**
     * Map of entities, entities are retrievable using their id.
     */
    private static RECSIntMap<Entity> entities = new RECSIntMap<Entity>();
    /**
     * Bitset used to know which id's are used.
     */
    private static RECSBits entityIds = new RECSBits();
    private static int lastUsedId = 0;
    private static int numFreedIds = 0;

    protected static EntityReflection getEntityReflection(Class<? extends Entity> class1) {
        return entityReflections.get(class1);
    }

    /**
     * Add an entity to the world. The fields in the Entity child class should be its components.
     * 
     * @param entity
     *            The entity.
     */
    @SuppressWarnings({ "rawtypes" })
    public static void addEntity(Entity entity) {
        Class<? extends Entity> entityClass = entity.getClass();
        int id = entity.id;

        EntityReflection reflection = entityReflections.get(entityClass);
        Keys k = reflection.componentFields.keys();
        while (k.hasNext) {
            int next = k.next();
            Field field = reflection.componentFields.get(next);
            ComponentManager componentManager = componentManagers.get(next);

            Object contents = null;
            try {
                contents = field.get(entity);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            System.out.println("adding component to manager, entity: " + id + ":" + contents.getClass().getName());
            componentManager.add(id, contents);
        }
        addEntityToSystems(entity);
        entities.put(id, entity);
    }

    /**
     * Remove an entity from the world.
     * 
     * @param entity
     *            The entity.
     */
    public static void removeEntity(Entity entity) {
        numFreedIds++;
        int id = entity.id;
        for (EntitySystem system : systems) {
            system.removeEntity(id);
        }
        for (ComponentManager<?> manager : componentManagers.values()) {
            Object removedComponent = manager.remove(id);
            if (removedComponent != null) {
                ComponentDestructionListener listener = destructionListeners.get(removedComponent.getClass());
                if (listener != null)
                    listener.destroyed(removedComponent);
            }
        }
        entityIds.clear(id);
    }

    /**
     * Remove an entity from the world.
     * 
     * @param id
     *            The id of an entity.
     */
    public static void removeEntity(int id) {
        Entity entity = entities.get(id);
        removeEntity(entity);
    }

    /**
     * Get an unique id for an entity, may reuse id's of removed entities.
     * 
     * @return
     */
    protected static int getEntityId() {
        if (numFreedIds > entityIds.numBits() * 0.2f) {
            lastUsedId = 0;
            numFreedIds = 0;
        }
        while (entityIds.get(++lastUsedId))
            entityIds.set(lastUsedId);
        return lastUsedId;
    }

    protected static void returnEntityId(int id) {
        numFreedIds++;
        entityIds.clear(id);
    }

    /**
     * Create a new EntityManager representing an entity.
     * 
     * @param class1
     *            The entity's class.
     * @return
     */
    @SuppressWarnings("unchecked")
    private static EntityReflection addNewEntityReflection(Class<? extends Entity> class1) {
        System.out.println("creating new reflection: " + class1.getName());
        Class<? extends Entity> mainClass = class1;
        RECSIntMap<Field> fieldMap = new RECSIntMap<Field>();
        RECSBits componentBits = new RECSBits();
        // Iterate all the subclasses.
        while (class1 != Entity.class) {
            // Put every field object in a map with the fields class as key.
            for (Field f : class1.getDeclaredFields()) {
                Class<?> fieldClass = f.getType();
                System.out.println("checking field: " + fieldClass.getName());
                if (componentManagers.containsKey(getComponentId(fieldClass))) {
                    f.setAccessible(true);
                    int componentId = getComponentId(fieldClass);
                    System.out.println("added field: " + fieldClass.getName() + ":" + componentId);

                    componentBits.set(componentId);
                    fieldMap.put(componentId, f);
                }
            }
            class1 = (Class<? extends Entity>) class1.getSuperclass();
        }

        RECSBits systemBits = getSystemBits(componentBits);
        EntityDef def = new EntityDef(componentBits, systemBits);
        defManager.putDef(componentBits, def);

        EntityReflection reflection = new EntityReflection(fieldMap, def);
        entityReflections.put(mainClass, reflection);

        return reflection;
    }

    /**
     * Adds an entity to the systems it can be processed.
     * 
     * @param entityManager
     *            The EntityManager of the same type as the entity.
     * @param entity
     *            The entity.
     */
    private static void addEntityToSystems(Entity entity) {
        RECSBits systemBits = entity.def.systemBits;
        System.out.println("systembits for: " + entity.getClass().getName() + ":" + systemBits.toString());
        for (int i = 0; i < systemBits.numBits(); i++) {
            if (systemBits.get(i)) {
                System.out.println("adding to system: " + entity.getClass().getName());
                systemMap.get(i).addEntity(entity.id);
            }
        }
    }

    // /////////////////////////////////////////SYSTEMS/////////////////////////////////////////////

    /**
     * Linked list of EntitySystems for easy iteration.
     */
    private static LinkedList<EntitySystem> systems = new LinkedList<EntitySystem>();
    private static RECSIntMap<EntitySystem> systemMap = new RECSIntMap<EntitySystem>();

    /**
     * Add a list of systems to the world
     * 
     * @param systems
     */
    public static void addSystem(EntitySystem... systems) {
        for (EntitySystem s : systems)
            addSystem(s);
    }

    /**
     * Add a system to the world.
     * 
     * @param system
     *            The system.
     */
    @SuppressWarnings("unchecked")
    public static void addSystem(EntitySystem system) {
        System.out.println("adding system: " + system.getClass().getName());
        if (systems.contains(system))
            throw new RuntimeException("System already added");
        if (entities.size != 0)
            throw new RuntimeException("Systems must be added before entities");

        Class<? extends EntitySystem> class1 = system.getClass();
        do {
            for (Field field : class1.getDeclaredFields()) {
                // Check for ComponentManager declarations.
                if (field.getType() == ComponentManager.class) {
                    field.setAccessible(true);
                    // Read the type in the <> of componentmanager
                    Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    try {
                        // Set the component manager declaration with the right
                        // component manager.
                        field.set(system, componentManagers.get(getComponentId((Class<?>) type)));
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                // check for EventListener declarations.
                if (field.getType() == EventListener.class) {
                    field.setAccessible(true);
                    // Read the type in the <> of eventListener.
                    Type type = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                    EventListener<?> eventListener = new EventListener<>();
                    registerEventListener(eventListener, (Class<?>) type);

                    try {
                        // Set the event listener declaration with the right
                        // field listener.
                        field.set(system, eventListener);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
            class1 = (Class<? extends EntitySystem>) class1.getSuperclass();
        } while (class1 != EntitySystem.class);
        systems.add(system);
        systemMap.put(system.id, system);
    }

    // /////////////////////////////////////COMPONENTS/////////////////////////////////////////////
    /**
     * Collection of ComponentManagers, managers are retrievable by using the class they represent.
     */
    private static RECSIntMap<ComponentManager<?>> componentManagers = new RECSIntMap<ComponentManager<?>>();
    /**
     * Map with Id's for components.
     */
    private static RECSObjectIntMap<Class<?>> componentIds = new RECSObjectIntMap<Class<?>>();
    private static int componentIdCounter = 0;

    protected static int getComponentId(Class<?> component) {
        return componentIds.get(component, -1);
    }

    /**
     * Register all the component classes that are being used here. { Health.class, Position.class } etc.
     * 
     * @param <T>
     * 
     * @param componentClasses
     *            A list of component classes.
     */
    public static <T> void registerComponents(Class<?>... componentClasses) {
        for (Class<?> component : componentClasses) {
            componentIds.put(component, ++componentIdCounter);
            System.out.println("added component manager: " + component.getName() + ":" + componentIdCounter);
            componentManagers.put(componentIdCounter, new ComponentManager<T>());
        }
    }

    /**
     * Get a component of an entity. Always use a ComponentMapper<T> instead of this to retrieve components.
     * 
     * @param entityId
     *            The id of the entity.
     * @param class1
     *            The type of that component
     * @return The component
     */
    public static <T> T getComponent(int entityId, Class<T> class1) {
        int componentId = componentIds.get(class1, -1);
        if (componentId == -1)
            return null;
        return class1.cast(componentManagers.get(componentId).get(entityId));
    }

    /**
     * Get a manager for a type of component so you can more easily retrieve this type of components from entities.
     * 
     * @param class1
     *            The component's class.
     * @return The component manager, or null if none exists (component not registered).
     */
    @SuppressWarnings("unchecked")
    public static <T> ComponentManager<T> getComponentManager(Class<T> class1) {
        return (ComponentManager<T>) componentManagers.get(componentIds.get(class1, -1));
    }

    // /////////////////////////////////////DESTRUCTION////////////////////////////////////////////
    /**
     * Map of destructionListeners which get notified if a component of their type is destroyed.
     */
    private static RECSObjectMap<Class<?>, ComponentDestructionListener> destructionListeners = new RECSObjectMap<Class<?>, ComponentDestructionListener>();

    public static void registerDestuctionListener(ComponentDestructionListener listener, Class<?> componentClass) {
        destructionListeners.put(componentClass, listener);
    }

    // /////////////////////////////////////EVENTS////////////////////////////////////////////////
    private static EventManager eventManager = new EventManager();

    /**
     * Send a message to all EntitySystems that are registered to the tag of this event.
     * 
     * @param event
     *            The event.
     */
    public static void sendEvent(Object event) {
        eventManager.sendEvent(event);
    }

    /**
     * Register a system so it can receive events with the specified messageTags.
     * 
     * @param system
     *            The entitySystem.
     * @param messageTags
     *            The tags listened to.
     */
    private static void registerEventListener(EventListener<?> listener, Class<?> eventType) {
        eventManager.registerListener(listener, eventType);
    }

    // //////////////////////////////////////TASKS////////////////////////////////////////////////
    private static BlockingThreadPoolExecutor threads = new BlockingThreadPoolExecutor(2, 10);

    protected static void postRunnable(Runnable r) {
        threads.execute(r);
    }

    // //////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Use this to clear everything in the EntityWorld. Use with care.
     */
    public static void reset() {
        componentManagers.clear();
        entityReflections.clear();
        systems.clear();
        systemMap.clear();
        systemIdCounter = 0;
        componentIds.clear();
        componentIdCounter = 0;
        entities.clear();
        entityIds.clear();
        eventManager.clear();
        lastUsedId = 0;
        numFreedIds = 0;
        System.gc();
    }
}
