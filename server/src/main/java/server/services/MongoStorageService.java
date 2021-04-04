package server.services;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import server.data.FileDocument;
import server.exceptions.FileStorageException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class MongoStorageService implements IStorageService {

   @Autowired
   private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFsOperations operations;
    @Value("${app.upload.dir}")
    public String uploadDir;

    @Override
    public String storeDB(MultipartFile file) {
        DBObject metaData = new BasicDBObject();
        metaData.put("type", file.getResource().getDescription());
        metaData.put("title", file.getOriginalFilename());
        try {
            ObjectId id = gridFsTemplate.store(file.getInputStream(), file.getName(), file.getContentType(), metaData);
            return id.toString();
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileStorageException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }
    }

    @Override
    public String storeLocally(MultipartFile file) {
        try {
            Path copyLocation = Paths
                    .get(uploadDir + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            if(! Files.exists(copyLocation))
                Files.createDirectories(copyLocation);
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
            return copyLocation.toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException("Could not store file " + file.getOriginalFilename()
                    + ". Please try again!");
        }
    }

    @Override
    public FileDocument getFile(String id){
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        FileDocument fileDocument = new FileDocument();
        try {
            fileDocument.setTitle(file.getMetadata().get("title").toString());
            fileDocument.setStream(operations.getResource(file).getInputStream());
            return fileDocument;
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException("Could not get file " + id );
        }
    }
}
