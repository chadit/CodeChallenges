import java.util.Comparator;

/**
 * Orders students by name in dictionary order, ignoring case, so "alice" comes before "Bob"
 * rather than after every capitalized name. Students with the same name fall back to roll number.
 * Selection sort isn't stable, so without that tiebreak two students with one name could come out
 * in either order.
 */
public final class NameComparator implements Comparator<Student> {

    @Override
    public int compare(Student first, Student second) {
        int byName = String.CASE_INSENSITIVE_ORDER.compare(first.getName(), second.getName());
        if (byName != 0) {
            return byName;
        }

        return Integer.compare(first.getRollno(), second.getRollno());
    }
}
