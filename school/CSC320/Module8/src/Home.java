import java.util.Locale;

/**
 * One home in a builder's inventory, held as a single slot: the constructor and addHome fill it,
 * removeHome empties it, and the update methods change it. Field names are the assignment's
 * attributes in Java camelCase. Every method reports success or failure as a returned message;
 * listHome returns the attribute lines instead.
 */
public class Home {

    /** Sale statuses the assignment allows, in the spelling the class stores. */
    private static final String[] SALE_STATUSES = {"sold", "available", "under contract"};

    /** Largest five-digit ZIP code. */
    private static final int MAX_ZIP_CODE = 99999;

    private int squareFeet;
    private String address;
    private String city;
    private String state;
    private int zipCode;
    private String modelName;
    private String saleStatus;

    /** True while no home is stored; guards every method that needs one. */
    private boolean empty = true;

    /**
     * Creates a home from all seven attributes. A constructor cannot return a message, so a
     * rejected value is reported on the error stream and the home starts empty.
     *
     * @param squareFeet living area, greater than zero
     * @param address street address
     * @param city city name
     * @param state state name or abbreviation
     * @param zipCode five-digit ZIP code
     * @param modelName builder's model name
     * @param saleStatus sold, available, or under contract, in any capitalization
     */
    public Home(
            int squareFeet,
            String address,
            String city,
            String state,
            int zipCode,
            String modelName,
            String saleStatus) {
        try {
            store(squareFeet, address, city, state, zipCode, modelName, saleStatus);
        } catch (IllegalArgumentException invalidHome) {
            System.err.println("Failed to create home: " + invalidHome.getMessage());
        }
    }

    /**
     * Fills an empty slot. Fails when a home is already stored or a value is invalid.
     *
     * @param squareFeet living area, greater than zero
     * @param address street address
     * @param city city name
     * @param state state name or abbreviation
     * @param zipCode five-digit ZIP code
     * @param modelName builder's model name
     * @param saleStatus sold, available, or under contract, in any capitalization
     * @return a success or failure message
     */
    public String addHome(
            int squareFeet,
            String address,
            String city,
            String state,
            int zipCode,
            String modelName,
            String saleStatus) {
        try {
            if (!empty) {
                throw new IllegalStateException("a home is already stored; remove it first");
            }

            store(squareFeet, address, city, state, zipCode, modelName, saleStatus);
            return "Home added: " + this.address;
        } catch (IllegalArgumentException | IllegalStateException problem) {
            return "Failed to add home: " + problem.getMessage();
        }
    }

    /**
     * Clears every attribute. Fails when there is no home to remove.
     *
     * @return a success or failure message
     */
    public String removeHome() {
        try {
            requireStored();

            String removedAddress = address;
            clear();
            return "Home removed: " + removedAddress;
        } catch (IllegalStateException noHome) {
            return "Failed to remove home: " + noHome.getMessage();
        }
    }

    /**
     * Replaces every attribute of the stored home. Fails when no home is stored or a value is
     * invalid.
     *
     * @param squareFeet living area, greater than zero
     * @param address street address
     * @param city city name
     * @param state state name or abbreviation
     * @param zipCode five-digit ZIP code
     * @param modelName builder's model name
     * @param saleStatus sold, available, or under contract, in any capitalization
     * @return a success or failure message
     */
    public String updateHome(
            int squareFeet,
            String address,
            String city,
            String state,
            int zipCode,
            String modelName,
            String saleStatus) {
        try {
            requireStored();

            store(squareFeet, address, city, state, zipCode, modelName, saleStatus);
            return "Home updated: " + this.address;
        } catch (IllegalArgumentException | IllegalStateException problem) {
            return "Failed to update home: " + problem.getMessage();
        }
    }

