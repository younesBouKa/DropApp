package server.services;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.ftpserver.ftplet.FtpFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.exceptions.CustomException;

import java.io.IOException;
import java.util.logging.Logger;

import static server.exceptions.Message.*;

@Service
public class FsFilesService {

    private final Logger logger = Logger.getLogger(FsFilesService.class.getName());

   @Autowired
    private GridFsTemplate gridFsTemplate;

   @Autowired
    private GridFsOperations operations;

    public ObjectId saveFile(MultipartFile file, Node node) throws CustomException {
        try {
            return gridFsTemplate.store(file.getInputStream(), node.getName(), file.getContentType(), node);
        }catch (IOException e){
          throw new CustomException(ERROR_WHILE_SAVING_FILE,node.getName());
        }
    }

    public ObjectId saveFile(FtpFile file, Node node) throws CustomException {
        try {
            return gridFsTemplate.store(file.createInputStream(0), node.getName(), node);
        }catch (IOException e){
          throw new CustomException(ERROR_WHILE_SAVING_FILE,node.getName());
        }
    }

    public void deleteWithId(String fileId){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    public GridFsResource getFileContent(String id) throws CustomException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file==null){
            throw new CustomException(NO_CONTENT_FOR_FILE_ID, id);
        }
        return operations.getResource(file);
    }
}
