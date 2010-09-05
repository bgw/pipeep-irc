package org.pipeep.pircbot;

import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.Colors;
import org.jibble.pircbot.User;
import org.jibble.pircbot.NickAlreadyInUseException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.HashSet;
import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Date;
import java.text.DateFormat;
import java.io.FileNotFoundException;

//TODO: handle more than one channel per bot instance
//TODO: have a registered nick

public abstract class SimplePircBot<E extends ChannelHandler> extends PircBot {
  
  protected ChannelHandlerFactory<E> channelHandlerFactory =
    getNewChannelHandlerFactory();
  protected abstract ChannelHandlerFactory<E> getNewChannelHandlerFactory();
  //TODO: make channelHandlers a WeakHashMap
  private HashMap<String, E> channelHandlers = new HashMap<String, E>();
  private HashSet<String> joinedChannels = new HashSet<String>();
  private HashSet<String> channelsToJoin = new HashSet<String>();
  private String lastConnectedHostname;
  private int lastConnectedPort;
  
  /**
   * A very simple main method and possible reference implimentation.
   */
  /*public static void main(String[] args) {
    if(args.length < 2 || args.length > 3) {
      System.out.println("Usage: java PircBot server[/port] channel [nick]");
      System.out.println(
        "Ex:    java PircBot irc.foonetic.net/7001 ufsstp-dnd joebot"
      );
      return;
    }
    int slashIndex = args[0].indexOf('/');
    
    SimplePircBot pb = new SimplePircBot(
      args[0].substring(0, slashIndex),                               // server
      Integer.parseInt(args[0].substring(slashIndex+1)),              // port
      args.length > 2 ? args[2] : "bot"+((int)(Math.random()*10000))  // nick
    );
    pb.simpleJoinChannel("#" + args[2]);                              // channel
  }*/
  
  public SimplePircBot(String nick) {
    this(nick, false);
  }
  
  public SimplePircBot(String nick, boolean debugMode) {
    super(); setVerbose(debugMode);
    setName(nick);
  }
  
  public void simpleConnect(String hostname) {
    simpleConnect(hostname, 6667);
  }
  
  public void simpleConnect(String hostname, int port) {
    lastConnectedHostname = hostname; lastConnectedPort = port;
    setAutoNickChange(false);
    String nick = getName();
    for(int i = 1;; ++i) {
      try {
        connect(hostname, port);
        break;
      } catch(NickAlreadyInUseException ex) {
        String oldNick = nick;
        nick = onNickAlreadyInUse(nick);
        setName(nick);
        log("Nick \"" + oldNick + "\" was already in use, trying \"" + nick +
            "\" instead");
      } catch(Exception ex) {
        log(ex);
        long seconds = i*2; if(seconds > 120) { seconds = 120; }
        log("Connection to " + hostname +
            " failed. Reattempting to connect in " + seconds +
            " seconds...");
        try { Thread.sleep(seconds*1000); } catch(Exception sleepex) {}
      }
    }
    log("Connected to " + hostname);
  }
  
  public void simpleReconnect() {
    simpleConnect(lastConnectedHostname, lastConnectedPort);
  }
  
  public void simpleSetName(String name) {
    setName(name);
    for(ChannelHandler ch : channelHandlers.values()) {
      ch._setName(name);
    }
  }
  
  /**
   * Override this function to change how nick conflicts are handled when
   * <code>simpleConnect</code> is called.
   */
  protected String onNickAlreadyInUse(String oldNick) {
    return oldNick + "_";
  }
  
  /**
   * Remember to call super when overriding this function.
   */
  @Override
  protected void onNickChange(String oldNick, String login, String hostname,
                              String newNick) {
    //our nick has changed
    if(newNick.equals(getNick())) {
      for(ChannelHandler ch : channelHandlers.values()) {
        ch._setNick(newNick);
      }
    }
  }
  
  public void simpleJoinChannel(String channel) {
    synchronized(this) {
      String lowerCaseChannel = channel.toLowerCase();
      if(!joinedChannels.contains(lowerCaseChannel)) {
        if(isConnected()) {
          joinChannel(channel);
          joinedChannels.add(lowerCaseChannel);
        } else {
          channelsToJoin.add(lowerCaseChannel);
        }
      }
    }
  }
  
  /**
   * Remember to call super when overriding this function.
   */
  @Override
  protected void onConnect() {
    for(String ch : channelsToJoin) {
      simpleJoinChannel(ch);
    }
    channelsToJoin.clear();
  }
  
  /**
   * Default behavior is to join the channel that we were invited to
   */
  @Override
  protected void onInvite(String targetNick, String sourceNick,
                          String sourceLogin, String sourceHostname,
                          String channel) {
    simpleJoinChannel(channel);
  }
  
  @Override
  protected void onJoin(String channel, String sender, String login,
                        String hostname) {
    //must be us
    E ch = channelHandlers.get(channel);
    if(ch == null) {
      ch = channelHandlerFactory.getNew(channel, hostname);
      channelHandlers.put(channel, ch);
    }
    ch.onJoin(sender, login);
  }
  
