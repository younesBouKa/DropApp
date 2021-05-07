package server.providers;

import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;
import server.exceptions.CustomException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import static server.exceptions.Message.*;

@Component
public class FileProvider implements IFileProvider{

    private final Logger logger = Logger.getLogger(FileProvider.class.getName());

    @Autowired
    private GridFsTemplate gridFsTemplate;
    @Autowired
    private GridFsOperations operations;

    @Override
    public String saveFile(InputStream inputStream, String name, String contentType, Object meta) throws CustomException {
        ObjectId id = gridFsTemplate.store(inputStream, name, contentType, meta);
        return id.toHexString();
    }

    @Override
    public String saveFile(byte[] bytes, String name, String contentType, Object meta) throws CustomException {
        try (ByteArrayInputStream bin = new ByteArrayInputStream(bytes)){
                return saveFile(bin, name, contentType, meta);
        } catch (IOException e) {
            e.printStackTrace();
            throw new CustomException(e, ERROR_WHILE_SAVING_FILE, name);
        }
    }

    @Override
    public void deleteFile(String fileId){
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(fileId)));
    }

    @Override
    public GridFSFile getFile(String fileId) throws CustomException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(fileId)));
        if(file==null){
            throw new CustomException(NO_CONTENT_FOR_FILE_ID, fileId);
        }
        return file;
    }

    @Override
    public byte[] getContent(String fileId) throws CustomException {
        return getContent(fileId, 0);
    }

    @Override
    public byte[] getContent(String fileId, int startPos) throws CustomException{
        return getContent(fileId, startPos, -1);
    }

    @Override
    public byte[] getContent(String fileId, int startPos, int endPos) throws CustomException{
        GridFsResource resource = getFileResource(fileId);
        try {
            long contentLength = resource.contentLength();
            if(endPos==-1)
                endPos = (int)contentLength;
            if(endPos>contentLength)
                throw new CustomException(INDEX_OUT_OF_BOUND, startPos, contentLength);
            if(startPos>endPos || startPos<0)
                throw new CustomException(INDEX_OUT_OF_BOUND, startPos, contentLength);
            try (InputStream in = resource.getInputStream();
                 ByteArrayOutputStream bout = new ByteArrayOutputStream()
            ){
                IOUtils.copyLarge(in, bout, startPos,endPos-startPos);
                return bout.toByteArray();
            }
        }catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, fileId);
        }
    }

    @Override
    public InputStream getInputStream(String fileId) throws CustomException {
        return getInputStream(fileId, 0);
    }

    @Override
    public InputStream getInputStream(String fileId, int startPos) throws CustomException{
        return getInputStream(fileId, startPos, -1);
    }

    @Override
    public InputStream getInputStream(String fileId, int startPos, int endPos) throws CustomException{
        try {
            byte[] content = getContent(fileId, startPos, endPos);
            return new ByteArrayInputStream(content);
        }
        catch (Exception e){
            throw new CustomException(e, UNKNOWN_EXCEPTION, fileId);
        }
    }

    public GridFsResource getFileResource(String id) throws CustomException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("_id").is(id)));
        if(file==null){
            throw new CustomException(NO_CONTENT_FOR_FILE_ID, id);
        }
        return operations.getResource(file);
    }
}
