package ru.nikita.abeserver.client.dto;

public interface TokenDTO {
    String getGuid();
    void setGuid(String guid);
    String getAttrbutes();
    void setAttrbutes(String attrbutes);
    Boolean getUsed();
    void setUsed(Boolean used);
    Long getCreate();
    void setCreate(Long create);
    Long getGenerate();
    void setGenerate(Long generate);
}