    /**
     * Changes only the sale status, so a caller need not repeat the attributes that stay the same.
     *
     * @param saleStatus sold, available, or under contract, in any capitalization
     * @return a success or failure message
     */
    public String updateSaleStatus(String saleStatus) {
        try {
            requireStored();

            String previousStatus = this.saleStatus;
            this.saleStatus = normalizeSaleStatus(saleStatus);
            return "Sale status changed from " + previousStatus + " to " + this.saleStatus;
        } catch (IllegalArgumentException | IllegalStateException problem) {
            return "Failed to update sale status: " + problem.getMessage();
        }
    }

    /**
     * Lists the attributes in assignment order.
     *
     * @return one "label: value" line per attribute, or a single failure line when empty
     */
    public String[] listHome() {
        try {
            requireStored();

            return new String[] {
                "Square feet: " + squareFeet,
                "Address: " + address,
                "City: " + city,
                "State: " + state,
                // %05d keeps a leading zero like 02134; Locale.ROOT keeps the digits ASCII.
                "ZIP code: " + String.format(Locale.ROOT, "%05d", zipCode),
                "Model name: " + modelName,
                "Sale status: " + saleStatus
            };
        } catch (IllegalStateException noHome) {
            return new String[] {"Failed to list home: " + noHome.getMessage()};
        }
    }

    /**
     * Validates everything before writing anything, so a rejected home never leaves the slot
     * half-written.
     *
     * @throws IllegalArgumentException naming the first unusable value
     */
    private void store(
            int squareFeet,
            String address,
            String city,
            String state,
            int zipCode,
            String modelName,
            String saleStatus) {
        if (squareFeet <= 0) {
            throw new IllegalArgumentException("square feet must be greater than zero");
        }

        if (zipCode < 1 || zipCode > MAX_ZIP_CODE) {
            throw new IllegalArgumentException("zip code must be between 1 and " + MAX_ZIP_CODE);
        }

        String validAddress = requireText(address, "address");
        String validCity = requireText(city, "city");
        String validState = requireText(state, "state");
        String validModelName = requireText(modelName, "model name");
        String validSaleStatus = normalizeSaleStatus(saleStatus);

        this.squareFeet = squareFeet;
        this.address = validAddress;
        this.city = validCity;
        this.state = validState;
        this.zipCode = zipCode;
        this.modelName = validModelName;
        this.saleStatus = validSaleStatus;
        this.empty = false;
    }

    /** Resets every field so a removed home leaves nothing behind. */
    private void clear() {
        squareFeet = 0;
        address = null;
        city = null;
        state = null;
        zipCode = 0;
        modelName = null;
        saleStatus = null;
        empty = true;
    }

    /** Shared guard for the methods that need a stored home. */
    private void requireStored() {
        if (empty) {
            throw new IllegalStateException("no home is stored");
        }
    }

    /**
     * Strips the text and rejects blanks, so no attribute can be only whitespace.
     *
     * @param text value to check
     * @param label attribute name used in the failure message
     * @return the stripped text
     * @throws IllegalArgumentException when the text is null or only whitespace
     */
    private static String requireText(String text, String label) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }

        // strip(), not trim(): isBlank() above uses Character.isWhitespace, and only strip() shares
        // that definition. trim() stops at U+0020, so it leaves padding that isBlank() already
        // counted as whitespace, and a padded sale status then fails to match an allowed one.
        return text.strip();
    }

    /**
     * Matches the text to an allowed status, ignoring case and surrounding spaces.
     *
     * @param saleStatus text to check
     * @return the matching entry of SALE_STATUSES
     * @throws IllegalArgumentException when the text is blank or matches no allowed status
     */
    private static String normalizeSaleStatus(String saleStatus) {
        String requested = requireText(saleStatus, "sale status");

        for (String allowed : SALE_STATUSES) {
            if (allowed.equalsIgnoreCase(requested)) {
                return allowed;
            }
        }

        throw new IllegalArgumentException(
                "sale status must be one of: " + String.join(", ", SALE_STATUSES));
    }
}
