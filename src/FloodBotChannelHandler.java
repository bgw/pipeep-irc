import org.pipeep.pircbot.ChannelHandler;
import org.pipeep.pircbot.ChannelHandlerFactory;
import org.pipeep.pircbot.SimplePircBot;
import java.util.HashMap;
import java.util.LinkedList;

public class FloodBotChannelHandler extends ChannelHandler {
  
  private HashMap<String, LinkedList<Long>> timestamps =
    new HashMap<String, LinkedList<Long>>();
  private int maxMessages, sampleTime;
  
  public FloodBotChannelHandler(FloodBot parent, String channel,
                                String hostname, int maxMessages,
                                int sampleTime) {
    super(parent, channel, hostname);
    this.maxMessages = maxMessages;
    this.sampleTime = sampleTime;
  }
  
  public void onMessage(String sender, String login, String message) {
    LinkedList<Long> timestamps = this.timestamps.get(sender);
    if(timestamps == null) {
      timestamps = new LinkedList<Long>();
      this.timestamps.put(sender, timestamps);
    }
    timestamps.add(System.currentTimeMillis());
    if(getNumberOfRecentMessages(sender) > maxMessages) {
      kick(sender, "Sent more than " + maxMessages + " messages in " +
           sampleTime/1000. + " seconds. Spamming?"
      );
    }
  }
  
  public int getNumberOfRecentMessages(String nick) {
    LinkedList<Long> timestamps = this.timestamps.get(nick);
    if(timestamps == null) { return 0; }
    long oldestTime = System.currentTimeMillis()-sampleTime;
    while(!timestamps.isEmpty() && timestamps.peekFirst() < oldestTime) {
      timestamps.removeFirst();
    }
    return timestamps.size();
  }
  
  public static class FloodBotChannelHandlerFactory extends
                                 ChannelHandlerFactory<FloodBotChannelHandler> {
    
    private int maxMessages, sampleTime;
    
    public FloodBotChannelHandlerFactory(FloodBot parent, int maxMessages,
                                         int sampleTime) {
      super(parent);
      this.maxMessages = maxMessages;
      this.sampleTime = sampleTime;
    }
    
    public FloodBotChannelHandler getNew(String channel, String hostname) {
      FloodBotChannelHandler ch = new FloodBotChannelHandler(
        (FloodBot)getParent(), channel, hostname, maxMessages, sampleTime
      );
      ch._setName(((FloodBot)getParent()).getName());
      ch._setNick(((FloodBot)getParent()).getNick());
      return ch;
    }
  }
}
