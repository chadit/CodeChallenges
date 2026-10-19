import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * A hand-written selection sort, kept in its own file as the assignment asks instead of calling a
 * sort from the collections library. Each pass finds the smallest item in the unsorted rest of the
 * list and swaps it into the next position, so the sorted part grows by one item per pass.
 */
public final class SelectionSort {

    private SelectionSort() {}

    /**
     * Reorders the list in place into the order the comparator defines.
     *
     * @param items list to reorder; it must support set, as ArrayList does, unless it has fewer
     *     than two items
     * @param order decides which of two items comes first
     * @param <T> type of the items
     * @throws NullPointerException when the list or the comparator is null
     */
    public static <T> void sort(List<T> items, Comparator<? super T> order) {
        Objects.requireNonNull(items, "items");
        Objects.requireNonNull(order, "order");

        for (int i = 0; i < items.size() - 1; i++) {
            int smallest = indexOfSmallest(items, order, i);
            if (smallest != i) {
                T displaced = items.get(i);
                items.set(i, items.get(smallest));
                items.set(smallest, displaced);
            }
        }
    }

    /**
     * Finds the smallest item from the given position to the end. Only a strictly smaller item
     * replaces the current pick, so among equal items the first one wins.
     */
    private static <T> int indexOfSmallest(List<T> items, Comparator<? super T> order, int from) {
        int smallest = from;
        for (int j = from + 1; j < items.size(); j++) {
            if (order.compare(items.get(j), items.get(smallest)) < 0) {
                smallest = j;
            }
        }

        return smallest;
    }
}
