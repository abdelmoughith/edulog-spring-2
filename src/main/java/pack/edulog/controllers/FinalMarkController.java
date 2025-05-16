package pack.edulog.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pack.edulog.services.FinalmarkService;

@RestController
@RequestMapping("/finalmark")
@RequiredArgsConstructor
public class FinalMarkController {

    private final FinalmarkService finalmarkService;


    @PostMapping(path = "/create/{classroomId}", consumes = "multipart/form-data")
    public ResponseEntity<?> createAssignmentWithFiles(
            @PathVariable("classroomId") Long classroomId,
            @RequestParam(value = "file", required = false) MultipartFile file,
            @RequestHeader("Authorization") String token
    ) {

        try {
            if (!isExel(file)){
                return new ResponseEntity<>("Only Excel (.xls, .xlsx) or CSV (.csv) files are allowed", HttpStatus.BAD_REQUEST);
            }
            return ResponseEntity.ok(finalmarkService.createFinalMark(classroomId, file, token));
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // Get all assignments for a classroom
    @GetMapping("/classroom/{classroomId}")
    public ResponseEntity<?> getAssignmentsByClassroom(@PathVariable Long classroomId) {
        try {
            return new ResponseEntity<>(finalmarkService.getByClassroom(classroomId), HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    public boolean isExel(MultipartFile file) {
        String filename = file.getOriginalFilename();
        String contentType = file.getContentType();

        if (filename == null || file.isEmpty()) {
            throw new RuntimeException("File is required and must not be empty");
        }

        // Normalize filename
        String lowerCaseName = filename.toLowerCase();

        // Validate by extension and MIME type
        if (!(lowerCaseName.endsWith(".xls") || lowerCaseName.endsWith(".xlsx") || lowerCaseName.endsWith(".csv"))
                || !(contentType.equals("application/vnd.ms-excel")
                || contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                || contentType.equals("text/csv"))) {

            //throw new RuntimeException("Only Excel (.xls, .xlsx) or CSV (.csv) files are allowed");
            return false;
        }
        return true;

    }
}

