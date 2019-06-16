package de.htw.ds.tcp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.htw.tool.Copyright;


/**
 * Instances of this HTTP handler class redirect any request to redirect server.
 */
@Copyright(year=2014, holders="Sascha Baumeister")
public class HttpRedirectHandler implements HttpHandler {
	private final InetSocketAddress[] redirectServerAddresses;
	private final String scheme;

	/**
	 * Creates a new instance.
	 * @param sessionAware {@code true} if the server is aware of sessions, {@code false} otherwise
	 * @param redirectServerAddresses the redirect server addresses
	 */
	public HttpRedirectHandler (String scheme, final InetSocketAddress... redirectHostAddresses) {
		this.redirectServerAddresses = redirectHostAddresses;
		this.scheme = Objects.requireNonNull(scheme);
	}


	/**
	 * Returns the redirect server addresses.
	 * @return the redirect server addresses
	 */
	public InetSocketAddress[] getRedirectServerAddresses () {
		return this.redirectServerAddresses;
	}


	/**
	 * Selects a redirect server address corresponding to the given client address.
	 * @param clientAddress the client address
	 * @return the selected redirect server address
	 */

	public InetSocketAddress selectRedirectServerAddress (final InetAddress clientAddress) {
		final int serverIndex = ThreadLocalRandom.current().nextInt(this.redirectServerAddresses.length);
		return redirectServerAddresses[serverIndex];
	}


	/**
	 * Handles the given HTTP exchange by redirecting the request.
	 * @param exchange the HTTP exchange
	 * @throws NullPointerException if the given exchange is {@code null}
	 * @throws IOException if there is an I/O related problem
	 */
	@Override
	public void handle (final HttpExchange exchange) throws IOException {
		try {

			final InetSocketAddress redirectServerAddress = this.selectRedirectServerAddress(exchange.getRemoteAddress().getAddress());

			// TODO: create a redirect URI based on the exchange's request URI parts, and the redirect server address's
			// hostname and port. Set the response header "Location" of the exchange with this URI's
			// ASCII representation. Send the exchange's response headers using code 307 (temporary redirect)
			// and zero as reponse length. Note that the schema of the redirect URI will usually be null,
			// which works fine.
			final URI requestURI = exchange.getRequestURI();
			final String hostName = redirectServerAddress.getHostName();
			final int port = redirectServerAddress.getPort();
			final String path = requestURI.getPath();
			final URI redirectURI = URI.create(this.scheme + "://" + hostName + ":" + port + path); 
			final String ascii = redirectURI.toASCIIString();
			exchange.getRequestHeaders().add("Location", ascii);
			exchange.sendResponseHeaders(307, 0);
			Logger.getGlobal().log(Level.INFO, "Redirected request for \"{0}\" to \"{1}\".", new URI[] { requestURI, redirectURI });
		} catch(Throwable e) { 
			e.printStackTrace();
			throw e;
		} finally {

			exchange.close();
		}
	}
}
