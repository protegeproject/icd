package edu.stanford.bmir.icd.utils;

public class InfiniteCycleThread {
    public static void main(String[] args) {
        int sum = 0;
        for (int i=0; i<Integer.MAX_VALUE; i++) {
            sum += i;
            if (i == Integer.MAX_VALUE-1) {
                i=0;
                System.out.println("Sum: " + sum);
            }
        }
    }
}