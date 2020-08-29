public class Bound<T extends Number> {
    public T upper;
    public T lower;

    public Bound(T l, T u) {
        lower = l;
        upper = u;
    }
}
