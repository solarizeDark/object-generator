import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class Main {

    private static Random rand = new Random();

    private static IntFunction<IntFunction<Integer>> intG =
            lower -> upper -> lower + rand.nextInt(upper - lower);

    private static DoubleFunction<DoubleFunction<Double>> doubleG =
            lower -> upper -> lower + (upper - lower) * rand.nextDouble();

    private static LongFunction<LongFunction<Long>> longG =
            lower -> upper -> lower + (long) ((upper - lower) * Math.random());

    private static List<Class<?>> classes =
            Arrays.asList(int.class, double.class, float.class, long.class);

    private static Map<String, Method> getSetters(Class<?> type, Map<String, Class<?>> fields) {
        Map<String, Method> setters = new HashMap<>();
        fields.forEach((key, value) -> {
            try {
                setters.put(key, type.getMethod(
                        "set" + key.substring(0, 1).toUpperCase() + key.substring(1),
                        value));
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException();
            }
        });
        return setters;
    }

    private static Number generateNumber(Class<?> clazz, Bound<?> bound) {
        if (double.class.equals(clazz)) {

            return doubleG.apply(bound.lower.doubleValue())
                            .apply(bound.upper.doubleValue());

        } else if (int.class.equals(clazz)) {

            return intG.apply(bound.lower.intValue())
                    .apply(bound.upper.intValue());

        } else if (long.class.equals(clazz)) {

            return longG.apply(bound.lower.longValue())
                    .apply(bound.upper.longValue());

        }
        return 0;
    }

    private static <T> List<T> generateObjects (Class<T> clazz, int amount, Map<String, Bound<?>> bounds) {

        Field[] fieldsPerson = clazz.getDeclaredFields();
        Map<String, Class<?>> fieldsToGenerate =
                Arrays.stream(fieldsPerson).filter(x -> classes.contains(x.getType()))
                                           .collect(Collectors.toMap(Field::getName, Field::getType));

        List<T> res = new ArrayList<>();
        LongStream.range(0, amount).forEach(x -> {
            try {
                res.add(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        Map<String, Method> setters = getSetters(clazz, fieldsToGenerate);

        res.forEach(x -> setters.keySet()
                            .forEach(field -> {
                                Class<?> type = setters.get(field).getParameterTypes()[0];
                                Number number = generateNumber(type, bounds.get(field));
                                try {
                                    setters.get(field).invoke(x, number);
                                } catch (IllegalAccessException | InvocationTargetException e) {
                                    e.printStackTrace();
                                }
                            }));

        return res;
    }

    public static void main(String[] args) {
        Map<String, Bound<?>> foo = new HashMap<>();
        foo.put("loo", new Bound<>(1000L, 2000L));
        foo.put("doo", new Bound<>(100.5, 120.5));
        foo.put("age", new Bound<>(18, 25));

        System.out.println(longG.apply(1000).apply(2000));

        List<Person> pl = generateObjects(Person.class, 10, foo);
        int a = 5;
    }
}
