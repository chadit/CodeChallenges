import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Checks the shape classes and the ShapeArray driver without a test framework, since the
 * coursework build is plain javac. Run with "make test"; a non-zero exit code means at least one
 * check failed.
 */
public final class ShapeChecks {

    /**
     * How far a computed measurement may sit from the expected one, as a fraction of the expected
     * value. The formulas multiply in a different order than the expectations do, so the last few
     * bits can differ.
     */
    private static final double RELATIVE_TOLERANCE = 1e-12;

    /** Number of failed checks, used for the exit code. */
    private static int failures = 0;

    /** Number of check methods that ended early by throwing, also used for the exit code. */
    private static int aborted = 0;

    /** A check paired with the name the report uses when it throws. */
    private record NamedCheck(String name, Runnable body) {}

    private ShapeChecks() {}

    /**
     * Runs every check and exits non-zero when any fail or throw.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "sphere measurements match the formulas",
                        ShapeChecks::sphereMeasurementsMatchTheFormulas),
                new NamedCheck(
                        "cylinder measurements match the formulas",
                        ShapeChecks::cylinderMeasurementsMatchTheFormulas),
                new NamedCheck(
                        "cone measurements match the formulas",
                        ShapeChecks::coneMeasurementsMatchTheFormulas),
                new NamedCheck(
                        "sphere rejects a radius that is not a finite positive number",
                        ShapeChecks::sphereRejectsARadiusThatIsNotAFinitePositiveNumber),
                new NamedCheck(
                        "cylinder rejects dimensions that are not finite positive numbers",
                        ShapeChecks::cylinderRejectsDimensionsThatAreNotFinitePositiveNumbers),
                new NamedCheck(
                        "cone rejects dimensions that are not finite positive numbers",
                        ShapeChecks::coneRejectsDimensionsThatAreNotFinitePositiveNumbers),
                new NamedCheck(
                        "toString returns the dimensions, surface area, and volume",
                        ShapeChecks::toStringReturnsTheDimensionsSurfaceAreaAndVolume),
                new NamedCheck(
                        "demo main prints one line per shape",
                        ShapeChecks::demoMainPrintsOneLinePerShape));

        for (NamedCheck check : checks) {
            runContained(check);
        }

        if (failures > 0 || aborted > 0) {
            System.out.printf(
                    "%d check(s) failed and %d of %d check method(s) aborted.%n",
                    failures, aborted, checks.size());
            System.exit(1);
        }

        System.out.printf("All checks passed. (%d check methods)%n", checks.size());
    }

    /**
     * Runs one check and keeps a throw from ending the run. Without this a single unexpected
     * exception cancels every later check and the tally never prints.
     *
     * @param check check to run
     */
    private static void runContained(NamedCheck check) {
        try {
            check.body().run();
        } catch (RuntimeException thrown) {
            // Errors are deliberately not caught: a JVM that is already unwell should stop, not
            // keep reporting checks.
            aborted++;
            System.out.println("ABORTED: " + check.name() + " threw " + thrown);
        }
    }

    private static void sphereMeasurementsMatchTheFormulas() {
        Sphere unit = new Sphere(1.0);
        checkClose(4 * Math.PI, unit.surface_area(), "unit sphere surface area is 4 pi");
        checkClose(4 * Math.PI / 3, unit.volume(), "unit sphere volume is 4/3 pi");

        Sphere fractional = new Sphere(2.5);
        checkClose(25 * Math.PI, fractional.surface_area(), "radius 2.5 sphere surface area");
        checkClose(125 * Math.PI / 6, fractional.volume(), "radius 2.5 sphere volume");

        Sphere tiny = new Sphere(0.001);
        checkClose(4e-6 * Math.PI, tiny.surface_area(), "radius 0.001 sphere surface area");
        checkClose(4e-9 * Math.PI / 3, tiny.volume(), "radius 0.001 sphere volume");
    }

    private static void cylinderMeasurementsMatchTheFormulas() {
        Cylinder unit = new Cylinder(1.0, 1.0);
        checkClose(4 * Math.PI, unit.surface_area(), "unit cylinder surface area is 4 pi");
        checkClose(Math.PI, unit.volume(), "unit cylinder volume is pi");

        Cylinder tall = new Cylinder(2.0, 5.0);
        checkClose(28 * Math.PI, tall.surface_area(), "radius 2 height 5 cylinder surface area");
        checkClose(20 * Math.PI, tall.volume(), "radius 2 height 5 cylinder volume");

        Cylinder flat = new Cylinder(10.0, 0.5);
        checkClose(
                210 * Math.PI, flat.surface_area(), "radius 10 height 0.5 cylinder surface area");
        checkClose(50 * Math.PI, flat.volume(), "radius 10 height 0.5 cylinder volume");
    }

    private static void coneMeasurementsMatchTheFormulas() {
        // A 3-4-5 triangle gives a whole-number slant height, so the expectations need no root.
        Cone wholeSlant = new Cone(3.0, 4.0);
        checkClose(24 * Math.PI, wholeSlant.surface_area(), "radius 3 height 4 cone surface area");
        checkClose(12 * Math.PI, wholeSlant.volume(), "radius 3 height 4 cone volume");

        Cone unit = new Cone(1.0, 1.0);
        checkClose(Math.PI * (1 + Math.sqrt(2)), unit.surface_area(), "unit cone surface area");
        checkClose(Math.PI / 3, unit.volume(), "unit cone volume is pi/3");

        Cone tall = new Cone(5.0, 12.0);
        checkClose(90 * Math.PI, tall.surface_area(), "radius 5 height 12 cone surface area");
        checkClose(100 * Math.PI, tall.volume(), "radius 5 height 12 cone volume");
    }

