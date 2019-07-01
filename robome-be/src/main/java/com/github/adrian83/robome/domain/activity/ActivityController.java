package com.github.adrian83.robome.domain.activity;

import static akka.http.javadsl.server.PathMatchers.segment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletionStage;

import com.github.adrian83.robome.auth.JwtAuthorizer;
import com.github.adrian83.robome.common.time.TimeUtils;
import com.github.adrian83.robome.common.web.AbstractController;
import com.github.adrian83.robome.common.web.ExceptionHandler;
import com.github.adrian83.robome.common.web.Response;
import com.github.adrian83.robome.domain.stage.StageController;
import com.github.adrian83.robome.domain.stage.StageId;
import com.github.adrian83.robome.domain.table.TableController;
import com.github.adrian83.robome.domain.table.model.TableState;
import com.github.adrian83.robome.domain.user.User;
import com.google.inject.Inject;
import com.typesafe.config.Config;

import akka.Done;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.model.StatusCodes;
import akka.http.javadsl.model.headers.Location;
import akka.http.javadsl.server.Route;

public class ActivityController extends AbstractController {

	public static final String PATH = "activities";

	private ActivityService activityService;

	@Inject
	public ActivityController(JwtAuthorizer jwtAuthorizer, Config config,
			ActivityService activityService, ExceptionHandler exceptionHandler, Response responseProducer) {
		super(jwtAuthorizer, exceptionHandler,config, responseProducer);
		this.activityService = activityService;
	}

	public Route createRoute() {
		return route(
				get(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> pathPrefix(segment(), activityId -> jwtSecured(tableId, stageId, activityId, this::getActivityById)))))))),

				post(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> jwtSecured(tableId, stageId, NewActivity.class, this::persistActivity))))))),
				
				get(() -> pathPrefix(TableController.TABLES, () -> pathPrefix(segment(), tableId -> pathPrefix(StageController.PATH, () -> pathPrefix(segment(), stageId -> pathPrefix(PATH, () -> jwtSecured(tableId, stageId, this::getStageActivities))))))));
	}

	private Route getStageActivities(CompletionStage<Optional<User>> maybeUserF, String tableIdStr, String stageIdStr) {
		
		UUID tableUuid = UUID.fromString(tableIdStr);
		UUID stageUuid = UUID.fromString(stageIdStr);

		StageId stageId = new StageId(tableUuid, stageUuid);

		final CompletionStage<List<Activity>> futureStages = activityService.getStageActivities(stageId);
		return onSuccess(() -> futureStages, stages -> complete(responseProducer.jsonFromObject(stages)));
	}

	private Route getActivityById(CompletionStage<Optional<User>> maybeUserF, String tableId, String stageId, String activityId) {
		
		UUID tableUuid = UUID.fromString(tableId);
		UUID stageUuid = UUID.fromString(stageId);
		UUID activityUuid = UUID.fromString(activityId);
		
		ActivityId id = new ActivityId(tableUuid, stageUuid, activityUuid);

		final CompletionStage<Optional<Activity>> futureMaybeTable = activityService.getActivity(id);

		return onSuccess(() -> futureMaybeTable,
				maybeItem -> complete(responseProducer.jsonFromOptional(maybeItem)));
	}

	private Route persistActivity(CompletionStage<Optional<User>> maybeUserF, String tableId, String stageId, NewActivity newActivity) {

		UUID tableUuid = UUID.fromString(tableId);
		UUID stageUuid = UUID.fromString(stageId);
		UUID activityUuid = UUID.randomUUID();
		
		LocalDateTime utcNow = TimeUtils.utcNow();
		

		Location locationHeader = locationFor(TableController.TABLES, tableId, StageController.PATH, stageId, PATH,
				activityUuid.toString());

		ActivityId activityId = new ActivityId(tableUuid, stageUuid, activityUuid);

		Activity activity = new Activity(activityId, newActivity.getName(), TableState.ACTIVE, utcNow, utcNow);

		HttpResponse redirectResponse = HttpResponse.create().withStatus(StatusCodes.CREATED).addHeader(locationHeader);

		CompletionStage<Done> futureSaved = activityService.saveActivity(activity);
		return onSuccess(() -> futureSaved, done -> complete(redirectResponse));
	}

}
