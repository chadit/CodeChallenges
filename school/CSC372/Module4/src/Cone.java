import java.util.Locale;

/** A right circular cone: a circular base that narrows to a point directly above its center. */
public final class Cone extends Shape {

    private final double radius;
    private final double height;

    /**
     * Creates a cone whose dimensions, and so its measurements, never change.
     *
     * @param radius radius of the circular base, greater than zero
     * @param height distance from the base to the tip, greater than zero
     * @throws IllegalArgumentException when either dimension is zero, negative, infinite, or not a
     *     number
     */
    public Cone(double radius, double height) {
        this.radius = requireFinitePositive("radius", radius);
        this.height = requireFinitePositive("height", height);
    }

    /** {@inheritDoc} Counts the base and the slanted side. */
    @Override
    public double surface_area() {
        double slantHeight = Math.hypot(radius, height);
        return Math.PI * radius * (radius + slantHeight);
    }

    @Override
    public double volume() {
        return Math.PI * radius * radius * height / 3;
    }

    /**
     * Describes the cone for the driver's printout.
     *
     * @return the radius and height followed by the surface area and volume
     */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "Cone (radius = %.2f, height = %.2f): %s",
                radius,
                height,
                super.toString());
    }
}
