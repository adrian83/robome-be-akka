package ab.java.robome.table;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;
import java.util.function.BiFunction;

import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SimpleStatement;
import com.datastax.driver.core.Statement;
import com.google.inject.Inject;

import ab.java.robome.common.time.TimeUtils;
import ab.java.robome.table.model.ImmutableTable;
import ab.java.robome.table.model.Table;
import ab.java.robome.table.model.TableState;
import akka.Done;
import akka.NotUsed;
import akka.actor.ActorSystem;
import akka.stream.alpakka.cassandra.javadsl.CassandraSink;
import akka.stream.alpakka.cassandra.javadsl.CassandraSource;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableRepository {

	private static final String INSERT_TABLE_STMT = "INSERT INTO robome.tables (id, name, state, "
			+ "created_at, modified_at) VALUES (?, ?, ?, ?, ?)";
	private static final String SELECT_ALL_STMT = "SELECT * FROM robome.tables";
	private static final String SELECT_BY_ID_STMT = "SELECT * FROM robome.tables WHERE id = ?";

	private Session session;
	private ActorSystem actorSystem;

	@Inject
	public TableRepository(Session session, ActorSystem actorSystem) {
		this.session = session;
		this.actorSystem = actorSystem;
	}

	public Sink<Table, CompletionStage<Done>> saveTable() {
		PreparedStatement preparedStatement = session.prepare(INSERT_TABLE_STMT);
		
		BiFunction<Table, PreparedStatement, BoundStatement> statementBinder = (tab, statement) -> {
			Date created = TimeUtils.toDate(tab.createdAt());
			Date modified = TimeUtils.toDate(tab.modifiedAt());
			return statement.bind(tab.id(), tab.name(), tab.state().name(), created, modified);
		};

		Sink<Table, CompletionStage<Done>> sink = CassandraSink.create(1, preparedStatement, statementBinder, session,
				actorSystem.dispatcher());
		return sink;
	}

	public Optional<Table> getById(UUID tableId) {
		PreparedStatement preparedStatement = session.prepare(SELECT_BY_ID_STMT);
		BoundStatement bound = preparedStatement.bind(tableId);

		ResultSet r = session.execute(bound);

		Row row = r.one();
		if (row == null) {
			return Optional.empty();
		}
		
		Table table = ImmutableTable.builder()
				.id(row.get("id", UUID.class))
				.name(row.getString("name"))
				.state(TableState.valueOf(row.getString("state")))
				.createdAt(TimeUtils.toUtcLocalDate(row.getTimestamp("created_at")))
				.modifiedAt(TimeUtils.toUtcLocalDate(row.getTimestamp("modified_at")))
				.build();

		return Optional.of(table);
	}

	public Source<Row, NotUsed> getAllTables() {
		final Statement stmt = new SimpleStatement(SELECT_ALL_STMT).setFetchSize(1000);
		Source<Row, NotUsed> source = CassandraSource.create(stmt, session);
		return source;
	}

}
