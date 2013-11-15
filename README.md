Reflection-Entity-Component-System
==================================

An high performance Entity-Component-System with focus on ease of use and minimal programmer overhead.

<br>

	public class UsageExample {
	
		private EntityWorld world;
		
		public UsageExample() {
			world = new EntityWorld();
			
			//Add your systems.
			world.addSystem(new HealthSystem());
			world.addSystem(new MovementSystem());
			
			//Add entities.
			Entity e = new Entity();
			e.addComponent(new Position(50, 42));
			e.addComponent(new Velocity(0, 0));
			world.addEntity(e);
			
			//Or through reflection
			world.addEntity(new Player(4, 6));
			world.addEntity(new Zombie(1, 2));
			
			startGameLoop();
		}
		
		private void loop(float deltaInSec) {
			//Process the systems.
			world.process(deltaInSec);
		}
	}

A component is a class which extends Component with only data, no logic:

	public class Position extends Component {
		public float x;
		public float y;
	
		public Position(float x, float y) {
			this.x = x;
			this.y = y;
		}
	}

Entities can be created through the normal .addComponent(), or through parsing classes which extend Entity.

	public void createSomeEntity(EntityWorld world) {
		Entity e = new Entity();
		e.addComponent(new Health(10, 20);
		e.addComponent(new Position(2, 0);
		e.addComponent(new Velocity());
		world.addEntity(e);
	}

	public class Player extends Entity {
		//Simply define and instantiate the components you use.
		Health health;
		Position position;
		Velocity velocity = new Velocity(2, 1);
	
		public Player(float x, float y) {
			position = new Position(x, y);
			health = new Health(10, 15);
		}
	}
	
	world.addEntity(new Player(5, 42));
	
Creating Entities using the 2nd method, will greatly improve performance through abusing the way java
arranges memory for classes. The components will be kept close together in memory which improves cache hits.

The 2nd method also allows you use normal inheritance programming, and adding methods specific to an entity class.
Though this breaks the Entity-Component paragdim, it is completely possible. Supports multiple layers of inheritance.
<br><br>
	
A system is a class which extends EntitySystem, systems can be added/removed runtime just like entities/components.

	public class MovementSystem extends EntitySystem {
		//Declare component mappers so you can retrieve components easily.
		private ComponentMapper<Position> positionMapper;
		private ComponentMapper<Velocity> velocityMapper;
		
		public MovementSystem() {
			//Define what components an entity requires to let it be processed by this system.
			super(Position.class, Velocity.class);
		}
	
		// Gets called once for every entity that matches the required components
		@Override
		private void processEntity(int id, float deltaSec) {
			//Retrieve components from entities using the component mappers.
			Position position = positionMapper.get(id);
			Velocity velocity = velocityMapper.get(id);
			//Do something with the components.
			position.x += velocity.x * deltaSec;
			position.y += velocity.y * deltaSec;
		}
	}
	
Allows for sending events between system to handle logic.

	public class DamageEvent extends Event {
		public int entityId;
		public int damage;
	
		public DamageEvent(int entityId, int damage) {
			this.entityId = entityId;
			this.damage = damage;
		}
	}

Events can be created easily and are passed to every system with a listener.

	world.sendEvent(new DamageEvent(entityId, 1));
	
Event are received with EventListeners and can be polled at any time.

	public class HealthSystem extends EntitySystem {
		ComponentMapper<Health> healthMapper;
	
		EventListener<DamageEvent> damageListener;
	
		@SuppressWarnings("unchecked")
		public HealthSystem() {
			super(Health.class);
		}
	
		// Process system happens once per world.process(), 
		// it then calls processEntity for every entity contained.
		@Override
		protected void processSystem(float deltaInSec) {
			for(DamageEvent damageEvent: damageListener.pollEvents()) {
				Health health = healthMapper.get(damageEvent.entityId);
				health.health -= damageEvent.damage;
			}
			super.processSystem(deltaInSec);
		}
	
		@Override
		protected void processEntity(int entityId, float deltaInSec) {
			Health health = healthMapper.get(entityId);
			if (health.health <= 0) {
				world.removeEntity(entityId);
			}
		}
	}

Some convenience classes:

DestructionListeners get notified whenever a component is removed from the world either through
removal of its entity or through .removeComponent()

	new ComponentDestructionListener<SomeComponentWithNativeData>(world) {
		@Override
		public void destroyed(SomeComponentWithNativeData component) {
			component.dispose();
		}
	};
	
(Random) BinarySerializer can very efficiently and fast, write any Java object to and from a file or byte[].
Could use more intensive testing, but didnt fail to parse a single thing so far. Good lightweight option for networking.

	byte[] data = BinarySerializer.saveToByteArr(someBigObjectWithLotsOfStuff);
	SomeBigObject loadedObject = BinarySerializer.readFromByteArr(data, new SomeBigObject()); //just need an instance
	assert someBigObjectWithLotsOfStuff.equals(loadedObject)
