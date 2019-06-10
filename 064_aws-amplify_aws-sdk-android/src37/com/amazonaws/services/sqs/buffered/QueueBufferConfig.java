/*
 * Copyright 2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazonaws.services.sqs.buffered;

import com.amazonaws.AmazonClientException;

public class QueueBufferConfig {

    public static final int MAX_BATCH_SIZE_DEFAULT = 10;

    /** the maximum number of entries in a batch command */
    private int maxBatchSize;

    /** Updated as the service now supports messages of size max 256 KiB. */
    public static final long SERVICE_MAX_BATCH_SIZE_BYTES = 256 * 1024 - 1;

    /**
     * The maximum time (milliseconds) a send batch is held open for additional
     * outbound requests. The longer this timeout, the longer messages wait for
     * other messages to be added to the batch. Increasing this timeout reduces
     * the number of calls made and increases throughput, but also increases
     * average message latency.
     */
    private long maxBatchOpenMs;

    /** 200 milliseconds */
    public static final long MAX_BATCH_OPEN_MS_DEFAULT = 200;

    /**
     * Should we use long polling or not?
     */
    private boolean longPoll;

    /** true */
    private static final boolean LONG_POLL_DEFAULT = true;

    /**
     * The maximum number of concurrent batches for each type of outbound
     * request. The greater the number, the greater the throughput that can be
     * achieved (at the expense of consuming more threads).
     */
    private int maxInflightOutboundBatches;

    /** 5 batches */
    public static final int MAX_INFLIGHT_OUTBOUND_BATCHES_DEFAULT = 5;

    /**
     * The maximum number of concurrent receive message batches. The greater
     * this number, the faster the queue will be pulling messages from the SQS
     * servers (at the expense of consuming more threads).
     */
    private int maxInflightReceiveBatches;

    /** 10 batches */
    public static final int MAX_INFLIGHT_RECEIVE_BATCHES_DEFAULT = 10;

    /**
     * If more than that number of completed receive batches are waiting in the
     * buffer, the querying for new messages will stop. The larger this number,
     * the more messages the buffer queue will pre-fetch and keep in the buffer
     * on the client side, and the faster receive requests will be satisfied.
     * The visibility timeout of a pre-fetched message starts at the point of
     * pre-fetch, which means that while the message is in the local buffer it
     * is unavailable for other clients to process, and when this client
     * retrieves it, part of the visibility timeout may have already expired.
     * The number of messages prefetched will not exceed 10 *
     * maxDoneReceiveBatches, as there can be a maximum of 10 messages per
     * batch.
     */
    private int maxDoneReceiveBatches;

    /** 10 batches */
    public static final int MAX_DONE_RECEIVE_BATCHES_DEFAULT = 10;

    /**
     * Maximum permitted size of a SendMessage or SendMessageBatch message, in
     * bytes
     */
    private long maxBatchSizeBytes;

    /** 256 kilobytes */
    public static final long MAX_BATCH_SIZE_BYTES_DEFAULT = SERVICE_MAX_BATCH_SIZE_BYTES;

    /**
     * Custom visibility timeout to use when retrieving messages from SQS. If
     * set to a value greater than zero, this timeout will override the default
     * visibility timeout set on the SQS queue. Set it to -1 to use the default
     * visiblity timeout of the queue. Visibility timeout of 0 seconds is not
     * supported.
     */
    private int visibilityTimeoutSeconds;

    /** -1, which means use the visibility timeout of the queue */
    public static final int VISIBILITY_TIMEOUT_SECONDS_DEFAULT = -1;

    /**
     * Specifies the amount of time, in seconds, the receive call will block on
     * the server waiting for messages to arrive if the queue is empty when the
     * receive call is first made. This setting has no effect if long polling is
     * disabled.
     */

    private int longPollWaitTimeoutSeconds;

    public static final int LONGPOLL_WAIT_TIMEOUT_SECONDS_DEFAULT = 20;

    public QueueBufferConfig(long maxBatchOpenMs,
            int maxInflightOutboundBatches,
            int maxInflightReceiveBatches,
            int maxDoneReceiveBatches,
            boolean paramLongPoll,
            long maxBatchSizeBytes,
            int visibilityTimeout,
            int longPollTimeout,
            int maxBatch) {
        super();
        this.maxBatchOpenMs = maxBatchOpenMs;
        this.maxInflightOutboundBatches = maxInflightOutboundBatches;
        this.maxInflightReceiveBatches = maxInflightReceiveBatches;
        this.maxDoneReceiveBatches = maxDoneReceiveBatches;
        this.longPoll = paramLongPoll;
        this.maxBatchSizeBytes = maxBatchSizeBytes;
        this.visibilityTimeoutSeconds = visibilityTimeout;
        this.longPollWaitTimeoutSeconds = longPollTimeout;
        this.maxBatchSize = maxBatch;
    }

    public QueueBufferConfig() {
        this(MAX_BATCH_OPEN_MS_DEFAULT,
                MAX_INFLIGHT_OUTBOUND_BATCHES_DEFAULT,
                MAX_INFLIGHT_RECEIVE_BATCHES_DEFAULT,
                MAX_DONE_RECEIVE_BATCHES_DEFAULT,
                LONG_POLL_DEFAULT,
                MAX_BATCH_SIZE_BYTES_DEFAULT,
                VISIBILITY_TIMEOUT_SECONDS_DEFAULT,
                LONGPOLL_WAIT_TIMEOUT_SECONDS_DEFAULT,
                MAX_BATCH_SIZE_DEFAULT);
    }

    /** copy constructor */
    public QueueBufferConfig(QueueBufferConfig other) {
        longPoll = other.longPoll;
        longPollWaitTimeoutSeconds = other.longPollWaitTimeoutSeconds;
        maxBatchOpenMs = other.maxBatchOpenMs;
        maxBatchSize = other.maxBatchSize;
        maxBatchSizeBytes = other.maxBatchSizeBytes;
        maxDoneReceiveBatches = other.maxDoneReceiveBatches;
        maxInflightOutboundBatches = other.maxInflightOutboundBatches;
        maxInflightReceiveBatches = other.maxInflightReceiveBatches;
        visibilityTimeoutSeconds = other.visibilityTimeoutSeconds;
    }

    @Override
    public String toString() {
        return "QueueBufferConfig [maxBatchSize=" + maxBatchSize
                + ", maxBatchOpenMs=" + maxBatchOpenMs + ", longPoll="
                + longPoll + ", maxInflightOutboundBatches="
                + maxInflightOutboundBatches + ", maxInflightReceiveBatches="
                + maxInflightReceiveBatches + ", maxDoneReceiveBatches="
                + maxDoneReceiveBatches + ", maxBatchSizeBytes="
                + maxBatchSizeBytes + ", visibilityTimeoutSeconds="
                + visibilityTimeoutSeconds + ", longPollWaitTimeoutSeconds="
                + longPollWaitTimeoutSeconds + "]";
    }

    /**
     * The maximum time (milliseconds) a send batch is held open for additional
     * outbound requests. The longer this timeout, the longer messages wait for
     * other messages to be added to the batch. Increasing this timeout reduces
     * the number of calls made and increases throughput, but also increases
     * average message latency.
     */

    public long getMaxBatchOpenMs() {
        return maxBatchOpenMs;
    }

    /**
     * The maximum time (milliseconds) a send batch is held open for additional
     * outbound requests. The longer this timeout, the longer messages wait for
     * other messages to be added to the batch. Increasing this timeout reduces
     * the number of calls made and increases throughput, but also increases
     * average message latency.
     */

    public void setMaxBatchOpenMs(long maxBatchOpenMs) {
        this.maxBatchOpenMs = maxBatchOpenMs;
    }

    public QueueBufferConfig withMaxBatchOpenMs(long maxBatchOpenMs) {
        this.maxBatchOpenMs = maxBatchOpenMs;
        return this;
    }

    /**
     * @return true if the queue buffer will use long polling while retrieveing
     *         messages from the SQS server, false otherwise.
     */

    public boolean isLongPoll() {
        return longPoll;
    }

    /**
     * Specify "true" for receive requests to use long polling.
     */

    public void setLongPoll(boolean longPoll) {
        this.longPoll = longPoll;
    }

    public QueueBufferConfig withLongPoll(boolean longPoll) {
        this.longPoll = longPoll;
        return this;
    }

    /**
     * The maximum number of concurrent batches for each type of outbound
     * request. The greater the number, the greater the throughput that can be
     * achieved (at the expense of consuming more threads).
     */

    public int getMaxInflightOutboundBatches() {
        return maxInflightOutboundBatches;
    }

    /**
     * The maximum number of concurrent batches for each type of outbound
     * request. The greater the number, the greater the throughput that can be
     * achieved (at the expense of consuming more threads).
     */

    public void setMaxInflightOutboundBatches(int maxInflightOutboundBatches) {
        this.maxInflightOutboundBatches = maxInflightOutboundBatches;
    }

    public QueueBufferConfig withMaxInflightOutboundBatches(int maxInflightOutboundBatches) {
        this.maxInflightOutboundBatches = maxInflightOutboundBatches;
        return this;
    }

    /**
     * The maximum number of concurrent receive message batches. The greater
     * this number, the faster the queue will be pulling messages from the SQS
     * servers (at the expense of consuming more threads).
     */

    public int getMaxInflightReceiveBatches() {
        return maxInflightReceiveBatches;
    }

    /**
     * The maximum number of concurrent receive message batches. The greater
     * this number, the faster the queue will be pulling messages from the SQS
     * servers (at the expense of consuming more threads).
     */
    public void setMaxInflightReceiveBatches(int maxInflightReceiveBatches) {
        this.maxInflightReceiveBatches = maxInflightReceiveBatches;
    }

    public QueueBufferConfig withMaxInflightReceiveBatches(int maxInflightReceiveBatches) {
        this.maxInflightReceiveBatches = maxInflightReceiveBatches;
        return this;
    }

    /**
     * If more than that number of completed receive batches are waiting in the
     * buffer, the querying for new messages will stop.<br>
     * The larger this number, the more messages the queue buffer will pre-fetch
     * and keep in the buffer on the client side, and the faster receive
     * requests will be satisfied. <br>
     * The visibility timeout of a pre-fetched message starts at the point of
     * pre-fetch, which means that while the message is in the local buffer it
     * is unavailable for other clients to process, and when this client
     * retrieves it, part of the visibility timeout may have already expired.<br>
     * The number of messages prefetched will not exceed 10 *
     * maxDoneReceiveBatches, as there can be a maximum of 10 messages per
     * batch.<br>
     */
    public int getMaxDoneReceiveBatches() {
        return maxDoneReceiveBatches;
    }

    /**
     * If more than that number of completed receive batches are waiting in the
     * buffer, the querying for new messages will stop.<br>
     * The larger this number, the more messages the queue buffer will pre-fetch
     * and keep in the buffer on the client side, and the faster receive
     * requests will be satisfied. <br>
     * The visibility timeout of a pre-fetched message starts at the point of
     * pre-fetch, which means that while the message is in the local buffer it
     * is unavailable for other clients to process, and when this client
     * retrieves it, part of the visibility timeout may have already expired.<br>
     * The number of messages prefetched will not exceed 10 *
     * maxDoneReceiveBatches, as there can be a maximum of 10 messages per
     * batch.<br>
     */
    public void setMaxDoneReceiveBatches(int maxDoneReceiveBatches) {
        this.maxDoneReceiveBatches = maxDoneReceiveBatches;
    }

    public QueueBufferConfig withMaxDoneReceiveBatches(int maxDoneReceiveBatches) {
        this.maxDoneReceiveBatches = maxDoneReceiveBatches;
        return this;
    }

    /**
     * Maximum permitted size of a SendMessage or SendMessageBatch message, in
     * bytes. This setting is also enforced on the server, and if this client
     * submits a request of a size larger than the server can support, the
     * server will reject the request.
     */
    public long getMaxBatchSizeBytes() {
        return maxBatchSizeBytes;
    }

    /**
     * Maximum permitted size of a SendMessage or SendMessageBatch message, in
     * bytes. This setting is also enforced on the server, and if this client
     * submits a request of a size larger than the server can support, the
     * server will reject the request.
     *
     * @throws IllegalArgumentException if the size being set is greater than
     *             the service allowed size for message body.
     */
    public void setMaxBatchSizeBytes(long maxBatchSizeBytes) {
        if (maxBatchSizeBytes > SERVICE_MAX_BATCH_SIZE_BYTES) {
            throw new IllegalArgumentException(
                    "Maximum Size of the message cannot be greater than the allowed limit of "
                            + SERVICE_MAX_BATCH_SIZE_BYTES + " bytes");
        }
        this.maxBatchSizeBytes = maxBatchSizeBytes;
    }

    public QueueBufferConfig withMaxBatchSizeBytes(long maxBatchSizeBytes) {
        this.maxBatchSizeBytes = maxBatchSizeBytes;
        return this;
    }

    /**
     * Custom visibility timeout to use when retrieving messages from SQS. If
     * set to a value greater than zero, this timeout will override the default
     * visibility timeout set on the SQS queue. Set it to -1 to use the default
     * visiblity timeout of the queue. Visibility timeout of 0 seconds is not
     * supported.
     */
    public int getVisibilityTimeoutSeconds() {
        return visibilityTimeoutSeconds;
    }

    /**
     * Custom visibility timeout to use when retrieving messages from SQS. If
     * set to a value greater than zero, this timeout will override the default
     * visibility timeout set on the SQS queue. Set it to -1 to use the default
     * visiblity timeout of the queue. Visibility timeout of 0 seconds is not
     * supported.
     */
    public void setVisibilityTimeoutSeconds(int visibilityTimeoutSeconds) {
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
    }

    public QueueBufferConfig withVisibilityTimeoutSeconds(int visibilityTimeoutSeconds) {
        this.visibilityTimeoutSeconds = visibilityTimeoutSeconds;
        return this;
    }

    /**
     * Specifies the amount of time, in seconds, the receive call will block on
     * the server waiting for messages to arrive if the queue is empty when the
     * receive call is first made. This setting has no effect if long polling is
     * disabled.
     */
    public void setLongPollWaitTimeoutSeconds(int longPollWaitTimeoutSeconds) {
        this.longPollWaitTimeoutSeconds = longPollWaitTimeoutSeconds;
    }

    /**
     * Specifies the amount of time, in seconds, the receive call will block on
     * the server waiting for messages to arrive if the queue is empty when the
     * receive call is first made. This setting has no effect if long polling is
     * disabled.
     */
    public int getLongPollWaitTimeoutSeconds() {
        return longPollWaitTimeoutSeconds;
    }

    public QueueBufferConfig withLongPollWaitTimeoutSeconds(int longPollWaitTimeoutSeconds) {
        this.longPollWaitTimeoutSeconds = longPollWaitTimeoutSeconds;
        return this;
    }

    /**
     * Specifies the maximum number of entries the bufferinc client will put in
     * a single batch request.
     */
    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    /**
     * Specifies the maximum number of entries the bufferinc client will put in
     * a single batch request.
     */
    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    /**
     * Specifies the maximum number of entries the bufferinc client will put in
     * a single batch request.
     */
    public QueueBufferConfig withMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
        return this;
    }

    /**
     * this method checks the config for validity. If the config is deemed to be
     * invalid, an informative exception is thrown.
     *
     * @throws AmazonClientException with a message explaining why the config
     *             was invalid
     */
    void validate() {
        if (visibilityTimeoutSeconds == 0) {
            throw new AmazonClientException("Visibility timeout value may not be equal to zero ");
        }
    }

}
