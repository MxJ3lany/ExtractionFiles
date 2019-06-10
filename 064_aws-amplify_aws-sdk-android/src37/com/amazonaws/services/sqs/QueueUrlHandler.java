/*
 * Copyright 2010-2012 Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package com.amazonaws.services.sqs;

import com.amazonaws.AmazonClientException;
import com.amazonaws.Request;
import com.amazonaws.handlers.AbstractRequestHandler;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Custom request handler for SQS that processes the request before it gets
 * routed to the client runtime layer.
 * <p>
 * SQS MessageQueue operations take a QueueUrl parameter that needs special
 * handling to update the endpoint and resource path on the request before it's
 * executed.
 */
public class QueueUrlHandler extends AbstractRequestHandler {
    private static final String QUEUE_URL_PARAMETER = "QueueUrl";

    @Override
    public void beforeRequest(Request<?> request) {
        if (request.getParameters().get(QUEUE_URL_PARAMETER) != null) {
            String queueUrl = request.getParameters().remove(QUEUE_URL_PARAMETER);

            try {
                URI uri = new URI(queueUrl);
                request.setResourcePath(uri.getPath());

                if (uri.getHost() != null) {
                    // If the URI has a host specified, set the request's
                    // endpoint to the queue URLs
                    // endpoint, so that queue URLs from different regions will
                    // send the request to
                    // the correct endpoint.
                    URI uriWithoutPath = new URI(uri.toString().replace(uri.getPath(), ""));
                    request.setEndpoint(uriWithoutPath);
                }
            } catch (URISyntaxException e) {
                throw new AmazonClientException("Unable to parse SQS queue URL '" + queueUrl + "'",
                        e);
            }
        }
    }
}
