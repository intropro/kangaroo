package com.conductor.core.diff;

import java.util.Comparator;
import java.util.List;

import com.google.common.base.Preconditions;

/**
 * A Utility class for producing diffs of {@link List}s.
 * <p/>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
public final class ListDiffUtil {
    private ListDiffUtil() {
    }

    public static <T> DiffFacts<T> getDiff(final DiffRequest<T> request) {
        final DiffProducer<T> differ = getDiffProducer(request);
        return differ.diff(request.getOriginal(), request.getEdited(), getComparator(request));
    }

    private static <T> DiffProducer<T> getDiffProducer(final DiffRequest<T> request) {
        if (request.isDataPresorted()) {
            return new StreamingDiffProducer<T>();
        } else {
            return new MyersDiffProducer<T>(request.getLimit());
        }
    }

    private static <T> Comparator<T> getComparator(final DiffRequest<T> request) {
        final Comparator<T> supplied = request.getComparator();
        if (supplied == null) {
            Preconditions.checkArgument(Comparable.class.isAssignableFrom(request.getType()),
                    "If no comparator is supplied, then list items must implement Comparable.");
            return new Comparator<T>() {
                @Override
                public int compare(final T o1, final T o2) {
                    // this will be alright because of the check above.
                    return ((Comparable<T>) o1).compareTo(o2);
                }
            };
        }
        return supplied;
    }
}
