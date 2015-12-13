package com.conductor.core.diff;

import static com.google.common.collect.Lists.newArrayList;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.conductor.core.diff.Snake.Direction;
import com.conductor.core.diff.Snake.Point;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Maps;

/**
 * An implementation of the Myers' diff algorithm.
 * <p>
 * A difference limit can be provided in order to truncate diffs for very different lists. Since the running time of
 * this algorithm scales arithmetically with the number of differences (that is, {@code O(n * d)}), limiting the number
 * of differences is encouraged whenever a full diff is not necessary.
 * </p>
 * <p/>
 * 2015
 *
 * @author Tyrone Hinderson (╯°□°）╯︵ ┻━┻
 */
public final class MyersDiffProducer<T> implements DiffProducer<T> {
    private final int limit;

    public MyersDiffProducer(final int limit) {
        this.limit = limit;
    }

    @Override
    public DiffFacts<T> diff(final List<T> original, final List<T> edited, final Comparator<T> comparator) {
        final DiffFacts<T> facts = new DiffFacts<T>();
        final List<Snake> snakes = getSnakes(original, edited, limit, comparator);
        facts.setTruncated(!snakes.isEmpty() && snakes.get(snakes.size() - 1) == null);

        int originalIndex = 0;
        int editedIndex = 0;
        for (Snake s : snakes) {
            if (s == null) {
                break;
            }
            if (s.getDirection().equals(Direction.FORWARD)) {
                if (s.getMid().getX() > originalIndex) { // horizontal move: deletion from original
                    facts.addDeletion(original.get(originalIndex), s.getMid().getX());
                } else if (s.getMid().getY() > editedIndex) { // vertical move: insertion to edited
                    facts.addInsertion(edited.get(editedIndex), s.getMid().getY());
                }
            } else if (s.getDirection().equals(Direction.REVERSE)) {
                originalIndex = s.getMid().getX();
                editedIndex = s.getMid().getY();
                if (s.getEnd().getX() > originalIndex) {
                    facts.addDeletion(original.get(originalIndex), s.getEnd().getX());
                } else if (s.getEnd().getY() > editedIndex) {
                    facts.addInsertion(edited.get(editedIndex), s.getEnd().getY());
                }
            }
            originalIndex = s.getEnd().getX();
            editedIndex = s.getEnd().getY();
        }
        return facts;
    }

    static <T> List<Snake> getSnakes(final List<T> seq1, final List<T> seq2, final int limit,
            final Comparator<? super T> comparator) {
        List<Snake> snakes = newArrayList();
        getSnakesHelper(seq1, seq2, snakes, limit, comparator);
        return snakes;
    }

