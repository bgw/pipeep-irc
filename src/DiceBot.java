import org.jibble.pircbot.PircBot;
import org.pipeep.pircbot.SimplePircBot;
import org.pipeep.pircbot.ChannelHandlerFactory;
import java.util.Random;

public class DiceBot extends SimplePircBot<DiceBotChannelHandler> {
  
  public Random rand;
  private long randSeed;
  private int port;
  public int rollCount;
  
  public static void main(String[] args) {
    if(args.length < 2 || args.length > 3) {
      System.out.println("Usage: java DiceBot server[/port] channel [nick]");
      System.out.println(
        "Ex:    java DiceBot irc.foonetic.net/7001 ufsstp-dnd ufsstp-dicebot"
      );
      return;
    }
    int slashIndex = args[0].indexOf('/');
    
    DiceBot db = new DiceBot(
      slashIndex > -1 ? args[0].substring(0, slashIndex) : args[0],   // server
      slashIndex > -1 ?                                               // port
        Integer.parseInt(args[0].substring(slashIndex+1)) : 6667,     // port
      args.length > 2 ? args[2] : "bot"+((int)(Math.random()*10000)), // nick
      true                                                            // debug
    );
    db.simpleJoinChannel("#" + args[1]);                              // channel
  }
  
  public DiceBot(String hostname, int port, String nick, boolean debugMode) {
    super(hostname, port, nick, debugMode);
    rollCount = 0;
    randSeed = System.currentTimeMillis();
    rand = new Random(randSeed);
  }
  
  @Override
  protected void onConnect() {
    super.onConnect();
  }
  
  //TODO: Only supports simple rolls right now, add complex roll support
  
  @Override
  protected void onPrivateMessage(String sender, String login, String hostname, String message) {
    if(message.equals("seed")) {
      sendMessage(sender, Long.toString(randSeed));
    } else if(message.equals("rollcount")) {
      sendMessage(sender, Integer.toString(rollCount));
    }
  }
  
  @Override
  protected ChannelHandlerFactory<DiceBotChannelHandler>
            getNewChannelHandlerFactory() {
    return new DiceBotChannelHandler.DiceBotChannelHandlerFactory(this);
  }
}
