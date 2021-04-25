package server.ftp;

import org.apache.ftpserver.ftplet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.data.Node;
import server.exceptions.CustomException;
import server.services.NodeService;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomFtplet extends DefaultFtplet implements Ftplet {

    @Autowired
    NodeService nodeService;
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
            String homeDir = ftpUser.getHomeDirectory();
            String relativeParentPath = ftpFile
                    .getAbsolutePath()
                    .substring(
                            0,
                            ftpFile.getAbsolutePath().lastIndexOf(fileName)
                    );
            Path filePath = Paths.get(relativeParentPath,fileName);
            Path parentPath = filePath.getParent();
            File file = filePath.toFile();
            Node node = null;
            try {
                Map<String, Object> metadata = new HashMap<>();
                metadata.put("parentPath", relativeParentPath);
                if(file.isFile())
                    node = nodeService.createFile(ftpFile, metadata,true);
            } catch (CustomException e) {
                logger.error(e.getMessage());
            }
            logger.info("user:'{}'，Upload file to directory:'{}'，The file name is:'{}，Status: successful!'", ftpUser.getName(), filePath, fileName);
        }
        return super.onUploadEnd(session, request);
    }
}