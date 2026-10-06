import java.util.Locale;

/** A right circular cylinder: two equal circular ends joined by a straight side. */
public final class Cylinder extends Shape {

    private final double radius;
    private final double height;

    /**
     * Creates a cylinder whose dimensions, and so its measurements, never change.
     *
     * @param radius radius of each circular end, greater than zero
     * @param height distance between the two ends, greater than zero
     * @throws IllegalArgumentException when either dimension is zero, negative, infinite, or not a
     *     number
     */
    public Cylinder(double radius, double height) {
        this.radius = requireFinitePositive("radius", radius);
        this.height = requireFinitePositive("height", height);
    }

    /** {@inheritDoc} Counts both ends and the side. */
    @Override
    public double surface_area() {
        return 2 * Math.PI * radius * (radius + height);
    }

    @Override
    public double volume() {
        return Math.PI * radius * radius * height;
    }

    /**
     * Describes the cylinder for the driver's printout.
     *
     * @return the radius and height followed by the surface area and volume
     */
    @Override
    public String toString() {
        return String.format(
                Locale.ROOT,
                "Cylinder (radius = %.2f, height = %.2f): %s",
                radius,
                height,
                super.toString());
    }
}
