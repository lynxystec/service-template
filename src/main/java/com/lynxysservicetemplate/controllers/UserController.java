package com.lynxysservicetemplate.controllers;

import com.lynxysservicetemplate.entities.UserEntity;
import com.lynxysservicetemplate.models.UserModel;
import com.lynxysservicetemplate.services.UserService;
import com.lynxysservicetemplate.utils.CookieUtil;
import com.lynxysservicetemplate.utils.JwtTokenUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.lynxysservicetemplate.utils.Constants.LOGN_SUCCESSFUL;
import static com.lynxysservicetemplate.utils.Constants.USER_ADDED_SUCCESSFULLY;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "api/v1/users")
public class UserController {

    private final UserService userService;
    private final JwtTokenUtil jwtTokenUtil;
    private final CookieUtil cookieUtil;

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserModel userModel) {
        try {
            userService.addUser(userModel);
            return ResponseEntity.status(HttpStatus.CREATED).body(USER_ADDED_SUCCESSFULLY);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(exception.getMessage());
        }
    }

    @PostMapping(path = "/login")
    public ResponseEntity<String> login(@RequestBody UserModel userModel, HttpServletResponse response) {
        try {
            UserEntity userEntity = userService.login(userModel);

            String accessToken = jwtTokenUtil.generateAccessToken(userEntity.getEmail());
            String refreshToken = jwtTokenUtil.generateRefreshToken(userEntity.getEmail());

            cookieUtil.setTokensAsCookies(response, accessToken, refreshToken);

            return ResponseEntity.ok(LOGN_SUCCESSFUL);
        } catch (IllegalStateException exception) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(exception.getMessage());
        }
    }

}
