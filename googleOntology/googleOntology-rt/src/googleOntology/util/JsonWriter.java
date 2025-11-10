//
// Copyright (c) 2025
// Based on Jasper's JsonWriter (MIT License)
//

package googleOntology.util;

import java.io.*;
import java.util.*;
import javax.baja.status.*;

/**
 * JsonWriter.
 */
public final class JsonWriter
{
  /** Constructor. */
  public JsonWriter(OutputStream out)
  {
    this.out = new PrintWriter(out);
  }

  /** Flush underlying output stream. */
  public JsonWriter flush() throws IOException
  {
    out.flush();
    return this;
  }

  /** Close underlying output stream. */
  public JsonWriter close() throws IOException
  {
    out.close();
    return this;
  }

  /** Write given char to output stream. */
  public JsonWriter write(char val) throws IOException
  {
    out.print(val);
    return this;
  }

  /** Write given name as "<name>": to output stream. */
  public JsonWriter writeKey(String name) throws IOException
  {
    out.print('\"');
    out.print(escapeString(name));
    out.print('\"');
    out.print(':');
    return this;
  }

  /** Write given int to output stream. */
  public JsonWriter writeVal(int val) throws IOException
  {
    out.print(val);
    return this;
  }

  /** Write given double to output stream. */
  public JsonWriter writeVal(double val) throws IOException
  {
    if (Double.isNaN(val)) { out.print("\"na\""); return this; }
    if (val == Double.POSITIVE_INFINITY) { out.print("\"na\""); return this; }
    if (val == Double.NEGATIVE_INFINITY) { out.print("\"na\""); return this; }
    out.print(val);
    return this;
  }

  /** Write given object to output stream. */
  public JsonWriter writeVal(Object val) throws IOException
  {
    // null
    if (val == null)
    {
      out.print("null");
      return this;
    }

    // String
    if (val instanceof String)
    {
      out.print('\"');
      out.print(escapeString((String)val));
      out.print('\"');
      return this;
    }

    // Integer
    if (val instanceof Integer)
    {
      int i = ((Integer)val).intValue();
      this.writeVal(i);
      return this;
    }

    // Double
    if (val instanceof Double)
    {
      double d = ((Double)val).doubleValue();
      this.writeVal(d);
      return this;
    }

    // BStatusBoolean
    if (val instanceof BStatusBoolean)
    {
      BStatusBoolean b = (BStatusBoolean)val;
      out.print(b.getValue() ? 1 : 0);
      return this;
    }

    // BStatusNumeric
    if (val instanceof BStatusNumeric)
    {
      BStatusNumeric n = (BStatusNumeric)val;
      double d = n.getValue();
      this.writeVal(d);
      return this;
    }

    // BStatusEnum
    if (val instanceof BStatusEnum)
    {
      BStatusEnum e = (BStatusEnum)val;
      out.print(e.getValue().getOrdinal());
      return this;
    }

    // unsupported type
    throw new IOException("Unsupported type '" + val + "' [" + val.getClass().getName() + "]");
  }

  /** Escape special characters in JSON string */
  private String escapeString(String s)
  {
    if (s == null) return "";

    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < s.length(); i++)
    {
      char ch = s.charAt(i);
      switch (ch)
      {
        case '"':  sb.append("\\\""); break;
        case '\\': sb.append("\\\\"); break;
        case '\b': sb.append("\\b");  break;
        case '\f': sb.append("\\f");  break;
        case '\n': sb.append("\\n");  break;
        case '\r': sb.append("\\r");  break;
        case '\t': sb.append("\\t");  break;
        default:
          if (ch < ' ')
          {
            String hex = "000" + Integer.toHexString(ch);
            sb.append("\\u" + hex.substring(hex.length() - 4));
          }
          else
          {
            sb.append(ch);
          }
      }
    }
    return sb.toString();
  }

  private PrintWriter out;
}
