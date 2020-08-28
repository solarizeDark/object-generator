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
    public static Map<Class<?>, RandomInRange<? extends Number>> numberGenerators;

    private static RandomInRange<Integer> iRandom;
    private static RandomInRange<Double> dRandom;
    private static RandomInRange<Long> lRandom;

    private static Function<Class<?>, String> strGenerator;
    private static Function<Integer, String> strRandomGenerator;
    private static Function<List<?>, ?> strGeneratorFromList;

    private static int cnt = 0;

    static  {
        numberGenerators = new HashMap<>();

        iRandom = (lower, upper) -> lower.intValue()
                    + rand.nextInt(upper.intValue() - lower.intValue());

        dRandom = (lower, upper) -> lower.doubleValue()
                    + (upper.doubleValue() - lower.doubleValue()) * rand.nextDouble();

        lRandom = (lower, upper) -> lower.longValue()
                    + (long) ((upper.longValue() - lower.longValue()) * Math.random());


        strGenerator = clazz -> clazz.getSimpleName() + " obj #" + cnt++;

        strRandomGenerator =
                length -> { Character[] arr = new Character[length];
                            IntStream.range(0, length)
                                      .forEach(x -> arr[x] = (char)(iRandom.apply((int)'a', (int)'z').intValue()));
                            return Arrays.toString(arr);
                };

        strGeneratorFromList = list -> list.get(iRandom.apply(0, list.size()));

        classesToGenerate = Arrays.asList(int.class, double.class, float.class, long.class, String.class);

        numberGenerators.put(Integer.TYPE, iRandom);
        numberGenerators.put(Double.TYPE, dRandom);
        numberGenerators.put(Long.TYPE, lRandom);
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

    public <T> List<T> generateObjects (Class<T> clazz, int amount, Map<String, Object> bounds) {

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
                    Object value;
                    if (type.equals(Integer.TYPE) || type.equals(Double.TYPE) || type.equals(Long.TYPE)) {
                        value = numberGenerators.get(type).apply(((Bound<? extends Number>)bounds.get(field)).lower,
                                ((Bound<? extends Number>)bounds.get(field)).upper);
                    } else {
                        if (bounds.get(field).getClass().equals(Integer.class))
                            value = strRandomGenerator.apply((Integer) bounds.get(field));

                        else if (bounds.get(field).getClass().equals(List.class))
                            value = strGeneratorFromList.apply((List<?>) bounds.get(field));

                        else value = strGenerator.apply((Class<?>) bounds.get(field));
                    }
                    try {
                        setters.get(field).invoke(x, value);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }));

        return res;
    }
}
