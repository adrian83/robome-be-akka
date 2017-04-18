package ab.java.robome.stage.model;

import org.immutables.value.Value;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


@Value.Immutable
@JsonSerialize(as = ImmutableNewStage.class)
@JsonDeserialize(as = ImmutableNewStage.class)
public interface NewStage {
	
	String name();
	
}