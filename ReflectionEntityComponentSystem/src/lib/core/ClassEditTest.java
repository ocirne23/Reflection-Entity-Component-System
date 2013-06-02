package lib.core;

import java.lang.reflect.Field;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import lib.test.components.Attack;
import lib.test.entities.Player;

public class ClassEditTest {
	public static void main(String[] args) throws NotFoundException {
		Player player = new Player(1, 2);
		Attack attack = new Attack(1);

		try {
			Class<?> da = addFieldToClass(player.getClass(), attack.getClass());
			Object playerWithAttack =  da.newInstance();
		} catch (CannotCompileException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

	private static Class<?> addFieldToClass(Class<?> class1, Class<?> fieldClass) throws NotFoundException, CannotCompileException, ClassNotFoundException {
		ClassPool pool = ClassPool.getDefault();

		CtClass superClass = pool.get(class1.getName());
		CtClass newClass = pool.makeClass(class1.getSimpleName() + "_" + fieldClass.getSimpleName());

		for(CtField f: superClass.getFields()) {
			if(f.getName().equals("id")) continue;
			CtField newField = new CtField(f, newClass);
			newClass.addField(newField);
		}

		//newClass.addConstructor(new CtConstructor(new CtClass[0], newClass));

		newClass.addField(new CtField(pool.get(fieldClass.getName()), fieldClass.getSimpleName().toLowerCase(), newClass));

		for(CtConstructor ctc: newClass.getConstructors()) {
			System.out.println(ctc.toString());
		}


		Class<?> c = newClass.toClass();

		for(Field field: c.getDeclaredFields()) {
			System.out.println(field.getName());
		}

		return c;
	}

	private static void setField(Object entity, Object field) {
		for(Field f: entity.getClass().getFields()) {
			if(f.getType() == field.getClass()) {
				try {
					f.setAccessible(true);
					f.set(entity, field);
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
				System.out.println("set");
				return;
			}
		}
	}
}
