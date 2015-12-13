package com.conductor.core.diff;

import java.util.Comparator;
import java.util.List;

/**
 * An algorithm for comparing two like-sorted lists side by side in linear time.
 * <p/>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
public final class StreamingDiffProducer<T> implements DiffProducer<T> {

    @Override
    public DiffFacts<T> diff(final List<T> original, final List<T> edited, final Comparator<T> comparator) {
        final DiffFacts<T> diffResult = new DiffFacts<T>();
        int i = 0;
        int j = 0;
        for (; i < original.size() && j < edited.size();) {
            final T leftItem = original.get(i);
            final T rightItem = edited.get(j);
            final int comparison = comparator.compare(leftItem, rightItem);
            if (comparison < 0) { // item missing from right
                diffResult.addDeletion(leftItem, ++i);
            } else if (comparison > 0) { // item missing from left
                diffResult.addInsertion(rightItem, ++j);
            } else { // all good
                i++;
                j++;
            }
        }
        for (; i < original.size(); i++) { // any remnants of original are deletions
            diffResult.addDeletion(original.get(i), i);
        }
        for (; j < edited.size(); j++) { // any remnants of edited are insertions
            diffResult.addInsertion(edited.get(j), j);
        }

        return diffResult;
    }
}
