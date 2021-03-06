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
package test.junit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.columba.ristretto.message.Address;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;
import org.mailster.core.mail.SmtpMessage;
import org.mailster.core.smtp.MailsterSMTPServer;
import org.mailster.core.smtp.events.SMTPServerAdapter;
import org.mailster.core.smtp.events.SMTPServerEvent;

public class SmtpServerTest extends TestCase 
{
  private static final int SMTP_PORT = 1002;

  private MailsterSMTPServer server;
  private List<SmtpMessage> messages = new ArrayList<SmtpMessage>();

  public SmtpServerTest(String s) 
  {
    super(s);
  }

  protected void setUp() 
  	throws Exception 
  {
      super.setUp();
      server = new MailsterSMTPServer(SMTP_PORT);
      server.addSMTPServerListener(new SMTPServerAdapter() {		
			public void started(SMTPServerEvent event) {
				messages.clear();
			}
			public void emailReceived(SMTPServerEvent event) {
				messages.add(event.getMessage());
			}
		});
      server.start();
  }

  protected void tearDown() 
  	throws Exception 
  {
      super.tearDown();
      server.stop();
  }

  public void testSendEncodedMessage() throws InterruptedException 
  {
      String body = "\u3042\u3044\u3046\u3048\u304a"; // some Japanese letters
      String charset = "iso-2022-jp";
      try 
      {
        sendMessageWithCharset(SMTP_PORT, 
				        		"sender@hereagain.com", 
				        		"EncodedMessage", 
				        		body, 
				        		"receivingagain@there.com", 
				        		charset);
      } 
      catch (Exception e) 
      {
        e.printStackTrace();
        fail("Unexpected exception: " + e);
      }

      Thread.sleep(500);
      assertEquals(1, messages.size());
      Iterator<SmtpMessage> emailIter = messages.iterator();
      SmtpMessage email = (SmtpMessage) emailIter.next();
      assertEquals(body, email.getBody());
    }
   
    private void sendMessageWithCharset(int port, String from, String subject, String body, String to, String charset) 
    	throws MessagingException 
    {
       Properties mailProps = getMailProperties(port);
       Session session = Session.getInstance(mailProps, null);
       //session.setDebug(true);
   
       MimeMessage msg = createMessageWithCharset(session, from, to, subject, body, charset);
       Transport.send(msg);
     }
   
    private MimeMessage createMessageWithCharset(
      Session session, String from, String to, String subject, String body, String charset) 
    	throws MessagingException 
    {
       MimeMessage msg = new MimeMessage(session);
       msg.setFrom(new InternetAddress(from));
       msg.setSubject(subject);
       msg.setSentDate(new Date());
       if (charset != null) 
       {
    	   msg.setText(body, charset);
    	   msg.setHeader("Content-Transfer-Encoding", "7bit");
       } 
       else
    	   msg.setText(body);

       msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
       return msg;
     }
  
  public void testSend() throws InterruptedException 
  {
    try 
    {
      sendMessage(SMTP_PORT, "sender@here.com", 
    		  "Test", "Test Body", "receiver@there.com");
    } 
    catch (Exception e) 
    {
      e.printStackTrace();
      fail("Unexpected exception: " + e);
    }

    Thread.sleep(500);
    assertEquals(1, messages.size());
    Iterator<SmtpMessage> emailIter = messages.iterator();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("Test", email.getHeaderValue("Subject"));
    assertEquals("Test Body", email.getBody());
    assertEquals("Wrong number of recipients", 1, email.getRecipients().size());
    assertEquals("Wrong recipient", "receiver@there.com", email.getRecipients().get(0));    
  }
  
  public void sendMessageWithRistretto(String from, String to, String subject, String body) 
  		throws IOException, SMTPException
  {
      SMTPProtocol protocol = new SMTPProtocol("localhost", SMTP_PORT);
      
      // Open the port
      protocol.openPort();
      protocol.helo(InetAddress.getLocalHost());
                  
      // Setup from and to recipient
      protocol.mail(new Address(from));
      protocol.rcpt(new Address(to));
      
      // Create mail
      StringBuilder sb = new StringBuilder();
      sb.append("Subject: ").append(subject).append("\r\n");
      sb.append("From: <").append(from).append(">\r\n");
      sb.append("To: <").append(to).append(">\r\n");
      sb.append("\r\n").append(body);
      
      // Finally send the data
      protocol.data(new ByteArrayInputStream(sb.toString().getBytes()));
      
      // And close the session
      protocol.quit();  
  }
  
  public void testSendMessageWithCarriageReturn() throws InterruptedException 
  {
    String bodyWithCR = "\n\nKeep these pesky carriage returns\n\n";
    try 
    {
    	sendMessageWithRistretto("sender@hereagain.com", 
    			"receivingagain@there.com", 
    			"CRTest", bodyWithCR);
    }
    catch (Exception e) 
    {
      e.printStackTrace();
      fail("Unexpected exception: " + e);
    }

    Thread.sleep(500);
    assertEquals(1, messages.size());
    Iterator<SmtpMessage> emailIter = messages.iterator();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals(bodyWithCR, email.getBody());
  }
  
  public void testSendMessageWithCarriageReturnV2() throws InterruptedException 
  {
      String bodyWithCR = "\n\nKeep these pesky\ncarriage returns\n\n";
      try 
      {
      		sendMessageWithRistretto("sender@hereagain.com", 
    			"receivingagain@there.com", 
    			"CRTest", bodyWithCR);
      } 
      catch (Exception e) 
      {
        e.printStackTrace();
        fail("Unexpected exception: " + e);
      }

      Thread.sleep(500);
      assertEquals(1, messages.size());
      Iterator<SmtpMessage> emailIter = messages.iterator();
      SmtpMessage email = (SmtpMessage) emailIter.next();
      assertEquals(bodyWithCR, email.getBody());
    }

