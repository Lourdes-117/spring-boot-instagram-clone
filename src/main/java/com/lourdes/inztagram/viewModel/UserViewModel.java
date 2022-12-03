package com.lourdes.inztagram.viewModel;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.web.multipart.MultipartFile;
import com.lourdes.inztagram.enums.RegistrationValidationStatus;
import com.lourdes.inztagram.model.FileUploadDetailRequest;
import com.lourdes.inztagram.model.UserDetails;
import com.lourdes.inztagram.model.UserLoginMapping;
import com.lourdes.inztagram.repository.FileUploadDetailRepository;
import com.lourdes.inztagram.repository.UserDetailsRepository;
import com.lourdes.inztagram.repository.UserLoginMappingRepository;
import com.lourdes.inztagram.utility.Hashing;

public class UserViewModel {
    private final String FOLDER_PATH = "/Users/lourdes/Downloads/Server-AttachedFilsSystem/";
    private final String FOLDER_PATH_PROFILE = "/Users/lourdes/Downloads/Server-AttachedFilsSystem/profile-photos/";

    public String getRegistrationValidationStatusString(RegistrationValidationStatus status) {
        switch (status) {
            case GOOD_TO_GO:
                return "";
            case EMAIL_INVALID:
                return "Pleae Enter Proper Email ID";
            case EMAIL_EMPTY:
                return "Pleae Enter Email ID";
            case NAME_EMPTY:
                return "Pleae Enter Name";
            case USERNAME_EMPTY:
                return "Pleae Enter UserName";
            case USERNAME_INVALID:
                return "Pleae Enter Valid UserName";
            case PASSWORD_EMPTY:
                return "Pleae Enter Password";
            case PASSWORD_SHORT:
                return "Pleae Enter Strong Password";
            default:
                return "";
        }
    }

    public RegistrationValidationStatus validateRegistrationFields(UserDetails userDetails) {
        if(userDetails.getUserName() == null) { return RegistrationValidationStatus.USERNAME_EMPTY; }
        if(!userDetails.getUserName().matches("(?=[a-zA-Z0-9._]{3,20}$)(?!.*[_.]{2})[^_.].*[^_.]$")) { return RegistrationValidationStatus.USERNAME_INVALID; }
        if(userDetails.getEmailId() == null) { return RegistrationValidationStatus.EMAIL_EMPTY; }
        if(!userDetails.getEmailId().matches("[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$")) { return RegistrationValidationStatus.EMAIL_INVALID; }
        if(userDetails.getFullName() == null) { return RegistrationValidationStatus.NAME_EMPTY; }
        if(userDetails.getPassword() == null) { return RegistrationValidationStatus.PASSWORD_EMPTY; }
        if(userDetails.getPassword().length() < 6) { return RegistrationValidationStatus.PASSWORD_SHORT; }
        return RegistrationValidationStatus.GOOD_TO_GO;
    }

    public Boolean doesUserExist(String userName, UserDetailsRepository userDetailsRepository) {
        return userDetailsRepository.findById(userName).isPresent();
    }

    public void registerUser(UserDetails userDetails, UserDetailsRepository userDetailsRepository) {
        try {
            String hashedPassword = Hashing.hashSha256(userDetails.getPassword());
            userDetails.setPassword(hashedPassword);
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
        }
        userDetailsRepository.save(userDetails);
    }

    public Optional<String> loginUserAndGetSecretKey(String userName, String passwordUnhashed, UserDetailsRepository userDetailsRepository, UserLoginMappingRepository userLoginMappingRepository) {
        String uuidString = UUID.randomUUID().toString();
        String password;
        try {
            password = Hashing.hashSha256(passwordUnhashed);
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }

        // Check if user exists
        Optional<UserDetails> userDetailsOptional = userDetailsRepository.findById(userName);
        if(userDetailsOptional.isEmpty()) {
            return null;
        }
        // Validate password
        UserDetails userDetails = userDetailsOptional.get();
        if(!userDetails.getPassword().equals(password)) {
            return null;
        }
        
        // Remove previous login
        if(userDetails.getUuid() != null) {
            userLoginMappingRepository.deleteById(userDetails.getUuid());
        }
        userDetails.setUuid(uuidString);
        userDetailsRepository.save(userDetails);

        UserLoginMapping userLoginMapping = new UserLoginMapping();
        userLoginMapping.setUsername(userName);
        userLoginMapping.setUuid(uuidString);
        userLoginMappingRepository.save(userLoginMapping);
        return Optional.ofNullable(uuidString);
    }

    public Boolean logoutUser(String userId, UserLoginMappingRepository userLoginMappingRepository) {
        if(isUseLoggedIn(userId, userLoginMappingRepository)) {
            userLoginMappingRepository.deleteById(userId);
            return true;
        } else {
            return false;
        }
    }

