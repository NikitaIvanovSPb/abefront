package ru.nikita.abeserver.client.dto;

import java.util.List;

public interface UserDTOList {
    List<UserDTO> getResults();
    void setResults(List<UserDTO> list);
}
