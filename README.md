Reflection-Entity-Component-System
==================================

A tiny entity component system which is high performance and quick/easy to use.

Libary .jar:

https://dl.dropboxusercontent.com/u/18555381/Permanent/reflectionecs.jar
<br>

	public class UsageExample {
		public static void main(String[] args) {
			//Register what component classes to use
			EntityWorld.registerComponents({ Health.class, Position.class, Velocity.class });
			
			//Add your systems.
			EntityWorld.addSystem(new HealthSystem());
			EntityWorld.addSystem(new MovementSystem());
			
			//Create entities.
			EntityWorld.createEntity(new Player(4, 6));
			EntityWorld.createEntity(new Player(12, 9));
			EntityWorld.createEntity(new Zombie(1, 2));
			
			startGameLoop();
		}
		
		private void loop(float deltaInSec) {
			//Process the systems.
			EntityWorld.process(deltaInSec);
		}
	}

<br>
A component is a class with only data, no logic:

	public class Position {
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
		public MovementSystem() {
			//Define what components an entity requires to let it be processed
			//by this system.
			super(Position.class, Velocity.class);
		}
	
		@Override
		protected void processEntities(IntArray entities, float deltaInSec) {
			//process each of the entityId's in the IntArray
			for(int i = 0; i < entities.size; i++) {
				processEntity(entities.items[i], deltaInSec);
			}
		}
	
		private void processEntity(int entityId, float deltaInSec) {
			//Retrieve components from entities with getComponent
			Position position = EntityWorld.getComponent(entityId, Position.class);
			Velocity velocity = EntityWorld.getComponent(entityId, Velocity.class);
			//Do something with the components.
			position.x += velocity.x * deltaInSec;
			position.y += velocity.y * deltaInSec;
		}
	}