  @Override
  protected void onDisconnect() {
    System.out.println("Disconnected; Attempting to reconnect in 5 seconds...");
    try { Thread.sleep(5000); } catch(Exception ex) {}
    simpleReconnect();
    channelsToJoin.addAll(joinedChannels);
    joinedChannels.clear();
    for(E ch : channelHandlers.values()) {
      ch.onDispose();
    }
    channelHandlers.clear();
  }
  
  protected E getChannelHandler(String channel, String reason) {
    String lowerCaseChannel = channel.toLowerCase();
    E ch = channelHandlers.get(lowerCaseChannel);
    if(ch == null || !joinedChannels.contains(lowerCaseChannel)) {
      throw new RuntimeException(
        "Unexpected " + reason + " from an unjoined channel: " +
        lowerCaseChannel
      );
    }
    return ch;
  }
  
  /**
   * Finds the ChannelHandler for the channel that the message came from and
   * calls it's onMessage function.
   */
  @Override
  protected void onMessage(String channel, String sender, String login,
                           String hostname, String message) {
    getChannelHandler(channel, "message").onMessage(sender, login, message);
  }
  
  /**
   * Finds the ChannelHandler for the channel that the action came from and
   * calls it's onMessage function.
   */
  @Override
  protected void onAction(String sender, String login, String hostname,
                          String target, String action) {
    // TODO: when we get a UserHandler, handle when target is not a channel, but
    // a user
    getChannelHandler(target, "action").onAction(sender, login, action);
  }
  
  
  // Here are a few IRC utility functions
  // Auto-Boxing FTW
  private static final HashSet<Character> PUNCTUATION =
    toHashSet(" \\|/,~`\'.;:<>\"?!@#$%^&*(){}[]-+=_");
  // these must be escaped in regex
  private static HashSet<Character> REGEX_METACHARS =
    toHashSet("^[.${*(\\+)|?<>");
  public static final String REGEX_IS_PUNCTUATION =
    toRegexString(PUNCTUATION);
  
  private static HashSet<Character> toHashSet(String str) {
    return toHashSet(str.toCharArray());
  }
  
  private static HashSet<Character> toHashSet(char[] ca) {
    HashSet<Character> hs = new HashSet<Character>();
    for(char c: ca) {
      hs.add(c);
    } return hs;
  }
  
  private static final String toRegexString(Set<Character> cs) {
    StringBuilder sb = new StringBuilder("(");
    for(char c: cs) {
      // metacharacters must me escaped
      if(REGEX_METACHARS.contains(c)) { sb.append('\\'); }
      sb.append(c); sb.append('|');
    }
    if(cs.size() > 0) { sb.deleteCharAt(sb.length()-1); }
    sb.append(')');
    return sb.toString();
  }
  
  public static boolean smartContains(String in, String searchTerm) {
    return smartIndexOf(in, searchTerm) > -1;
  }
  
  public static int smartIndexOf(String in, String searchTerm) {
    return smartIndexOf(in, searchTerm, 0);
  }
  
  /**
   * Finds the index of a searchTerm, but only if it is surrounded by
   * punctuation (or is at the beginning of a line), Eg. Assassination will not
   * set off a bot designed to prevent cussing.
   */
  public static int smartIndexOf(String in, String searchTerm, int start) {
    in = in.toLowerCase(); searchTerm = searchTerm.toLowerCase();
    int index = start;
    while((index = in.indexOf(searchTerm, index)) != -1) {
      if((index == 0 || //if is wrapped only by punctuation
          isPunctuation(in.charAt(index-1))) &&
         (index+searchTerm.length() > in.length()-2 ||
          isPunctuation(in.charAt(index+searchTerm.length()))) ) {
        return index;
      } index += searchTerm.length(); //indexOf already handles bounds
    } return -1;
  }
  
  /**
   * Finds 
   */
  public static boolean isPunctuation(char c) {
    return PUNCTUATION.contains(c);
  }
  
  public static boolean isDigit(char c) { return c >= '0' && c <= '9'; }
  
  @Override
  protected void onKick(String channel, String kickerNick, String kickerLogin,
                        String kickerHostname, String recipientNick,
                        String reason) {
    ChannelHandler ch = getChannelHandler(channel, "kick");
    if(recipientNick.equals(getNick())) {
      joinedChannels.remove(channel);
      ch.onDispose();
      return;
    }
  }
  
  public void log(Object obj) {
    log(obj.toString());
  }
  
  public void log(int val) {
    log(Integer.toString(val));
  }
  
  public void log(long val) {
    log(Long.toString(val));
  }
  
  public void log(byte val) {
    log(Byte.toString(val));
  }
  
  public void log(short val) {
    log(Short.toString(val));
  }
  
  public void log(float val) {
    log(Float.toString(val));
  }
  
  public void log(double val) {
    log(Double.toString(val));
  }
  
  public void log(char val) {
    log(Character.toString(val));
  }
}
