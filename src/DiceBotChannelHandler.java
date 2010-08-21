import org.pipeep.pircbot.ChannelHandler;
import org.pipeep.pircbot.ChannelHandlerFactory;
import org.pipeep.pircbot.SimplePircBot;
import org.jibble.pircbot.Colors;
import java.util.Arrays;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Handles rolls, such as 3d20 (three 20 sided die), or d6 (one six sided die)
 */
public class DiceBotChannelHandler extends ChannelHandler {
  
  public int rollCount = 0;
  private Random rand;
  public long randSeed;
  
  public DiceBotChannelHandler(DiceBot parent, String channel,
                               String hostname) {
    super(parent, channel, hostname);
    randSeed = System.currentTimeMillis();
    rand = new Random(randSeed);
  }
  
  // simplier version using \s instead of SimplePircBot.REGEX_IS_PUNCTUATION,
  // and no java excape characters:
  //     (^|\s)\d*(d|D)\d{1,}($|\s)
  // it is useful to use a tool like kiki to debug regex
  private static Pattern diceRegexPattern = Pattern.compile(
    // begins at the beginning of the line or with a space
    "(^|" + SimplePircBot.REGEX_IS_PUNCTUATION + ")" +
    // 0 or more digits before the d, telling us the number of dice to roll
    "\\d*" +
    // d or D, telling us that this is a dice roll
    "(d|D)" +
    // 1 or more digits after the d, telling us the number of sides on each die
    "\\d{1,}" +
    // the command ends with an end of line or punctuation character
    "($|" + SimplePircBot.REGEX_IS_PUNCTUATION + ")"
  );
  
  public void onMessage(String sender, String login, String message) {
    char tempChar; // for fast caching
    boolean hadStartNumber;
    int startNumber, // number of dice
        endNumber,   // number of sides on each die
        i, tenToI;
    
    // the default number of dice is 1
    startNumber = 1;
    
    Matcher regexMatcher = diceRegexPattern.matcher(message);
    
    if(!regexMatcher.find()) { return; } // no dice here
    
    String matchedText = regexMatcher.group();
    // trim
    if(SimplePircBot.isPunctuation(matchedText.charAt(0))) {
      matchedText = matchedText.substring(1);
    }
    if(SimplePircBot.isPunctuation(
        matchedText.charAt(matchedText.length()-1))
      )
    {
      matchedText = matchedText.substring(0, matchedText.length()-1);
    }
    
    char startChar = matchedText.charAt(0);
    if(startChar == 'd' || startChar == 'D') {
      endNumber = Integer.parseInt(matchedText.substring(1));
    } else {
      String[] nums = matchedText.split("d|D");
      startNumber = Integer.parseInt(nums[0]);
      if(startNumber > 999 || startNumber < 0) {
        sendMessage(sender + ": Number of dice must be 0-999"); return;
      }
      endNumber = Integer.parseInt(nums[1]);
    }
    if(endNumber > 9999 || endNumber < 0) {
      sendMessage(sender + ": Number of sides on dice must be 0-9999"); return;
    }
    
    int[] rolls = new int[startNumber];
    long sum = 0L;
    for(i = 0; i < startNumber; ++i) {
      rolls[i] = rand.nextInt(endNumber)+1;
      sum += (long)rolls[i];
    }
    rollCount += startNumber;
    sendMessage(sender + ": " + Colors.BOLD + Long.toString(sum) +
                Colors.NORMAL + " " + Arrays.toString(rolls));
    return;
  }
  
  public static class DiceBotChannelHandlerFactory extends
                                  ChannelHandlerFactory<DiceBotChannelHandler> {
    public DiceBotChannelHandlerFactory(DiceBot parent) {
      super(parent);
    }
    
    public DiceBotChannelHandler getNew(String channel, String hostname) {
      DiceBotChannelHandler ch
           = new DiceBotChannelHandler((DiceBot)getParent(), channel, hostname);
      ch._setName(((DiceBot)getParent()).getName());
      ch._setNick(((DiceBot)getParent()).getNick());
      return ch;
    }
  }
}
