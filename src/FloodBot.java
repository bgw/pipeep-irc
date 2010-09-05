import org.jibble.pircbot.PircBot;
import org.pipeep.pircbot.SimplePircBot;
import org.pipeep.pircbot.ChannelHandlerFactory;

/**
 * A <b>very</b> simple flood (spam) prevention bot. Kicks users if they send
 * too many messages within a time period.
 */

public class FloodBot extends SimplePircBot<FloodBotChannelHandler> {
  
  private int port;
  private int maxMessages, sampleTime;
  
  public static void main(String[] args) {
    if(args.length < 4 || args.length > 5) {
      System.out.println(
        "Usage: java FloodBot server[/port] channel [nick] maxmessages seconds"
      );
      System.out.println(
        "Ex:    java FloodBot irc.freenode.net/6667 jibble floodbot 5 2.0"
      );
      System.out.println("FloodBot must be a HOP or greater to kick people");
      return;
    }
    int slashIndex = args[0].indexOf('/');
    
    FloodBot fb = new FloodBot(
      args.length > 2 ? args[2] : "bot"+((int)(Math.random()*10000)), // nick
      true,                                                           // debug
      Integer.parseInt(args[args.length-2]),                          // maxmsgs
      Double.parseDouble(args[args.length-1])                         // secs
    );
    
    fb.simpleConnect(
      slashIndex > -1 ? args[0].substring(0, slashIndex) : args[0],   // server
      slashIndex > -1 ?                                               // port
        Integer.parseInt(args[0].substring(slashIndex+1)) : 6667
    );
    fb.simpleJoinChannel("#" + args[1]);                              // channel
  }
  
  public FloodBot(String nick, boolean debugMode, int maxMessages,
                  double seconds) {
    super(nick, debugMode);
    this.maxMessages = maxMessages;
    sampleTime = (int)(seconds*1000.);
    // we need to regenerate this:
    channelHandlerFactory = getNewChannelHandlerFactory();
  }
  
  @Override
  protected ChannelHandlerFactory<FloodBotChannelHandler>
            getNewChannelHandlerFactory() {
    return new FloodBotChannelHandler.FloodBotChannelHandlerFactory(
      this, maxMessages, sampleTime
    );
  }
}
