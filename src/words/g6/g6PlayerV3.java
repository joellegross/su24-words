// tried to prioritize higher value letters - but this one doesn't do as well
package words.g6;

import words.core.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class g6PlayerV3 extends Player {

    /**
     * Method is called when it is this player's turn to submit a bid. They should guarantee that
     * the value they return is between 0 and secretstate.getScore(). The highest bid wins, but
     * the winner only pays the price of the second-highest bid.
     * <p>
     * This implementation just bids the value of the letter
     *
     * @param bidLetter the Letter currently up for bidding on
     * @param playerBidList an unmodifiable list of previous bids from this game
     * @param totalRounds the total number of rounds in the game, which is different from the current round
     * @param playerList list of all player names
     * @param secretstate set of data that is stored unique to each player (their score and secret letters)
     * @param playerID the ID of the player being asked to bid (can ignore)
     * @return the amount to bid for the letter
     */
    private int totalRounds;
    private int remainingRounds;
    private int lettersPerPlayer;
    private Set<Word> wordSet;

    private ThreadLocalRandom random;


    public g6PlayerV3() {
        this.random = ThreadLocalRandom.current();
        this.wordSet = new HashSet<>();
    }


    public void startNewGame(int playerID, int numPlayers) {
        myID = playerID; // store my ID

        initializeWordlist(); // read the file containing all the words

        this.numPlayers = numPlayers; // so we know how many players are in the game

        // Initialize other variables needed for the game
        this.totalRounds = 0; // Reset or initialize total rounds
        this.remainingRounds = 0; // Reset or initialize remaining rounds
        this.lettersPerPlayer = 8 * numPlayers; // Calculate letters per player

        for (Word word : wordlist) {
            wordSet.add(word);
        }
    }

    @Override
    public void startNewRound(SecretState secretstate) {
        myLetters.clear(); // clear the letters that I have
        // this puts the secret letters into the currentLetters List
        myLetters.addAll(secretstate.getSecretLetters().stream().map(Letter::getCharacter).toList());

        playerLetters.clear(); // clear the letters that all the players have
        for (int i = 0; i < numPlayers; i++) {
            playerLetters.add(new LinkedList<Character>()); // initialize each player's list of letters
        }

        // Reset or update any round-specific variables
        remainingRounds--; // Decrement the remaining rounds
    }


    public String returnHypoWord(List<Character> myLettersHypothetical) {

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

        char currentBidLetter = bidLetter.getCharacter();
        int bidLetterValue = bidLetter.getValue();
        int myScore = secretstate.getScore();

        // Determine the type of letter
        boolean isVowel = isVowel(currentBidLetter);
        boolean isHighValueLetter = isHighValueLetter(currentBidLetter);

        // Hypothetical word analysis
        List<Character> myLettersHypothetical = new ArrayList<>(this.myLetters);
        myLettersHypothetical.add(currentBidLetter);

        String endingWordHypo = returnHypoWord(myLettersHypothetical);
        String endingWord = returnWord();

        int scoreHypo = ScrabbleValues.getWordScore(endingWordHypo);
        int scoreNow = ScrabbleValues.getWordScore(endingWord);

        int potentialGain = scoreHypo - scoreNow;

        // Bidding logic
        int myBid = 0;

        // Base bid on potential score gain and letter importance
        if (potentialGain > 0) {
            myBid = Math.min(myScore, bidLetterValue + potentialGain / 2);
        }

        // Adjust bid for high-value letters
        if (isHighValueLetter) {
            myBid = Math.min(myScore, Math.max(myBid, bidLetterValue + 3));
        }

        // Adjust bid for vowels
        if (isVowel) {
            myBid = Math.min(myScore, Math.max(myBid, bidLetterValue + 2));
        }

        // Consider current score and round dynamics
        if (myScore > 50) {
            myBid = Math.min(myBid, Math.max(1, myBid - 1)); // Conservative approach
        }

        // Adapt based on game progress
        int remainingRounds = totalRounds - playerBidList.size();
        if (remainingRounds < totalRounds / 3) {
            myBid = Math.min(myScore, myBid + 2); // Aggressive approach in later rounds
        }

        // Random adjustment to add unpredictability
        myBid += random.nextInt(0, 2);

        // Ensure bid is within score
        myBid = Math.min(myBid, myScore);

        // Return the final bid value
        return myBid;
    }

    // Helper method to check if a letter is a vowel
    private boolean isVowel(char letter) {
        return "AEIOU".indexOf(letter) != -1;
    }

    // Helper method to check if a letter is high-value
    private boolean isHighValueLetter(char letter) {
        return "QZJXK".indexOf(letter) != -1;
    }
}
