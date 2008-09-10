package com.dgrid.helpers.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dgrid.errors.SQSException;
import com.dgrid.errors.TransportException;
import com.dgrid.gen.InvalidApiKey;
import com.dgrid.helpers.AWSConstants;
import com.dgrid.helpers.SQSHelper;
import com.dgrid.service.DGridClient;
import com.xerox.amazonws.sqs2.Message;
import com.xerox.amazonws.sqs2.MessageQueue;
import com.xerox.amazonws.sqs2.SQSUtils;

public class SQSHelperImpl implements SQSHelper {

	private Log log = LogFactory.getLog(getClass());

	private DGridClient gridClient;

	public void setGridClient(DGridClient gridClient) {
		this.gridClient = gridClient;
	}

	public int getQueueSize(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey {
		log.trace("getQueueSize()");
		try {
			MessageQueue msgQueue = doGetMessageQueue(queue);
			return msgQueue.getApproximateNumberOfMessages();
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getQueueSize()", e);
			throw new SQSException(e);
		}
	}

	public String send(String queue, String message) throws TransportException,
			IOException, SQSException, InvalidApiKey {
		log.trace("send()");
		try {
			MessageQueue msgQueue = doGetMessageQueue(queue);
			String msgId = msgQueue.sendMessage(message);
			return msgId;
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getQueueSize()", e);
			throw new SQSException(e);
		}
	}

	public Message receive(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey {
		log.trace("receive()");
		try {
			MessageQueue msgQueue = doGetMessageQueue(queue);
			Message msg = msgQueue.receiveMessage();
			return msg;
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getQueueSize()", e);
			throw new SQSException(e);
		}
	}

	public void delete(String queue, Message message)
			throws TransportException, IOException, SQSException, InvalidApiKey {
		log.trace("delete()");
		try {
			MessageQueue msgQueue = doGetMessageQueue(queue);
			msgQueue.deleteMessage(message);
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getQueueSize()", e);
			throw new SQSException(e);
		}
	}

	public void deleteMessageQueue(String queue) throws TransportException,
			IOException, SQSException, InvalidApiKey {
		log.trace("deleteMessageQueue()");
		try {
			MessageQueue msgQueue = doGetMessageQueue(queue);
			msgQueue.deleteQueue();
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getQueueSize()", e);
			throw new SQSException(e);
		}
	}

	public MessageQueue getMessageQueue(String queue)
			throws TransportException, IOException, SQSException, InvalidApiKey {
		log.trace("getMessageQueue()");
		try {
			return doGetMessageQueue(queue);
		} catch (com.xerox.amazonws.sqs2.SQSException e) {
			log.error("SQSException in getMessageQueue()", e);
			throw new SQSException(e);
		}
	}

	private MessageQueue doGetMessageQueue(String queue)
			throws TransportException, InvalidApiKey,
			com.xerox.amazonws.sqs2.SQSException {
		log.trace("doGetMessageQueue()");
		String awsAccessKey = gridClient.getSetting(
				AWSConstants.AWS_ACCESS_KEY_SETTING, "0A9GVWM7W60MMFQYQP82");
		String awsSecretKey = gridClient.getSetting(
				AWSConstants.AWS_SECRET_KEY_SETTING,
				"lfNVXWxL+PTYslcmKKAszW1HARL0nPpv8sa6nMyB");
		MessageQueue msgQueue = SQSUtils.connectToQueue(queue, awsAccessKey,
				awsSecretKey);
		return msgQueue;
	}
}
