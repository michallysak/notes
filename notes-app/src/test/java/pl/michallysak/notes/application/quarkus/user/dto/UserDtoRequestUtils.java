package pl.michallysak.notes.application.quarkus.user.dto;

public class UserDtoRequestUtils {
  private static final String PASSWORD = "Pass123!";

  public static RegisterUserRequest.RegisterUserRequestBuilder getRegisterUserRequestBuilder(
      String email) {
    return RegisterUserRequest.builder().email(email).password(PASSWORD);
  }

  public static LoginUserRequest.LoginUserRequestBuilder getLoginUserRequestBuilder(String email) {
    return LoginUserRequest.builder().email(email).password(PASSWORD);
  }
}
