import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Checks the students, the two comparators, the selection sort, and the StudentSort driver without
 * a test framework, since the coursework build is plain javac. Run with "make test"; a non-zero
 * exit code means at least one check failed.
 */
public final class StudentChecks {

    /** Roll numbers of the sample students in the order StudentSort adds them. */
    private static final List<Integer> ADDED_ROLLNOS = List.of(23, 7, 42, 15, 3, 31, 19, 11, 27, 8);

    /** Roll numbers of the sample students once they are sorted by name. */
    private static final List<Integer> ROLLNOS_BY_NAME =
            List.of(31, 15, 7, 42, 8, 19, 23, 27, 11, 3);

    private static final List<Integer> ROLLNOS_ASCENDING =
            List.of(3, 7, 8, 11, 15, 19, 23, 27, 31, 42);

    private static final List<String> SECTION_TITLES = List.of(
            "Students in the order they were added",
            "Students sorted by name",
            "Students sorted by roll number");

    /** Number of failed checks, used for the exit code. */
    private static int failures = 0;

    /** Number of check methods that ended early by throwing, also used for the exit code. */
    private static int aborted = 0;

    /** A check paired with the name the report uses when it throws. */
    private record NamedCheck(String name, Runnable body) {}

    private StudentChecks() {}

    /**
     * Runs every check and exits non-zero when any fail or throw.
     *
     * @param args command-line arguments, which aren't used
     */
    public static void main(String[] args) {
        List<NamedCheck> checks = List.of(
                new NamedCheck(
                        "selection sort orders students by roll number",
                        StudentChecks::selectionSortOrdersStudentsByRollNumber),
                new NamedCheck(
                        "selection sort orders students by name ignoring case",
                        StudentChecks::selectionSortOrdersStudentsByNameIgnoringCase),
                new NamedCheck(
                        "equal names fall back to roll number",
                        StudentChecks::equalNamesFallBackToRollNumber),
                new NamedCheck(
                        "equal roll numbers keep the order they were added",
                        StudentChecks::equalRollNumbersKeepTheOrderTheyWereAdded),
                new NamedCheck(
                        "sort leaves an empty list and a single-element list alone",
                        StudentChecks::sortLeavesAnEmptyListAndASingleElementListAlone),
                new NamedCheck(
                        "sort handles an already-sorted and a reverse-sorted list",
                        StudentChecks::sortHandlesAnAlreadySortedAndAReverseSortedList),
                new NamedCheck(
                        "sort keeps every element exactly once in the same list",
                        StudentChecks::sortKeepsEveryElementExactlyOnceInTheSameList),
                new NamedCheck(
                        "sort works on integers with a plain comparator",
                        StudentChecks::sortWorksOnIntegersWithAPlainComparator),
                new NamedCheck(
                        "sort rejects a null list or comparator",
                        StudentChecks::sortRejectsANullListOrComparator),
                new NamedCheck(
                        "comparators return zero for one student and flip sign when swapped",
                        StudentChecks::comparatorsReturnZeroForOneStudentAndFlipSignWhenSwapped),
                new NamedCheck(
                        "student rejects a bad roll number, name, or address and trims text",
                        StudentChecks::studentRejectsABadRollNumberNameOrAddressAndTrimsText),
                new NamedCheck(
                        "student toString is one row aligned under the header",
                        StudentChecks::studentToStringIsOneRowAlignedUnderTheHeader),
                new NamedCheck(
                        "students returns ten students in neither sorted order",
                        StudentChecks::studentsReturnsTenStudentsInNeitherSortedOrder),
                new NamedCheck(
                        "main prints three titled sections of ten rows each",
                        StudentChecks::mainPrintsThreeTitledSectionsOfTenRowsEach));

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

    private static void selectionSortOrdersStudentsByRollNumber() {
        ArrayList<Student> students = StudentSort.students();
        SelectionSort.sort(students, new RollnoComparator());

        checkEquals(ROLLNOS_ASCENDING, rollnos(students), "sample students by roll number");
    }

    private static void selectionSortOrdersStudentsByNameIgnoringCase() {
        ArrayList<Student> students = StudentSort.students();
        SelectionSort.sort(students, new NameComparator());

        checkEquals(
                List.of("Aisha Rahman", "Dean Foster", "DeAndre Wilson", "Ethan Brooks",
                        "Grace Kim", "Jacob Miller", "Maria Lopez", "Noah Patel", "Olivia Chen",
                        "Sophia Nguyen"),
                names(students),
                "sample students by name, with Dean before DeAndre");

        // Plain String.compareTo would put "Bob" first, since every capital sorts before "a".
        List<Student> mixedCase = new ArrayList<>(List.of(
                student(1, "carol"), student(2, "Bob"), student(3, "alice")));
        SelectionSort.sort(mixedCase, new NameComparator());
        checkEquals(List.of("alice", "Bob", "carol"), names(mixedCase),
                "lowercase and capitalized names interleave in dictionary order");
    }

    private static void equalNamesFallBackToRollNumber() {
        List<Student> students = new ArrayList<>(List.of(
                student(9, "Alex Morgan"), student(4, "alex morgan"), student(6, "Alex Morgan"),
                student(2, "Zoe Hart")));
        SelectionSort.sort(students, new NameComparator());

        checkEquals(List.of(4, 6, 9, 2), rollnos(students),
                "names equal apart from case are ordered by roll number");
    }

    private static void equalRollNumbersKeepTheOrderTheyWereAdded() {
        // Two different students with one roll number. If indexOfSmallest let an equal item
        // replace its pick, the later one would be swapped ahead of the earlier one.
        Student addedFirst = student(5, "Pat Quinn");
        Student addedSecond = student(5, "Lee Park");
        List<Student> students =
                new ArrayList<>(List.of(addedFirst, addedSecond, student(9, "Ivy Ng")));
        SelectionSort.sort(students, new RollnoComparator());

        check(students.get(0) == addedFirst && students.get(1) == addedSecond,
                "the student added first stays ahead of an equal roll number added later",
                "got " + names(students));
    }

    private static void sortLeavesAnEmptyListAndASingleElementListAlone() {
        // Immutable lists throw on set, so these also prove the sort writes nothing here.
        List<Student> empty = List.of();
        SelectionSort.sort(empty, new RollnoComparator());
        check(empty.isEmpty(), "empty list stays empty");

        Student only = student(5, "Sam Ortiz");
        List<Student> single = List.of(only);
        SelectionSort.sort(single, new NameComparator());
        check(single.size() == 1 && single.get(0) == only, "single-element list keeps its student");
    }

    private static void sortHandlesAnAlreadySortedAndAReverseSortedList() {
        List<Student> sorted = studentsWithRollnos(1, 2, 3, 4, 5);
        SelectionSort.sort(sorted, new RollnoComparator());
        checkEquals(List.of(1, 2, 3, 4, 5), rollnos(sorted), "already sorted list is unchanged");

        List<Student> reversed = studentsWithRollnos(5, 4, 3, 2, 1);
        SelectionSort.sort(reversed, new RollnoComparator());
        checkEquals(List.of(1, 2, 3, 4, 5), rollnos(reversed), "reverse-sorted list is reversed");
    }

    private static void sortKeepsEveryElementExactlyOnceInTheSameList() {
        ArrayList<Student> students = StudentSort.students();
        List<Student> before = List.copyOf(students);
        SelectionSort.sort(students, new NameComparator());

        check(students.size() == before.size(), "sorted list keeps its size");
        boolean eachOnce =
                before.stream().allMatch(original -> timesPresent(students, original) == 1);
        check(eachOnce, "every original student object appears exactly once after sorting");
        checkEquals(ROLLNOS_BY_NAME, rollnos(students),
                "the list passed in is the one reordered, since sort returns nothing");
    }

    private static void sortWorksOnIntegersWithAPlainComparator() {
        List<Integer> numbers = new ArrayList<>(List.of(5, -3, 12, 0, 5, -8, 1));
        SelectionSort.sort(numbers, Integer::compare);
        checkEquals(List.of(-8, -3, 0, 1, 5, 5, 12), numbers,
                "integers with negatives and a duplicate sort ascending");

        SelectionSort.sort(numbers, Comparator.reverseOrder());
        checkEquals(List.of(12, 5, 5, 1, 0, -3, -8), numbers,
                "a reversed comparator sorts the same integers descending");
    }

    private static void sortRejectsANullListOrComparator() {
        checkThrowsNullPointer(() -> SelectionSort.sort(null, new RollnoComparator()),
                "items", "sort rejects a null list");
        checkThrowsNullPointer(() -> SelectionSort.sort(new ArrayList<Student>(), null),
                "order", "sort rejects a null comparator");
    }

    private static void comparatorsReturnZeroForOneStudentAndFlipSignWhenSwapped() {
        Student low = new Student(3, "Zoe Hart", "1 Main Street, Reno, NV");
        Student high = new Student(40, "Abe Lane", "2 Main Street, Reno, NV");

        checkConsistent(new NameComparator(), low, high, "name comparator");
        checkConsistent(new RollnoComparator(), low, high, "roll number comparator");
        check(new RollnoComparator().compare(low, high) < 0, "lower roll number comes first");
        check(new NameComparator().compare(high, low) < 0, "Abe comes before Zoe by name");
    }

    private static void studentRejectsABadRollNumberNameOrAddressAndTrimsText() {
        checkRejected(() -> new Student(0, "Ann Lee", "3 Main Street"), "rollno", "roll number 0");
        checkRejected(() -> new Student(-4, "Ann Lee", "3 Main Street"), "rollno",
                "a negative roll number");
        checkRejected(() -> new Student(1, null, "3 Main Street"), "name", "a null name");
        checkRejected(() -> new Student(1, "   ", "3 Main Street"), "name", "a blank name");
        checkRejected(() -> new Student(1, "Ann Lee", null), "address", "a null address");
        checkRejected(() -> new Student(1, "Ann Lee", ""), "address", "an empty address");

        Student trimmed = new Student(1, "  Ann Lee ", " 3 Main Street, Ames, IA  ");
        checkEquals("Ann Lee", trimmed.getName(), "name is trimmed");
        checkEquals("3 Main Street, Ames, IA", trimmed.getAddress(), "address is trimmed");
        checkEquals(1, trimmed.getRollno(), "roll number is kept");
    }

    private static void studentToStringIsOneRowAlignedUnderTheHeader() {
        Student student = new Student(23, "Maria Lopez", "1420 Pine Street, Denver, CO");
        String row = student.toString();

        checkEquals("23      Maria Lopez         1420 Pine Street, Denver, CO", row,
                "toString row");
        check(!row.contains("\n"), "toString is a single line");
        checkEquals("Roll No Name                Address", Student.HEADER, "header row");
        check(Student.HEADER.indexOf("Name") == row.indexOf("Maria")
                        && Student.HEADER.indexOf("Address") == row.indexOf("1420"),
                "name and address columns start under their titles");
    }

    private static void studentsReturnsTenStudentsInNeitherSortedOrder() {
        ArrayList<Student> students = StudentSort.students();

        checkEquals(10, students.size(), "ten students");
        checkEquals(ADDED_ROLLNOS, rollnos(students), "students are listed in insertion order");
        check(!rollnos(sortedCopy(new RollnoComparator())).equals(rollnos(students)),
                "sorting by roll number changes the order");
        check(!rollnos(sortedCopy(new NameComparator())).equals(rollnos(students)),
                "sorting by name changes the order");
        check(StudentSort.students() != students, "each call builds a new list");
    }

    private static void mainPrintsThreeTitledSectionsOfTenRowsEach() {
        String printed = captureOutput(() -> StudentSort.main(new String[0]));
        String[] sections = printed.split(System.lineSeparator() + System.lineSeparator());

        checkEquals(3, sections.length, "three sections separated by blank lines");
        List<List<Integer>> expectedOrders =
                List.of(ADDED_ROLLNOS, ROLLNOS_BY_NAME, ROLLNOS_ASCENDING);
        for (int i = 0; i < Math.min(sections.length, SECTION_TITLES.size()); i++) {
            checkSection(sections[i], SECTION_TITLES.get(i), expectedOrders.get(i));
        }
    }

    /** Checks one printed section: its title, the header, then ten rows in the expected order. */
    private static void checkSection(String section, String title, List<Integer> rollnos) {
        List<String> lines = section.lines().toList();

        checkEquals(title, lines.get(0), "section title \"" + title + "\"");
        checkEquals(Student.HEADER, lines.get(1), "header under \"" + title + "\"");
        List<String> rows = lines.subList(2, lines.size());
        checkEquals(10, rows.size(), "ten rows under \"" + title + "\"");
        List<Integer> printedRollnos =
                rows.stream().map(row -> Integer.parseInt(row.substring(0, 8).trim())).toList();
        checkEquals(rollnos, printedRollnos, "row order under \"" + title + "\"");
    }

    private static List<Student> sortedCopy(Comparator<Student> order) {
        List<Student> copy = StudentSort.students();
        SelectionSort.sort(copy, order);
        return copy;
    }

    private static Student student(int rollno, String name) {
        return new Student(rollno, name, rollno + " Test Street, Ames, IA");
    }

    private static List<Student> studentsWithRollnos(Integer... rollnos) {
        return new ArrayList<>(Arrays.stream(rollnos)
                .map(rollno -> student(rollno, "Student " + rollno))
                .toList());
    }

    private static List<Integer> rollnos(List<Student> students) {
        return students.stream().map(Student::getRollno).toList();
    }

    private static List<String> names(List<Student> students) {
        return students.stream().map(Student::getName).toList();
    }

    /** Counts by identity, so two equal-looking students can't stand in for each other. */
    private static long timesPresent(List<Student> students, Student wanted) {
        return students.stream().filter(student -> student == wanted).count();
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
     * Checks the comparator contract on two different students: a student equals itself, and
     * swapping the arguments flips the sign of a non-zero answer.
     */
    private static void checkConsistent(
            Comparator<Student> order, Student first, Student second, String description) {
        checkEquals(0, order.compare(first, first), description + " returns zero for one student");
        int forward = Integer.signum(order.compare(first, second));
        int backward = Integer.signum(order.compare(second, first));
        check(forward != 0 && forward == -backward,
                description + " flips sign when the arguments swap",
                "got " + forward + " and " + backward);
    }

    /**
     * Passes when the action refuses its input and the error names the field at fault. A
     * constructor that checked the right value under the wrong label would fail here.
     */
    private static void checkRejected(Runnable action, String field, String description) {
        try {
            action.run();
        } catch (IllegalArgumentException rejected) {
            check(rejected.getMessage().startsWith(field + " must"),
                    description + " is rejected for its " + field,
                    "message \"" + rejected.getMessage() + "\"");
            return;
        }

        check(false, description + " is rejected");
    }

    /**
     * Passes when the action throws NullPointerException naming the argument, so a null list and a
     * null comparator can't pass for each other's reason.
     */
    private static void checkThrowsNullPointer(
            Runnable action, String argument, String description) {
        try {
            action.run();
        } catch (NullPointerException rejected) {
            check(argument.equals(rejected.getMessage()), description + " by naming " + argument,
                    "message \"" + rejected.getMessage() + "\"");
            return;
        }

        check(false, description);
    }

    private static void checkEquals(Object expected, Object actual, String description) {
        check(Objects.equals(expected, actual), description,
                "expected " + expected + ", got " + actual);
    }

    private static void check(boolean condition, String description) {
        check(condition, description, "");
    }

    /**
     * Prints PASS or FAIL and counts failures for the exit code. The detail is shown only on FAIL,
     * because the expected lists and rows are long and would bury the PASS list.
     */
    private static void check(boolean condition, String description, String detail) {
        if (condition) {
            System.out.println("PASS: " + description);
            return;
        }

        failures++;
        System.out.println("FAIL: " + description + (detail.isEmpty() ? "" : " (" + detail + ")"));
    }
}
