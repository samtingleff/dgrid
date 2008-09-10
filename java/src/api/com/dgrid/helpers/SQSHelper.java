package com.dgrid.helpers;

import java.io.IOException;

import com.dgrid.errors.SQSException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.xerox.amazonws.sqs2.Message;
import com.xerox.amazonws.sqs2.MessageQueue;

public interface SQSHelper {
	public static final String NAME = "sqsHelper";

	public int getQueueSize(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey;

	public String send(String queue, String message) throws TransportException,
			IOException, SQSException, InvalidApiKey;

	public Message receive(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey;

	public void delete(String queue, Message message)
			throws TransportException, IOException, SQSException, InvalidApiKey;

	public void deleteMessageQueue(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey;

	public MessageQueue getMessageQueue(String queue)
			throws TransportException, IOException, SQSException, InvalidApiKey;
}