    private static void sphereRejectsARadiusThatIsNotAFinitePositiveNumber() {
        checkRejected(() -> new Sphere(0.0), "radius", "sphere with a zero radius");
        checkRejected(() -> new Sphere(-1.0), "radius", "sphere with a negative radius");
        checkRejected(() -> new Sphere(Double.NaN), "radius", "sphere with a NaN radius");
        checkRejected(
                () -> new Sphere(Double.POSITIVE_INFINITY),
                "radius",
                "sphere with an infinite radius");
    }

    private static void cylinderRejectsDimensionsThatAreNotFinitePositiveNumbers() {
        checkRejected(() -> new Cylinder(0.0, 5.0), "radius", "cylinder with a zero radius");
        checkRejected(() -> new Cylinder(-2.0, 5.0), "radius", "cylinder with a negative radius");
        checkRejected(() -> new Cylinder(Double.NaN, 5.0), "radius", "cylinder with a NaN radius");
        checkRejected(() -> new Cylinder(2.0, 0.0), "height", "cylinder with a zero height");
        checkRejected(() -> new Cylinder(2.0, -5.0), "height", "cylinder with a negative height");
        checkRejected(
                () -> new Cylinder(2.0, Double.POSITIVE_INFINITY),
                "height",
                "cylinder with an infinite height");
    }

    private static void coneRejectsDimensionsThatAreNotFinitePositiveNumbers() {
        checkRejected(() -> new Cone(0.0, 4.0), "radius", "cone with a zero radius");
        checkRejected(() -> new Cone(-3.0, 4.0), "radius", "cone with a negative radius");
        checkRejected(
                () -> new Cone(Double.POSITIVE_INFINITY, 4.0),
                "radius",
                "cone with an infinite radius");
        checkRejected(() -> new Cone(3.0, 0.0), "height", "cone with a zero height");
        checkRejected(() -> new Cone(3.0, -4.0), "height", "cone with a negative height");
        checkRejected(() -> new Cone(3.0, Double.NaN), "height", "cone with a NaN height");
    }

    private static void toStringReturnsTheDimensionsSurfaceAreaAndVolume() {
        checkEquals(
                "Sphere (radius = 1.00): surface area = 12.57, volume = 4.19",
                new Sphere(1.0).toString(),
                "sphere toString");
        checkEquals(
                "Cylinder (radius = 1.50, height = 2.00): surface area = 32.99, volume = 14.14",
                new Cylinder(1.5, 2.0).toString(),
                "cylinder toString");
        checkEquals(
                "Cone (radius = 5.00, height = 12.00): surface area = 282.74, volume = 314.16",
                new Cone(5.0, 12.0).toString(),
                "cone toString");
    }

    private static void demoMainPrintsOneLinePerShape() {
        String printed = captureOutput(() -> ShapeArray.main(new String[0]));

        checkEquals(
                lines(
                        "Sphere (radius = 2.50): surface area = 78.54, volume = 65.45",
                        "Cylinder (radius = 2.00, height = 5.00): surface area = 87.96,"
                                + " volume = 62.83",
                        "Cone (radius = 3.00, height = 4.00): surface area = 75.40,"
                                + " volume = 37.70"),
                printed,
                "demo prints the sphere, the cylinder, then the cone");
    }

    /** Joins lines with the platform separator, matching what println produces. */
    private static String lines(String... text) {
        return String.join(System.lineSeparator(), text) + System.lineSeparator();
    }

    /** Runs an action with System.out redirected and returns what it printed. */
    private static String captureOutput(Runnable action) {
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        PrintStream original = System.out;
        try (PrintStream redirect = new PrintStream(captured, true, StandardCharsets.UTF_8)) {
            System.setOut(redirect);
            action.run();
        } finally {
            System.setOut(original);
        }

        return captured.toString(StandardCharsets.UTF_8);
    }

    /**
     * Passes when the action refuses its input and the error names the dimension at fault. A
     * constructor that checked the right value under the wrong label would fail here.
     */
    private static void checkRejected(Runnable action, String dimension, String description) {
        try {
            action.run();
        } catch (IllegalArgumentException rejected) {
            check(rejected.getMessage().startsWith(dimension + " must be"),
                    description + " is rejected for its " + dimension
                            + " (message \"" + rejected.getMessage() + "\")");
            return;
        }

        check(false, description + " is rejected");
    }

    private static void checkClose(double expected, double actual, String description) {
        check(Math.abs(expected - actual) <= Math.abs(expected) * RELATIVE_TOLERANCE,
                description + " (expected " + expected + ", got " + actual + ")");
    }

    private static void checkEquals(String expected, String actual, String description) {
        check(expected.equals(actual),
                description + " (expected \"" + expected + "\", got \"" + actual + "\")");
    }

    /** Prints PASS or FAIL and counts failures for the exit code. */
    private static void check(boolean condition, String description) {
        if (condition) {
            System.out.println("PASS: " + description);
            return;
        }

        failures++;
        System.out.println("FAIL: " + description);
    }
}
