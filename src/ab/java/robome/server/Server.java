package ab.java.robome.server;

import akka.actor.ActorSystem;
import akka.http.javadsl.model.HttpEntities;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.ContentType;
import akka.http.javadsl.model.ContentTypes;
import akka.http.javadsl.server.HttpApp;
import akka.http.javadsl.server.Route;
import akka.http.scaladsl.model.HttpEntity.Chunked;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.io.IOException;

public class Server extends HttpApp {

	public static void main(String[] args) throws IOException {

		final ActorSystem system = ActorSystem.create();

		final Server server = new Server();
		server.bindRoute("127.0.0.1", 8080, system);

		System.in.read();
		system.shutdown();
	}

	@Override
	public Route createRoute() {
		return route(
				get(
						pathEndOrSingleSlash().route(complete(index())), 
						path("ping").route(complete("PONG!"))
					));
	}

	private HttpResponse index() {

		final Source<ByteString, Object> data = Source
				.single("<html><body><h1>Hello Akka HTTP</h1></body></html>")
				.map(ByteString::fromString)
				.mapMaterializedValue(s -> null);

		final ContentType ct = ContentTypes.TEXT_HTML_UTF8;
		final Chunked chunked = HttpEntities.create(ct, data);
		return HttpResponse.create().withEntity(chunked);
	}

}