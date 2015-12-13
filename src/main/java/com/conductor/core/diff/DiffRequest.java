package com.conductor.core.diff;

import java.util.Comparator;
import java.util.List;

/**
 * A request for producing a diff between given lists of items.
 * <p/>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
public final class DiffRequest<T> {
    private final Class<T> type;
    private final List<T> original;
    private final List<T> edited;
    private final Comparator<T> comparator;
    private final int limit;
    private final boolean dataPresorted;

    private DiffRequest(final Builder<T> builder) {
        type = builder.type;
        original = builder.original;
        edited = builder.edited;
        comparator = builder.comparator;
        limit = builder.limit;
        dataPresorted = builder.dataPresorted;
    }

    public Class<T> getType() {
        return type;
    }

    public List<T> getOriginal() {
        return original;
    }

    public List<T> getEdited() {
        return edited;
    }

    public Comparator<T> getComparator() {
        return comparator;
    }

    public int getLimit() {
        return limit;
    }

    public boolean isDataPresorted() {
        return dataPresorted;
    }

    public static <T> Builder<T> newBuilder(final Class<T> type) {
        return new Builder<T>(type);
    }

    public static <T> Builder<T> newBuilder(final DiffRequest<T> copy) {
        Builder<T> builder = new Builder<T>(copy.type);
        builder.original = copy.original;
        builder.edited = copy.edited;
        builder.comparator = copy.comparator;
        builder.limit = copy.limit;
        builder.dataPresorted = copy.dataPresorted;
        return builder;
    }

    public static final class Builder<T> {
        private final Class<T> type;
        private List<T> original;
        private List<T> edited;
        private Comparator<T> comparator;
        private int limit = -1;
        private boolean dataPresorted;

        private Builder(final Class<T> type) {
            this.type = type;
        }

        public Builder<T> withOriginal(final List<T> val) {
            original = val;
            return this;
        }

        public Builder<T> withEdited(final List<T> val) {
            edited = val;
            return this;
        }

        public Builder<T> withComparator(final Comparator<T> val) {
            comparator = val;
            return this;
        }

        public Builder<T> withLimit(final int val) {
            limit = val;
            return this;
        }

        public Builder<T> withDataPresorted(final boolean val) {
            dataPresorted = val;
            return this;
        }

        public DiffRequest<T> build() {
            return new DiffRequest<T>(this);
        }
    }
}
