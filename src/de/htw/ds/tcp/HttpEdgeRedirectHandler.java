package de.htw.ds.tcp;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import de.htw.tool.Copyright;


/**
 * Instances of this HTTP handler class redirect any request to an appropriate edge
 * server, based on timezone information provided as a query parameter.
 */
@Copyright(year=2014, holders="Sascha Baumeister")
public class HttpEdgeRedirectHandler implements HttpHandler {
	private final InetSocketAddress[] edgeServerAddresses;
	private final String scheme;


	/**
	 * Creates a new instance.
	 * @param edgeServerAddresses the edge server addresses
	 * @throws NullPointerException if the given argument is {@code null}
	 * @throws IllegalArgumentException if the given argument doesn't have 48 elements
	 */
	public HttpEdgeRedirectHandler (String scheme, final InetSocketAddress[] edgeServerAddresses) throws NullPointerException, IllegalArgumentException {
		if (edgeServerAddresses.length != 48) throw new IllegalArgumentException();
		this.scheme = Objects.requireNonNull(scheme);
		this.edgeServerAddresses = edgeServerAddresses;
	}


	/**
	 * Returns the edge server addresses.
	 * @return the edge server addresses
	 */
	public InetSocketAddress[] getEdgeServerAddresses () {
		return this.edgeServerAddresses;
	}


	/**
	 * Selects an edge server address corresponding to the given timezone offset.
	 * @param timezoneOffset a timezone offset in hours
	 * @return the selected edge server address
	 */
	public InetSocketAddress selectEdgeServerAddress (float timezoneOffset) {
		// TODO: return an edge server address based on the given timezone offset
		while (timezoneOffset < -12.0) timezoneOffset += 24;
		while (timezoneOffset > 11.5) timezoneOffset -= 24;
		final int index = (int) Math.floor(2 * timezoneOffset);

		return edgeServerAddresses[index];
	}


	/**
	 * Handles the given HTTP exchange by redirecting the request.
	 * @param exchange the HTTP exchange
	 * @throws NullPointerException if the given exchange is {@code null}
	 * @throws IOException if there is an I/O related problem
	 */
	@Override
	public void handle (final HttpExchange exchange) throws IOException {
		// TODO: implement by redirecting to an edge server matching the query parameter timezoneOffset.
		try {
			Map<String, String> query = parseQueryParameters(exchange.getRequestURI().getQuery());
			float timezoneOffset = Float.parseFloat(query.get("timezoneOffset"));
			
			final InetSocketAddress redirectServerAddress = this.selectEdgeServerAddress(timezoneOffset);
			
			final URI requestURI = exchange.getRequestURI();
			final String hostName = redirectServerAddress.getHostName();
			final int port = redirectServerAddress.getPort();
			final String path = requestURI.getPath();
			final URI redirectURI = URI.create(this.scheme + "://" + hostName + ":" + port + path); 
			final String ascii = redirectURI.toASCIIString();
			exchange.getRequestHeaders().add("Location", ascii);
			exchange.sendResponseHeaders(307, 0);
			
		} finally {
			exchange.close();
		}

		
	}


	/**
	 * Parses the parameters contained within the given URI query, and returns them as a map.
	 * @param uriQuery the URI query, or {@code null}
	 * @return the URI query parameters
	 */

	static private Map<String,String> parseQueryParameters (final String uriQuery) {
		final Map<String,String> result = new HashMap<String,String>();
		if (uriQuery == null) return result;

		for (final String association : uriQuery.split("&")) {
			final int offset = association.indexOf('=');
			final String key = association.substring(0, offset);
			final String value = association.substring(offset + 1);
			result.put(key, value);
		}

		return result;
	}
}