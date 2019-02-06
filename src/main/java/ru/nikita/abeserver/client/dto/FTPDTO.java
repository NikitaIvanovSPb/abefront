package ru.nikita.abeserver.client.dto;

public interface FTPDTO extends KeyDTO {
    Long getId();
    void setId(Long id);
    String getUrl();
    void setUrl(String url);
    Integer getPort();
    void setPort(Integer port);
    String getAdminLogin();
    void setAdminLogin(String adminLogin);
    String getAdminPass();
    void setAdminPass(String adminPass);
    String getUserLogin();
    void setUserLogin(String userLogin);
    String getUserPass();
    void setUserPass(String userPass);
}
