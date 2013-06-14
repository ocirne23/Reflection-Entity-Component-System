package recs.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import recs.core.ComponentDestructionListener;
import recs.core.Entity;
import recs.core.EntitySystem;
import recs.core.EntityWorld;
import recs.core.utils.Saver;
import recs.test.components.Attack;
import recs.test.components.Gravity;
import recs.test.components.Health;
import recs.test.components.Position;
import recs.test.components.Velocity;
import recs.test.components.extras.CopyOfAttack;
import recs.test.components.extras.CopyOfGravity;
import recs.test.components.extras.CopyOfHealth;
import recs.test.components.extras.CopyOfPosition;
import recs.test.components.extras.CopyOfVelocity;
import recs.test.components.extras.Copy_2_of_Attack;
import recs.test.components.extras.Copy_2_of_Gravity;
import recs.test.components.extras.Copy_2_of_Health;
import recs.test.components.extras.Copy_2_of_Position;
import recs.test.components.extras.Copy_2_of_Velocity;
import recs.test.components.extras.Copy_3_of_Attack;
import recs.test.components.extras.Copy_3_of_Gravity;
import recs.test.components.extras.Copy_3_of_Health;
import recs.test.components.extras.Copy_3_of_Position;
import recs.test.components.extras.Copy_3_of_Velocity;
import recs.test.components.extras.Copy_4_of_Attack;
import recs.test.components.extras.Copy_4_of_Gravity;
import recs.test.components.extras.Copy_4_of_Health;
import recs.test.components.extras.Copy_4_of_Position;
import recs.test.components.extras.Copy_4_of_Velocity;
import recs.test.components.extras.Copy_5_of_Attack;
import recs.test.components.extras.Copy_5_of_Gravity;
import recs.test.components.extras.Copy_5_of_Health;
import recs.test.components.extras.Copy_5_of_Position;
import recs.test.components.extras.Copy_5_of_Velocity;
import recs.test.components.extras.Copy_6_of_Attack;
import recs.test.components.extras.Copy_6_of_Gravity;
import recs.test.components.extras.Copy_6_of_Health;
import recs.test.components.extras.Copy_6_of_Position;
import recs.test.components.extras.Copy_6_of_Velocity;
import recs.test.components.extras.Copy_7_of_Attack;
import recs.test.components.extras.Copy_7_of_Gravity;
import recs.test.components.extras.Copy_7_of_Health;
import recs.test.components.extras.Copy_7_of_Position;
import recs.test.components.extras.Copy_7_of_Velocity;
import recs.test.entities.Player;
import recs.test.entities.PlayerWithAttack;
import recs.test.entities.Zombie;
import recs.test.events.DamageEvent;
import recs.test.systems.AttackSystem;
import recs.test.systems.HealthSystem;
import recs.test.systems.MovementSystem;
import recs.test.systems.ThreadedMovementSystem;

public class TestAllTehThings {

    private static final Class<?>[] COMPONENTS = { Health.class, Position.class, Velocity.class, Attack.class, Gravity.class };
    private static final Class<?>[] COMPONENTS1 = { CopyOfHealth.class, CopyOfPosition.class, CopyOfVelocity.class, CopyOfAttack.class, CopyOfGravity.class };
    private static final Class<?>[] COMPONENTS2 = { Copy_2_of_Health.class, Copy_2_of_Position.class, Copy_2_of_Velocity.class, Copy_2_of_Attack.class, Copy_2_of_Gravity.class };
    private static final Class<?>[] COMPONENTS3 = { Copy_3_of_Health.class, Copy_3_of_Position.class, Copy_3_of_Velocity.class, Copy_3_of_Attack.class, Copy_3_of_Gravity.class };
    private static final Class<?>[] COMPONENTS4 = { Copy_4_of_Health.class, Copy_4_of_Position.class, Copy_4_of_Velocity.class, Copy_4_of_Attack.class, Copy_4_of_Gravity.class };
    private static final Class<?>[] COMPONENTS5 = { Copy_5_of_Health.class, Copy_5_of_Position.class, Copy_5_of_Velocity.class, Copy_5_of_Attack.class, Copy_5_of_Gravity.class };
    private static final Class<?>[] COMPONENTS6 = { Copy_6_of_Health.class, Copy_6_of_Position.class, Copy_6_of_Velocity.class, Copy_6_of_Attack.class, Copy_6_of_Gravity.class };
    private static final Class<?>[] COMPONENTS7 = { Copy_7_of_Health.class, Copy_7_of_Position.class, Copy_7_of_Velocity.class, Copy_7_of_Attack.class, Copy_7_of_Gravity.class };

