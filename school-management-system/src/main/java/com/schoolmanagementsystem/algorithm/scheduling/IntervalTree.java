package com.schoolmanagementsystem.algorithm.scheduling;

import java.util.ArrayList;
import java.util.List;

/**
 * Interval Tree implementation for efficient scheduling conflict detection
 */
public class IntervalTree {

    public static class Interval {
        private final int start;
        private final int end;
        private final Object data;

        public Interval(int start, int end, Object data) {
            this.start = start;
            this.end = end;
            this.data = data;
        }

        public int getStart() { return start; }
        public int getEnd() { return end; }
        public Object getData() { return data; }

        public boolean overlaps(Interval other) {
            return !(this.end <= other.start || this.start >= other.end);
        }

        @Override
        public String toString() {
            return "[" + start + ", " + end + "]";
        }
    }

    private static class Node {
        Interval interval;
        int max;
        Node left, right;

        Node(Interval interval) {
            this.interval = interval;
            this.max = interval.end;
        }
    }

    private Node root;

    public void insert(Interval interval) {
        root = insertRecursive(root, interval);
    }

    private Node insertRecursive(Node node, Interval interval) {
        if (node == null) {
            return new Node(interval);
        }

        if (interval.start < node.interval.start) {
            node.left = insertRecursive(node.left, interval);
        } else {
            node.right = insertRecursive(node.right, interval);
        }

        if (node.max < interval.end) {
            node.max = interval.end;
        }

        return node;
    }

    public List<Interval> findOverlapping(Interval queryInterval) {
        List<Interval> result = new ArrayList<>();
        findOverlappingRecursive(root, queryInterval, result);
        return result;
    }

    private void findOverlappingRecursive(Node node, Interval queryInterval, List<Interval> result) {
        if (node == null) {
            return;
        }

        if (node.interval.overlaps(queryInterval)) {
            result.add(node.interval);
        }

        if (node.left != null && node.left.max > queryInterval.start) {
            findOverlappingRecursive(node.left, queryInterval, result);
        }

        if (node.right != null && node.interval.start < queryInterval.end) {
            findOverlappingRecursive(node.right, queryInterval, result);
        }
    }

    public boolean hasOverlap(Interval queryInterval) {
        return hasOverlapRecursive(root, queryInterval);
    }

    private boolean hasOverlapRecursive(Node node, Interval queryInterval) {
        if (node == null) {
            return false;
        }

        if (node.interval.overlaps(queryInterval)) {
            return true;
        }

        if (node.left != null && node.left.max > queryInterval.start) {
            if (hasOverlapRecursive(node.left, queryInterval)) {
                return true;
            }
        }

        if (node.right != null && node.interval.start < queryInterval.end) {
            if (hasOverlapRecursive(node.right, queryInterval)) {
                return true;
            }
        }

        return false;
    }

    public List<Interval> getAllIntervals() {
        List<Interval> result = new ArrayList<>();
        getAllIntervalsRecursive(root, result);
        return result;
    }

    private void getAllIntervalsRecursive(Node node, List<Interval> result) {
        if (node != null) {
            getAllIntervalsRecursive(node.left, result);
            result.add(node.interval);
            getAllIntervalsRecursive(node.right, result);
        }
    }

    public void clear() {
        root = null;
    }

    public boolean isEmpty() {
        return root == null;
    }
}