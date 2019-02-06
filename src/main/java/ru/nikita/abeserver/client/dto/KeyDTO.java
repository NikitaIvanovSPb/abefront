package ru.nikita.abeserver.client.dto;


public interface KeyDTO {
    Long getId();
    void setId(Long id);
    String getName();
    void setName(String name);
    Long getCreate();
    void setCreate(Long create);
    boolean getActive();
    void setActive(boolean active);
}
