package com.dgrid.helpers;

import javax.mail.internet.MimeMessage;

import com.dgrid.errors.MailException;

public interface MailSender {
	public static final String NAME = "mailSender";

	public void send(String from, String to, String subject, String text)
			throws MailException;

	public void sendHtml(String from, String to, String subject, String html)
			throws MailException;

	public void send(String from, MimeMessage message) throws MailException;
}
