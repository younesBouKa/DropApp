package server.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import server.data.IUser;
import server.exceptions.CustomException;
import server.exceptions.Message;
import server.models.NodeWebRequest;
import server.data.Node;
import server.models.ZipRequest;
import server.services.INodeService;
import server.user.services.CustomUserDetails;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static server.exceptions.Message.*;

@RestController
@RequestMapping("/api/v0/nodes")
public class NodeController {

    private static final Logger logger = Logger.getLogger(NodeController.class.getName());
    private static final String NEW_ORDER_LOG = "New order was created id:{}";
    private static final String ORDER_UPDATED_LOG = "Order:{} was updated";

    private final INodeService nodeService;

    @Autowired
    public NodeController(INodeService nodeService) {
        this.nodeService = nodeService;
    }

    @GetMapping("")
    public List<Node> getRootNodes() throws CustomException {
        return nodeService.getRootNodes(currentUser());
    }

    @PostMapping
    public Node saveRootNode(@RequestPart(value = "file", required = false) MultipartFile file,
                             @RequestParam(value = "nodeInfo") String nodeInfo) throws CustomException {
        NodeWebRequest node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        if(file!=null){
            node.updateWithFile(file);
        }
        Node createdNode = nodeService.insertNode(currentUser(), node);
        return createdNode;
    }

    @GetMapping(value = "/{nodeId}")
    public Node getNodeById(@PathVariable String nodeId) throws CustomException {
        Node node = nodeService.getNodeById(currentUser(), nodeId);
        return node;
    }

    @GetMapping(value = "/{nodeId}/stream")
    public void getNodeContent(@PathVariable String nodeId, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        logger.log(INFO, String.format("streaming file %s %n", nodeId));
        Node node = nodeService.getNodeContent(currentUser(), nodeId);
        getContentWithRange(node, request, response);
    }

    @PostMapping(value = "/compress")
    public Node zipNodes(@RequestBody ZipRequest zipRequest,
                         HttpServletRequest request,
                         HttpServletResponse response) throws CustomException {
        logger.log(INFO, String.format("zipped nodes %s %n", zipRequest));
        return nodeService.createZipNode(currentUser(), zipRequest);
    }

    @GetMapping("/{parentId}/children")
    public List<Node> getNodes(@PathVariable String parentId,
                               @RequestParam(required = false, name = "page", defaultValue = "0") int page,
                               @RequestParam(required = false, name = "size", defaultValue = "20") int size,
                               @RequestParam(required = false, name = "sortField", defaultValue = "creationDate") String sortField,
                               @RequestParam(required = false, name = "direction", defaultValue = "DESC") String direction,
                               @RequestParam(required = false, name = "status") List<String> status,
                               @RequestParam(required = false, name = "search", defaultValue = "") String search) throws CustomException {

        return nodeService.getNodes(currentUser(), parentId, page, size, sortField, direction, status, search);
    }

    @PostMapping("/{parentId}")
    public Node saveNode(@PathVariable(required = false, name = "parentId") String parentId,
                         @RequestPart(value = "file", required = false) MultipartFile file,
                         @RequestParam(value = "nodeInfo") String nodeInfo) throws CustomException {
        NodeWebRequest node = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.updateWithFile(file);
        node.setParentId(parentId);
        Node createdNode = nodeService.insertNode(currentUser(), node);
        return createdNode;
    }

    @PutMapping(path = "/{nodeId}")
    public Node updateNode(@PathVariable String nodeId,
                           @RequestPart (value = "file", required = false)  MultipartFile file,
                           @RequestPart (value = "chunk", required = false)  String chunk,
                           @RequestParam(value = "nodeInfo") String nodeInfo,
                           HttpServletRequest request,
                           HttpServletResponse response) throws CustomException {
        NodeWebRequest node ;
        ObjectMapper mapper = new ObjectMapper();
        try {
            node = mapper.readValue(nodeInfo, NodeWebRequest.class);
        } catch (JsonProcessingException e) {
            throw new CustomException(e, Message.ERROR_WHILE_PARSING_NODE_INFO, nodeInfo);
        }
        node.updateWithFile(file);
        if(request.getHeader("x-chunk-start-pos")!=null && chunk!=null){
            Node savedNode = nodeService.getNodeContent(currentUser(), nodeId);
            node = prepareUpdateWithChunks(savedNode, node, chunk, request, response);
        }
        Node createdNode = nodeService.updateNode(currentUser(), nodeId, node);
        return createdNode;
    }

