@FunctionalInterface
public interface RandomInRange<T extends Number> {
    T apply(Number l, Number u);
}