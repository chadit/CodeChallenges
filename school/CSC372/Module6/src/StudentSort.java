import java.util.ArrayList;
import java.util.List;

/**
 * Stores ten students in an ArrayList and prints them three ways: in the order they were added,
 * sorted by name, and sorted by roll number. Both sorts go through SelectionSort.
 */
public final class StudentSort {

    private StudentSort() {}

    /**
     * Runs the demonstration.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        ArrayList<Student> students = students();
        print("Students in the order they were added", students);

        System.out.println();
        SelectionSort.sort(students, new NameComparator());
        print("Students sorted by name", students);

        System.out.println();
        SelectionSort.sort(students, new RollnoComparator());
        print("Students sorted by roll number", students);
    }

    /**
     * Builds the ten students, added in neither name nor roll number order so that each sort has
     * work to do. Dean Foster and DeAndre Wilson only sort correctly when case is ignored, because
     * a capital A sorts before every lowercase letter.
     *
     * @return a new list each call, so a caller can sort it without affecting anyone else
     */
    static ArrayList<Student> students() {
        ArrayList<Student> students = new ArrayList<>();
        students.add(new Student(23, "Maria Lopez", "1420 Pine Street, Denver, CO"));
        students.add(new Student(7, "DeAndre Wilson", "88 Oak Avenue, Austin, TX"));
        students.add(new Student(42, "Ethan Brooks", "305 Maple Drive, Columbus, OH"));
        students.add(new Student(15, "Dean Foster", "712 Cedar Lane, Portland, OR"));
        students.add(new Student(3, "Sophia Nguyen", "49 Birch Road, San Jose, CA"));
        students.add(new Student(31, "Aisha Rahman", "1600 Elm Court, Raleigh, NC"));
        students.add(new Student(19, "Jacob Miller", "220 Walnut Street, Boise, ID"));
        students.add(new Student(11, "Olivia Chen", "931 Spruce Way, Seattle, WA"));
        students.add(new Student(27, "Noah Patel", "57 Willow Bend, Tampa, FL"));
        students.add(new Student(8, "Grace Kim", "1144 Aspen Circle, Fort Collins, CO"));
        return students;
    }

    /**
     * Prints a title, the column header, and one row per student.
     *
     * @param title line printed above the table
     * @param students students to list, in the order given
     */
    static void print(String title, List<Student> students) {
        System.out.println(title);
        System.out.println(Student.HEADER);
        for (Student student : students) {
            System.out.println(student);
        }
    }
}
