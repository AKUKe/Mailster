/**
 * ---<br>
 * Mailster (C) 2007-2009 De Oliveira Edouard
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 675 Mass
 * Ave, Cambridge, MA 02139, USA.
 * <p>
 * See&nbsp; <a href="http://tedorg.free.fr/en/projects.php" target="_parent">Mailster
 * Web Site</a> <br>
 * ---
 * <p>
 * SmtpMessage.java - This class represents a structure smtp mail.
 * 
 * This was originally a class from Dumbster but it has been through 
 * so much modifications that it has almost anything left in common 
 * with it. 
 * 
 * @author <a href="mailto:doe_wanted@yahoo.fr">Edouard De Oliveira</a>
 * @version $Revision: 1.5 $, $Date: 2009/03/22 21:58:13 $
 */
package org.mailster.core.mail;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;

import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import org.mailster.core.smtp.MailsterConstants;
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
    private transient final static Session MAIL_SESSION = Session.getInstance(new Properties());

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
    private final static AtomicLong id = new AtomicLong(0);

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
     * Update the headers or body of the message.
     * 
     * @param isHeaderData true if data is part of the mail header
     * @param line the line of mail data 
     */
    protected void append(boolean isHeaderData, String line)
    {
        if (line != null)
        {
            if (isHeaderData)
                headers.addHeaderLine(line);
            else
            {
            	if (needsConversion == null)
            	{
            		charset = getBodyCharset();
            		needsConversion = charset  != null && !MailsterConstants.DEFAULT_CHARSET_NAME.equals(charset);
            		/*if (charset  != null && SimpleSmtpServer.DEFAULT_CHARSET.equals(charset))
            			serverCharset = Charset.forName(SimpleSmtpServer.DEFAULT_CHARSET);*/
            	}
            	
                if (Boolean.TRUE.equals(needsConversion))
                {
                    try
                    {                        
                        line = new String(line.getBytes(MailsterConstants.DEFAULT_CHARSET_NAME), charset);
                        // Since 1.6
                        //params = new String(params.getBytes(serverCharset), charset);
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();                        
                    }
                }
                body.append(line);
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
     * <hashcode>.<id>.<currentTime>.Mailster@<host>
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
                s.append('.').append(id.incrementAndGet()).append(System.currentTimeMillis())
                        .append(".Mailster@");
                try
                {
                    InetAddress addr = InetAddress.getLocalHost();
                    if (addr != null)
                    {
                    	String h = addr.toString();
                    	int pos = h.indexOf('/');
                    	if (pos != -1)
                    		s.append(h.substring(pos+1));
                    	else
                    		s.append(h);
                    }                        
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
        return getInternalParts().toString(false);
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
        	charset = MailsterConstants.DEFAULT_CHARSET_NAME;
        
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
        		date = MailUtilities.getNonNullHeaderValue(getHeaders(), SmtpHeadersInterface.DATE, false);
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
                SmtpHeadersInterface.SUBJECT, true);
    }

    public SmtpMessagePart getInternalParts()
    {
        if (internalParts == null)
        {
        	body.trimToSize();
            internalParts = MailUtilities.parseInternalParts(this, body.toString());
            internalParts.compress();
            body = null;
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
                
                if (upperContent.indexOf("<BODY>") == -1)
                    content = "<html><head> <style type=\"text/css\"><!--\n" +
                        "html,body {margin:2px;font: 10px Verdana;}" +
                        "--></style></head><body>"
                        + content + "</body></html>";
                else
                if (upperContent.indexOf("<HTML>") == -1)	
                    content = "<html>"+content+"</html>";
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
     * Add a list of recipients to the list of recipients.
     */
    protected void addRecipients(List<String> l)
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