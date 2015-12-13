package com.conductor.core.diff;

import java.util.Comparator;
import java.util.List;

/**
 * An interface for a particular diff algorithm, e.g. Myers'.
 * <p/>
 * 2015
 *
 * @param <T>
 *            the type of item in the lists.
 * @author Tyrone Hinderson ༼ つ ◕_◕ ༽つ
 */
interface DiffProducer<T> {

    /**
     * Produce a diff of the given lists.
     *
     * @param original
     *            the original list. Items contained in this list but not in {@code edited }are called deletions.
     * @param edited
     *            the edited list. Items contained in this list but not in {@code original} are called insertions.
     * @return the result of the diff.
     */
    DiffFacts<T> diff(final List<T> original, final List<T> edited, final Comparator<T> comparator);
}
