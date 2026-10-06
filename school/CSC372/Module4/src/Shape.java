import java.util.Locale;

/**
 * A solid that can measure itself. Each subclass supplies the formulas for its own geometry, so
 * code holding a Shape reports measurements without knowing which solid it has.
 *
 * <p>Designed for extension. A subclass implements {@link #surface_area} and {@link #volume}, and
 * passes each dimension through {@link #requireFinitePositive} before storing it. Its toString
 * puts its own dimensions in front of the text {@link #toString} returns here.
 */
public abstract class Shape {

    /**
     * Computes the total outer area. The snake_case name is the one the assignment specifies.
     *
     * @return area in square units
     */
    public abstract double surface_area();

    /**
     * Computes how much space the solid encloses.
     *
     * @return volume in cubic units
     */
    public abstract double volume();

    /**
     * Formats both measurements in one place, so every subclass reports them the same way.
     *
     * @return the surface area and volume, each rounded to two decimal places
     */
    @Override
    public String toString() {
        // Locale.ROOT keeps the decimal point a period on every machine.
        return String.format(
                Locale.ROOT, "surface area = %.2f, volume = %.2f", surface_area(), volume());
    }

    /**
     * Checks a dimension before a subclass stores it, so every stored dimension is a finite number
     * above zero.
     *
     * @param name dimension's name, used in the error message
     * @param value length to check
     * @return the value, unchanged
     * @throws IllegalArgumentException when the value is zero, negative, infinite, or not a number
     */
    protected static double requireFinitePositive(String name, double value) {
        if (!Double.isFinite(value) || value <= 0) {
            throw new IllegalArgumentException(
                    name + " must be a finite number greater than zero, got " + value);
        }

        return value;
    }
}
