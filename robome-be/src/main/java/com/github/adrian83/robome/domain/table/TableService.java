package com.github.adrian83.robome.domain.table;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;

import com.github.adrian83.robome.domain.stage.StageService;
import com.github.adrian83.robome.domain.table.model.NewTable;
import com.github.adrian83.robome.domain.table.model.Table;
import com.github.adrian83.robome.domain.table.model.TableEntity;
import com.github.adrian83.robome.domain.table.model.TableKey;
import com.github.adrian83.robome.domain.table.model.UpdatedTable;
import com.github.adrian83.robome.domain.user.model.User;
import com.google.inject.Inject;

import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

public class TableService {

  private TableRepository tableRepository;
  private StageService stageService;
  private ActorSystem actorSystem;

  @Inject
  public TableService(
      TableRepository tableRepository, StageService stageService, ActorSystem actorSystem) {
    this.tableRepository = tableRepository;
    this.stageService = stageService;
    this.actorSystem = actorSystem;
  }

  public CompletionStage<Optional<Table>> getTable(User user, TableKey tableKey) {
    return tableRepository
        .getById(user.getId(), tableKey.getTableId())
        .map((maybeEntity) -> maybeEntity.map(this::toTable))
        .mapAsync(1, (table) -> fetchStages(user, tableKey, table))
        .runWith(Sink.head(), actorSystem);
  }

  public CompletionStage<Table> saveTable(User user, NewTable newTable) {
    var entity = new TableEntity(user.getId(), newTable.getTitle(), newTable.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .saveTable()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<Table> updateTable(
      User user, TableKey tableID, UpdatedTable updatedTable) {

    TableEntity entity =
        TableEntity.newTable(
            tableID, user.getId(), updatedTable.getTitle(), updatedTable.getDescription());

    Sink<TableEntity, CompletionStage<Table>> sink =
        tableRepository
            .updateTable()
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> toTable(entity)));

    return Source.single(entity).runWith(sink, actorSystem);
  }

  public CompletionStage<List<Table>> getTables(User user) {
    return tableRepository
        .getUserTables(user.getId())
        .map(this::toTable)
        .runWith(Sink.seq(), actorSystem);
  }

  public CompletionStage<TableKey> deleteTable(User user, TableKey tableKey) {
    Sink<TableKey, CompletionStage<TableKey>> sink =
        tableRepository
            .deleteTable(user.getId())
            .mapMaterializedValue(doneF -> doneF.thenApply(done -> tableKey));

    return Source.single(tableKey).runWith(sink, actorSystem);
  }

  private Table toTable(TableEntity entity) {
    return new Table(
        entity.getKey(),
        entity.getUserId(),
        entity.getTitle(),
        entity.getDescription(),
        entity.getState(),
        entity.getCreatedAt(),
        entity.getModifiedAt());
  }

  private CompletionStage<Optional<Table>> fetchStages(User user, Table table) {
    return stageService
        .getTableStages(user, table.getKey())
        .thenApply((stages) -> table.withStages(stages))
        .thenApply(Optional::ofNullable);
  }

  private CompletionStage<Optional<Table>> fetchStages(
      User user, TableKey tableKey, Optional<Table> maybeTable) {
    return maybeTable
        .map((table) -> fetchStages(user, table))
        .orElse(CompletableFuture.<Optional<Table>>completedFuture(Optional.<Table>empty()));
  }
}
