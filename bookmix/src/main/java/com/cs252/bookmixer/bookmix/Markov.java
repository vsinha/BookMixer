package com.cs252.bookmixer.bookmix;

import java.util.Dictionary;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.*;

public class Markov{
    public static void main(String[] args){
        MarkovGen testGen = new MarkovGen();
        testGen.addDatum("ask not what your country can do for you ask what you can do for your country");
        testGen.printDict();
        for(String s:testGen.nextNWords(100)){
            System.out.print(s+" ");
        }
    }
}