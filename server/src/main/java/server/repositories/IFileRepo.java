package server.repositories;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import server.data.Node;
import server.exceptions.CustomException;

import java.io.InputStream;

public interface IFileRepo {
     ObjectId saveFile(InputStream inputStream, Node node) throws CustomException;
     void deleteFile(String fileId);
     GridFSFile getFile(String id) throws CustomException;
     GridFsResource getResource(String id) throws CustomException;
}
