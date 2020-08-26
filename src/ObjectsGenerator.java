import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class ObjectsGenerator {

    private static Random rand = new Random();
    private static List<Class<?>> classesToGenerate;
    private static Map<Class<?>, RandomInRange<?>> numberGenerators;
    private static Map<Class<?>, Function<?,?>> stringGenerators;

    private static RandomInRange<Integer> iRandom;
    private static RandomInRange<Double> dRandom;
    private static RandomInRange<Long> lRandom;

    private static Function<Class<?>, Function<Integer, String>> strGenerator;
    private static Function<Integer, String> strRandomGenerator;
    private static Function<List<?>, ?> generatorFromList;

    static  {
        numberGenerators = new HashMap<>();
        stringGenerators = new HashMap<>();

        numberGenerators.put(int.class, iRandom);
        numberGenerators.put(double.class, dRandom);
        numberGenerators.put(long.class, lRandom);

        // fill stringGenerators

        iRandom = (lower, upper) -> lower + rand.nextInt(upper - lower);
        dRandom = (lower, upper) -> lower + (upper - lower) * rand.nextDouble();
        lRandom = (lower, upper) -> lower + (long) ((upper - lower) * Math.random());

        strGenerator = clazz -> number -> clazz.getSimpleName() + " obj #" + number.toString();
        strRandomGenerator =
                length -> { Character[] arr = new Character[length];
                            IntStream.range(0, length)
                                      .forEach(x -> arr[x] = (char)(iRandom.apply((int)'a', (int)'z').intValue()));
                            return Arrays.toString(arr);
                };

        generatorFromList = list -> list.get(iRandom.apply(0, list.size()));

        classesToGenerate = Arrays.asList(int.class, double.class, float.class, long.class, String.class);
    }

    // <FieldName - FieldSetter>
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

    // <FieldName - FieldType>
    private static Map<String, Class<?>> fieldsToGenerate(Class<?> clazz) {
        Field[] fieldsPerson = clazz.getDeclaredFields();
        return Arrays.stream(fieldsPerson).filter(x -> classesToGenerate.contains(x.getType()))
                        .collect(Collectors.toMap(Field::getName, Field::getType));
    }

    public <T> List<T> generateObjects (Class<T> clazz, int amount, Map<String, Bound<?>> bounds) {

        List<T> res = new ArrayList<>();
        LongStream.range(0, amount).forEach(x -> {
            try {
                res.add(clazz.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        });

        Map<String, Method> setters = getSetters(clazz, fieldsToGenerate(clazz));

        res.forEach(x -> setters.keySet()
                .forEach(field -> {
                    Class<?> type = setters.get(field).getParameterTypes()[0];
                    //Evaluate number
                    try {
                        setters.get(field).invoke(x, number);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }));

        return res;
    }
}
