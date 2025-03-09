package com.lynxysservicetemplate.services;

import com.lynxysservicetemplate.entities.UserEntity;
import com.lynxysservicetemplate.mappers.UserMapper;
import com.lynxysservicetemplate.models.UserModel;
import com.lynxysservicetemplate.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.lynxysservicetemplate.utils.Constants.*;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public void addUser(UserModel userModel) {
        avoidDuplicity(userModel.getEmail());
        UserEntity userEntity = userMapper.toEntity(userModel);
        userEntity.setSecret(passwordEncoder.encode(userModel.getSecret()));
        userRepository.save(userEntity);
    }

    private void avoidDuplicity(String email) {
        UserEntity userEntity = userRepository.findByEmail(email).orElse(null);
        if (userEntity != null) {
            throw new IllegalStateException(USER_ALREADY_EXISTS);
        }
    }

    public UserEntity login(UserModel userModel) {
        UserEntity userEntity = userRepository.findByEmail(userModel.getEmail()).orElse(null);

        if (userEntity == null) {
            throw new IllegalStateException(USER_NOT_FOUND);
        }

        if (!passwordEncoder.matches(userModel.getSecret(), userEntity.getSecret())) {
            throw new IllegalStateException(INVALID_CREDENTIALS);
        }

        return userEntity;
    }

}
