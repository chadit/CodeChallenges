/**
 * One student's record: a roll number, a name, and an address. The fields are checked when the
 * student is created and never change afterward, so a sorted list can't drift out of order because
 * a student in it was edited.
 */
public final class Student {

    /**
     * Column widths shared by every row and the header, so the two can't drift apart. A name of 20
     * or more characters runs into the address column, which none of the sample names do.
     */
    private static final String COLUMNS = "%-8s%-20s%s";

    /** The column titles, lined up over the rows toString returns. */
    static final String HEADER = String.format(COLUMNS, "Roll No", "Name", "Address");

    private final int rollno;
    private final String name;
    private final String address;

    /**
     * Creates a student whose fields never change.
     *
     * @param rollno roll number, greater than zero
     * @param name student's name; surrounding spaces are dropped
     * @param address mailing address; surrounding spaces are dropped
     * @throws IllegalArgumentException when the roll number is zero or negative, or the name or
     *     address is null or blank
     */
    public Student(int rollno, String name, String address) {
        if (rollno <= 0) {
            throw new IllegalArgumentException(
                    "rollno must be greater than zero, got " + rollno);
        }

        this.rollno = rollno;
        this.name = requireText("name", name);
        this.address = requireText("address", address);
    }

    /** @return the roll number, always greater than zero */
    public int getRollno() {
        return rollno;
    }

    /** @return the name, trimmed and never blank */
    public String getName() {
        return name;
    }

    /** @return the address, trimmed and never blank */
    public String getAddress() {
        return address;
    }

    /**
     * Formats the student as one row of the driver's table.
     *
     * @return the roll number, name, and address in the columns {@link #HEADER} titles
     */
    @Override
    public String toString() {
        return String.format(COLUMNS, rollno, name, address);
    }

    /**
     * Trims a text field after checking it holds something, so the error names the field at fault.
     */
    private static String requireText(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be null or blank");
        }

        return value.trim();
    }
}
