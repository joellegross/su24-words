package words.g6;

import words.core.*;

import java.util.*;
import java.util.stream.Collectors;

public class g6ComboPlayer extends Player {

    private static final int MAX_BID = 15;
    private Random rand = new Random();

    private int[] letterFrequency = new int[26];
    private List<Word> longWords = new ArrayList<>();
    private Set<Character> vowels = new HashSet<>(Arrays.asList('A', 'E', 'I', 'O', 'U'));
    private boolean hasHiddenLetters;

    @Override
    public void startNewGame(int playerID, int numPlayers) {
        super.startNewGame(playerID, numPlayers);
        calculateLetterFrequencies();
    }

    @Override
    public void startNewRound(SecretState secretstate) {
        super.startNewRound(secretstate);
        hasHiddenLetters = !secretstate.getSecretLetters().isEmpty();
    }

    @Override
    public int bid(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds,
                   ArrayList<String> playerList, SecretState secretstate, int playerID) {
        return hasHiddenLetters
                ? bidWithHiddenLetters(bidLetter, playerBidList, totalRounds, secretstate)
                : bidWithoutHiddenLetters(bidLetter, playerBidList, secretstate);
    }

    private void calculateLetterFrequencies() {
        int totalLetters = 0;
        for (Word word : wordlist) {
            if (word.word.length() >= 7) {
                longWords.add(word);
                for (char c : word.word.toCharArray()) {
                    letterFrequency[c - 'A']++;
                    totalLetters++;
                }
            }
        }
        for (int i = 0; i < 26; i++) {
            letterFrequency[i] = (int) Math.ceil((double) letterFrequency[i] / totalLetters * 100);
        }
    }

    private int bidWithHiddenLetters(Letter bidLetter, List<PlayerBids> playerBidList, int totalRounds,
                                     SecretState secretstate) {
        List<Character> hypotheticalLetters = new ArrayList<>(myLetters);
        char currentLetter = bidLetter.getCharacter();

        int baseBid = 3;
        int currentScore = secretstate.getScore();

        if (currentScore > 50) {
            baseBid = numPlayers;
        }

        hypotheticalLetters.add(currentLetter);
        String hypotheticalWord = generateBestWord(hypotheticalLetters);
        String currentWord = generateBestWord(myLetters);

        int hypotheticalScore = ScrabbleValues.getWordScore(hypotheticalWord);
        int currentScoreWord = ScrabbleValues.getWordScore(currentWord);

        if (hypotheticalScore > currentScoreWord) {
            baseBid = rand.nextInt(numPlayers, numPlayers + 3);
        }

        if (currentScoreWord > 50 || currentScore < 25) {
            baseBid = 1;
        }

        return baseBid;
    }

    private int bidWithoutHiddenLetters(Letter bidLetter, List<PlayerBids> playerBidList, SecretState secretstate) {
        if (canBuildLongWord()) {
            return 2;
        }

        if (vowels.contains(bidLetter.getCharacter()) && countVowelsInMyLetters() >= 3) {
            return 2;
        }

        if (countOccurrences(bidLetter.getCharacter()) >= 2) {
            return 4;
        }

        int currentScore = secretstate.getScore();
        int letterValue = bidLetter.getValue();
        int bid = Math.min(currentScore, letterValue);

        int frequency = letterFrequency[bidLetter.getCharacter() - 'A'];
        bid += calculateFrequencyBonus(frequency);

        if (isHighValueLetter(bidLetter.getCharacter())) {
            return 3;
        }

        bid = adjustBidBasedOnHistory(bid, bidLetter, playerBidList);
        bid += rand.nextInt(2);
        return Math.min(Math.max(bid, 2), MAX_BID);
    }

    private int countOccurrences(char letter) {
        return (int) myLetters.stream().filter(ch -> ch == letter).count();
    }

    private int countVowelsInMyLetters() {
        return (int) myLetters.stream().filter(vowels::contains).count();
    }

    private int calculateFrequencyBonus(int frequency) {
        if (frequency >= 12) return 7;  // High frequency letters get the highest bonus
        if (frequency >= 9) return 5;   // Slightly lower bonus for high frequency letters
        if (frequency >= 6) return 4;   // Medium bonus for moderate frequency
        if (frequency >= 4) return 2;   // Small bonus for lower frequency letters
        if (frequency <= 2) return -2;  // Small penalty for very low frequency letters
        return 0;                       // No change for letters with medium frequency
    }

    private boolean isHighValueLetter(char letter) {
        return "ZQJXWK".indexOf(letter) >= 0;
    }

    private boolean canBuildLongWord() {
        return longWords.stream().anyMatch(word -> canBuildWord(word.word));
    }

    private boolean canBuildWord(String word) {
        Map<Character, Integer> letterCount = new HashMap<>();
        for (char letter : myLetters) {
            letterCount.put(letter, letterCount.getOrDefault(letter, 0) + 1);
        }
        for (char ch : word.toCharArray()) {
            if (!letterCount.containsKey(ch) || letterCount.get(ch) == 0) {
                return false;
            }
            letterCount.put(ch, letterCount.get(ch) - 1);
        }
        return true;
    }

    private String generateBestWord(List<Character> letters) {
        String word = new String(letters.stream().map(String::valueOf).collect(Collectors.joining()));
        Word bestWord = new Word("");
        for (Word w : wordlist) {
            if (new Word(word).contains(w) && w.score > bestWord.score) {
                bestWord = w;
            }
        }
        return bestWord.word;
    }

    private int adjustBidBasedOnHistory(int initialBid, Letter bidLetter, List<PlayerBids> playerBidList) {
        return playerBidList.stream()
                .filter(playerBid -> playerBid.getTargetLetter().equals(bidLetter))
                .flatMap(playerBid -> playerBid.getBidValues().stream())
                .reduce((highestBid, secondHighestBid) -> {
                    int highest = Math.max(highestBid, secondHighestBid);
                    int secondHighest = Math.min(highestBid, secondHighestBid);
                    return initialBid <= secondHighest ? secondHighest + 1 : highest;
                }).orElse(initialBid);
    }
}
