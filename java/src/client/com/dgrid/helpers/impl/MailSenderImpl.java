package com.dgrid.helpers.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.MailException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.helpers.MailSender;
import com.dgrid.service.DGridClient;
import com.sun.mail.smtp.SMTPSSLTransport;

public class MailSenderImpl implements MailSender {
	private static final String SMTP_HOST_SETTING = "mail.smtpHost";

	private static final String SMTP_PORT_SETTING = "mail.smtpPort";

	private static final String SMTP_AUTH_SETTING = "mail.enableAuth";

	private static final String SMTP_SSL_SETTING = "mail.enableSSL";

	private static final String SMTP_USERNAME = "mail.smtpUsername";

	private static final String SMTP_PASSWORD = "mail.smtpPassword";

	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public void send(String from, String to, String subject, String text)
			throws MailException {
		log.trace(String.format("send(%1$s, %2$s, %3$s, ...)", from, to,
				subject));
		try {
			doSend(from, to, subject, text);
		} catch (TransportException e) {
			log.error("TransportException in send()", e);
			throw (new MailException(e));
		} catch (InvalidApiKey e) {
			log.error("InvalidApiKey in send()", e);
			throw (new MailException(e));
		} finally {
		}
	}

	public void sendHtml(String from, String to, String subject, String html)
			throws MailException {
		log.trace(String.format("send(%1$s, %2$s, %3$s, ...)", from, to,
				subject));
		try {
			doSendHtml(from, to, subject, html);
		} catch (TransportException e) {
			log.error("TransportException in send()", e);
			throw (new MailException(e));
		} catch (InvalidApiKey e) {
			log.error("InvalidApiKey in send()", e);
			throw (new MailException(e));
		} finally {
		}
	}

	public void send(String from, MimeMessage message) throws MailException {
		try {
			Session session = getSession(from);
			send(session, message);
		} catch (Exception e) {
			log.error("Exception in send()", e);
			throw (new MailException(e));
		} finally {
		}
	}

	private void doSend(String from, String to, String subject, String text)
			throws TransportException, InvalidApiKey {
		log.trace("doSend");
		if (log.isDebugEnabled()) {
			log.debug("Sending email from " + from + " to " + to
					+ " with subject: " + subject);
		}
		Session session = getSession(from);
		MimeMessage msg = new MimeMessage(session);
		try {
			Address fromAddress = new InternetAddress(from);
			Address toAddress = new InternetAddress(to);
			msg.addRecipient(Message.RecipientType.TO, toAddress);
			msg.setFrom(fromAddress);
			msg.setSubject(subject);
			msg.setText(text);
			send(session, msg);
		} catch (AddressException e) {
			log.error("AddressException in send():", e);
			throw (new RuntimeException("error.email.address"));
		} catch (MessagingException e) {
			log.error("MessagingException in send():", e);
			throw (new RuntimeException("error.email.messaging"));
		} finally {

		}
	}

	private void doSendHtml(String from, String to, String subject, String text)
			throws TransportException, InvalidApiKey {
		log.trace("doSendHtmlEmail()");
		if (log.isDebugEnabled()) {
			log.debug("Sending email from " + from + " to " + to
					+ " with subject: " + subject);
		}
		Session session = getSession(from);
		MimeMessage msg = new MimeMessage(session);
		try {
			msg.setDataHandler(new DataHandler(new HTMLDataSource(text)));
			Address fromAddress = new InternetAddress(from);
			Address toAddress = new InternetAddress(to);
			msg.addRecipient(Message.RecipientType.TO, toAddress);
			msg.setFrom(fromAddress);
			msg.setSubject(subject);
			msg.setText(text);
			send(session, msg);
		} catch (AddressException e) {
			log.error("AddressException in send():", e);
			throw (new RuntimeException("error.email.address"));
		} catch (MessagingException e) {
			log.error("MessagingException in send():", e);
			throw (new RuntimeException("error.email.messaging"));
		} finally {

		}
	}

	private void send(Session session, MimeMessage msg)
			throws NumberFormatException, TransportException,
			SendFailedException, InvalidApiKey, MessagingException {
		log.trace("send()");
		if (isAuthEnabled()) {
			URLName url = new URLName("smtp", getSmtpHost(), getSmtpPort(), "",
					getSmtpUsername(), getSmtpPassword());
			SMTPSSLTransport tr = new SMTPSSLTransport(session, url);
			tr.connect();
			msg.saveChanges();
			tr.sendMessage(msg, msg.getAllRecipients());
			tr.close();
			if (log.isDebugEnabled()) {
				log.debug("Email sent");
			}
		} else {
			Transport.send(msg);
		}
	}

	private Session getSession(String from) throws TransportException,
			InvalidApiKey {
		Properties props = new Properties();
		props.put("mail.host", getSmtpHost());
		props.put("mail.user", from);
		if (isAuthEnabled()) {
			props.put("mail.smtp.auth", Boolean.TRUE.toString());
		}
		if (isSslEnabled()) {
			props.put("mail.smtp.socketFactory.class",
					"javax.net.ssl.SSLSocketFactory");
			// props.put("mail.smtp.socketFactory.class",
			// EasySSLProtocolSocketFactory.class.getName());
			props.put("mail.smtp.socketFactory.port", Integer
					.toString(getSmtpPort()));
			props.put("mail.smtp.port", Integer.toString(getSmtpPort()));
			props.put("mail.smtp.starttls.enable", Boolean.TRUE.toString());
			props.put("mail.transport.protocol", "smtp");
			props.put("mail.smtp.quitwait", "false");
		}
		Session session = Session.getInstance(props, null);
		return (session);
	}

	private String getSmtpHost() throws TransportException, InvalidApiKey {
		return gridClient.getSetting(SMTP_HOST_SETTING, "stevie.samnbree.net");
	}

	private int getSmtpPort() throws NumberFormatException, TransportException,
			InvalidApiKey {
		return Integer.parseInt(gridClient.getSetting(SMTP_PORT_SETTING,
				Integer.toString(25)));
	}

	private boolean isAuthEnabled() throws TransportException, InvalidApiKey {
		return Boolean.parseBoolean(gridClient.getSetting(SMTP_AUTH_SETTING,
				Boolean.toString(false)));
	}

	private boolean isSslEnabled() throws TransportException, InvalidApiKey {
		return Boolean.parseBoolean(gridClient.getSetting(SMTP_SSL_SETTING,
				Boolean.toString(false)));
	}

	private String getSmtpUsername() throws TransportException, InvalidApiKey {
		return gridClient.getSetting(SMTP_USERNAME, null);
	}

	private String getSmtpPassword() throws TransportException, InvalidApiKey {
		return gridClient.getSetting(SMTP_PASSWORD, null);
	}

	private static class HTMLDataSource implements DataSource {
		private String html;

		public HTMLDataSource(String html) {
			this.html = html;
		}

		public String getContentType() {
			return "text/html";
		}

		public InputStream getInputStream() throws IOException {
			if (html == null)
				throw new IOException("Null html");
			return new ByteArrayInputStream(html.getBytes());
		}

		public String getName() {
			return "JAF text/html dataSource to send e-mail only";
		}

		public OutputStream getOutputStream() throws IOException {
			throw new IOException("This DataHandler cannot write html");
		}
	}
}
