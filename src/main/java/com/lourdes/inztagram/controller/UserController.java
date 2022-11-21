package com.lourdes.inztagram.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.FileDownloadDetailsRequest;
import com.lourdes.inztagram.model.FileUploadDetailRequest;
import com.lourdes.inztagram.model.GetPostsRequest;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserNameAndPassword;
import com.lourdes.inztagram.model.UuidStingOnly;
import com.lourdes.inztagram.repository.FileUploadDetailRepository;
import com.lourdes.inztagram.repository.UserDetailsRepository;
import com.lourdes.inztagram.repository.UserLoginMappingRepository;
import com.lourdes.inztagram.viewModel.UserViewModel;

@RestController
public class UserController {
    private final Integer PAGINATION_NUMBER_OF_POSTS_TO_SHOW = 8;
    private final String COLLECTION_FILE_UPLOAD_MAPPING_NAME = "FileUploadMapping";

    private final Logger LOGGER = LoggerFactory.getLogger(UserController.class);
    private UserViewModel viewModel = new UserViewModel();

    @Autowired
    private UserDetailsRepository userDetailsRepository;
    @Autowired
    private UserLoginMappingRepository userLoginMappingRepository;
    @Autowired
    private FileUploadDetailRepository fileUploadDetailRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @PostMapping("/userRegister")
    @ResponseBody
    public ResponseEntity<?> registerUser(@RequestBody UserDetails user) {
        long startTime = System. currentTimeMillis ();
        String userExistsError = "{\"error\": \"User Already Exists\"}";
        String userCreatedSuccess = "{\"success\": \"User Created\"}";
        RegistrationValidationStatus validationStatus = viewModel.validateRegistrationFields(user);
        if(validationStatus != RegistrationValidationStatus.GOOD_TO_GO) {
            String contextualErrorString = "{\"error\":" + viewModel.getRegistrationValidationStatusString(validationStatus) +" }";
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            user, contextualErrorString, endTime - startTime);
            return new ResponseEntity<>(contextualErrorString, HttpStatus.BAD_REQUEST);
        }
        if(viewModel.doesUserExist(user.getUserName(), userDetailsRepository)) {
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            user, userExistsError, endTime - startTime);
            return new ResponseEntity<>(userExistsError, HttpStatus.OK);
        }
        viewModel.registerUser(user, userDetailsRepository);
        long endTime = System.currentTimeMillis ();
        LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
        user, userCreatedSuccess, endTime - startTime);
        return new ResponseEntity<>(userCreatedSuccess, HttpStatus.OK);
    }

    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<?> loginuser(@RequestBody UserNameAndPassword userNameAndPassword) {
        long startTime = System. currentTimeMillis ();
        String pleaseEnterUsernameAndPasswordError = "{\"error\": \"Please Enter Both Username and Password\"}";
        String invalidUsenameOrPasswordError = "{\"error\": \"Wrong Password or Invlid User\"}";
        if(userNameAndPassword.getUserName() == null || userNameAndPassword.getPassword() == null) {
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            userNameAndPassword, pleaseEnterUsernameAndPasswordError, endTime - startTime);
            return new ResponseEntity<>(pleaseEnterUsernameAndPasswordError, HttpStatus.OK);
        }
        Optional<String> uuidStringOptional = viewModel.loginUserAndGetSecretKey(userNameAndPassword.getUserName(), userNameAndPassword.getPassword(), userDetailsRepository, userLoginMappingRepository);
        if(uuidStringOptional == null) {
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            userNameAndPassword, invalidUsenameOrPasswordError, endTime - startTime);
            return new ResponseEntity<>(invalidUsenameOrPasswordError, HttpStatus.OK);
        }
        UuidStingOnly uuidStingOnly = new UuidStingOnly();
        uuidStingOnly.setUuid(uuidStringOptional.get());
        long endTime = System.currentTimeMillis ();
        LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
        userNameAndPassword, uuidStingOnly, endTime - startTime);
        return new ResponseEntity<>(uuidStingOnly, HttpStatus.OK);
    }

    @PostMapping("/upload-post")
    @ResponseBody
    public ResponseEntity<?> uploadPost(@ModelAttribute FileUploadDetailRequest fileUploadDetail) {
        long startTime = System.currentTimeMillis ();
        String userUnAuthenticatedError = "{\"error\": \"User Unauthenticated\"}";
        String noImageAvailableError = "{\"error\": \"No Image Available\"}";
        String unableToUploadFileError = "{\"error\": \"Unable to upload file\"}";
        String fileUploadSuccessful = "{\"success\": \"File Uploaded Successfully\"}";
        String randomIdString = UUID.randomUUID().toString();
        if(fileUploadDetail.getUserId() == null) {
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileUploadDetail, userUnAuthenticatedError, endTime - startTime);
            return new ResponseEntity<>(userUnAuthenticatedError, HttpStatus.OK);
        } else if(!viewModel.isUseLoggedIn(fileUploadDetail.getUserId(), userLoginMappingRepository)) {
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileUploadDetail, userUnAuthenticatedError, endTime - startTime);
            return new ResponseEntity<>(userUnAuthenticatedError, HttpStatus.OK);
        } else if(fileUploadDetail.getImageFile() == null){
            long endTime = System.currentTimeMillis ();
            LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileUploadDetail, noImageAvailableError, endTime - startTime);
            return new ResponseEntity<>(noImageAvailableError, HttpStatus.OK);
        } else {
            String uploadFilePath = viewModel.saveImageToFileSystem(fileUploadDetail.getImageFile(), randomIdString);
            if(uploadFilePath == null) {
                long endTime = System.currentTimeMillis ();
                LOGGER.warn("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
                fileUploadDetail, unableToUploadFileError, endTime - startTime);
                return new ResponseEntity<>(unableToUploadFileError, HttpStatus.OK);
            } else {
                fileUploadDetail.setFileId(randomIdString);
                fileUploadDetail.setImageFile(null);
                fileUploadDetail.setFilePath(uploadFilePath);
                fileUploadDetail.setUserName(viewModel.getUserNameForId(fileUploadDetail.getUserId(), userLoginMappingRepository));
                ArrayList<String> likedUsers = new ArrayList<>();
                fileUploadDetail.setLikes(likedUsers);
                viewModel.saveImageUploadInfoInDatabase(fileUploadDetail, fileUploadDetailRepository);
                long endTime = System.currentTimeMillis ();
                LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
                fileUploadDetail, fileUploadSuccessful, endTime - startTime);
                return new ResponseEntity<>(fileUploadSuccessful, HttpStatus.OK);
            }
        }
    }

    @GetMapping("/fetch-image")
    @ResponseBody
    public ResponseEntity<?> downloadImageFromFileSystem(@RequestBody FileDownloadDetailsRequest fileDownloadDetailsRequest) {
        long startTime = System.currentTimeMillis ();
        String userUnAuthenticatedError = "{\"error\": \"User Unauthenticated\"}";
        String fileNotFoundError = "{\"error\": \"File Not Found\"}";
        if(!viewModel.isUseLoggedIn(fileDownloadDetailsRequest.getUserId(), userLoginMappingRepository)) {
            long endTime = System.currentTimeMillis ();
            LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileDownloadDetailsRequest, userUnAuthenticatedError, endTime - startTime);
            return new ResponseEntity<>(userUnAuthenticatedError, HttpStatus.OK);
        }
        byte[] imageData = viewModel.getImageForId(fileDownloadDetailsRequest.getFileId());
        if(imageData == null) {
            long endTime = System.currentTimeMillis ();
            LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileDownloadDetailsRequest, fileNotFoundError, endTime - startTime);
            return new ResponseEntity<>(fileNotFoundError, HttpStatus.OK);
        }
        long endTime = System.currentTimeMillis ();
            LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            fileDownloadDetailsRequest, "image sent successfully", endTime - startTime);
        return ResponseEntity.status(HttpStatus.OK)
            . contentType(MediaType.valueOf("image/jpeg"))
            . body(imageData);
    }

    @GetMapping("/get-posts")
    @ResponseBody
    public ResponseEntity<?> getInztaPosts(@RequestBody GetPostsRequest getPostsRequest) {
        long startTime = System.currentTimeMillis ();
        String userUnAuthenticatedError = "{\"error\": \"User Unauthenticated\"}";
        if(!viewModel.isUseLoggedIn(getPostsRequest.getUserId(), userLoginMappingRepository)) {
            long endTime = System.currentTimeMillis ();
            LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            getPostsRequest, userUnAuthenticatedError, endTime - startTime);
            return new ResponseEntity<>(userUnAuthenticatedError, HttpStatus.OK);
        }
        SampleOperation sampleOperation = new SampleOperation(PAGINATION_NUMBER_OF_POSTS_TO_SHOW);
        Aggregation aggregation = Aggregation.newAggregation(sampleOperation);
        List<FileUploadDetailRequest> output = mongoTemplate.aggregate(aggregation, COLLECTION_FILE_UPLOAD_MAPPING_NAME, FileUploadDetailRequest.class).getMappedResults();
        long endTime = System.currentTimeMillis ();
            LOGGER.info("REQUEST BODY = {}; RESPONSE BODY = {}; TIME TAKEN = {}",
            getPostsRequest, output, endTime - startTime);
        return new ResponseEntity<>(output, HttpStatus.OK);
    }
}