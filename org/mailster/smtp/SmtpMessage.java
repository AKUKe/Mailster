/*
 * Dumbster - a dummy SMTP server Copyright 2004 Jason Paul Kitchen Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0 Unless required by
 * applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS
 * OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */

/**
 * Mailster additions : Copyright (c) 2007 De Oliveira Edouard
 * <p>
 * Some modifications to original code have been made in order to get efficient
 * parsing and to make bug corrections.
 */
package org.mailster.smtp;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.mailster.util.MailUtilities;

/**
 * Container for a complete SMTP message - headers and message body.
 */
public class SmtpMessage implements Serializable
{
	private static final long serialVersionUID = -6813322065075326931L;

	/**
	 * Variable used for MimeMessage conversion.
	 */
    private transient final static Session MAIL_SESSION = Session.getDefaultInstance(System.getProperties(), null);

    /** 
     * Headers. 
     */
    private SmtpHeadersInterface headers;
    
    /** 
     * Message body. 
     */
    private StringBuilder body;
    
    /** 
     * Recipients (read from envelope) 
     */
    private List<String> recipients;

    private SmtpMessagePart internalParts;

    private String content;
    private String oldPreferredContentType;
    private String messageID;

    private String internalDate;
    
    private String charset;
    //private Charset serverCharset = null;
    private Boolean needsConversion = null;
    
    /**
     * Likewise, a global id for Message-ID generation.
     */
    private static int id = 0;

    /**
     * Constructor. Initializes headers Map and body buffer.
     */
    public SmtpMessage()
    {
        headers = new SmtpHeaders();
        body = new StringBuilder();
        recipients = new ArrayList<String>();
    }
    
    /**
     * Update the headers or body depending on the SmtpResponse object and line
     * of input.
     * 
     * @param response SmtpResponse object
     * @param params remainder of input line after SMTP command has been removed
     */
    public void store(SmtpResponse response, String params)
    {
        if (params != null)
        {
            if (SmtpState.DATA_HDR == response.getNextState())
                headers.addHeaderLine(params);
            else if (SmtpState.DATA_BODY == response.getNextState())
            {
            	if (needsConversion == null)
            	{
            		charset = getBodyCharset();
            		needsConversion = charset  != null && !SimpleSmtpServer.DEFAULT_CHARSET.equals(charset);
            		/*if (charset  != null && SimpleSmtpServer.DEFAULT_CHARSET.equals(charset))
            			serverCharset = Charset.forName(SimpleSmtpServer.DEFAULT_CHARSET);*/
            	}
            	
                if (Boolean.TRUE.equals(needsConversion))
                {
                    try
                    {                        
                        params = new String(params.getBytes(SimpleSmtpServer.DEFAULT_CHARSET), charset);
                        // Since 1.6
                        //params = new String(params.getBytes(serverCharset), charset);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();                        
                    }
                }
                body.append(params);
            }
        }
    }

    /**
     * Get a unique value to use 'as' a Message-ID. If the headers don't contain
     * a Message-ID header value, this implementation generates it by
     * concatenating the message object's <code>hashCode()</code>, a global
     * ID (incremented on every use), the current time (in milliseconds), the
     * string "Mailster", and this user's local address generated by
     * <code>InetAddress.getLocalHost()</code>. (The address defaults to
     * "localhost" if <code>getLocalHost()</code> returns null.)
     * 
     * @see java.net.InetAddress
     */
    public String getMessageID()
    {
        if (messageID == null)
        {
            messageID = getHeaderValue(SmtpHeadersInterface.MESSAGE_ID);
            if (messageID == null)
            {
                StringBuilder s = new StringBuilder(hashCode());
                s.append('.').append(id++).append(System.currentTimeMillis())
                        .append(".Mailster@");
                try
                {
                    InetAddress addr = InetAddress.getLocalHost();
                    if (addr != null)
                        s.append(addr.getAddress());
                    else
                        s.append("localhost");
                }
                catch (UnknownHostException e)
                {
                    s.append("localhost");
                }

                // Unique string is
                // <hashcode>.<id>.<currentTime>.Mailster@<host>
                messageID = s.toString();
            }
        }

        return messageID;
    }

    /**
     * Get the value(s) associated with the given header name.
     * 
     * @param name header name
     * @return value(s) associated with the header name
     */
    public String[] getHeaderValues(String name)
    {
        return headers.getHeaderValues(name);
    }

