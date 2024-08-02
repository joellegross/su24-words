package words.g6;

import words.core.*;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class g6PlayerV1 extends Player{


   public String returnHypoWord(List<Character> myLettersHypothetical){

       // verbose way of building a String from a bunch of characters.
       char c[] = new char[myLettersHypothetical.size()];
       for (int i = 0; i < c.length; i++) {
           c[i] = myLettersHypothetical.get(i);
       }
       String s = new String(c);

       // iterate through our word list. If we find one we can build,
       // check to see if it's an improvement on the best one we've seen.
       Word ourletters = new Word(s);
       Word bestword = new Word("");
       for (Word w : wordlist) {
           if (ourletters.contains(w)) {
               if (w.score > bestword.score) {
                   bestword = w;
               }

           }
       }

       return bestword.word;

   }



    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        List<Character> myLettersHypothetical = new ArrayList<>(this.myLetters);


        List<Character> vowels = new ArrayList<>();
        vowels.add('a');
        vowels.add('e');
        vowels.add('i');
        vowels.add('o');
        vowels.add('u');

        char currentbidLetter = bidLetter.getCharacter();
        int bidLetterValue = bidLetter.getValue();
        int myBid = 0;

        int occurrences = Collections.frequency(playerList, currentbidLetter);

        myLettersHypothetical.add(currentbidLetter);
        String endingWordHypo = returnHypoWord(myLettersHypothetical);
        String endingWord = returnWord();
//
        int scoreHypo = ScrabbleValues.getWordScore(endingWordHypo);
        int scoreNow = ScrabbleValues.getWordScore(endingWord);

        if (scoreHypo >= scoreNow) {
            myBid = 3;
        }

//       if (!vowels.contains(currentbidLetter) && occurrences <= 1){
//            myBid = 2;
//        }
//
          //if (occurrences > 2){
//            myBid = 4;
         // }
//
//        if (vowels.contains(currentbidLetter)){
//            myBid = 3;
//        }
//
        return myBid;
        }
//    }
//




}
