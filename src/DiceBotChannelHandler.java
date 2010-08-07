import org.pipeep.pircbot.ChannelHandler;
import org.pipeep.pircbot.ChannelHandlerFactory;
import org.pipeep.pircbot.SimplePircBot;
import org.jibble.pircbot.Colors;
import java.util.Arrays;

public class DiceBotChannelHandler extends ChannelHandler {
  public DiceBotChannelHandler(DiceBot parent, String channel,
                               String hostname) {
    super(parent, channel, hostname);
  }
  
  public void onMessage(String sender, String login, String message) {
    message = message.toLowerCase();
    char tempChar; // for fast caching
    boolean hadStartNumber;
    int startNumber,
        endNumber,
        i, tenToI;
    int index = -1; // index of 'd'
    while(true) {
      index = message.indexOf((int)'d', index+1);
      if(index < 0) { break; }
      if(index < message.length()-1) { // make sure d is not the last char
        endNumber = 0;
        for(i = 0; i+index+1 < message.length(); ++i) {
          tempChar = message.charAt(i+index+1);
          if(!SimplePircBot.isDigit(tempChar)) { break; }
          endNumber = endNumber*10 - '0'+tempChar;
        }
        if(endNumber != 0) {
          startNumber = 0; hadStartNumber = false; tenToI = 1;
          for(i = 0; index-i > 0; ++i) {
            tempChar = message.charAt(index-i-1);
            if(!SimplePircBot.isDigit(tempChar)) { break; }
            hadStartNumber = true;
            startNumber += tenToI * (tempChar-'0');
            tenToI *= 10; // why am I bothering to optimize this code so much???
          }
          if(!hadStartNumber) { startNumber = 1; }
          //and here begins the part where I get lazy and don't optimize at all
          int[] rolls = new int[startNumber];
          long sum = 0L;
          for(i = 0; i < startNumber; ++i) {
            rolls[i] = ((DiceBot)getParent()).rand.nextInt(endNumber)+1;
            sum += (long)rolls[i];
          }
          ((DiceBot)getParent()).rollCount += startNumber;
          sendMessage(sender + ": " + Colors.BOLD + Long.toString(sum) +
                      Colors.NORMAL + " " + Arrays.toString(rolls));
        }
      }
    }
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