    private Player player;
    private Player player2;
    private PlayerWithAttack playerWithAttack;
    private Zombie zombie;

    private MovementSystem ms;
    private ThreadedMovementSystem tms;
    private HealthSystem hs;
    private AttackSystem as;

    private EntityWorld world;

    @Before
    public void setup() {
        world = new EntityWorld();
        System.out.println("starting test");
        world.registerComponents(COMPONENTS);

        ms = new MovementSystem();
        world.addSystem(ms);

        tms = new ThreadedMovementSystem();
        tms.setEnabled(false);
        world.addSystem(tms);

        hs = new HealthSystem();
        world.addSystem(hs);

        as = new AttackSystem();
        world.addSystem(as);
        as.setEnabled(false);
    }

    private void addEntities() {
        player = new Player(4, 6);
        player2 = new Player(12, 9);
        playerWithAttack = new PlayerWithAttack(6, 11);
        zombie = new Zombie(1, 2);
        world.addEntity(player);
        world.addEntity(player2);
        world.addEntity(playerWithAttack);
        world.addEntity(zombie);
    }

    @Test
    public void testId() {
        addEntities();
        assertTrue(player.getId() == 1);
        assertTrue(player2.getId() == 2);
        assertTrue(playerWithAttack.getId() == 3);
        assertTrue(zombie.getId() == 4);
    }

    @Test
    public void testGetComponent() {
        addEntities();
        int playerId = player.getId();
        Position position = world.getComponent(playerId, Position.class);
        Velocity velocity = world.getComponent(playerId, Velocity.class);
        Health health = world.getComponent(playerId, Health.class);

        assertTrue(position.x == 4f && position.y == 6f);
        assertTrue(velocity.x == 2f && velocity.y == 1f);
        assertTrue(health.amount == 10 && health.max == 15);

        // player2
        int player2Id = player2.getId();
        Position position2 = world.getComponent(player2Id, Position.class);
        assertTrue(position2.x == 12 && position2.y == 9);

        // zombie
        int zombieId = zombie.getId();
        Health healthNull = world.getComponent(zombieId, Health.class);
        assertTrue(healthNull == null);
    }

    @Test
    public void testMovementSystem() {
        addEntities();
        int playerId = player.getId();
        final float deltaInSec1 = 2f;
        final float deltaInSec2 = 1.5f;
        // player
        Position position = world.getComponent(playerId, Position.class);
        Velocity velocity = world.getComponent(playerId, Velocity.class);

        assertTrue(position != null);
        assertTrue(velocity != null);
        assertTrue(ms.hasEntity(playerId));

        float startX = position.x;
        float startY = position.y;
        float xSpeed = velocity.x;
        float ySpeed = velocity.y;
        float expectedX = startX + xSpeed * deltaInSec1;
        float expectedY = startY + ySpeed * deltaInSec1;

        world.process(deltaInSec1);
        assertTrue(position.x == expectedX && position.y == expectedY);

        startX = expectedX;
        startY = expectedY;
        expectedX = startX + xSpeed * deltaInSec2;
        expectedY = startY + ySpeed * deltaInSec2;

        world.process(deltaInSec2);
        assertTrue(position.x == expectedX && position.y == expectedY);
    }

    @Test
    public void testInheritance() {
        addEntities();
        int playerWithAttackId = playerWithAttack.getId();
        Attack attack = world.getComponent(playerWithAttackId, Attack.class);
        assertTrue(attack != null);

        Position position = world.getComponent(playerWithAttackId, Position.class);
        assertTrue(position != null);
    }

