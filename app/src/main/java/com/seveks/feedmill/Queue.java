package com.seveks.feedmill;

import android.os.AsyncTask;

public class Queue {
    boolean isPaused = false;

    public static class Node {
        AsyncTask<Void, Void, Void> value;
        Node next;

        public Node(AsyncTask<Void, Void, Void> value) {
            this.value = value;
        }
    }

    private Node first = null;
    private Node last = null;

    public void pauseQueue() {
        isPaused = true;
    }

    public void resumeQueue() {
        isPaused = false;
        run();
    }

    public void enqueue(Node node) {
        if (last == null)
            first = last = node;
        else {
            last.next = node;
            last = node;
        }
        run();
    }

    public void dequeue() {
        first = first.next;
        if(first == null)
            last = null;
    }

    public void run() {
        if(!isPaused) {
            Node node = first;
            if (node != null) {
                node.value.execute();
                dequeue();
            }
        }
    }
}
