import org.jibble.pircbot.PircBot;
import org.pipeep.pircbot.SimplePircBot;
import org.pipeep.pircbot.ChannelHandlerFactory;

public class DiceBot extends SimplePircBot<DiceBotChannelHandler> {
  
  private int port;
  
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
      args.length > 2 ? args[2] : "bot"+((int)(Math.random()*10000)), // nick
      true                                                            // debug
    );
    
    db.simpleConnect(
      slashIndex > -1 ? args[0].substring(0, slashIndex) : args[0],   // server
      slashIndex > -1 ?                                               // port
        Integer.parseInt(args[0].substring(slashIndex+1)) : 6667
    );
    db.simpleJoinChannel("#" + args[1]);                              // channel
  }
  
  public DiceBot(String nick, boolean debugMode) {
    super(nick, debugMode);
  }
  
  @Override
  protected void onConnect() {
    super.onConnect();
  }
  
  //TODO: Only supports simple rolls right now, add complex roll support
  
  @Override
  protected void onPrivateMessage(String sender, String login, String hostname,
                                  String message) {
    DiceBotChannelHandler ch = getChannelHandler(
      message.substring(0, message.indexOf(' ')), "private message"
    );
    if(smartContains(message, "seed")) {
      sendMessage(sender, Long.toString(ch.randSeed));
    } else if(smartContains(message, "rollcount")) {
      sendMessage(sender, Integer.toString(ch.rollCount));
    }
  }
  
  @Override
  protected ChannelHandlerFactory<DiceBotChannelHandler>
            getNewChannelHandlerFactory() {
    return new DiceBotChannelHandler.DiceBotChannelHandlerFactory(this);
  }
}
