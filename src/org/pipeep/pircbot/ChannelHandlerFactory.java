package org.pipeep.pircbot;

public class ChannelHandlerFactory<E extends ChannelHandler> {
  
  private SimplePircBot parent;
  
  public ChannelHandlerFactory(SimplePircBot parent) { this.parent = parent; }
  
  protected SimplePircBot getParent() { return parent; }
  
  public E getNew(String channel, String hostname) {
    ChannelHandler ch = new ChannelHandler(parent, channel, hostname);
    ch._setName(parent.getName());
    ch._setNick(parent.getNick());
    return (E)ch;
  }
}