  protected String headerLinesToString(String[] header)
  {
      if (header == null)
          return null;
      
      StringBuilder sb = new StringBuilder();
      for (int i=0,max=header.length;i<max;i++)
      {
          sb.append(header[i]);
          if (i<max-1)
              sb.append("\r\n");
      }      
      
      return sb.toString();
  }
  
  public void testContinuedHeadersArriveIntact() 
  	throws MessagingException, InterruptedException 
  {
      Properties mailProps = getMailProperties(SMTP_PORT);
      Session session = Session.getInstance(mailProps, null);
      
      MimeMessage msg = createMessage(session, 
    		  "sender@example.com", "guy@example.net", "Re: Hello", "Virus");
      msg.addHeaderLine("X-LongHeader: 12345");
      msg.addHeaderLine("\t67890");
      msg.addHeaderLine("X-LongerHeader: baz");
      msg.addHeaderLine("   foo bar");
      msg.addHeaderLine(" quux");

      Transport.send(msg);

      Thread.sleep(500);
      assertEquals(1, messages.size());
      SmtpMessage recvd = (SmtpMessage) messages.iterator().next();
      assertEquals("12345\r\n\t67890", headerLinesToString(recvd.getHeaderValues("X-LongHeader")));
      assertEquals("baz\r\n   foo bar\r\n quux", headerLinesToString(recvd.getHeaderValues("X-LongerHeader")));
    }
  
  public void testSendTwoMessagesSameConnection() throws InterruptedException 
  {
    try 
    {
      MimeMessage[] mimeMessages = new MimeMessage[2];
      Properties mailProps = getMailProperties(SMTP_PORT);
      Session session = Session.getInstance(mailProps, null);
      //session.setDebug(true);

      mimeMessages[0] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle1", "Bug1");
      mimeMessages[1] = createMessage(session, "sender@whatever.com", "receiver@home.com", "Doodle2", "Bug2");

      Transport transport = session.getTransport("smtp");
      transport.connect("localhost", SMTP_PORT, null, null);

      for (int i = 0; i < mimeMessages.length; i++) 
      {
        MimeMessage mimeMessage = mimeMessages[i];
        transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());
      }

      transport.close();
    } 
    catch (MessagingException e) 
    {
      e.printStackTrace();
      fail("Unexpected exception: " + e);
    }
    
    Thread.sleep(500);
    assertTrue(messages.size() == 2);
  }

  public void testSendTwoMsgsWithLogin() throws InterruptedException 
  {
    try 
    {
      String Server = "localhost";
      String From = "sender@here.com";
      String To = "receiver@there.com";
      String Subject = "Test";
      String body = "Test Body";

      Properties props = new Properties();

      if (Server != null)
        props.put("mail.smtp.host", Server);

      Session session = Session.getInstance(props);
      Message msg = new MimeMessage(session);

      if (From != null) 
        msg.setFrom(new InternetAddress(From));
      else
        msg.setFrom();

      InternetAddress.parse(To, false);
      msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(To, false));
      msg.setSubject(Subject);

      msg.setText(body);
      msg.setHeader("X-Mailer", "musala");
      msg.setSentDate(new Date());
      msg.saveChanges();

      Transport transport = null;

      try 
      {
        transport = session.getTransport("smtp");
        transport.connect(Server, SMTP_PORT, "ddd", "ddd");
        transport.sendMessage(msg, InternetAddress.parse(To, false));
        transport.sendMessage(msg, InternetAddress.parse("dimiter.bakardjiev@musala.com", false));

      } 
      catch (javax.mail.MessagingException me) 
      {
        me.printStackTrace();
      } 
      catch (Exception e) 
      {
        e.printStackTrace();
      } 
      finally 
      {
        if (transport != null)
          transport.close();
      }
    } 
    catch (Exception e) 
    {
      e.printStackTrace();
    }

    Thread.sleep(500);
    assertEquals(2, messages.size());
    Iterator<SmtpMessage> emailIter = messages.iterator();
    SmtpMessage email = (SmtpMessage) emailIter.next();
    assertEquals("Test", email.getHeaderValue("Subject"));
    assertEquals("Test Body", email.getBody());
  }

  private Properties getMailProperties(int port) 
  {
    Properties mailProps = new Properties();
    mailProps.setProperty("mail.smtp.host", "localhost");
    mailProps.setProperty("mail.smtp.port", "" + port);
    mailProps.setProperty("mail.smtp.sendpartial", "true");
    return mailProps;
  }


  private void sendMessage(int port, String from, String subject, String body, String to) 
  	throws MessagingException 
  {
    Properties mailProps = getMailProperties(port);
    Session session = Session.getInstance(mailProps, null);
    //session.setDebug(true);

    MimeMessage msg = createMessage(session, from, to, subject, body);
    Transport.send(msg);
  }

  private MimeMessage createMessage(
    Session session, String from, String to, String subject, String body) 
  	throws MessagingException 
  {
    MimeMessage msg = new MimeMessage(session);
    msg.setFrom(new InternetAddress(from));
    msg.setSubject(subject);
    msg.setSentDate(new Date());
    msg.setText(body);
    msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
    return msg;
  }
}