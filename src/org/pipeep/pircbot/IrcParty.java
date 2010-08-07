package org.pipeep.pircbot;

import java.io.IOException;
import java.util.Properties;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.pipeep.util.SerializableList;

public class IrcParty extends SerializableList {
  
  private String partyName;
  
  /**
   * Makes a new IrcParty, poplulated with the members from the party name
   * pulled from prop
   * @throws IOException if the property key is not found
   */
  public IrcParty(String partyName, Properties prop) throws IOException {
    super(prop.getProperty(partyName+".content"));
  }
  
  /**
   * Makes a new empty IrcParty
   */
  public IrcParty() {
    super();
  }
  
  /**
   * Warning: Only writes content, does not update party list
   */
  public void writeToProperties(Properties prop) {
    prop.setProperty(partyName+".content", serialize());
  }
  
  public String getName() {
    return partyName;
  }
  
  public void setName(String name) {
    partyName = name;
  }
}
