package io.chubao.joyqueue.toolkit.reflect;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * Created by hexiaofeng on 16-5-9.
 */
public class ReflectTest {

    @Test
    public void testUpdate() throws NoSuchFieldException, ReflectException {
        Employee employee = new Employee();
        employee.setSalary(1);
        employee.setAge(25);
        employee.weight = 65;

        Field field1 = Person.class.getDeclaredField("age");
        Field field2 = Person.class.getDeclaredField("weight");
        Field field3 = Employee.class.getDeclaredField("salary");
        Reflect.set(field1, employee, 26);
        Reflect.set(field2, employee, 66);
        Reflect.set(field3, employee, 2);
        Assert.assertEquals(26, employee.getAge());
        Assert.assertEquals(66, employee.weight, 0);
        Assert.assertEquals(2, employee.getSalary(), 0);
    }


    public static class Person {
        private int age;
        protected double weight;

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }

    public static class Employee extends Person {
        private double salary;

        public double getSalary() {
            return salary;
        }

        public void setSalary(double salary) {
            this.salary = salary;
        }
    }

}
