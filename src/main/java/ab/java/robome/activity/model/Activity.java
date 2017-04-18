package ab.java.robome.activity.model;

import java.time.LocalDateTime;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import ab.java.robome.table.model.TableState;

@Value.Immutable
@JsonSerialize(as = ImmutableActivity.class)
@JsonDeserialize(as = ImmutableActivity.class)
public interface Activity {

	ActivityId id();

	String name();

	TableState state();

	LocalDateTime createdAt();

	LocalDateTime modifiedAt();

}
