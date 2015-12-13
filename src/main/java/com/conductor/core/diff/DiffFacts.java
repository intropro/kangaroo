package com.conductor.core.diff;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.conductor.core.diff.DiffFacts.DiffLine.TYPE;
import com.google.common.collect.Lists;

/**
 * TODO: document this sheit.
 * <p/>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
public class DiffFacts<T> {
    static final char DELETE_CHAR = 'L';
    static final char INSERT_CHAR = 'R';

    private final List<T> insertions = newArrayList();
    private final List<T> deletions = newArrayList();
    private final List<DiffLine<T>> diffLines = newArrayList();
    private boolean truncated;

    public List<T> getInsertions() {
        return insertions;
    }

    public List<T> getDeletions() {
        return deletions;
    }

    public Integer getDiffSize() {
        return diffLines.size();
    }

    /**
     * Gets the diff string, on demand.
     *
     * @return the diff string.
     */
    public String getDiffString() {
        return getDiffStringForList(diffLines);
    }

    /**
     * Gets the diff string, on demand, sorted by {@code sortComparator}, and then by {@code diffLine}.
     *
     * @param sortComparator
     *            the {@link Comparator} to sort the diff output by
     * @return the sorted diff string.
     */
    public String getSortedDiffString(final Comparator<T> sortComparator) {
        final List<DiffLine<T>> sortedList = Lists.newArrayList(diffLines);
        Collections.sort(sortedList, new Comparator<DiffLine<T>>() {
            @Override
            public int compare(final DiffLine<T> o1, final DiffLine<T> o2) {
                final int compare = sortComparator.compare(o1.entity, o2.entity);
                if (compare == 0) {
                    return o1.lineNum - o2.lineNum;
                } else {
                    return compare;
                }
            }
        });
        return getDiffStringForList(sortedList);
    }

    private String getDiffStringForList(final List<DiffLine<T>> diffLines) {
        final StringBuilder buf = new StringBuilder();
        for (final DiffLine<T> diffLine : diffLines) {
            buf.append(diffLine).append("\n");
        }
        return buf.toString();
    }

    void addInsertion(T entity, int line) {
        insertions.add(entity);
        diffLines.add(new DiffLine<T>(entity, line, TYPE.INSERTION));
    }

    void addDeletion(T entity, int line) {
        deletions.add(entity);
        diffLines.add(new DiffLine<T>(entity, line, TYPE.DELETION));
    }

    public boolean isTruncated() {
        return truncated;
    }

    public void setTruncated(final boolean truncated) {
        this.truncated = truncated;
    }

    static class DiffLine<T> {
        static enum TYPE {
            INSERTION(INSERT_CHAR), DELETION(DELETE_CHAR);

            final char diffChar;

            private TYPE(final char diffChar) {
                this.diffChar = diffChar;
            }
        }

        private final T entity;
        private final int lineNum;
        private final TYPE type;

        private DiffLine(final T entity, final int lineNum, final TYPE type) {
            this.entity = entity;
            this.lineNum = lineNum;
            this.type = type;
        }

        @Override
        public String toString() {
            return String.format("%s %d %s", type.diffChar, lineNum, entity);
        }
    }
}
