package org.pipeep.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;

public class SerializableList extends ArrayList<String> {
  
  public SerializableList() {
    super();
  }
  
  public SerializableList(Collection<String> c) {
    super(c);
  }
  
  public SerializableList(String str) {
    super(constructorHelper(str));
  }
  
  private static List<String> constructorHelper(String str) {
    String[] mems = str.split(",");
    String tmpMem;
    for(int i = 0; i < mems.length; ++i) {
      tmpMem = mems[i];
      tmpMem = tmpMem.replace("\\c", ",");   // '\c' --> ','
      tmpMem = tmpMem.replace("\\\\", "\\"); // '\\' --> '\'
      mems[i] = tmpMem;
    }
    return Arrays.asList(mems);
  }
  
  public String serialize() {
    if(size() == 0) { return ""; }
    StringBuilder sb = new StringBuilder(size()*10);
                  //the IRC RFC says that nicks should not be <=9 chars,
                  //although nobody follows that
    for(String s : this) {
      s = s.replace("\\", "\\\\");
      s = s.replace(",", "\\c");
      sb.append(s); sb.append(s);
    }
    sb.delete(sb.length()-1, sb.length());
    return sb.toString();
  }
}