    /**
     * Get the first value associated with a given header name.
     * 
     * @param name header name
     * @return first value associated with the header name
     */
    public String getHeaderValue(String name)
    {
        return headers.getHeaderValue(name);
    }

    /**
     * Get the message body.
     * 
     * @return message body
     */
    public String getBody()
    {
    	body.trimToSize();
        return body.toString(); //getInternalParts().toString(false);
    }
    
    public String getStringToParse()
    {
    	body.trimToSize();
    	return body.toString();
    }
    
    /**
     * Converts from a <code>SmtpMessage</code> to a <code>MimeMessage</code>.
     * 
     * @return a <code>MimeMessage</code> object
     * @throws MessagingException if MimeMessage creation fails
     * @throws UnsupportedEncodingException if charset is unknown
     */
    public MimeMessage asMimeMessage() throws MessagingException, UnsupportedEncodingException
    {
    	String charset = getBodyCharset();
        if (charset == null)
        	charset = SimpleSmtpServer.DEFAULT_CHARSET;
        
        return new MimeMessage(MAIL_SESSION, 
        		new ByteArrayInputStream(toString().getBytes(charset)));
    }        

    /**
     * String representation of the SmtpMessage.
     * 
     * @return a String
     */
    public String toString()
    {
    	return getInternalParts().toString();
    }

    public SmtpHeadersInterface getHeaders()
    {
        return headers;
    }

    /**
     * Note : this method generates and stores a date header if the mail doesn't have one.
     */
    public String getDate()
    {
        String date = getHeaders().getHeaderValue(SmtpHeadersInterface.DATE);
        
        if (date == null)
        {
        	if (internalDate == null)
        	{
        		date = MailUtilities.getNonNullHeaderValue(getHeaders(), SmtpHeadersInterface.DATE);
        		internalDate = date;
        	}
        	else
        		date = internalDate;
        }
        
        return date;
    }

    public String getTo()
    {
        return getHeaderValue(SmtpHeadersInterface.TO);
    }
    
    public String getSubject()
    {
        return MailUtilities.getNonNullHeaderValue(getHeaders(),
                SmtpHeadersInterface.SUBJECT);
    }

    public SmtpMessagePart getInternalParts()
    {
        if (internalParts == null)
        {
            internalParts = MailUtilities.parseInternalParts(this);
            internalParts.compress();
            //body = null;
        }

        return internalParts;
    }

    public String getPreferredContent(String preferredContentType)
    {
        if (content == null
                || !oldPreferredContentType.equals(preferredContentType))
        {
            oldPreferredContentType = preferredContentType;
            content = getInternalParts().getPreferredContent(preferredContentType);
            if (content != null)
            {
                String upperContent = content.toUpperCase();
                if (upperContent.indexOf("<HTML>") == -1)
                {
                    if (upperContent.indexOf("<BODY>") == -1)
                        content = "<html><head> <style type=\"text/css\"><!--\n" +
                            "html,body {margin:2px;font: 10px Verdana;}" +
                            "--></style></head><body>"
                            + content + "</body></html>";
                    else
                        content = "<html>"+content+"</html>";
                }
            }
        }

        return content == null ? "" : content;
    }

    /**
     * Get the charset specified in Content-Type header.
     * 
     * @return charset, null if none specified.
     */
    public String getBodyCharset()
    {
        return MailUtilities.getHeaderParameterValue(headers,
                SmtpHeadersInterface.CONTENT_TYPE,
                SmtpHeadersInterface.CHARSET_PARAMETER);
    }

    /**
     * Add a recipient to the list of recipients.
     */
    protected void addRecipient(String recipient)
    {
        recipients.add(recipient);
    }
    
    /**
     * Add a list of recipients to the list of recipients.
     */
    public void addRecipients(List<String> l)
    {
        this.recipients.addAll(l);
    }    
    
    /**
     * Returns the recipients of this message (from the SMTP
     * envelope). Bcc recipients are consequently exposed for testing.
     * 
     * @return the list of recipients
     */
    public List<String> getRecipients()
    {
        return recipients;
    }

    /**
     * Return the size of the content of this message in bytes. Return 0 if the
     * size cannot be determined.
     * <p>
     * Note that this number may not be an exact measure of the content size and
     * may or may not account for any transfer encoding of the content.
     * <p>
     * This implementation returns the size of the message body (if not null),
     * otherwise, it returns 0.
     * 
     * @return size of content in bytes
     */
    public int getSize()
    {
        String s = getBody();
        if (s != null)
            return s.length();

        return 0;
    }
}
