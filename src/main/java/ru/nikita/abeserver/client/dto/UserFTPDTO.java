package ru.nikita.abeserver.client.dto;

public interface UserFTPDTO {
    Long getId();
    void setId(Long id);
    String getUrl();
    void setUrl(String url);
    Integer getPort();
    void setPort(Integer port);
    String getLogin();
    void setLogin(String login);
    String getPass();
    void setPass(String pass);
}
