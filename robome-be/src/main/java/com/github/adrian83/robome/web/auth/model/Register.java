package com.github.adrian83.robome.web.auth.model;

import static com.github.adrian83.robome.common.validation.Validation.check;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.adrian83.robome.common.validation.ValidationError;
import com.github.adrian83.robome.common.validation.Validator;
import com.google.common.base.Strings;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@Builder
@ToString(exclude = {"password", "repeatedPassword"})
@EqualsAndHashCode
public class Register implements Validator {

  private static final String EMAIL_LABEL = "email";
  private static final String EMPTY_EMAIL_KEY = "user.register.email.empty";
  private static final String EMPTY_EMAIL_MSG = "Email cannot be empty";

  private static final String PASSWORD_1_LABEL = "password";
  private static final String EMPTY_PASSWORD_1_KEY = "user.register.password.empty";
  private static final String EMPTY_PASSWORD_1_MSG = "Password cannot be empty";

  private static final String PASSWORD_2_LABEL = "repeatedPassword";
  private static final String DIFFERENT_PASSWORDS_KEY = "user.register.passwords.different";
  private static final String DIFFERENT_PASSWORDS_MSG = "Repeated password cannot be different";

  private static final ValidationError EMPTY_EMAIL =
      ValidationError.builder()
          .field(EMAIL_LABEL)
          .messageCode(EMPTY_EMAIL_KEY)
          .message(EMPTY_EMAIL_MSG)
          .build();

  private static final ValidationError EMPTY_PASSWORD_1 =
      ValidationError.builder()
          .field(PASSWORD_1_LABEL)
          .messageCode(EMPTY_PASSWORD_1_KEY)
          .message(EMPTY_PASSWORD_1_MSG)
          .build();

  private static final ValidationError DIFFERENT_PASSWORDS =
      ValidationError.builder()
          .field(PASSWORD_2_LABEL)
          .messageCode(DIFFERENT_PASSWORDS_KEY)
          .message(DIFFERENT_PASSWORDS_MSG)
          .build();

  private String email;
  private String password;
  private String repeatedPassword;

  @JsonCreator
  public Register(
      @JsonProperty(EMAIL_LABEL) String email,
      @JsonProperty(PASSWORD_1_LABEL) String password,
      @JsonProperty(PASSWORD_2_LABEL) String repeatedPassword) {
    super();
    this.email = email;
    this.password = password;
    this.repeatedPassword = repeatedPassword;
  }

  @Override
  public List<ValidationError> validate() {
    return Stream.of(
            check(email, EMPTY_EMAIL, Strings::isNullOrEmpty),
            check(password, EMPTY_PASSWORD_1, Strings::isNullOrEmpty),
            check(repeatedPassword, DIFFERENT_PASSWORDS, (String pass2) -> !pass2.equals(password)))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .collect(Collectors.toList());
  }
}
