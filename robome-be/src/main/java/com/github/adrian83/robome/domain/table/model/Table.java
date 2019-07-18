package com.github.adrian83.robome.domain.table.model;

import java.time.LocalDateTime;
import java.util.UUID;

import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.domain.common.Ownerable;

public class Table implements Ownerable {

  private TableKey key;
  private UUID userId;
  private String title;
  private String description;
  private TableState state;
  private LocalDateTime createdAt;
  private LocalDateTime modifiedAt;

  public Table(UUID userId, String title, String description) {
    this(
        new TableKey(),
        userId,
        title,
        description,
        TableState.ACTIVE,
        TimeUtils.utcNow(),
        TimeUtils.utcNow());
  }

  public Table(
      TableKey key,
      UUID userId,
      String title,
      String description,
      TableState state,
      LocalDateTime createdAt,
      LocalDateTime modifiedAt) {
    super();
    this.key = key;
    this.userId = userId;
    this.title = title;
    this.description = description;
    this.state = state;
    this.createdAt = createdAt;
    this.modifiedAt = modifiedAt;
  }

  public static Table newTable(TableKey id, UUID userId, String title, String description) {
    return new Table(
        id, userId, title, description, TableState.ACTIVE, TimeUtils.utcNow(), TimeUtils.utcNow());
  }

  public static Table updatedTable(
      TableKey id,
      UUID userId,
      String title,
      String description,
      TableState state,
      LocalDateTime createdAt) {
    return new Table(id, userId, title, description, state, createdAt, TimeUtils.utcNow());
  }

  public TableKey getKey() {
    return key;
  }

  public UUID getUserId() {
    return userId;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public TableState getState() {
    return state;
  }

  public LocalDateTime getCreatedAt() {
    return createdAt;
  }

  public LocalDateTime getModifiedAt() {
    return modifiedAt;
  }

  @Override
  public UUID getOwner() {
    return getUserId();
  }
}