    public Boolean isUseLoggedIn(String userId, UserLoginMappingRepository userLoginMappingRepository) {
        if(userId == null) { return false; }
        return userLoginMappingRepository.findById(userId).isPresent();
    }

    public String getUserNameForId(String userId, UserLoginMappingRepository userLoginMappingRepository) {
        Optional<UserLoginMapping> loginMapping = userLoginMappingRepository.findById(userId);
        if(loginMapping.isPresent()) {
            return loginMapping.get().getUsername();
        } else {
            return null;
        }
    }

    public Optional<UserDetails> getDetailsOfUserName(String userName, UserDetailsRepository userDetailsRepository) {
        return userDetailsRepository.findById(userName);
    }

    public String saveImageToFileSystem(MultipartFile file, String randomIDString) {
        if(file == null) { return null;}
        String filePath = FOLDER_PATH + randomIDString + ".jpeg";
        try {
            file.transferTo(new File(filePath));
        } catch( Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }
        return filePath;
    }

    public String saveProfilePhotoToFileSystem(MultipartFile file, String userName) {
        if(file == null) { return null;}
        String filePath = FOLDER_PATH_PROFILE + userName + ".jpeg";
        try {
            file.transferTo(new File(filePath));
        } catch( Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }
        return filePath;
    }

    public void saveImageUploadInfoInDatabase(FileUploadDetailRequest fileUploadDetailRequest, FileUploadDetailRepository fileUploadDetailRepository) {
        fileUploadDetailRepository.save(fileUploadDetailRequest);
    }

    public byte[] getImageForId(String fileId) {
        if(fileId == null) { return null; }
        byte[] image;
        String filePath = FOLDER_PATH + fileId + ".jpeg";
        try {
            image = Files.readAllBytes(new File(filePath).toPath());
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }
        return image;
    }

    public byte[] getImageProfilePhotoForUserName(String userNae) {
        if(userNae == null) { return null; }
        byte[] image;
        String filePath = FOLDER_PATH_PROFILE + userNae + ".jpeg";
        try {
            image = Files.readAllBytes(new File(filePath).toPath());
        } catch(Exception exception) {
            System.out.println("InztagramLog - ERROR "+ exception.toString());
            return null;
        }
        return image;
    }

    public List<FileUploadDetailRequest> getRandomPosts(Integer maxNumberOfPosts, String collectionName, MongoTemplate mongoTemplate) {
        SampleOperation sampleOperation = new SampleOperation(maxNumberOfPosts);
        Aggregation aggregation = Aggregation.newAggregation(sampleOperation);
        List<FileUploadDetailRequest> output = mongoTemplate.aggregate(aggregation, collectionName, FileUploadDetailRequest.class).getMappedResults();
        return output;
    }

    public List<FileUploadDetailRequest> getPostsOfUser(Integer maxNumberOfPosts, Integer pagination, String userNameToGet, String collectionName, MongoTemplate mongoTemplate) {
        MatchOperation matchOperation = new MatchOperation(Criteria.where("userName").is(userNameToGet));
        Aggregation aggregation = Aggregation.newAggregation(matchOperation);
        List<FileUploadDetailRequest> postsOfUser = mongoTemplate.aggregate(aggregation, collectionName, FileUploadDetailRequest.class).getMappedResults();
        if(postsOfUser == null) { return null; }
        int startIndex = 0;
        int endIndex = 0;
        if(pagination < 0) {
            pagination = 0;
        }
        if(postsOfUser.size() >= maxNumberOfPosts * pagination) {
            startIndex = maxNumberOfPosts * pagination;
            endIndex = maxNumberOfPosts * pagination;
        } else {
            return new ArrayList<>();
        }
        if(postsOfUser.size() > endIndex+maxNumberOfPosts) {
            endIndex = endIndex+maxNumberOfPosts;
        } else {
            endIndex = postsOfUser.size();
        }
        List<FileUploadDetailRequest> output = postsOfUser.subList(startIndex, endIndex);
        return output;
    }


    public Optional<FileUploadDetailRequest> getPostById(String fileId, FileUploadDetailRepository fileUploadDetailRepository) {
        return fileUploadDetailRepository.findById(fileId);
    }

    public Boolean likeOrUnlikePostAndReturnArray(FileUploadDetailRequest file, String userName, FileUploadDetailRepository fileUploadDetailRepository) {
        ArrayList<String> previouslyLikedUsers = file.getLikes();
        if(previouslyLikedUsers == null) {
            previouslyLikedUsers = new ArrayList<String>();
        }
        if(previouslyLikedUsers.remove(userName)) {
        } else {
            previouslyLikedUsers.add(userName);
        }
        file.setLikes(previouslyLikedUsers);
        fileUploadDetailRepository.save(file);
        return true;
    }
}
