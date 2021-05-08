package server.ftp;

import org.apache.commons.io.FileUtils;
import org.apache.ftpserver.ftplet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.IUser;
import server.data.Node;
import server.data.NodeType;
import server.exceptions.CustomException;
import server.models.NodeFtpRequest;
import server.services.INodeService;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.FileAttribute;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
public class CustomFtplet extends DefaultFtplet implements Ftplet {

    @Autowired
    INodeService nodeService;
    private static final Logger logger = LoggerFactory.getLogger(CustomFtplet.class);

    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            new Thread(() -> {
                try {
                    Node createdNode = nodeService.insertNode(nodeFtpRequest);
                    logger.info("Node {} was saved with success ", createdNode.getName());
                } catch (CustomException e) {
                    logger.error("Error while saving node from FTP server : '{}' ", e.getMessage());
                }
            }).start();
        }
        return super.onUploadEnd(session, request);
    }

    @Override
    public FtpletResult onDeleteEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            new Thread(() -> {
                try {
                    nodeService.deleteNode(nodeFtpRequest);
                    logger.info("deleting ftp node with success : '{} '", nodeFtpRequest.getName());
                } catch (CustomException e) {
                    logger.error("Error while deleting node from FTP server : '{} '", e.getMessage());
                }
            }).start();
        }
        return super.onDeleteEnd(session, request);
    }

    @Override
    public FtpletResult onRmdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            new Thread(() -> {
                try {
                    nodeService.deleteNode(nodeFtpRequest);
                    logger.info("deleting folder from FTP server with success : '{}' ", nodeFtpRequest.getName());
                } catch (CustomException e) {
                    logger.error("Error while deleting folder from FTP server : '{}' ", e.getMessage());
                }
            }).start();
        }
        return super.onRmdirEnd(session, request);
    }

    @Override
    public FtpletResult onMkdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            new Thread(() -> {
                try {
                    nodeService.insertNode(nodeFtpRequest);
                    logger.info("creating folder in FTP server : '{} '", nodeFtpRequest.getName());
                } catch (CustomException e) {
                    logger.error("Error while creating folder in FTP server : '{} '", e.getMessage());
                }
            }).start();
        }
        return super.onMkdirEnd(session, request);
    }

    @Override
    public FtpletResult onRenameEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            new Thread(() -> {
                try {
                    nodeService.insertNode(nodeFtpRequest);
                    logger.info("renaming node in FTP server : '{} '", nodeFtpRequest.getName());
                } catch (CustomException e) {
                    logger.error("Error while renaming ftp node : '{} '", e.getMessage());
                }
            }).start();
        }
        return super.onRenameEnd(session, request);
    }

    public void onChangeDirectory(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if(nodeFtpRequest==null)
            return;
        IUser user = nodeFtpRequest.getUser();
        List<String> pathParts = nodeFtpRequest.getPath();
        new Thread(()->{
            try {
                // creating user home dir first
                Path homeDirPath = Paths.get(user.getHomeDirectory());
                if(!homeDirPath.toFile().exists())
                    Files.createDirectories(homeDirPath);
                // create files and folders
                List<Node> nodeList;
                if(pathParts.isEmpty()){
                    nodeList = nodeService.getRootNodes(user);
                }else{
                    Node dir = nodeService.getNodeFromPath(user, pathParts , false);
                    if(dir==null)
                        return;
                    nodeList = nodeService.getNodes(user, dir.getId());
                }
                for(Node node : nodeList){
                    try{
                        String[] pathPartsArray = new String[node.getPath().size()];
                        for(int i=0; i<node.getPath().size(); i++){
                            pathPartsArray[i] = node.getPath().get(i);
                        }
                        Path path = Paths.get(user.getHomeDirectory(), pathPartsArray);
                        if(NodeType.FILE.equals(node.getType())) {
                            node = nodeService.getNodeWithContent(user, node.getId());
                            if(!path.toFile().exists()){
                                Files.createDirectories(path.getParent());
                                Files.createFile(path);
                            }
                            Files.copy(node.getContent(), path, StandardCopyOption.REPLACE_EXISTING);
                        }else{
                            if(!path.toFile().exists())
                                Files.createDirectories(path);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            } catch (CustomException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public FtpletResult onDisconnect(FtpSession session) throws FtpException, IOException {
        new Thread(()->{
            IUser user = (FtpUser)session.getUser();
            try {
                Path pathHomeDir = Paths.get(user.getHomeDirectory());
                if(pathHomeDir.toFile().exists())
                    FileUtils.deleteDirectory(pathHomeDir.toFile());
            }catch (Exception e){
                e.printStackTrace();
                logger.error("Error while cleaning home dir for user {}",user.getUsername());
            }
        }).start();
        return null;
    }

    public FtpletResult beforeCommand(FtpSession session, FtpRequest request) throws FtpException, IOException {
        String command = request.getCommand().toUpperCase();
        if ("CWD".equals(command) || "MLSD".equals(command) || "PWD".equals(command)) {
            this.onChangeDirectory(session, request);
        }
        return super.beforeCommand(session, request);
    }

    /************ tools ********************************/
    private NodeFtpRequest getNodeRequest(FtpSession session, FtpRequest request) throws FtpException {
        if (request.hasArgument()) {
            String fileName = request.getArgument();
            FtpFile ftpFile = session.getFileSystemView().getFile(fileName);
            FtpUser ftpUser = (FtpUser) session.getUser();
            List<String> pathParts = getPathParts(ftpUser, ftpFile);
            NodeFtpRequest nodeFtpRequest = new NodeFtpRequest(ftpUser, ftpFile, pathParts);
            logger.info("New FTP request at '{}' from user: '{}'，Upload file '{}' to directory:'{}'", Instant.now().toString(), ftpUser.getName(), fileName, ftpFile.getAbsolutePath());
            return nodeFtpRequest;
        }
        return null;
    }

    private List<String> getPathParts(IUser user, FtpFile ftpFile){
        Path path = Paths.get(user.getHomeDirectory(),ftpFile.getAbsolutePath()).normalize();
        List<String> pathParts = new ArrayList<>();
        for (int i=0; i<path.getNameCount(); i++){
            pathParts.add(path.getName(i).toFile().getName());
        }
        String homeDir = user.getUsername()+"_"+user.getId(); // root dir for current user
        int indexOfHomeDir = pathParts.indexOf(homeDir);
        if(indexOfHomeDir!=-1)
            pathParts = pathParts.subList(indexOfHomeDir+1, pathParts.size());
        return pathParts;
    }
}