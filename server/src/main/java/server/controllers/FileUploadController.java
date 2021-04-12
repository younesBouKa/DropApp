package server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import server.data.Node;
import server.exceptions.FileStorageException;
import server.services.FsFilesService;
import server.services.NodeService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    private final Logger logger = Logger.getLogger(FileUploadController.class.getName());
    @Autowired
    FsFilesService fileService;
    @Autowired
    NodeService nodeService;

    @PostMapping("/uploadFile")
    public Node uploadFile(MultipartHttpServletRequest request) {
        logger.log(INFO, String.format("upload file %s %n", "fileWrapper.toString()"));
        HashMap<String, Object> map = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((name , values)-> map.put(name,request.getParameter(name)));
        MultipartFile file = request.getFile("file");
        try {
            return doUplaodFile(file, map);
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Could not store file in DB %s %n", file.getOriginalFilename()));
            throw new FileStorageException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        } catch (Exception e) {
            logger.log(SEVERE, String.format("File already exist with same name [%s] %n", file.getOriginalFilename()));
            throw new FileStorageException("File already exist with same name [" + file.getOriginalFilename()+"]");
        }
    }

    @PostMapping("/uploadMultiFiles")
    public List<Node> uploadMultiFiles(MultipartHttpServletRequest request) {
        logger.log(INFO, String.format("upload files %s %n", "fileWrapper.toString()"));
        HashMap<String, Object> map = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((name , values)-> map.put(name,request.getParameter(name)));
        List<Node> nodes = new ArrayList<>();
        request.getFileMap().forEach((name, file)->{
            map.put("name", name);
            try {
                nodes.add(doUplaodFile(file,map));
            } catch (IOException e) {
                logger.log(SEVERE, String.format("Could not store file in DB %s %n", file.getOriginalFilename()));
            } catch (Exception e) {
                logger.log(SEVERE, String.format("File already exist with same name %s %n", file.getOriginalFilename()));
            }
        });
        return nodes;
    }

    @GetMapping("/streamContent/{fileId}")
    public void streamFileContent(@PathVariable String fileId, HttpServletRequest request,HttpServletResponse response){
        logger.log(INFO, String.format("streaming file %s %n", fileId));
        GridFsResource file = fileService.getFileContent(fileId);
        try {
            FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Could not read file %s %n", file.getFilename()));
            throw new FileStorageException("Could not read file " + file.getFilename()
                    + ". Please try again!");
        }
    }

    @GetMapping("/getFileInfo/{fileId}")
    public GridFsResource getFileInfo(@PathVariable String fileId,HttpServletRequest request) {
        logger.log(INFO, String.format("searching file %s %n", fileId));
        return fileService.getFileContent(fileId);
    }

    public Node doUplaodFile(MultipartFile file, Map<String, Object> metaData) throws Exception {
        boolean upsert = false;
        if(metaData.containsKey("upsert") && metaData.get("upsert")!=null && ((String[])metaData.get("upsert")).length>0)
            upsert = Boolean.getBoolean(((String[])metaData.get("upsert"))[0]);
        return nodeService.createFile(file, metaData, upsert);
    }
}