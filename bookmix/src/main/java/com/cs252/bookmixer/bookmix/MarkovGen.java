package com.cs252.bookmixer.bookmix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class MarkovGen{
    public static final int PREDICTOR_LENGTH=2;
    HashMap<List<String>, List<String>> dict;
    List<List<String>> startingKeys = new ArrayList<List<String>>(); //keys that started a datum
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
        ArrayList<String> newStartingKey=new ArrayList<String>();
        newStartingKey.add(splitInput.get(0));
        newStartingKey.add(splitInput.get(1));
        startingKeys.add(newStartingKey);
        for(int n=0;n<splitInput.size()-2;n++){
            if(splitInput.get(n).indexOf('.')!=-1){ //if the current word has a period, add the next two as a starting key
                newStartingKey=new ArrayList<String>();
                newStartingKey.add(splitInput.get(n+1));
                newStartingKey.add(splitInput.get(n+2));
                startingKeys.add(newStartingKey);
            }
        }

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
        //System.out.println(dict.entrySet());
    }


    public void setSeed(String firstWord, String secondWord){
        word1=firstWord;
        word2=secondWord;
    }

    public String nextWord(){
        List<String> nextWordList = dict.get(Arrays.asList(new String[]{word1,word2}));
        String nextWord;
        if(nextWordList==null){
            //System.out.println("Dictionary does not contain key for ("+word1+", "+word2+"). Substituting a random key.");
            List<List<String>> keysAsArray = new ArrayList<List<String>>(dict.keySet());

            List<String> randomKey=keysAsArray.get(rand.nextInt(keysAsArray.size()));
            //System.out.println("Random key: "+randomKey);
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
    public void setRandomStartingSeed(){
        //System.out.println(startingKeys.size());
        List<String> randomSeed=startingKeys.get(rand.nextInt(startingKeys.size()));
        word1=randomSeed.get(0);
        word2=randomSeed.get(1);

    }
    public String[] nextNWords(int n){
        String[] returnWords = new String[n];
        for(int a=0; a<n; a++){
            returnWords[a]=this.nextWord();
        }
        return returnWords;
    }
    public String nextSentence(){
        StringBuilder returnString = new StringBuilder();
        String nextWord = "";
        setRandomStartingSeed();  //not needed if making a non-starting sentence
        returnString.append(' '+word1+' '+word2);//append the current start of the sentence

        while(nextWord.indexOf('.')==-1 && nextWord.indexOf('!')==-1 && nextWord.indexOf('?')==-1){
            returnString.append(nextWord+' ');
            nextWord=this.nextWord();
        }
        returnString.append(nextWord);
        return returnString.toString();
    }

    public String[] nextNSentences(int n){
        String[] returnSentences = new String[n];
        for(int a=0; a<n; a++){
            returnSentences[a]=this.nextSentence();
        }
        return returnSentences;
    }
}