    @Test
    public void testThreadedSystem() {
        addEntities();
        ms.setEnabled(false);
        tms.setEnabled(true);
        Position position = world.getComponent(player.getId(), Position.class);
        Velocity velocity = world.getComponent(player.getId(), Velocity.class);
        float startX = position.x;
        float startY = position.y;
        try {
            // First iteration does not process threaded systems.
            world.process(0);
            Thread.sleep(100);
            world.process(0);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertTrue(velocity.x != 0 || velocity.y != 0);
        assertTrue(startX != position.x || startY != position.y);
    }

    @Test
    public void testThreadPool() {
        addEntities();

        ms.setEnabled(false);
        tms.setEnabled(true);

        try {
            for (int i = 0; i < 10000; i++) {
                world.process(0f);
            }
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue(false);
        }
        assertTrue(true);
    }

    @Test
    public void testEvents() {
        addEntities();

        Health health = world.getComponent(player.getId(), Health.class);
        int currentHealth = health.amount;

        world.sendEvent(new DamageEvent(player.getId(), 2));
        world.process(1f);

        assertTrue(health.amount == currentHealth - 2);
    }

    @Test
    public void testDyamicComponentAdd() {
        addEntities();
        int player2Id = player2.getId();
        Attack attack = world.getComponent(player2Id, Attack.class);
        assertTrue(attack == null);
        assertFalse(as.hasEntity(player2Id));

        player2.addComponent(new Attack(2), new Gravity(1));
        Attack attack2 = world.getComponent(player2Id, Attack.class);
        assertTrue(attack2 != null);
        assertTrue(as.hasEntity(player2Id));

        Gravity gravity = world.getComponent(player2Id, Gravity.class);
        assertTrue(gravity != null);

        player.addComponent(new Gravity(2), new Attack(3));
    }

    @Test
    public void testDynamicComponentRemove() {
        addEntities();
        int player2Id = player2.getId();
        Position position = world.getComponent(player2Id, Position.class);
        assertTrue(position != null);
        assertTrue(ms.hasEntity(player2Id));

        player2.removeComponent(position);
        Position position2 = world.getComponent(player2Id, Position.class);
        assertTrue(position2 == null);
        assertFalse(ms.hasEntity(player2Id));
    }

    @Test
    public void testComponentAddRemove() {
        addEntities();
        int player2Id = player2.getId();

        Attack attack = world.getComponent(player2Id, Attack.class);
        assertTrue(attack == null);
        assertFalse(as.hasEntity(player2Id));

        player2.addComponent(new Attack(2), new Gravity(1));
        Attack attack2 = world.getComponent(player2Id, Attack.class);
        assertTrue(attack2 != null);
        assertTrue(as.hasEntity(player2Id));

        player2.removeComponent(attack2);
        Attack attack3 = world.getComponent(player2Id, Attack.class);
        assertTrue(attack3 == null);
        assertFalse(as.hasEntity(player2Id));
    }

    @Test
    public void testRemove() {
        addEntities();
        int playerId = player.getId();
        Position position = world.getComponent(playerId, Position.class);
        assertTrue(position != null);

        world.removeEntity(playerId);

        Position position2 = world.getComponent(playerId, Position.class);
        assertTrue(position2 == null);
    }

    @Test
    public void testRemoveEntityWithAddedComponent() {
        addEntities();
        int playerId = player.getId();

        player.addComponent(new Attack(2));
        Attack attack = world.getComponent(playerId, Attack.class);
        assertTrue(attack != null);
        assertTrue(as.hasEntity(playerId));

        world.removeEntity(playerId);

        Attack attack2 = world.getComponent(playerId, Attack.class);
        assertTrue(attack2 == null);
        assertFalse(as.hasEntity(playerId));
    }

    private class MyDestructionListener extends ComponentDestructionListener {
        public boolean destroyed = false;

        public MyDestructionListener() {
            super(world, Position.class);
        }

        @Override
        public void destroyed(Object object) {
            destroyed = true;
        }

    }

    @Test
    public void testDestructionListener() {
        addEntities();
        int playerId = player.getId();
        Position position = world.getComponent(playerId, Position.class);
        assertTrue(position != null);

        MyDestructionListener dl = new MyDestructionListener();

        world.removeEntity(playerId);

        assertTrue(dl.destroyed == true);

        Position position2 = world.getComponent(playerId, Position.class);
        assertTrue(position2 == null);
    }

    @Test
    public void testDynamicEntity() {
        // addEntities();
        Entity e = new Entity();
        e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15));
        world.addEntity(e);

        Position position = world.getComponent(e.getId(), Position.class);
        assertTrue(position != null);

        assertTrue(ms.hasEntity(e.getId()));
        assertTrue(hs.hasEntity(e.getId()));

        Health health = world.getComponent(e.getId(), Health.class);
        e.removeComponent(health);

