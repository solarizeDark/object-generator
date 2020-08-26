import java.util.*;

public class Main {

    public static void main(String[] args) {
        ObjectsGenerator generator = new ObjectsGenerator();

        Map<String, Bound<?>> foo = new HashMap<>();
        foo.put("loo", new Bound<>(1000L, 2000L));
        foo.put("doo", new Bound<>(100.5, 120.5));
        foo.put("age", new Bound<>(18, 25));

        List<Person> pl = generator.generateObjects(Person.class, 10, foo);
        int a = 5;
    }
}
