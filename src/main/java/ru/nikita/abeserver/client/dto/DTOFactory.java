package ru.nikita.abeserver.client.dto;

import com.google.web.bindery.autobean.shared.AutoBean;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;

public interface DTOFactory extends AutoBeanFactory {
    AutoBean<UserDTO> usersDTOFactory();
    AutoBean<UserDTOList> usersDTOListFactory();
    AutoBean<FileDTO> filesDTOFactory();
    AutoBean<FileDTOList> filesDTOListFactory();
    AutoBean<KeyDTO> keysDTOFactory();
    AutoBean<KeyDTOList> keysDTOListFactory();
    AutoBean<FTPDTO> FTPDTOFactory();
    AutoBean<FTPDTOList> FTPDTOListFactory();
    AutoBean<UserFTPDTO> UserFTPDTOFactory();
    AutoBean<TokenDTO> tokenFactory();
    AutoBean<TokenDTOList> tokenListFactory();
    AutoBean<AttributeDTO> attrFactory();
    AutoBean<AttributeDTOList> attrListFactory();
}