        Health health2 = world.getComponent(e.getId(), Health.class);
        assertTrue(health2 == null);
        assertFalse(hs.hasEntity(e.getId()));
    }

    @Test
    public void testDynamicEntity2() {
        // addEntities();
        Entity e = new Entity();
        e.addComponent(new Position(1, 2));
        e.addComponent(new Velocity(1, 2));
        world.addEntity(e);

        Position position = world.getComponent(e.getId(), Position.class);
        assertTrue(position != null);
        assertTrue(ms.hasEntity(e.getId()));
    }

    @Test
    public void testLotsOfComponents() {
        addEntities();
        world.registerComponents(COMPONENTS1);
        world.registerComponents(COMPONENTS2);
        world.registerComponents(COMPONENTS3);
        world.registerComponents(COMPONENTS4);
        world.registerComponents(COMPONENTS5);
        world.registerComponents(COMPONENTS6);
        world.registerComponents(COMPONENTS7);

        Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
        Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

        Entity e = new Entity();
        e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f), new CopyOfPosition(1, 2), new CopyOfVelocity(4, 0),
                new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f), new Copy_2_of_Position(1, 2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15),
                new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f), new Copy_3_of_Position(1, 2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(
                        2), new Copy_3_of_Gravity(1f), new Copy_4_of_Position(1, 2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2),
                new Copy_4_of_Gravity(1f), new Copy_5_of_Position(1, 2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f),
                new Copy_6_of_Position(1, 2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f),
                new Copy_7_of_Position(1, 2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);
        world.addEntity(e);

        Copy_7_of_Gravity gravity = world.getComponent(e.getId(), Copy_7_of_Gravity.class);
        assertTrue(gravity != null);

        Copy_5_of_Velocity velocity = world.getComponent(e.getId(), Copy_5_of_Velocity.class);
        assertTrue(velocity != null);
    }

    @Test
    public void testLotsOfComponentAddAfterCreate() {
        addEntities();
        world.registerComponents(COMPONENTS1);
        world.registerComponents(COMPONENTS2);
        world.registerComponents(COMPONENTS3);
        world.registerComponents(COMPONENTS4);
        world.registerComponents(COMPONENTS5);
        world.registerComponents(COMPONENTS6);
        world.registerComponents(COMPONENTS7);

        Copy_7_of_Gravity gravity7 = new Copy_7_of_Gravity(1f);
        Copy_5_of_Velocity velocity5 = new Copy_5_of_Velocity(4, 0);

        Entity e = new Entity();
        world.addEntity(e);

        e.addComponent(new Position(1, 2), new Velocity(4, 0), new Health(10, 15), new Attack(2), new Gravity(1f), new CopyOfPosition(1, 2), new CopyOfVelocity(4, 0),
                new CopyOfHealth(10, 15), new CopyOfAttack(2), new CopyOfGravity(1f), new Copy_2_of_Position(1, 2), new Copy_2_of_Velocity(4, 0), new Copy_2_of_Health(10, 15),
                new Copy_2_of_Attack(2), new Copy_2_of_Gravity(1f), new Copy_3_of_Position(1, 2), new Copy_3_of_Velocity(4, 0), new Copy_3_of_Health(10, 15), new Copy_3_of_Attack(
                        2), new Copy_3_of_Gravity(1f), new Copy_4_of_Position(1, 2), new Copy_4_of_Velocity(4, 0), new Copy_4_of_Health(10, 15), new Copy_4_of_Attack(2),
                new Copy_4_of_Gravity(1f), new Copy_5_of_Position(1, 2), velocity5, new Copy_5_of_Health(10, 15), new Copy_5_of_Attack(2), new Copy_5_of_Gravity(1f),
                new Copy_6_of_Position(1, 2), new Copy_6_of_Velocity(4, 0), new Copy_6_of_Health(10, 15), new Copy_6_of_Attack(2), new Copy_6_of_Gravity(1f),
                new Copy_7_of_Position(1, 2), new Copy_7_of_Velocity(4, 0), new Copy_7_of_Health(10, 15), new Copy_7_of_Attack(2), gravity7);

        Copy_7_of_Gravity gravity = world.getComponent(e.getId(), Copy_7_of_Gravity.class);
        assertTrue(gravity != null);

        Copy_5_of_Velocity velocity = world.getComponent(e.getId(), Copy_5_of_Velocity.class);
        assertTrue(velocity != null);
    }

    @Test
    public void testLotsOfSystems() {
        for (int i = 0; i < 40; i++) {
            world.addSystem(new EntitySystem(Health.class) {
                @Override
                protected void process(int entityId, float deltaInSec) {
                }
            });
        }
        addEntities();
        assertTrue(true);
    }

    @Test
    public void testSaveEntity() {
        Player player = new Player(3, 5);
        float x = player.position.x;
        float y = player.position.y;
        int health = player.health.amount -= 5;

        File playerFile = Saver.saveObject(player, new File("player"));

        Player player2 = Saver.readObject(new Player(), playerFile);

        assertTrue(player2.health.amount == health);
        assertTrue(player2.position.x == x);
        assertTrue(player2.position.y == y);
    }

    @Test
    public void testSaveLotsEntities() {
        final int amount = 10000;
        Entity[] entities = new Entity[amount];
        for (int i = 0; i < amount; i++) {
            entities[i] = new Player(i, i / 2f);
        }
        File entitiesFile = Saver.saveObject(entities, new File("entities"));
    }

    @After
    public void breakDown() {
        world.reset();
        System.out.println("finished test");
    }
}
