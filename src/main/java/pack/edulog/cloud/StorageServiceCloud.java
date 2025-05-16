package pack.edulog.cloud;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class StorageServiceCloud {

    @Value("${firebase.token}")
    private String firebaseTokenPath;

    // Initialize Firebase once at startup
    @PostConstruct
    public void initializeFirebase() {
        try {
            FileInputStream serviceAccount = new FileInputStream(firebaseTokenPath);
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setStorageBucket("indus-532b7.appspot.com")
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            System.err.println("Firebase initialization error: " + e.getMessage());
        }
    }

    /**
     * Uploads any file to Firebase Storage.
     *
     * @param file the uploaded file from the client
     * @return public URL of the uploaded file; null if upload fails
     * @throws IOException if reading the file fails
     */
    public String saveFile(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        // Generate unique path in Firebase
        String fileExtension = getFileExtension(originalFilename);
        String baseFileName = UUID.randomUUID().toString();
        String storagePath = "pdf_assignement/" + baseFileName + (!fileExtension.isEmpty() ? "." + fileExtension : "");

        // Get content type or fallback to application/octet-stream
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // Upload file
        return uploadToFirebase(file.getBytes(), storagePath, contentType, fileExtension);
    }

    /**
     * Uploads file data to Firebase Storage.
     *
     * @param fileData     the file data as a byte array
     * @param path           the path in the Firebase bucket
     * @param contentType    MIME type of the file
     * @param fileExtension  extension used in filename
     * @return the public URL of the uploaded file or null if upload fails
     */
    private String uploadToFirebase(byte[] fileData, String path, String contentType, String fileExtension) {
        try {
            // Generate a unique download token
            String downloadToken = UUID.randomUUID().toString();

            Storage storage = StorageClient.getInstance().bucket().getStorage();
            BlobId blobId = BlobId.of("indus-532b7.appspot.com", path);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setMetadata(java.util.Map.of("firebaseStorageDownloadTokens", downloadToken))
                    .setContentType(contentType)
                    .build();

            Blob blob = storage.create(blobInfo, fileData);
            if (blob == null) {
                return null;
            }

            // Construct the public URL
            return "https://firebasestorage.googleapis.com/v0/b/indus-532b7.appspot.com/o/"
                    + path.replace("/", "%2F")
                    + "?alt=media&token=" + downloadToken;
        } catch (Exception e) {
            System.err.println("Error uploading to Firebase: " + e.getMessage());
            return null;
        }
    }

    /**
     * Extracts file extension from filename.
     */
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}