package words.g6;

import words.core.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class g6PlayerV5 extends Player {

    private int totalRounds;
    private int remainingRounds;
    private int lettersPerPlayer;
    private Set<Word> wordSet;
    private ThreadLocalRandom random;
    private int initialBid = 3; // Initial bid for the first round

    // Store the minimum bid values from previous rounds
    private List<Integer> previousRoundMinBids = new ArrayList<>();

    public g6PlayerV5() {
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
        previousRoundMinBids.clear(); // Reset the bid history at the start of a new game
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
        // Create a String from the list of characters
        String s = myLettersHypothetical.stream()
                .map(String::valueOf)
                .collect(Collectors.joining());

        // Iterate through the word list to find the best word we can build
        Word ourletters = new Word(s);
        Word bestword = new Word("");
        for (Word w : wordlist) {
            if (ourletters.contains(w) && w.score > bestword.score) {
                bestword = w;
            }
        }
        return bestword.word;
    }

    private boolean canFormHighScoringWord() {
        // Check if the letters can form a high-scoring word
        for (Word word : wordSet) {
            if (canBuildWord(word.word) && word.score > 50) {
                return true;
            }
        }
        return false;
    }

    private boolean canBuildWord(String word) {
        Map<Character, Integer> letterCount = new HashMap<>();
        for (char letter : myLetters) {
            letterCount.put(letter, letterCount.getOrDefault(letter, 0) + 1);
        }
        for (int i = 0; i < word.length(); i++) {
            char ch = word.charAt(i);
            if (!letterCount.containsKey(ch) || letterCount.get(ch) == 0) {
                // if the letter is not in myLetters or there are no more of this letter left, return false
                return false;
            }
            letterCount.put(ch, letterCount.get(ch) - 1);
        }
        return true;
    }

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {

        List<Character> myLettersHypothetical = new ArrayList<>(myLetters);
        char currentBidLetter = bidLetter.getCharacter();
        int myBid = initialBid;

        int scoreInThisRound = secretstate.getScore();

        // Initialize bidLetterCounter if it's the first bid of the game
        int bidLetterCounter = previousRoundMinBids.isEmpty() ? 0 : previousRoundMinBids.size() + 1;

        if (bidLetterCounter == 1) {
            // For the first round, bid 3
            myBid = initialBid;
        } else {
            // For subsequent rounds, bid 1 plus the minimum bid from the second-to-last round
            if (previousRoundMinBids.size() > 1) {
                int minBidSecondLastRound = previousRoundMinBids.get(previousRoundMinBids.size() - 2);
                myBid = 1 + minBidSecondLastRound;
            }
        }

        // Check if we can form a high-scoring word
        if (canFormHighScoringWord()) {
            myBid = 0;
        } else {
            // Learn from other players' bids
            if (bidLetterCounter > numPlayers) {
                List<Integer> allBidValues = playerBidList.stream()
                        .flatMap(bid -> bid.getBidValues().stream())
                        .collect(Collectors.toList());
                int maxBidValue = allBidValues.isEmpty() ? 5 : Collections.max(allBidValues);

                myLettersHypothetical.add(currentBidLetter);
                String endingWordHypo = returnHypoWord(myLettersHypothetical);
                String endingWord = returnWord();

                int scoreHypo = ScrabbleValues.getWordScore(endingWordHypo);
                int scoreNow = ScrabbleValues.getWordScore(endingWord);

                if (scoreHypo > scoreNow) {
                    myBid = Math.min(this.random.nextInt(maxBidValue, maxBidValue + 2), 10);
                }

                if (scoreNow > 50) {
                    myBid = 0;
                }
            }
        }

        // Update bidLetterCounter
        bidLetterCounter++;
        // Store the current minimum bid for the round
        if (bidLetterCounter > 1) {
            int minBidThisRound = previousRoundMinBids.isEmpty() ? myBid : Math.min(myBid, Collections.min(previousRoundMinBids));
            previousRoundMinBids.add(minBidThisRound);
        }

        return myBid;
    }
}
