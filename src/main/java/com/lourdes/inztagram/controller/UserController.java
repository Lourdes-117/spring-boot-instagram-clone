package com.lourdes.inztagram.controller;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.FileUploadDetailRequest;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserNameAndPassword;
import com.lourdes.inztagram.model.UuidStingOnly;
import com.lourdes.inztagram.repository.FileUploadDetailRepository;
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
    @Autowired
    private FileUploadDetailRepository fileUploadDetailRepository;

    @PostMapping("/userRegister")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody UserDetails user) {
        RegistrationValidationStatus validationStatus = viewModel.validateRegistrationFields(user);
        if(validationStatus != RegistrationValidationStatus.GOOD_TO_GO) {
            return new ResponseEntity<>("{\"error\":" + viewModel.getRegistrationValidationStatusString(validationStatus) +" }", HttpStatus.BAD_REQUEST);
        }
        if(viewModel.doesUserExist(user.getUserName(), userDetailsRepository)) {
            return new ResponseEntity<>("{\"error\": \"User Already Exists\"}", HttpStatus.OK);
        }
        viewModel.registerUser(user, userDetailsRepository);
        return new ResponseEntity<>("{\"success\": \"User Created\"}", HttpStatus.OK);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> loginuser(@RequestBody UserNameAndPassword userNameAndPassword) {
        if(userNameAndPassword.getUserName() == null || userNameAndPassword.getPassword() == null) {
            return new ResponseEntity<>("{\"error\": \"Please Enter Both Username and Password\"}", HttpStatus.OK);
        }
        Optional<String> uuidStringOptional = viewModel.loginUserAndGetSecretKey(userNameAndPassword.getUserName(), userNameAndPassword.getPassword(), userDetailsRepository, userLoginMappingRepository);
        if(uuidStringOptional == null) {
            return new ResponseEntity<>("{\"error\": \"Wrong Password or Invlid User\"}", HttpStatus.OK);
        }
        UuidStingOnly uuidStingOnly = new UuidStingOnly();
        uuidStingOnly.setUuid(uuidStringOptional.get());
        return new ResponseEntity<>(uuidStingOnly, HttpStatus.OK);
    }

    @PostMapping("/upload-post")
    @ResponseBody
    public ResponseEntity<?> uploadPost(@ModelAttribute FileUploadDetailRequest fileUploadDetail) {
        String randomIdString = UUID.randomUUID().toString();
        if(fileUploadDetail.getUserId() == null) {
            return new ResponseEntity<>("{\"error\": \"User Unauthenticated\"}", HttpStatus.OK);
        } else if(!viewModel.isUseLoggedIn(fileUploadDetail.getUserId(), userLoginMappingRepository)) {
            return new ResponseEntity<>("{\"error\": \"User Unauthenticated\"}", HttpStatus.OK);
        } else if(fileUploadDetail.getImageFile() == null){
            return new ResponseEntity<>("{\"error\": \"No Image Available\"}", HttpStatus.OK);
        } else {
            String uploadFilePath = viewModel.saveImageToFileSystem(fileUploadDetail.getImageFile(), randomIdString);
            if(uploadFilePath == null) {
                return new ResponseEntity<>("{\"error\": \"Unable to upload file\"}", HttpStatus.OK);
            } else {
                fileUploadDetail.setFileId(randomIdString);
                fileUploadDetail.setImageFile(null);
                fileUploadDetail.setFilePath(uploadFilePath);
                fileUploadDetail.setUserName(viewModel.getUserNameForId(fileUploadDetail.getUserId(), userLoginMappingRepository));
                ArrayList<String> likedUsers = new ArrayList<>();
                fileUploadDetail.setLikes(likedUsers);
                viewModel.saveImageUploadInfoInDatabase(fileUploadDetail, fileUploadDetailRepository);
                return new ResponseEntity<>("{\"success\": \"File Uploaded Successfully\"}", HttpStatus.OK);
            }
        }
    }
}