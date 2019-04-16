/* Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.engedu.anagrams;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit.*;

public class AnagramDictionary {

    private static final int MIN_NUM_ANAGRAMS = 5;
    private static final int DEFAULT_WORD_LENGTH = 3;
    private static final int MAX_WORD_LENGTH = 7;
    private Random random = new Random();

    private static int wordLength = DEFAULT_WORD_LENGTH;
    private static HashMap<String, ArrayList<String>> lettersToWord;
    private static HashMap<Integer, ArrayList<String>> sizeToWords = new HashMap<>();

    private static ArrayList<String> wordList = new ArrayList<>();
    private static HashSet<String> wordSet = new HashSet<>();


    //Milestone 1
    public AnagramDictionary(Reader reader) throws IOException {
        double startTime = System.nanoTime();

        BufferedReader in = new BufferedReader(reader);
        String line;

        while((line = in.readLine()) != null) {
            String word = line.trim();
            wordList.add(word);
            wordSet.add(word);

            //Milestone 3
            int wordSizeKey = word.length();

            //Optimize word selection Extension
            if (wordSizeKey < DEFAULT_WORD_LENGTH || wordSizeKey > MAX_WORD_LENGTH)
                continue;                             //skips adding small/big words to sizeToWords
            ArrayList<String> wordsOfLengthList;
            if (!sizeToWords.containsKey(wordSizeKey))
                wordsOfLengthList = new ArrayList<>();
            else
                wordsOfLengthList = sizeToWords.get(wordSizeKey);
            wordsOfLengthList.add(word);
            sizeToWords.put(wordSizeKey, wordsOfLengthList);
        }
        generateLettersToWord();

        //Optimize word selection Extension
        filterLowAnagramStarterWords();

//        String base = "post";
//        String[] testMe = {"nonstop", "poster", "lamp post", "spots", "apostrophe"};
//        for (String word : testMe)
//            Log.d("TestTag", Boolean.toString(isGoodWord(word, base)));

//        List<String> test = getAnagrams("green");
//        List<String> test2 = getAnagrams("agiogergoiaermg");
//        Log.d("TestTag",Integer.toString(test.size()));
//        Log.d("TestTag",Integer.toString(test2.size()));

        double runTime = (System.nanoTime() - startTime) / 1000000000;
        String timeMessage = "Finished in " + Double.toString(runTime) + " seconds";
        Log.d("TestTag", timeMessage);
    }

    //Milestone 2
    public boolean isGoodWord(String word, String base) {
        //the provided word is a valid dictionary word (i.e., in wordSet), and
        //the word does not contain the base word as a substring.
        if (!wordSet.contains(word) || word.contains(base))
            return false;

        return true;
    }


    //Milestone 1
    public List<String> getAnagrams(String targetWord) {
        ArrayList<String> result = new ArrayList<String>();
        String targetWordSorted = sortLetters(targetWord);


//        if (!lettersToWord.containsKey(targetWordSorted))
//            Log.d("TestTag", targetWord + ": NO ANAGRAM");
        if (lettersToWord.containsKey(targetWordSorted))
            result = lettersToWord.get(targetWordSorted);

        return result;

    }

    //Milestone 1
    private void generateLettersToWord() {
        double startTime = System.nanoTime();
        lettersToWord = new HashMap<>();

        for (String word: wordList) {
            String sortedWord = sortLetters(word);
            if (!lettersToWord.containsKey(sortedWord)) {
                ArrayList<String> newList = new ArrayList<String>();
                newList.add(word);
                lettersToWord.put(sortedWord, newList);
            }
            else {
                ArrayList<String> tempList = lettersToWord.get(sortedWord);
                tempList.add(word);
                lettersToWord.put(sortedWord, tempList);
            }
        }
        double runTime = (System.nanoTime() - startTime) / 1000000000;
        String timeMessage = "Finished generating in " + Double.toString(runTime) + " seconds";
        Log.d("TestTag", timeMessage);
    }

    //Milestone 1
    private String sortLetters(String word) {
        char[] charArray = word.toCharArray();
        Arrays.sort(charArray);

        return new String(charArray);
    }

    //Milestone 2
    public List<String> getAnagramsWithOneMoreLetter(String word) {
        ArrayList<String> result = new ArrayList<String>();

        StringBuilder wordSB = new StringBuilder(word);
        int indexOfLastChar = word.length();

        for (char alpha = 'a'; alpha <= 'z'; alpha++){
            wordSB.append(alpha);
            result.addAll(getAnagrams(wordSB.toString()));
            wordSB.deleteCharAt(indexOfLastChar);
        }

        //removing all words that contain the base word
        //WHY: the result list is small size to iterate
        Iterator<String> iterator = result.iterator();
        while (iterator.hasNext()) {
            String testWord = iterator.next();
            if (testWord.contains(word))
                iterator.remove();
        }
        return result;
    }

    //Optimize word selection Extension
    private void filterLowAnagramStarterWords() {
        double startTime = System.nanoTime();
        Iterator<HashMap.Entry<Integer, ArrayList<String>>> iterator = sizeToWords.entrySet().iterator();
        while (iterator.hasNext()) {
            HashMap.Entry<Integer, ArrayList<String>> entry = iterator.next();
            ArrayList<String> listOfWords = entry.getValue();
            Iterator<String> stringIterator = listOfWords.iterator();
            boolean listHasChanged = false;         //skips unnecessary updates
            while (stringIterator.hasNext()) {
                String testWord = stringIterator.next();
                if (getAnagramsWithOneMoreLetter(testWord).size() < MIN_NUM_ANAGRAMS) {
                    stringIterator.remove();
                    listHasChanged = true;
                }
            }
            if (listHasChanged)
                entry.setValue(listOfWords);
        }
        double runTime = (System.nanoTime() - startTime) / 1000000000;
        String timeMessage = "Finished filtering in " + Double.toString(runTime) + " seconds";
        Log.d("TestTag", timeMessage);
    }



    //Milestone 2 + 3
    public String pickGoodStarterWord() {
        int upperBound;
        String testWord;
        ArrayList<String> sizeToWordsList;
        while (true) {
            sizeToWordsList = sizeToWords.get(wordLength);
            upperBound = sizeToWordsList.size();
            testWord = sizeToWordsList.get(random.nextInt(upperBound));
            if (wordLength < MAX_WORD_LENGTH)
                wordLength++;
            break;

        }
        return testWord;
    }
}
