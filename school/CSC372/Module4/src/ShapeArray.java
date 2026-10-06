/**
 * Builds one of each shape and stores them in one array typed as the abstract class. The loop
 * prints each element without naming a subclass, so every element answers through Shape.
 */
public final class ShapeArray {

    private ShapeArray() {}

    /**
     * Runs the demonstration.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        Shape[] shapeArray = {
            new Sphere(2.5),
            new Cylinder(2.0, 5.0),
            new Cone(3.0, 4.0),
        };

        for (Shape shape : shapeArray) {
            System.out.println(shape.toString());
        }
    }
}
