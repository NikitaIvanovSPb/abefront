package ru.nikita.abeserver.client.dto;

import java.util.Date;

public interface UserDTO {
    Long getId();

    void setId(Long id);

    String getLogin();

    void setLogin(String login);

    Long getRegistrationDate();

    void setRegistrationDate(Long registrationDate);

    String getName();

    void setName(String name);

    String getLastName();

    void setLastName(String lastName);

    String getPatronymic();

    void setPatronymic(String patronymic);

    String getEmail();

    void setEmail(String email);

    Boolean getAdmin();

    void setAdmin(Boolean admin);

    String getPassword();

    void setPassword(String password);
}
