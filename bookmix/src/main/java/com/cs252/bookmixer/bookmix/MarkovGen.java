package com.cs252.bookmixer.bookmix;

import android.graphics.pdf.PdfDocument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MarkovGen{
    public static final int PREDICTOR_LENGTH=2;
    HashMap<List<String>, List<String>> dict;
    private String word1, word2;
    Random rand = new Random();

    public MarkovGen(){
        dict = new HashMap<List<String>, List<String>>();
    }

    public void addDatum( Scanner in){

        String fileContents = in.useDelimiter("\\A").next();
        List<String> splitInput = new ArrayList<String>(Arrays.asList(fileContents.split(" ")));
        for(int a=0; a<splitInput.size()-PREDICTOR_LENGTH; a++){
            List<String> l = dict.get(splitInput.subList(a, a+PREDICTOR_LENGTH));
            if(l==null){
                dict.put(splitInput.subList(a, a+PREDICTOR_LENGTH), l=new ArrayList<String>());
            }
            l.add(splitInput.get(a+PREDICTOR_LENGTH));
        }
    }

    public void addDatum( String[] tokenizedString ){
        ArrayList<String> splitInput = new ArrayList<String>(Arrays.asList(tokenizedString));
        for(int a=0; a<splitInput.size()-PREDICTOR_LENGTH; a++){
            List<String> l = dict.get(splitInput.subList(a, a+PREDICTOR_LENGTH));
            if(l==null){
                dict.put(splitInput.subList(a, a+PREDICTOR_LENGTH), l=new ArrayList<String>());
            }
            l.add(splitInput.get(a+PREDICTOR_LENGTH));
        }
    }

    public void addDatum( String rawStringInput ){
        List<String> splitInput = new ArrayList<String>(Arrays.asList(rawStringInput.split(" ")));
        for(int a=0; a<splitInput.size()-PREDICTOR_LENGTH; a++){
            List<String> l = dict.get(splitInput.subList(a, a+PREDICTOR_LENGTH));
            if(l==null){
                dict.put(splitInput.subList(a, a+PREDICTOR_LENGTH), l=new ArrayList<String>());
            }
            l.add(splitInput.get(a+PREDICTOR_LENGTH));
        }
    }

    //adds new reference material to the dictionary
    public void addDatum( ArrayList<String> splitInput ){
        for(int a=0; a<splitInput.size()-PREDICTOR_LENGTH; a++){
            List<String> l = dict.get(splitInput.subList(a, a+PREDICTOR_LENGTH));
            if(l==null){
                dict.put(splitInput.subList(a, a+PREDICTOR_LENGTH), l=new ArrayList<String>());
            }
            l.add(splitInput.get(a+PREDICTOR_LENGTH));
        }
    }

    public void printDict(){
        System.out.println(dict.entrySet());
    }


    public void setSeed(String firstWord, String secondWord){
        word1=firstWord;
        word2=secondWord;
    }

    public String nextWord(){
        List<String> nextWordList = dict.get(Arrays.asList(new String[]{word1,word2}));
        String nextWord;
        if(nextWordList==null){
            System.out.println("Dictionary does not contain key for ("+word1+", "+word2+"). Substituting a random key.");
            List<List<String>> keysAsArray = new ArrayList<List<String>>(dict.keySet());

            List<String> randomKey=keysAsArray.get(rand.nextInt(keysAsArray.size()));
            System.out.println("Random key: "+randomKey);
            word1=randomKey.get(0);
            word2=randomKey.get(1);
            nextWordList = dict.get(Arrays.asList(new String[]{word1,word2}));
            assert(nextWordList!=null);
            nextWord=nextWordList.get(rand.nextInt(nextWordList.size()));
            word1=word2;
            word2=nextWord;
            return nextWord;
        }else{
            nextWord=nextWordList.get(rand.nextInt(nextWordList.size()));
            word1=word2;
            word2=nextWord;
            return nextWord;
        }
    }

    public String[] nextNWords(int n){
        String[] returnWords = new String[n];
        for(int a=0; a<n; a++){
            returnWords[a]=this.nextWord();
        }
        return returnWords;
    }
}