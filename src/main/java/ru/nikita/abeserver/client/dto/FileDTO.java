package ru.nikita.abeserver.client.dto;

public interface FileDTO {
    String getGuid();
    void setGuid(String guid);
    String getName();
    void setName(String name);
    String getAttributes();
    void setAttributes(String attributes);
    UserFTPDTO getFtp();
    void setFtp(UserFTPDTO ftp);
    Long getCreate();
    void setCreate(Long create);
}
