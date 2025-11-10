//
// Copyright (c) 2025
// Licensed under the MIT License
//

package googleOntology.util;

import java.io.*;
import javax.baja.control.*;
import javax.baja.status.*;
import javax.baja.sys.*;
import javax.baja.util.*;
import javax.baja.web.*;
import javax.servlet.http.*;

/**
 * GoogleOntologyUtil provides common utility methods.
 */
public final class GoogleOntologyUtil
{
////////////////////////////////////////////////////////////////
// BComponent
////////////////////////////////////////////////////////////////

  /**
   * Get the entity ID for given component.
   */
  public static String getEntityId(BComponent c)
  {
    // strip h: from handle ord
    String handle = c.getHandleOrd().toString();
    String suffix = handle.substring(2);
    return suffix;
  }

  /**
   * Get point address relative to entity.
   */
  public static String getPointAddr(BComponent entity, BComponent point)
  {
    // get relative slot path from parent entity
    String eslot = entity.getSlotPath().toString();
    String pslot = point.getSlotPath().toString();

    if (!pslot.startsWith(eslot))
      return point.getName();  // fallback

    String suffix = pslot.substring(eslot.length() + 1);

    // cleanup slotpath suffix
    suffix = slotPathToSuffix(suffix);

    return suffix;
  }

  /**
   * Convert a component slot path to point addr suffix.
   */
  public static String slotPathToSuffix(String orig)
  {
    StringBuffer buf = new StringBuffer();

    for (int i = 0; i < orig.length(); i++)
    {
      int ch = orig.charAt(i);
      if (ch == '/') { ch = '.'; }
      else if (ch == '$') { i += 2; continue; }
      else if (!Character.isLetterOrDigit(ch) && ch != '.') { continue; }
      buf.append((char)ch);
    }

    return buf.toString();
  }

  /**
   * Unescape component slot path.
   */
  public static String unescapeSlotPath(String orig)
  {
    StringBuffer buf = new StringBuffer();
    StringBuffer temp = new StringBuffer();

    for (int i = 0; i < orig.length(); i++)
    {
      int ch = orig.charAt(i);
      if (ch == '$' && (i + 2 < orig.length()))
      {
        // clear out temp buffer for reuse
        temp.setLength(0);
        temp.append(orig.charAt(++i));
        temp.append(orig.charAt(++i));
        ch = (char)Integer.parseInt(temp.toString(), 16);
      }
      buf.append((char)ch);
    }

    return buf.toString();
  }

  /**
   * Get value of given point or null if not available.
   */
  public static BStatusValue getPointValue(BComponent c)
  {
    Object out = c.get("out");
    if (out instanceof BStatusValue) return (BStatusValue)out;
    return null;
  }

  /**
   * Parse a BFacet enum range into a string mapping.
   * Example: "false=Off,true=On" or "0=Off,1=Low,2=High"
   */
  public static String parseEnumRange(BEnumRange range)
  {
    // short-circuit if empty range
    if (range == null || range.isNull()) return null;

    StringBuffer buf = new StringBuffer();
    int[] ords = range.getOrdinals();
    for (int i = 0; i < ords.length; i++)
    {
      if (i > 0) buf.append(',');
      buf.append(ords[i]);
      buf.append('=');
      String tag = range.get(ords[i]).getTag();
      buf.append(unescapeSlotPath(tag));
    }

    // sanity check
    if (buf.length() == 0) return null;
    return buf.toString();
  }

  /**
   * Parse a boolean range into a string mapping.
   */
  public static String parseBooleanRange()
  {
    return "false=false,true=true";
  }

////////////////////////////////////////////////////////////////
// Servlet
////////////////////////////////////////////////////////////////

  /** Convenience for sendErr(404) */
  public static void sendNotFound(WebOp op) throws IOException
  {
    sendErr(op, 404, "Not Found");
  }

  /** Send an error response as JSON with code and msg. */
  public static void sendErr(WebOp op, int code, String msg) throws IOException
  {
    sendErr(op, code, msg, null);
  }

  /** Send an error response as JSON with code and msg. */
  public static void sendErr(WebOp op, int code, String msg, Exception cause) throws IOException
  {
    HttpServletResponse res = op.getResponse();
    res.setStatus(code);
    res.setHeader("Content-Type", "application/json");

    JsonWriter json = new JsonWriter(res.getOutputStream());
    json.write('{');
    json.writeKey("err_msg").writeVal(msg);
    if (cause != null)
    {
      json.write(',');
      json.writeKey("err_trace");
      json.writeVal(printStackTraceToString(cause));
    }
    json.write('}');
    json.flush().close();
  }

  /** Split a path string into array. */
  public static String[] splitPath(String path)
  {
    String[] orig = path.split("/");

    // get non-empty size
    int size = 0;
    for (int i = 0; i < orig.length; i++)
      if (orig[i].length() > 0) size++;

    // filter out empty
    String[] acc = new String[size];
    int p = 0;
    for (int i = 0; i < orig.length; i++)
      if (orig[i].length() > 0) acc[p++] = orig[i];
    return acc;
  }

  /** Read HTTP request body as string. */
  public static String readBody(HttpServletRequest req) throws IOException
  {
    BufferedReader reader = req.getReader();
    StringBuffer sb = new StringBuffer();
    String line;

    while ((line = reader.readLine()) != null)
    {
      sb.append(line);
    }

    return sb.toString();
  }

////////////////////////////////////////////////////////////////
// Exceptions
////////////////////////////////////////////////////////////////

  /** Print stack trace to string. */
  public static String printStackTraceToString(Exception ex)
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    ex.printStackTrace(pw);
    return sw.toString();
  }
}