    /**
     * Determines the edit script from one {@link List} to another by finding their middle snake and recursing upon the
     * sequence portions before and after the middle snake.
     *
     * @param seq1
     *            the "original" sequence
     * @param seq2
     *            the "revision" sequence
     * @param results
     *            the list that will hold the snakes
     * @param comparator
     *            {@link Comparator} to determine whether two elements are equal
     * @param <T>
     *            the type of element in each sequence
     */
    private static <T> void getSnakesHelper(final List<T> seq1, final List<T> seq2, final List<Snake> results,
            final int limit, final Comparator<? super T> comparator) {
        int s1Length = seq1.size();
        int s2Length = seq2.size();
        if (conditionalTruncate(results, limit)) {
            return;
        }
        final Snake recentSnake = results.isEmpty() ? new Snake() : results.get(results.size() - 1);

        // if s1 is empty and s2 is not, add s2Length insertions to the list
        if (s1Length == 0 && s2Length > 0) {
            for (int i = 0; i < s2Length && !conditionalTruncate(results, limit); i++) {
                final int rx = recentSnake.getEnd().getX();
                final int ry = recentSnake.getEnd().getY() + i;
                results.add(new Snake(new Point(rx, ry), new Point(rx, ry + 1), new Point(rx, ry + 1),
                        Direction.FORWARD, recentSnake.edits + i + 1));
            }
            return;
        }
        // if s2 is empty and s1 is not, add s1Length deletions to the list
        if (s2Length == 0 && s1Length > 0) {
            for (int i = 0; i < s1Length && !conditionalTruncate(results, limit); i++) {
                final int rx = recentSnake.getEnd().getX() + i;
                final int ry = recentSnake.getEnd().getY();
                results.add(new Snake(new Point(rx, ry), new Point(rx + 1, ry), new Point(rx + 1, ry),
                        Direction.FORWARD, recentSnake.edits + i + 1));
            }
            return;
        }
        if (s1Length == 0) {
            return;
        }
        // calculate middle snake
        final Snake middleSnake = getMiddleSnake(seq1, seq2, comparator);
        // recursion time
        if (middleSnake.edits > 1) { // general case
            getSnakesHelper(seq1.subList(0, middleSnake.getStart().getX()),
                    seq2.subList(0, middleSnake.getStart().getY()), results, limit, comparator);
            final int rx = recentSnake.getEnd().getX();
            final int ry = recentSnake.getEnd().getY();
            if (conditionalTruncate(results, limit)) {
                return;
            }
            results.add(new Snake(new Point(middleSnake.getStart().getX() + rx, middleSnake.getStart().getY() + ry),
                    new Point(middleSnake.getMid().getX() + rx, middleSnake.getMid().getY() + ry), new Point(
                            middleSnake.getEnd().getX() + rx, middleSnake.getEnd().getY() + ry), middleSnake
                            .getDirection(), results.get(results.size() - 1).edits + 1));
            getSnakesHelper(seq1.subList(middleSnake.getEnd().getX(), s1Length),
                    seq2.subList(middleSnake.getEnd().getY(), s2Length), results, limit, comparator);
        } else if (middleSnake.edits == 1) { // definitely forward snake: there is exactly one more edit.
            final int rx = recentSnake.getEnd().getX();
            final int ry = recentSnake.getEnd().getY();
            results.add(new Snake(new Point(rx, ry), new Point(rx, ry), new Point(middleSnake.getStart().getX() + rx,
                    middleSnake.getStart().getY() + ry), middleSnake.getDirection(), recentSnake.edits));
            results.add(new Snake(new Point(middleSnake.getStart().getX() + rx, middleSnake.getStart().getY() + ry),
                    new Point(middleSnake.getMid().getX() + rx, middleSnake.getMid().getY() + ry), new Point(
                            middleSnake.getEnd().getX() + rx, middleSnake.getEnd().getY() + ry), middleSnake
                            .getDirection(), recentSnake.edits + 1));
        } else if (middleSnake.edits == 0) { // The two sequences are identical: there are no more edits
            final int rx = recentSnake.getEnd().getX();
            final int ry = recentSnake.getEnd().getY();
            results.add(new Snake(new Point(middleSnake.getStart().getX() + rx, middleSnake.getStart().getY() + ry),
                    new Point(middleSnake.getMid().getX() + rx, middleSnake.getMid().getY() + ry), new Point(
                            middleSnake.getEnd().getX() + rx, middleSnake.getEnd().getY() + ry), middleSnake
                            .getDirection(), recentSnake.edits));
        } else {
            throw new IllegalStateException("Encountered middle snake with negative edits: " + middleSnake.edits);
        }
    }

    /**
     * Null-terminate the results list if the number of changes exceeds a {@code limit} parameter. Let the client know
     * that the list has been truncated.
     *
     * @param soFar
     * @param limit
     * @return
     */
    private static boolean conditionalTruncate(final List<Snake> soFar, final int limit) {
        if (!soFar.isEmpty()) {
            final int lastIdx = soFar.size() - 1;
            if (soFar.get(lastIdx) == null) {
                return true;
            } else if (limit >= 0 && soFar.get(lastIdx).edits > limit) {
                soFar.set(lastIdx, null);
                return true;
            }
        }
        return false;
    }

