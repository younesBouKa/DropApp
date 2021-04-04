package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.data.FileDocument;
import server.services.IStorageService;

import javax.servlet.http.HttpServletResponse;

/**
 *   POST /api/v0/files/uploadFile
 *   GET /api/v0/files/stream/{file_id}
 *   GET /api/v0/files/file/{id}
 */

@RestController
@RequestMapping("/api/v0/files")
public class FileUploadController {

    @Autowired
    IStorageService fileService ;

    @PostMapping("/uploadFile")
    public String uploadFile(MultipartFile file) {
        return fileService.storeDB(file);
    }

    @GetMapping("/stream/{file_id}")
    public void streamFile(@PathVariable String file_id, HttpServletResponse response) throws Exception {
        FileDocument video = fileService.getFile(file_id);
        FileCopyUtils.copy(video.getStream(), response.getOutputStream());
    }

    @GetMapping("/file/{id}")
    public FileDocument getFile(@PathVariable String id) throws Exception {
        FileDocument fileDocument = fileService.getFile(id);
        return fileDocument;
    }
}