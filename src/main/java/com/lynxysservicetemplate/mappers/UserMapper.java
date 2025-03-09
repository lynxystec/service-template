package com.lynxysservicetemplate.mappers;

import com.lynxysservicetemplate.entities.UserEntity;
import com.lynxysservicetemplate.models.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(UserModel userModel) {
        UserEntity userEntity = new UserEntity();
        BeanUtils.copyProperties(userModel, userEntity);
        return userEntity;
    }

}
