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
			world.addEntity(new Player(4, 6));
			world.addEntity(new Player(12, 9));
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

An entity is a class which extends Entity and has components as fields:

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
	
A system is a class which extends EntitySystem:

	public class MovementSystem extends EntitySystem {
		//Declare component managers so you can retrieve components easily.
		private ComponentMapper<Position> positionMapper;
		private ComponentMapper<Velocity> velocityMapper;
		
		public MovementSystem() {
			//Define what components an entity requires to let it be processed by this system.
			super(Position.class, Velocity.class);
		}
	
		@Override
		private void processEntity(int id, float deltaSec) {
			//Retrieve components from entities using the component managers.
			Position position = positionMapper.get(id);
			Velocity velocity = velocityMapper.get(id);
			//Do something with the components.
			position.x += velocity.x * deltaSec;
			position.y += velocity.y * deltaSec;
		}
	}
	
Allows for full inheritance programming (not reccomended, but completely possible, all the components are accessable through the fields).

	public class PlayerWithAttack extends Player {
		Attack attack;
		public PlayerWithAttack(float x, float y) {
			super(x, y);
			attack = new Attack(2);
		}
	}
	
Can dynamically create entities.

	public void createSomeEntity(EntityWorld world) {
		Entity e = new Entity();
		e.addComponent(new Health(10, 20);
		e.addComponent(new Position(2, 0);
		e.addComponent(new Velocity());
		world.addEntity(e);
	}
	
	
Event handling with EventListeners

	public class HealthSystem extends EntitySystem {
		public ComponentMapper<Health> healthMapper;
	
		public EventListener<DamageEvent> damageListener;
	
		@SuppressWarnings("unchecked")
		public HealthSystem() {
			super(Health.class);
		}
	
		@Override
		protected void processSystem(float deltaInSec) {
			for(DamageEvent damageEvent: damageListener.pollEvents()) {
				Health health = healthMapper.get(damageEvent.entityId);
				health.health -= damageEvent.damage;
			}
			super.processSystem(deltaInSec);
		}
	
		@Override
		protected void process(int entityId, float deltaInSec) {
			Health health = healthMapper.get(entityId);
			if (health.health <= 0) {
				EntityWorld.removeEntity(entityId);
			}
		}
	}
	
An event  can be any object.

	public class DamageEvent {
		public int entityId;
		public int damage;
	
		public DamageEvent(int entityId, int damage) {
			this.entityId = entityId;
			this.damage = damage;
		}
	}

Events can be created easily and are passed to every system with a listener.

	world.sendEvent(new DamageEvent(entityId, 1));

	
