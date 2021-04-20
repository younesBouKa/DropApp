package server.repositories;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.web.multipart.MultipartFile;
import server.exceptions.CustomException;

public interface IFileRepo {
     ObjectId saveFile(MultipartFile file, Object metaData) throws CustomException;
     void deleteFile(String fileId);
     GridFSFile getFile(String id) throws CustomException;
     GridFsResource getResource(String id) throws CustomException;
}
