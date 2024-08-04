package words.g6;

import java.util.*;

import words.core.*;

public class Group6Player extends Player{
    /**
     * Method is called when it is this player's turn to submit a bid. They should guarantee that
     * the value they return is between 0 and secretstate.getScore(). The highest bid wins, but
     * the winner only pays the price of the second-highest bid.
     *
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
    private Random random;
    private int totalRounds;
    private int remainingRounds;
    private int lettersPerPlayer;
    private Set<Word> wordSet;


    public Group6Player() {
        this.random = new Random();
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
    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList,
                   int totalRounds, ArrayList<String> playerList,
                   SecretState secretstate, int playerID) {
        // Always bid the letter value of the letter on auction.
        int bidLetterValue = bidLetter.getValue();
        int myScore = secretstate.getScore();
        int maxBid = Math.min(myScore, bidLetterValue);

        boolean isVowel = "AEIOU".indexOf(bidLetter.getCharacter()) >= 0;
        boolean isHighValueLetter = "JKQXZ".indexOf(bidLetter.getCharacter()) >= 0;

        // Calculate bid amount
        int myBid = bidLetterValue;

        if (isVowel || isHighValueLetter) {
            myBid += 2; // Slightly higher bid for important letters
        }

        // Ensure we do not overbid
        myBid = Math.min(myBid, myScore);

        return myBid;

    }

}

