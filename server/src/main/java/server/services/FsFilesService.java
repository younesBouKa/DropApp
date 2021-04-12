package server.services;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import server.data.Node;
import server.exceptions.FileStorageException;

import java.io.IOException;
import java.util.logging.Logger;

import static java.util.logging.Level.*;

@Service
public class FsFilesService {

    private final Logger logger = Logger.getLogger(FsFilesService.class.getName());

   @Autowired
    private GridFsTemplate gridFsTemplate;

   @Autowired
    private GridFsOperations operations;

    @Value("${app.upload.dir}")
    public String uploadDir;

    public ObjectId saveFile(MultipartFile file, Node node) throws IOException{
        ObjectId id = gridFsTemplate.store(file.getInputStream(), node.getName(), file.getContentType(), node);
        return id;
    }

    public void deleteWithId(String fileId){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    public GridFsResource getFileContent(String id){
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file==null){
            logger.log(SEVERE, String.format("Could not found file with id %s %n", id));
            throw new FileStorageException("Could not get file " + id );
        }
        return operations.getResource(file);
    }
}
