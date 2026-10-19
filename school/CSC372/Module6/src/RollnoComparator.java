import java.util.Comparator;

/** Orders students from the lowest roll number to the highest. */
public final class RollnoComparator implements Comparator<Student> {

    /**
     * Compares with Integer.compare rather than subtracting, because a difference of two large
     * roll numbers can overflow and flip the sign.
     */
    @Override
    public int compare(Student first, Student second) {
        return Integer.compare(first.getRollno(), second.getRollno());
    }
}