    /**
     * Runs both the forward and reverse furthest-snake algorithms, and finds the snake at which their paths overlap. It
     * has been proven that this snake must be part of the diff solution.
     *
     * @param seq1
     *            the first sequence
     * @param seq2
     *            the second sequence
     * @param <T>
     *            the type of element in the sequences
     * @return the "middle snake" where the two algorithm results overlap
     */
    @VisibleForTesting
    static <T> Snake getMiddleSnake(final List<T> seq1, final List<T> seq2, final Comparator<? super T> comparator) {
        final int s1Length = seq1.size();
        final int s2Length = seq2.size();
        final int max = (s1Length + s2Length + 1) / 2;
        final int delta = s1Length - s2Length;
        Map<Integer, Integer> forwardV = Maps.newHashMap();
        Map<Integer, Integer> reverseV = Maps.newHashMap();
        forwardV.put(1, 0);
        reverseV.put(delta - 1, s1Length);

        for (int dContour = 0; dContour <= max; dContour++) {
            // forward
            for (int kLine = -dContour; kLine <= dContour; kLine += 2) {
                final boolean down = kLine == -dContour
                        || (kLine != dContour && forwardV.get(kLine - 1) < forwardV.get(kLine + 1));
                final int kPrev = down ? kLine + 1 : kLine - 1;
                final int xStart = forwardV.get(kPrev);
                final int yStart = xStart - kPrev;
                // mid point
                final int xMid = down ? xStart : xStart + 1;
                final int yMid = xMid - kLine;
                // end point
                int xEnd = xMid;
                int yEnd = yMid;
                // follow diagonal
                while (xEnd < s1Length && yEnd < s2Length && comparator.compare(seq1.get(xEnd), seq2.get(yEnd)) == 0) {
                    xEnd++;
                    yEnd++;
                }
                // save end point
                forwardV.put(kLine, xEnd);

                // check for overlap, ruling out impossible overlaps
                if (delta % 2 != 0 && kLine >= delta - (dContour - 1) && kLine <= delta + (dContour - 1)) {
                    if (forwardV.get(kLine) >= reverseV.get(kLine)) {
                        return new Snake(new Point(xStart, yStart), new Point(xMid, yMid), new Point(xEnd, yEnd),
                                Direction.FORWARD, dContour * 2 - 1);
                    }
                }
            }
            // reverse
            for (int kLine = -dContour + delta; kLine <= dContour + delta; kLine += 2) {
                final boolean up = kLine == dContour + delta
                        || (kLine != -dContour + delta && reverseV.get(kLine - 1) < reverseV.get(kLine + 1));
                final int kPrev = up ? kLine - 1 : kLine + 1;
                final int xStart = reverseV.get(kPrev);
                final int yStart = xStart - kPrev;
                // mid point
                final int xMid = up ? xStart : xStart - 1;
                final int yMid = xMid - kLine;
                // end point
                int xEnd = xMid;
                int yEnd = yMid;
                // follow diagonal
                while (xEnd > 0 && yEnd > 0 && comparator.compare(seq1.get(xEnd - 1), seq2.get(yEnd - 1)) == 0) {
                    xEnd--;
                    yEnd--;
                }
                // save end point
                reverseV.put(kLine, xEnd);

                // check for overlap, ruling out impossible overlaps
                if (delta % 2 == 0 && kLine >= -dContour && kLine <= dContour) {
                    if (reverseV.get(kLine) <= forwardV.get(kLine)) {
                        if (dContour == 0) {
                            return new Snake(new Point(xEnd, yEnd), new Point(xMid, yMid),
                                    new Point(xStart, yStart - 1), Direction.REVERSE, dContour * 2);
                        }
                        return new Snake(new Point(xEnd, yEnd), new Point(xMid, yMid), new Point(xStart, yStart),
                                Direction.REVERSE, dContour * 2);
                    }
                }
            }
        }
        throw new IllegalStateException("No middle snake!");
    }
}
