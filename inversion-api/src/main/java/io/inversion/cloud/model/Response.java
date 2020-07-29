/*
 * Copyright (c) 2015-2018 Rocket Partners, LLC
 * https://github.com/inversion-api
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.inversion.cloud.model;

import io.inversion.cloud.service.Chain;
import io.inversion.cloud.service.Engine;
import io.inversion.cloud.utils.Utils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Response
{
   protected String                                 url               = null;

   protected Chain                                  chain             = null;

   protected ArrayListValuedHashMap<String, String> headers           = new ArrayListValuedHashMap();

   protected int                                    statusCode        = 200;
   protected String                                 statusMesg        = "OK";
   protected String                                 statusError       = null;
   protected String                                 redirect          = null;

   protected String                                 contentType       = null;
   protected StringBuffer                           out               = new StringBuffer();
   protected JSNode                                 json              = new JSNode("meta", new JSNode("createdOn", Utils.formatIso8601(new Date())), "data", new JSArray());
   protected String                                 text              = null;

   protected String                                 fileName          = null;
   protected File                                   file              = null;

   protected Throwable                              error             = null;

   protected String                                 contentRangeUnit  = null;
   protected long                                   contentRangeStart = -1;
   protected long                                   contentRangeEnd   = -1;
   protected long                                   contentRangeSize  = -1;

   protected List<Change>                           changes           = new ArrayList();
   protected StringBuffer                           debug             = new StringBuffer();

   protected int                                    retryCount        = 0;
   protected long                                   callDuration        = 0;
   public Response()
   {

   }

   public Response(String url)
   {
      withUrl(url);
   }

   public boolean hasStatus(int... statusCodes)
   {
      for (int statusCode : statusCodes)
      {
         if (this.statusCode == statusCode)
            return true;
      }
      return false;
   }

   public Response withMeta(String key, String value)
   {
      getJson().getNode("meta").put(key, value);
      return this;
   }

   public void write(StringBuffer buff, Object... msgs)
   {
      write0(buff, msgs);
      buff.append("\r\n");
   }

   protected void write0(StringBuffer buff, Object... msgs)
   {
      if (msgs != null && msgs.length == 0)
         return;

      if (msgs != null && msgs.length == 1 && msgs[0] != null && msgs[0].getClass().isArray())
         msgs = (Object[]) msgs[0];

      for (int i = 0; msgs != null && i < msgs.length; i++)
      {
         Object msg = msgs[i];

         if (msg == null)
            return;

         if (msg instanceof byte[])
            msg = new String((byte[]) msg);

         if (msg.getClass().isArray())
         {
            write0(buff, (Object[]) msg);
         }
         else
         {
            if (i > 0)
               buff.append(" ");
            buff.append(msg);
         }

      }
   }

   /**
    * @param status - one of the SC constants ex "200 OK"
    */
   public Response withStatus(String status)
   {
      statusMesg = status;
      try
      {
         statusCode = Integer.parseInt(status.substring(0, 3));

         if (statusMesg.length() > 4)
         {
            statusMesg = status.substring(4, status.length());
         }
      }
      catch (Exception ex)
      {
         //the status message did not start with numeric status code.
         //this can be ignored.
      }

      return this;
   }

   public String getStatus()
   {
      return statusCode + " " + statusMesg;
   }

   public Response withStatusCode(int statusCode)
   {
      this.statusCode = statusCode;
      return this;
   }

   public Response withStatusMesg(String statusMesg)
   {
      this.statusMesg = statusMesg;
      return this;
   }

   public Chain getChain()
   {
      return chain;
   }

   public Engine getEngine()
   {
      return chain != null ? chain.getEngine() : null;
   }

   public Response withChain(Chain chain)
   {
      this.chain = chain;
      return this;
   }

   public Response debug(Object... msgs)
   {
      write(debug, msgs);
      return this;
   }

   public Response out(Object... msgs)
   {
      debug(msgs);
      write(out, msgs);
      return this;
   }

   public Response withOutput(String output)
   {
      out = new StringBuffer(output);
      return this;
   }

   public String getOutput()
   {
      return out.toString();
   }

   public Response dump()
   {
      System.out.println(getDebug());
      return this;
   }

   public String getDebug()
   {
      return debug.toString();
   }

   public String getHeader(String key)
   {
      List<String> vals = headers.get(key);
      if (vals != null && vals.size() > 0)
         return vals.get(0);
      return null;
   }

   /**
    * @return the headers
    */
   public ArrayListValuedHashMap<String, String> getHeaders()
   {
      return headers;
   }

   public void withHeader(String key, String value)
   {
      if (!headers.containsMapping(key, value))
         headers.put(key, value);
   }

   /**
    * Sets the root output json document...you should use withData and withMeta
    * instead unless you REALLY want to change to wrapper document structure.
    *
    * @param json the json to set
    */
   public Response withJson(JSNode json)
   {
      this.json = json;
      return this;
   }

   public String findString(String path)
   {
      return getJson().findString(path);
   }

   public int findInt(String path)
   {
      return getJson().findInt(path);
   }

   public boolean findBoolean(String path)
   {
      return getJson().findBoolean(path);
   }

   public JSNode findNode(String path)
   {
      return getJson().findNode(path);
   }

   public JSArray findArray(String path)
   {
      return getJson().findArray(path);
   }

   public Object find(String path)
   {
      return getJson().find(path);
   }

   public Validation validate(String jsonPath)
   {
      return validate(jsonPath, null);
   }

   public Validation validate(String jsonPath, String customErrorMessage)
   {
      return new Validation(this, jsonPath, customErrorMessage);
   }

   public JSArray data()
   {
      JSNode json = getJson();
      if (json != null)
      {
         return json.getArray("data");
      }
      return null;
   }

   public Response withData(JSArray data)
   {
      getJson().put("data", data);
      return this;
   }

   public Response withRecord(Object record)
   {
      data().add(record);
      return this;
   }

   public Response withRecords(List records)
   {
      for (Object record : records)
         data().add(record);
      return this;
   }

   public JSNode meta()
   {
      return getJson().getNode("meta");
   }

   public Response withMeta(String key, Object value)
   {
      meta().put(key, value);
      return this;
   }

   public Response withFoundRows(int foundRows)
   {
      withMeta("foundRows", foundRows);
      updatePageCount();
      return this;
   }

   public int getFoundRows()
   {
      return findInt("meta.foundRows");
   }

   public Response withPageSize(int pageSize)
   {
      withMeta("pageSize", pageSize);
      return this;
   }

   public Response withPageNum(int pageNum)
   {
      withMeta("pageNum", pageNum);
      updatePageCount();

      return this;
   }

   public Response withPageCount(int pageCount)
   {
      withMeta("pageCount", pageCount);
      return this;
   }

   public int getPageSize()
   {
      int pageSize = getJson().findInt("meta.pageSize");
      if (pageSize < 0)
      {
         Object arr = getJson().find("data.0.name");
         if (arr instanceof JSArray)
         {
            pageSize = ((JSArray) arr).size();
         }
      }
      return pageSize;
   }

   protected void updatePageCount()
   {
      int ps = getPageSize();
      int fr = getFoundRows();
      if (ps > 0 && fr > 0)
      {
         int pageCount = fr / ps + (fr % ps == 0 ? 0 : 1);
         withPageCount(pageCount);
      }
   }

   public int getPageCount()
   {
      return getJson().findInt("meta.pageCount");
   }

   public String next()
   {
      return findString("meta.next");
   }

   public Response withNext(String nextPageUrl)
   {
      withMeta("next", nextPageUrl);
      return this;
   }

   /**
    * @return the statusMesg
    */
   public String getStatusMesg()
   {
      return statusMesg;
   }

   /**
    * @return the statusCode
    */
   public int getStatusCode()
   {
      return statusCode;
   }

   public Response withText(String text)
   {
      this.json = null;
      this.text = text;
      return this;
   }

   public String getEntityKey()
   {
      JSNode json = getJson();
      if (json != null)
      {
         String href = json.getString("href");
         if (href != null)
         {
            String[] parts = href.split("/");
            return parts[parts.length - 1];
         }
      }
      return null;
   }

   public String getRedirect()
   {
      return redirect;
   }

   public Response withRedirect(String redirect)
   {
      this.redirect = redirect;
      return this;
   }

   public String getContentType()
   {
      return contentType;
   }

   public Response withContentType(String contentType)
   {
      headers.remove("Content-Type");
      headers.put("Content-Type", contentType);
      this.contentType = contentType;
      return this;
   }

   public List<Change> getChanges()
   {
      return changes;
   }

   public Response withChanges(java.util.Collection<Change> changes)
   {
      this.changes.addAll(changes);
      return this;
   }

   public Response withChange(String method, String collectionKey, Object entityKey)
   {
      if (entityKey instanceof List)
      {
         List<String> deletedIds = (List<String>) entityKey;
         for (String id : deletedIds)
         {
            changes.add(new Change(method, collectionKey, id));
         }
      }
      else
      {
         changes.add(new Change(method, collectionKey, entityKey));
      }
      return this;
   }

   public Response withChange(String method, String collectionKey, String... entityKeys)
   {
      for (int i = 0; entityKeys != null && i < entityKeys.length; i++)
         withChange(method, collectionKey, entityKeys[i]);
      return this;
   }

   public boolean isSuccess()
   {
      return statusCode >= 200 && statusCode <= 300 && error == null;
   }

   public Throwable getError()
   {
      return error;
   }

   //   public String getLog()
   //   {
   //      return log;
   //   }

   //   public LinkedHashMap<String, String> getHeaders()
   //   {
   //      return new LinkedHashMap(headers);
   //   }

   //   public String getHeader(String header)
   //   {
   //      String value = headers.get(header);
   //      if (value == null)
   //      {
   //         for (String key : headers.keySet())
   //         {
   //            if (key.equalsIgnoreCase(header))
   //               return headers.get(key);
   //         }
   //      }
   //      return value;
   //   }

   public InputStream getInputStream() throws IOException
   {
      if (file != null)
         return new BufferedInputStream(new FileInputStream(file));

      return null;
   }

   /**
    * @return the json
    */
   public JSNode getJson()
   {
      if (json == null)
      {
         //lazy loads text/json
         getContent();
      }

      return json;
   }

   public String getText()
   {
      if (text == null)
      {
         //lazy loads text/json
         getContent();
      }

      return text;
   }

   public String getContent()
   {
      try
      {
         if (text == null && json == null && file != null && file.length() > 0)
         {
            String string = Utils.read(getInputStream());
            if (string != null)
            {
               try
               {
                  json = JSNode.parseJsonNode(string);
               }
               catch (Exception ex)
               {
                  //OK
                  text = string;
               }
            }
         }

         if (text != null)
         {
            return text;
         }
         else if (json != null)
         {
            return json.toString();
         }
      }
      catch (Exception ex)
      {
         Utils.rethrow(ex);
      }
      return null;
   }

   public String getErrorContent()
   {
      if (!isSuccess() && error != null)
         return Utils.getShortCause(error);

      try
      {
         if (!isSuccess())
         {
            String string = getContent();//Utils.read(getInputStream());
            return string;
         }
      }
      catch (Exception ex)
      {
         Utils.rethrow(ex);
      }
      return getContent();
   }

   public long getFileLength()
   {
      if (file != null)
      {
         return file.length();
      }
      return -1;
   }

   public Response withFile(File file) throws Exception
   {
      this.json = null;
      this.file = file;
      return this;
   }

   public File getFile()
   {
      return file;
   }

   /**
    * This is the value returned from the server via the "Content-Length" header
    * NOTE: this will not match file length, for partial downloads, consider also using ContentRangeSize
    * @return
    */
   public long getContentLength()
   {
      String length = getHeader("Content-Length");
      if (length != null)
      {
         return Long.parseLong(length);
      }
      return 0;
   }

   /**
    * This value come from the "Content-Range" header and is the unit part
    * Content-Range: <unit> <range-start>-<range-end>/<size>
    * @return
    */
   public String getContentRangeUnit()
   {
      parseContentRange();
      return contentRangeUnit;
   }

   /**
    * This value come from the "Content-Range" header and is the first part
    * Content-Range: <unit> <range-start>-<range-end>/<size>
    * @return
    */
   public long getContentRangeStart()
   {
      parseContentRange();
      return contentRangeStart;
   }

   /**
    * This value come from the "Content-Range" header and is the middle part
    * Content-Range: <unit> <range-start>-<range-end>/<size>
    * @return
    */
   public long getContentRangeEnd()
   {
      parseContentRange();
      return contentRangeEnd;
   }

   /**
    * This value come from the "Content-Range" header and is the last part
    * Content-Range: <unit> <range-start>-<range-end>/<size>
    * @return
    */
   public long getContentRangeSize()
   {
      parseContentRange();
      return contentRangeSize;
   }

   /**
    * Parses the "Content-Range" header
    * Content-Range: <unit> <range-start>-<range-end>/<size>
    */
   private void parseContentRange()
   {
      if (contentRangeUnit == null)
      {
         String range = getHeader("Content-Range");
         if (range != null)
         {
            String[] parts = range.split(" ");
            contentRangeUnit = parts[0];
            parts = parts[1].split("/");
            contentRangeSize = Long.parseLong(parts[1]);
            parts = parts[0].split("-");
            if (parts.length == 2)
            {
               contentRangeStart = Long.parseLong(parts[0]);
               contentRangeEnd = Long.parseLong(parts[1]);
            }
         }
      }
   }

   public Response withUrl(String url)
   {
      if (!Utils.empty(url))
      {
         url = url.trim();
         url = url.replaceAll(" ", "%20");
      }

      this.url = url;

      if (Utils.empty(fileName))
      {
         try
         {
            fileName = new URL(url).getFile();
            if (Utils.empty(fileName))
               fileName = null;
         }
         catch (Exception ex)
         {

         }
      }
      return this;
   }

   public Response withError(Throwable ex)
   {
      this.error = ex;
      return this;
   }

   public String getFileName()
   {
      return fileName;
   }

   public String getUrl()
   {
      return url;
   }

   //   public Response onSuccess(ResponseHandler handler)
   //   {
   //      if (isSuccess())
   //      {
   //         try
   //         {
   //            handler.onResponse(this);
   //         }
   //         catch (Exception ex)
   //         {
   //            logger.error("Error handling onSuccess", ex);
   //         }
   //      }
   //      return this;
   //   }
   //
   //   public Response onFailure(ResponseHandler handler)
   //   {
   //      if (!isSuccess())
   //      {
   //         try
   //         {
   //            handler.onResponse(this);
   //         }
   //         catch (Exception ex)
   //         {
   //            logger.error("Error handling onFailure", ex);
   //         }
   //      }
   //      return this;
   //   }
   //
   //   public Response onResponse(ResponseHandler handler)
   //   {
   //      try
   //      {
   //         handler.onResponse(this);
   //      }
   //      catch (Exception ex)
   //      {
   //         logger.error("Error handling onResponse", ex);
   //      }
   //      return this;
   //   }

   @Override
   public String toString()
   {
      return debug.toString();
   }

   @Override
   public void finalize()
   {
      if (file != null)
      {
         try
         {
            File tempFile = file;
            file = null;
            tempFile.delete();
         }
         catch (Throwable t)
         {
            // ignore
         }
      }
   }

   //----------------------------------------------------------------------------------------------------------------------
   //----------------------------------------------------------------------------------------------------------------------
   //TEST ASSERTION CONVENIENCE METHODS

   public Response assertOk(String... messages)
   {
      if (statusCode < 200 || statusCode > 299)
      {
         rethrow(statusCode, messages);
      }

      return this;
   }

   public void rethrow()
   {
      rethrow(statusCode);
   }

   public void rethrow(int statusCode)
   {
      rethrow(statusCode, (String[]) null);
   }

   public void rethrow(String... messages)
   {
      rethrow(statusCode, messages);
   }

   public void rethrow(int statusCode, String... messages)
   {
      if (error != null)
         Utils.rethrow(error);

      statusCode = statusCode > 399 ? statusCode : 500;

      String msg = "";
      for (int i = 0; messages != null && i < messages.length; i++)
         msg += messages[i] + "\r\n ";

      String message = getText();
      try
      {
         while (message != null && message.startsWith("{") && message.indexOf("\\\"message\\\"") > -1)
         {
            message = JSNode.parseJsonNode(message).getString("message");
         }
      }
      catch (Exception ex)
      {
         //igore
      }

      if (message != null)
         msg = msg + " " + message.trim();

      ApiException.throwEx(statusCode + "",  null,  msg);
   }

   public Response assertStatus(int... statusCodes)
   {
      return assertStatus(null, statusCodes);
   }

   public Response assertStatus(String message, int... statusCodes)
   {
      boolean matched = false;
      for (int statusCode : statusCodes)
      {
         if (statusCode == this.statusCode)
         {
            matched = true;
            break;
         }
      }

      if (!matched)
      {
         Object[] args = null;
         if (message == null)
         {
            message = "The returned status '%s' was not in the approved list '%s'";
            List debugList = new ArrayList();
            for (int i = 0; statusCodes != null && i < statusCodes.length; i++)
               debugList.add(statusCodes[i]);

            args = new Object[]{this.statusCode, debugList};
         }

         ApiException.throw500InternalServerError(message, args);
      }

      return this;
   }

   public Response assertDebug(String lineMatch, String... matches)
   {
      if (matches == null || matches.length == 0)
      {
         matches = new String[]{lineMatch.substring(lineMatch.indexOf(" ") + 1, lineMatch.length())};
         lineMatch = lineMatch.substring(0, lineMatch.indexOf(" "));
      }

      if (matches == null || matches.length == 0)
         return this;

      String debug = getDebug();

      debug = debug.substring(0, debug.indexOf("<< response"));

      int idx = debug.indexOf(" " + lineMatch + " ");
      if (idx < 0)
      {
         System.err.println("SKIPPING DEBUG MATCH: " + lineMatch + " " + Arrays.asList(matches));
         return this;
      }

      String debugLine = debug.substring(idx, debug.indexOf("\n", idx)).trim();

      for (int i = 0; i < matches.length; i++)
      {
         String match = matches[i];
         List<String> matchTokens = Utils.split(match, ' ', '\'', '"', '{', '}');
         for (String matchToken : matchTokens)
         {
            if (debugLine.indexOf(matchToken) < 0)
            {
               String msg = "ERROR: Can't find match token in debug line";
               msg += "\r\n" + "  - debug line    : " + debugLine;
               msg += "\r\n" + "  - missing token : " + matchToken;
               msg += "\r\n" + debug;

               System.err.println(msg);

               Utils.error(msg);

            }
         }
      }
      return this;
   }

   public Response withRetryCount(int retryCount){
      this.retryCount = retryCount;
      return this;
   }
   public int getRetryCount(){
      return retryCount;
   }

   public Response withCallDuration(long callDuration){
      this.callDuration = callDuration;
      return this;
   }
   public long getCallDuration(){
      return callDuration;
   }

}