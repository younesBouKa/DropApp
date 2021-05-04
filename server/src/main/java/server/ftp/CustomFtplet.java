package server.ftp;

import org.apache.commons.io.IOUtils;
import org.apache.ftpserver.ftplet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.IUser;
import server.data.Node;
import server.exceptions.CustomException;
import server.models.NodeFtpRequest;
import server.services.INodeService;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            try {
                nodeService.deleteNode(nodeFtpRequest);
                logger.info("deleting ftp node with success : '{} '", nodeFtpRequest.getName());
            } catch (CustomException e) {
                logger.error("Error while deleting node from FTP server : '{} '", e.getMessage());
            }
        }
        return super.onDeleteEnd(session, request);
    }

    @Override
    public FtpletResult onRmdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            try {
                nodeService.deleteNode(nodeFtpRequest);
                logger.info("deleting folder from FTP server with success : '{}' ", nodeFtpRequest.getName());
            } catch (CustomException e) {
                logger.error("Error while deleting folder from FTP server : '{}' ", e.getMessage());
            }
        }
        return super.onRmdirEnd(session, request);
    }

    @Override
    public FtpletResult onMkdirEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            try {
                nodeService.insertNode(nodeFtpRequest);
                logger.info("creating folder in FTP server : '{} '", nodeFtpRequest.getName());
            } catch (CustomException e) {
                logger.error("Error while creating folder in FTP server : '{} '", e.getMessage());
            }
        }
        return super.onMkdirEnd(session, request);
    }

    @Override
    public FtpletResult onRenameEnd(FtpSession session, FtpRequest request) throws FtpException, IOException {
        NodeFtpRequest nodeFtpRequest = getNodeRequest(session, request);
        if (nodeFtpRequest != null) {
            try {
                nodeService.insertNode(nodeFtpRequest);
                logger.info("renaming node in FTP server : '{} '", nodeFtpRequest.getName());
            } catch (CustomException e) {
                logger.error("Error while renaming ftp node : '{} '", e.getMessage());
            }
        }
        return super.onRenameEnd(session, request);
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
        if(indexOfHomeDir!=-1 && indexOfHomeDir != pathParts.size()-1)
            pathParts = pathParts.subList(indexOfHomeDir+1, pathParts.size());
        return pathParts;
    }
}