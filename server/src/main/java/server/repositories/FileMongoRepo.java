package server.repositories;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import server.exceptions.CustomException;

import java.io.IOException;
import java.util.logging.Logger;

import static server.exceptions.Message.*;

@Component
public class FileMongoRepo implements IFileRepo{

    private final Logger logger = Logger.getLogger(FileMongoRepo.class.getName());
    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFsOperations operations;

    public ObjectId saveFile(MultipartFile file, Object metaData) throws CustomException {
        try {
            return gridFsTemplate.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), metaData);
        }catch (IOException e){
            throw new CustomException(ERROR_WHILE_SAVING_FILE, file.getOriginalFilename());
        }
    }

    public void deleteFile(String fileId){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    public GridFSFile getFile(String id) throws CustomException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file==null){
            throw new CustomException(NO_CONTENT_FOR_FILE_ID, id);
        }
        return file;
    }

    public GridFsResource getResource(String id) throws CustomException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file==null){
            throw new CustomException(NO_CONTENT_FOR_FILE_ID, id);
        }
        return operations.getResource(file);
    }
}
