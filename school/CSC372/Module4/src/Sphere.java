import java.util.Locale;

/** A ball: every point on its surface sits one radius from the center. */
public final class Sphere extends Shape {

    private final double radius;

    /**
     * Creates a sphere whose radius, and so its measurements, never change.
     *
     * @param radius distance from the center to the surface, greater than zero
     * @throws IllegalArgumentException when the radius is zero, negative, infinite, or not a number
     */
    public Sphere(double radius) {
        this.radius = requireFinitePositive("radius", radius);
    }

    @Override
    public double surface_area() {
        return 4 * Math.PI * radius * radius;
    }

    @Override
    public double volume() {
        return 4 * Math.PI * radius * radius * radius / 3;
    }

    /**
     * Describes the sphere for the driver's printout.
     *
     * @return the radius followed by the surface area and volume
     */
    @Override
    public String toString() {
        return String.format(Locale.ROOT, "Sphere (radius = %.2f): %s", radius, super.toString());
    }
}
