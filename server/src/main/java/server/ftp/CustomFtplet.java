package server.ftp;

import org.apache.ftpserver.ftplet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.NodeType;
import server.exceptions.CustomException;
import server.models.NodeFtpRequest;
import server.services.INodeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class CustomFtplet extends DefaultFtplet implements Ftplet {

    @Autowired
    INodeService nodeService;
    private static final Logger logger = LoggerFactory.getLogger(CustomFtplet.class);

    @Override
    public FtpletResult onUploadStart(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        //Get the upload path of the uploaded file
        String path = session.getUser().getHomeDirectory();
        //Get upload users
        String name = session.getUser().getName();
        //Get upload file name
        String filename = request.getArgument();
        logger.info("user:'{}'，Upload file to directory:'{}'，The file name is:'{}'，Status: start upload~", name, path, filename);
        return super.onUploadStart(session, request);
    }


    @Override
    public FtpletResult onUploadEnd(FtpSession session, FtpRequest request)
            throws FtpException, IOException {
        //Get the upload path of the uploaded file
        if(request.hasArgument()){
            String fileName = request.getArgument();
            FtpFile ftpFile = session.getFileSystemView().getFile(fileName);
            FtpUser ftpUser = (FtpUser)session.getUser();
            Path filePath = Paths.get(ftpFile.getAbsolutePath());
            File file = filePath.toFile();
            NodeFtpRequest nodeFtpRequest = new NodeFtpRequest();
            nodeFtpRequest.setFile(file);
            nodeFtpRequest.setName(fileName);
            nodeFtpRequest.setOwnerName(ftpFile.getOwnerName());
            nodeFtpRequest.setFileSize(ftpFile.getSize());
            nodeFtpRequest.setPath(filePath.toString());
            nodeFtpRequest.setType(file.isFile() ? NodeType.FILE : NodeType.FOLDER);
            nodeFtpRequest.setUser(ftpUser);
            try {
                nodeService.insertNode(nodeFtpRequest);
            } catch (CustomException e) {
                logger.error("Error while saving ftp node : '{} '",e);
            }
            logger.info("user:'{}'，Upload file to directory:'{}'，The file name is:'{}，Status: successful!'", ftpUser.getName(), filePath, fileName);
        }
        return super.onUploadEnd(session, request);
    }
}