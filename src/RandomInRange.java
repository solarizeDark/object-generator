@FunctionalInterface
public interface RandomInRange<T extends Number> {
    T apply(T l, T u);
}