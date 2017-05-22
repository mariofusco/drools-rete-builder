package org.drools.retebuilder.benchmarks;

import java.util.List;

public class Person {
    private String name;
    private int age;

    private String town;

    private List<Person> parents;

    public Person() { }

    public Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    public Person(String name, int age, String town) {
        this.name = name;
        this.age = age;
        this.town = town;
    }

    public String getTown() {
        return town;
    }

    public String getName() {
        return name;
    }

    public String getNameWithAge() {
        return name + " (" + age + ")";
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public List<Person> getParents() {
        return parents;
    }

    public Person setParents(List<Person> parents) {
        this.parents = parents;
        return this;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Person person = (Person) o;
        return age == person.age && name.equals(person.name);

    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + age;
        return result;
    }
}
