package com.lourdes.inztagram.controller;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserNameAndPassword;
import com.lourdes.inztagram.model.UuidStingOnly;
import com.lourdes.inztagram.repository.UserDetailsRepository;
import com.lourdes.inztagram.repository.UserLoginMappingRepository;
import com.lourdes.inztagram.viewModel.UserViewModel;

@RestController
public class UserController {
    private UserViewModel viewModel = new UserViewModel();

    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private UserLoginMappingRepository userLoginMappingRepository;

    @PostMapping("/userRegister")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody UserDetails user) {
        RegistrationValidationStatus validationStatus = viewModel.validateRegistrationFields(user);
        if(validationStatus != RegistrationValidationStatus.GOOD_TO_GO) {
            return new ResponseEntity<>("{ \"error\":" + viewModel.getRegistrationValidationStatusString(validationStatus) +" }", HttpStatus.BAD_REQUEST);
        }
        if(viewModel.doesUserExist(user.getUserName(), userDetailsRepository)) {
            return new ResponseEntity<>("{ \"error\": \"User Already Exists\"}", HttpStatus.CONFLICT);
        }
        viewModel.registerUser(user, userDetailsRepository);
        return new ResponseEntity<>("{ \"success\": \"User Created\"}", HttpStatus.OK);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> loginuser(@RequestBody UserNameAndPassword userNameAndPassword) {
        if(userNameAndPassword.getUserName() == null || userNameAndPassword.getPassword() == null) {
            return new ResponseEntity<>("{ \"error\": \"Please Enter Both Username and Password\"}", HttpStatus.BAD_REQUEST);
        }
        Optional<String> uuidStringOptional = viewModel.loginUserAndGetSecretKey(userNameAndPassword.getUserName(), userNameAndPassword.getPassword(), userDetailsRepository, userLoginMappingRepository);
        if(uuidStringOptional == null) {
            return new ResponseEntity<>("{ \"error\": \"Wrong Password or Invlid User\"}", HttpStatus.UNAUTHORIZED);
        }
        UuidStingOnly uuidStingOnly = new UuidStingOnly();
        uuidStingOnly.setUuid(uuidStringOptional.get());
        return new ResponseEntity<>(uuidStingOnly, HttpStatus.OK);
    }
}