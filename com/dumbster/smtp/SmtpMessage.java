/*
 * Dumbster - a dummy SMTP server
 * Copyright 2004 Jason Paul Kitchen
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

/*
 * Mailster additions : (c) De Oliveira Edouard
 * Some modifications to original code have been made in order to get
 * efficient parsing
 */
package com.dumbster.smtp;

import org.mailster.util.MailUtilities;

/**
 * Container for a complete SMTP message - headers and message body.
 */
public class SmtpMessage {
  
  /** Headers: Map of List of String hashed on header name. */
  private SmtpHeadersInterface headers;
  /** Message body. */
  private StringBuffer body;
  
  private SmtpMessagePart internalParts;
  private StringBuffer rawMessage = new StringBuffer();
  
  private String content;
  private String oldPreferredContentType;  

  /**
   * Constructor. Initializes headers Map and body buffer.
   */
  public SmtpMessage() {
    headers = new SmtpHeaders();
    body = new StringBuffer();
  }

  /**
   * Update the headers or body depending on the SmtpResponse object and line of input.
   * @param response SmtpResponse object
   * @param params remainder of input line after SMTP command has been removed
   */
  public void store(SmtpResponse response, String params) {
      boolean log = response.getNextState() == SmtpState.DATA_HDR || 
      			response.getNextState() == SmtpState.DATA_BODY;
    if (params != null) {
      if (SmtpState.DATA_HDR == response.getNextState()) 
    	  headers.addHeaderLine(params);
      else if (SmtpState.DATA_BODY == response.getNextState()) 
        body.append(params);
      
      if (log)
      {
	  rawMessage.append(params);
	  if (response.getNextState() != SmtpState.DATA_BODY)
		  rawMessage.append('\n');
      }
    }
    else
	if (response.getNextState() == SmtpState.DATA_BODY)
	    rawMessage.append('\n');
  }

  /**
   * Get the value(s) associated with the given header name.
   * @param name header name
   * @return value(s) associated with the header name
   */
  public String[] getHeaderValues(String name) {
      return headers.getHeaderValues(name);
  }

  /**
   * Get the first value associated with a given header name.
   * @param name header name
   * @return first value associated with the header name
   */
  public String getHeaderValue(String name) {
      return headers.getHeaderValue(name);
  }

  /**
   * Get the message body.
   * @return message body
   */
  public String getBody() {
    return body.toString();
  }
  
  public String getRawMessage()
  {
      return rawMessage.toString();
  }
  
  /**
   * String representation of the SmtpMessage.
   * @return a String
   */
  public String toString() {
		StringBuffer msg = new StringBuffer(headers.toString());
		msg.append('\n').append(body).append('\n');
		return msg.toString();
	}

  public SmtpHeadersInterface getHeaders() {
    return headers;
  }
  
	public String getDate() 
	{
		return MailUtilities.getNonNullHeaderValue(getHeaders(), SmtpHeadersInterface.DATE);
	}  

	public String getSubject() 
	{
		return MailUtilities.getNonNullHeaderValue(getHeaders(), SmtpHeadersInterface.SUBJECT);
	}
	
  public SmtpMessagePart getInternalParts()
  {
	  if (internalParts == null)
		  internalParts = MailUtilities.parseInternalParts(this);
	  
	  return internalParts;
  }
  
  public String getContent(String preferredContentType)
  {
      if (content == null || !oldPreferredContentType.equals(preferredContentType))
      {
	  oldPreferredContentType = preferredContentType;
	  content = getInternalParts().getContent(preferredContentType);	  
      }
      
      return content;
  }  
}
