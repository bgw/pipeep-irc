package org.pipeep.pircbot;

/**
 * Allows a developer to easily keep IRC channel handling scope specific.
 */
public class ChannelHandler {
  
  private String channel, hostname, name, nick;
  private SimplePircBot parent;
  
  public ChannelHandler(SimplePircBot parent, String channel, String hostname) {
    this.parent = parent;
    this.channel = channel;
    this.hostname = hostname;
    this.nick = "";
  }
  
  // these should be set in SimplePircBot, the end-developer should never call
  // these functions directly
  public void _setNick(String nick) { this.nick = nick; }
  public void _setName(String name) { this.name = name; }
  
  public String getChannel() { return channel; }
  
  public String getHostname() { return hostname; }
  
  public String getNick() { return nick; }
  
  /**
   * Override this method to handle chat-room messages, private messages must be
   * handled by a SimplePircBot instance
   */
  public void onMessage(String sender, String login, String message) {}
  
  public void onJoin(String nick, String login) {}
  
  public void onKick(String kickerNick, String kickerLogin,
                     String kickerHostname, String recipientNick,
                     String reason) {}
  
  protected SimplePircBot getParent() {
    return parent;
  }
  
  /**
   * Called when the channel handler is being garbage collected. This may be
   * caused by the bot getting kicked from a channel, or by the bot being quit.
   */
  public void onDispose() {}
  
  public void sendMessage(String message) {
    parent.sendMessage(channel, message);
  }
  
  public void sendPrivateMessage(String user, String message) {
    parent.sendMessage(user, message);
  }
  
  public void sendAction(String action) {
    parent.sendAction(channel, action);
  }
  
  public void sendPrivateAction(String user, String action) {
    parent.sendAction(user, action);
  }
}
