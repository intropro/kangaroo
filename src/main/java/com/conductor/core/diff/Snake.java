package com.conductor.core.diff;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;

/**
 * Object representing a single insertion or deletion as well as a sequence of 0 or more matching elements.
 *
 * <p>
 * This is the basic molecule of the edit script we are generating. Each {@link Point} is an atom in this analogy.
 * <ul>
 * <li>x is the index for sequence 1 and y is the index for sequence 2.</li>
 * <li>The "line" from {@code start} to {@code mid} corresponds to the insertion/deletion, and as such is vertical or
 * and of length 1, and</li>
 * <li>The line from {@code mid} to {@code start} corresponds to the variable length subsequence of matching elements,
 * and is therefore diagonal.</li>
 * </ul>
 * </p>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
final class Snake {
    private final Point start;
    private final Point mid;
    private final Point end;
    private final Direction direction;
    public final int edits;

    public static class Point {
        private final int x;
        private final int y;

        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Point point = (Point) o;

            if (x != point.x)
                return false;
            if (y != point.y)
                return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }
    }

    public static enum Direction {
        FORWARD, REVERSE
    }

    Snake() {
        this(new Point(0, 0), new Point(0, 0), new Point(0, 0), Direction.FORWARD, 0);
    }

    @VisibleForTesting
    Snake(final Point start, final Point mid, final Point end, final Direction direction, final int edits) {
        this.start = start;
        this.mid = mid;
        this.end = end;
        this.direction = direction;
        this.edits = edits;
    }

    Direction getDirection() {
        return direction;
    }

    Point getStart() {
        return start;
    }

    Point getMid() {
        return mid;
    }

    Point getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Snake snake = (Snake) o;

        if (edits != snake.edits)
            return false;
        if (end != null ? !end.equals(snake.end) : snake.end != null)
            return false;
        if (mid != null ? !mid.equals(snake.mid) : snake.mid != null)
            return false;
        if (!direction.equals(snake.direction))
            return false;
        if (start != null ? !start.equals(snake.start) : snake.start != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = start != null ? start.hashCode() : 0;
        result = 31 * result + (mid != null ? mid.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + direction.hashCode();
        result = 31 * result + edits;
        return result;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this) //
                .add("direction", direction) //
                .add("start", start) //
                .add("mid", mid) //
                .add("end", end) //
                .add("edits", edits) //
                .toString();
    }
}
