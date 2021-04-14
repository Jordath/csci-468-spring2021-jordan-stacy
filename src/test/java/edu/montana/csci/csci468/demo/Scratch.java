package edu.montana.csci.csci468.demo;

import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;

public class Scratch {

    public void main() {
//        var x = 10;
//        var y = 20;
//        var z = x + y;
//        var q = x + y;

//        var l = true;
//        var m = !l;

//        var t = 1;
//        var u = 1;
//
//        if(t == u){
//            System.out.println("true");
//        }
//        String s = "1".concat("a");
//        //less(1,2);
//        inv(true);
//        greater(2,1);
        //notEqual(1,2);
//        equality(1, 1);
        print(1);
        //boolean x;
        //x = null == null;


    }
    boolean neq(boolean i , boolean j){
        return i == j;
    }

    boolean less(int i, int j){
        return i < j;
    }

    void print(Object i){
        System.out.println(i);
    }

    boolean greater(int i, int j){
        return i > j;
    }

    boolean greaterEq(int i, int j){
        return i >= j;
    }

    boolean lessEq(int i, int j){
        return i <= j;
    }

    boolean equality(int i, int j){
        return i == j;
    }

    boolean notEqual(int i, int j){
        return i != j;
    }


    boolean inv(boolean bool){
        return !bool;
    }

//    int add(int i) {
//        return i + 13;
//    }
//
//    public int intFunc(int i1, int i2) {
//        return i1 + i2;
//    }

}
