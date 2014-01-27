/*
 * Copyright (C) 2013 SCALITY SA. All rights reserved.
 * http://www.scality.com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY SCALITY SA ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL SCALITY SA OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of SCALITY SA.
 *
 * https://github.com/scality/CaDMIum
 */
package com.scality.sofs.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.thread.QueuedThreadPool;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

/**
 * 
 * A server providing a service to upload geosync data. Create a server through:
 * <code>GeoSyncServer.createServer(...)</code>
 * 
 * 
 * @author julien.muller@ezako.com for Scality
 * 
 */
public class GeoSyncServer extends AbstractHandler {

	/**
	 * A default port for the server. It is highly recommended to customize this
	 * value with
	 * {@link GeoSyncServer#createServer(int, int, GeoSyncPayLoadProcessor)}
	 */
	protected static final int DEFAULT_PORT = 5201;
	/**
	 * A default thread's pool size. Should be able to handle small to medium
	 * load
	 */
	protected static final int DEFAULT_THREAD_NB = 5;

	/** Default content type for the http interaction */
	protected static final String CONTENT_TYPE_JSON = "application/json";

	/** A processor to process the payLoad */
	protected GeoSyncPayLoadProcessor processor;

	/**
	 * protected constructor in order to get users to use createServer()
	 * 
	 * @param processor
	 */
	protected GeoSyncServer(GeoSyncPayLoadProcessor processor) {
		this.processor = processor;
	}

	/**
	 * 
	 * This method handles calls with the following json format:
	 * 
	 * <pre>
	 * <code>
	 * {
	 *  "Method" : "scality",
	 *  "scalitylog": "GEOSYNC content"
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @see org.eclipse.jetty.server.Handler#handle(java.lang.String,
	 *      org.eclipse.jetty.server.Request,
	 *      javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void handle(String target, Request baseRequest,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {

		if (!CONTENT_TYPE_JSON.equals(request.getContentType())) {
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"Content type should be json");
			return;
		}

		InputStream is = request.getInputStream();

		JsonParser parser = new JsonParser();
		JsonObject root;
		try {
			root = parser.parse(new InputStreamReader(is)).getAsJsonObject();
		} catch (IllegalStateException e) {
			// Send error
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"The payload is not a JSON Oject");
			return;
		} catch (JsonSyntaxException e) {
			// Send error
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"The payload contains a Json Syntax error");
			return;
		} catch (JsonParseException e) {
			// Send error
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"The payload was not parseable as a Json");
			return;
		}

		JsonElement methodElement = root.get("Method");
		if (methodElement == null) {
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"Json should contain a method element");
			return;
		}
		String method = methodElement.getAsString();
		if (!"scality".equals(method)) {
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"Json should contain a method element with value: scality");
			return;
		}

		JsonElement payLoadElement = root.get("scalitylog");
		if (payLoadElement == null) {
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"Json should contain a scalitylog element");
			return;
		}
		String payLoad = payLoadElement.getAsString();

		if ((payLoad == null) || ("".equals(payLoad))) {
			sendError(response, baseRequest,
					HttpServletResponse.SC_BAD_REQUEST,
					"scalitylog in Json is empty!");
			return;
		}

		if (processor != null) {
			try {
				processor.process(payLoad);
			} catch (GeoSyncPayLoadProcessingException e) {
				// send error
				sendError(response, baseRequest, e.getErrorCode(),
						e.getMessage());
				return;
			}
		}

		// Better to set a content type as some clients
		// will not be able to handle the return code otherwise
		response.setContentType(CONTENT_TYPE_JSON);
		response.setStatus(HttpServletResponse.SC_OK);
		baseRequest.setHandled(true);
	}

	/**
	 * @param processor
	 * @return a jetty server ready to start
	 */
	public static Server createServer(GeoSyncPayLoadProcessor processor) {
		return createServer(DEFAULT_PORT, DEFAULT_THREAD_NB, processor);
	}

	/**
	 * @param port
	 * @param processor
	 * @return a jetty server, ready to start
	 */
	public static Server createServer(int port,
			GeoSyncPayLoadProcessor processor) {
		return createServer(port, DEFAULT_THREAD_NB, processor);
	}

	/**
	 * @param port
	 * @param poolSize
	 * @param processor
	 * @return a jetty server ready to be started
	 */
	public static Server createServer(int port, int poolSize,
			GeoSyncPayLoadProcessor processor) {
		Server server = new Server(port);
		GeoSyncServer geoServer = new GeoSyncServer(processor);
		server.setHandler(geoServer);
		// Threads pool
		QueuedThreadPool threadPool = new QueuedThreadPool();
		threadPool.setMaxThreads(poolSize);
		server.setThreadPool(threadPool);

		return server;
	}

	/*
	 * Just a bean to create a serializable Json object for responses in error
	 */
	private static class JsonError {
		public JsonError(String msg) {
			error = msg;
		}

		// This is actually used in sendError, but compiler does not understand
		@SuppressWarnings("unused")
		public String error;
	}

	/* error sending */
	private static void sendError(HttpServletResponse response,
			Request baseRequest, int code, String message) throws IOException {
		response.setStatus(code);
		response.setContentType(CONTENT_TYPE_JSON);
		response.getWriter().write(new Gson().toJson(new JsonError(message)));
		baseRequest.setHandled(true);
	}

}
