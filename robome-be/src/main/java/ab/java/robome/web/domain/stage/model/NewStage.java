package ab.java.robome.web.domain.stage.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class NewStage {

	private String name;

	@JsonCreator
	public NewStage(@JsonProperty("name") String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
