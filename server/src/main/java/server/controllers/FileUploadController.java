package server.controllers;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    /*private final Logger logger = Logger.getLogger(FileUploadController.class.getName());
    @Autowired
    FsFilesService fileService;
    @Autowired
    NodeService nodeService;

    @PostMapping("/uploadFile")
    public Node uploadFile(MultipartHttpServletRequest request) throws Exception {
        logger.log(INFO, String.format("upload file %s %n", "fileWrapper.toString()"));
        HashMap<String, Object> map = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((name , values)-> map.put(name,request.getParameter(name)));
        MultipartFile file = request.getFile("file");
        return doUploadFile(file, map);
    }

    @PostMapping("/uploadMultiFiles")
    public List<Node> uploadMultiFiles(MultipartHttpServletRequest request) throws Exception {
        logger.log(INFO, String.format("upload files %s %n", "fileWrapper.toString()"));
        HashMap<String, Object> map = new HashMap<>();
        Map<String, String[]> parameterMap = request.getParameterMap();
        parameterMap.forEach((name , values)-> map.put(name,request.getParameter(name)));
        List<Node> nodes = new ArrayList<>();
        for (String name : request.getFileMap().keySet()){
            MultipartFile file = request.getFileMap().get(name);
            map.put("name", file.getOriginalFilename().trim());
            nodes.add(doUploadFile(file,map));
        }
        return nodes;
    }

    @GetMapping("/streamContent/{fileId}")
    public void streamFileContent(@PathVariable String fileId, HttpServletRequest request,HttpServletResponse response) throws Exception {
        logger.log(INFO, String.format("streaming file %s %n", fileId));
        GridFsResource file = fileService.getFileContent(fileId);
        try {
            FileCopyUtils.copy(file.getInputStream(), response.getOutputStream());
        } catch (IOException e) {
            logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", fileId, e.getMessage()));
            throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, fileId);
        }
    }

    @GetMapping("/getFileInfo/{fileId}")
    public GridFsResource getFileInfo(@PathVariable String fileId,HttpServletRequest request) throws Exception {
        logger.log(INFO, String.format("searching file %s %n", fileId));
        return fileService.getFileContent(fileId);
    }

    public Node doUploadFile(MultipartFile file, Map<String, Object> metaData) throws Exception {
        boolean upsert = false;
        if(metaData.containsKey("upsert") && metaData.get("upsert")!=null && ((String[])metaData.get("upsert")).length>0)
            upsert = Boolean.getBoolean(((String[])metaData.get("upsert"))[0]);
        return nodeService.createFile(file, metaData, upsert);
    }*/
}