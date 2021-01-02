package com.github.adrian83.robome.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.github.adrian83.robome.common.Time;
import com.github.adrian83.robome.domain.user.model.Role;
import com.github.adrian83.robome.domain.user.model.User;

public class PermissionCheckerTest {

  @Test
  public void shouldAdminHaveAllPermissions() {
    // given
    var admin = userWithRoles(Role.ADMIN);

    // when, then
    assertTrue(PermissionChecker.canReadStages(admin));
    assertTrue(PermissionChecker.canReadTables(admin));
    assertTrue(PermissionChecker.canWriteStages(admin));
    assertTrue(PermissionChecker.canWriteTables(admin));
  }

  @Test
  public void shouldCheckIfUserHasPermissionToReadStages() {
    // given
    var userWithoutRole = userWithRoles();
    var userWithRole = userWithRoles(Role.READ_STAGES);

    // when, then
    assertFalse(PermissionChecker.canReadStages(userWithoutRole));
    assertTrue(PermissionChecker.canReadStages(userWithRole));
  }

  @Test
  public void shouldCheckIfUserHasPermissionToReadTables() {
    // given
    var userWithoutRole = userWithRoles();
    var userWithRole = userWithRoles(Role.READ_TABLES);

    // when, then
    assertFalse(PermissionChecker.canReadTables(userWithoutRole));
    assertTrue(PermissionChecker.canReadTables(userWithRole));
  }

  @Test
  public void shouldCheckIfUserHasPermissionToWriteStages() {
    // given
    var userWithoutRole = userWithRoles();
    var userWithRole = userWithRoles(Role.WRITE_STAGES);

    // when, then
    assertFalse(PermissionChecker.canWriteStages(userWithoutRole));
    assertTrue(PermissionChecker.canWriteStages(userWithRole));
  }

  @Test
  public void shouldCheckIfUserHasPermissionToWriteTables() {
    // given
    var userWithoutRole = userWithRoles();
    var userWithRole = userWithRoles(Role.WRITE_TABLES);

    // when, then
    assertFalse(PermissionChecker.canWriteTables(userWithoutRole));
    assertTrue(PermissionChecker.canWriteTables(userWithRole));
  }

  private User userWithRoles(Role... roles) {
	    return User.builder()
	        .id(UUID.randomUUID())
	        .email("johndoe@somedomain.com")
	        .passwordHash("fbwyflwegrl")
	        .roles(Arrays.asList(roles))
	        .modifiedAt(Time.utcNow())
	        .createdAt(Time.utcNow())
	        .build();
	  }
}