    @DeleteMapping(path = "/{nodeId}", consumes = APPLICATION_JSON_VALUE)
    public int deleteNode(@PathVariable String nodeId,
                             HttpServletRequest request) throws CustomException {

        return nodeService.deleteNode(currentUser(), nodeId);
    }
    /********** tools **********************/
    public void getContentWithRange(Node node, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        try {
            // content length
            long resourceLength = node.getFileSize();
            // fill response header
            response.setHeader("Accept-Ranges","bytes");
            response.setHeader("Content-Length",String.valueOf(node.getFileSize()));
            response.setHeader("Content-Type", node.getContentType());
            // validate if-range header
            long ifRangeHeader = request.getDateHeader("if-range");// ex: (If-Range: Wed, 21 Oct 2015 07:28:00 GMT)
            Instant lastRequestInstant = Instant.ofEpochMilli(ifRangeHeader);
            boolean isIfRangeHeaderFullFilled = node.getModificationDate().isAfter(lastRequestInstant);
            if(!isIfRangeHeaderFullFilled){
                FileCopyUtils.copy(node.getContent(), response.getOutputStream());
                return;
            }
            // validate range header
            String rangeHeader = request.getHeader("range"); // ex: (bytes=200-1000)
            int firstBytePos = 0;
            int secondBytePos = 0;
            if(rangeHeader!=null){
                rangeHeader = rangeHeader.substring("bytes=".length());
                String[] stringRanges = rangeHeader.split(",");
                String firstRange = stringRanges.length>0 ? stringRanges[0] : null;
                if(firstRange!=null){
                    String[] twoPartsRange = firstRange.split("-");
                    firstBytePos = twoPartsRange.length>0 && !twoPartsRange[0].isEmpty() ? Integer.parseInt( twoPartsRange[0]) : -1;
                    secondBytePos = twoPartsRange.length>1 && !twoPartsRange[1].isEmpty() ? Integer.parseInt( twoPartsRange[1]) : -1;
                    if(firstBytePos!=-1){
                       if(firstBytePos>resourceLength){
                           response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                           return; // TODO to see later
                       }
                    }
                    if(secondBytePos!=-1){
                       if(secondBytePos>=resourceLength || secondBytePos <= firstBytePos){
                           response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                           return; // TODO to see later
                       }
                    }
                    if(firstBytePos==-1 && secondBytePos==-1){
                        response.setStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE.value());
                        return; // TODO to see later
                    }
                    int firstOne = firstBytePos, secondOne= secondBytePos;
                    firstBytePos = firstOne!=-1 ? firstBytePos : (int)(resourceLength-secondOne);
                    secondBytePos =  secondOne!=-1 && firstOne!=-1 ? secondOne : (int)(resourceLength);
                }
            }
            boolean isRangeFullFilled = rangeHeader!=null && firstBytePos<secondBytePos;
            try(OutputStream outputStream = response.getOutputStream();
                InputStream inputStream = node.getContent()){
                if(isRangeFullFilled){
                    response.setStatus(HttpStatus.PARTIAL_CONTENT.value());
                    String contentRange = firstBytePos+"-"+secondBytePos;
                    response.setHeader("Content-Range","bytes "+contentRange+"/"+resourceLength);
                    long contentLength = secondBytePos-firstBytePos;
                    response.setHeader("Content-Length", String.valueOf(contentLength));
                    IOUtils.copyLarge(inputStream, outputStream, firstBytePos, contentLength);
                }else{
                    IOUtils.copyLarge(inputStream, outputStream);
                }
            }catch (Exception e) {
                logger.log(SEVERE, String.format("Error while streaming file content: %s, Error: %s %n", node.getName(), e.getMessage()));
                throw new CustomException(ERROR_WHILE_STREAMING_FILE_CONTENT, node.getName());
            }
        }catch (Exception e){
            throw new CustomException(e,UNKNOWN_EXCEPTION, e.getMessage());
        }
    }

    public NodeWebRequest prepareUpdateWithChunks(Node node,
                                                  NodeWebRequest nodeWebRequest,
                                                  String chunk,
                                                  HttpServletRequest request,
                                                  HttpServletResponse response) throws CustomException{
        try{
            Base64.Decoder decoder = Base64.getDecoder();
            byte[] decodedBytes = decoder.decode(chunk.trim());
            byte[] savedBytes = IOUtils.toByteArray(node.getContent());
            long savedContentLength = node.getFileSize();
            int startPos = request.getIntHeader("x-chunk-start-pos");
            if(savedBytes!=null){
                if(startPos > savedContentLength || startPos < 0)
                    throw new CustomException(UNKNOWN_EXCEPTION, String.format("chunk start position invalid, file size is %s", savedContentLength));
                long endPos = startPos+decodedBytes.length;
                long bufferSize = Math.max(endPos, savedContentLength);
                try (
                        ByteArrayInputStream savedIn = new ByteArrayInputStream(savedBytes);
                        ByteArrayOutputStream out = new ByteArrayOutputStream((int)bufferSize);
                ){
                    IOUtils.copyLarge(savedIn, out, 0, startPos);
                    out.write(decodedBytes,0,decodedBytes.length);
                    if(endPos< savedContentLength)
                        out.write(savedBytes, (int)endPos, (int)(savedContentLength-endPos));
                    nodeWebRequest.setContent(out.toByteArray());
                }
            }
        }
        catch(IOException ex){
            ex.printStackTrace();
            return nodeWebRequest;
        }
        return nodeWebRequest;
    }

    public static IUser currentUser(){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }
}
