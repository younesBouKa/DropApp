package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.data.FileDocument;
import server.services.IStorageService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

/**
 *   POST /api/v0/files/uploadFile
 *   GET /api/v0/files/stream/{file_id}
 *   GET /api/v0/files/file/{id}
 */

@RestController
@RequestMapping("/api/v0/files")
public class FileUploadController {

    private final Logger logger = Logger.getLogger(FileUploadController.class.getName());

    @Autowired
    IStorageService fileService ;

    @PostMapping("/uploadFile")
    public String uploadFile(MultipartFile file, HttpServletRequest request) {
        HttpSession session = request.getSession();
        logger.log(INFO, String.format("upload file %s %n", file.getResource().getFilename()));
        HashMap uploadedFiles = (HashMap) session.getAttribute("uploadedFiles");
        if (uploadedFiles == null) {
            uploadedFiles = new HashMap<String,String>();
        }
        String storedFileId = fileService.storeDB(file);
        uploadedFiles.put(storedFileId,file.getResource().getFilename());
        logger.log(INFO, String.format("session id: %s, isNew: %b  uploaded files : %n %s %n",session.getId(), session.isNew(), uploadedFiles.toString()));
        session.setAttribute("uploadedFiles", uploadedFiles);
        return storedFileId;
    }

    @GetMapping("/stream/{file_id}")
    public void streamFile(@PathVariable String file_id, HttpServletResponse response) throws Exception {
        logger.log(INFO, String.format("streaming file %s %n", file_id));
        FileDocument video = fileService.getFile(file_id);
        FileCopyUtils.copy(video.getStream(), response.getOutputStream());
    }

    @GetMapping("/file/{file_id}")
    public FileDocument getFile(@PathVariable String file_id) throws Exception {
        logger.log(INFO, String.format("searching file %s %n", file_id));
        FileDocument fileDocument = fileService.getFile(file_id);
        return fileDocument;
    }
}