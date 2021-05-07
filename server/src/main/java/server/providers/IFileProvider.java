package server.providers;

import com.mongodb.client.gridfs.model.GridFSFile;
import server.exceptions.CustomException;

import java.io.InputStream;

public interface IFileProvider {

    String saveFile(InputStream inputStream, String name, String contentType, Object meta) throws CustomException;

    String saveFile(byte[] bytes, String name, String contentType, Object meta) throws CustomException;

    void deleteFile(String fileId);

     GridFSFile getFile(String fileId) throws CustomException; // TODO to see later

     byte[] getContent(String fileId) throws CustomException;

     byte[] getContent(String fileId, int startPos) throws CustomException;

     byte[] getContent(String fileId, int startPos, int endPos) throws CustomException;

     InputStream getInputStream(String fileId) throws CustomException;

     InputStream getInputStream(String fileId, int startPos) throws CustomException;

     InputStream getInputStream(String fileId, int startPos, int endPos) throws CustomException;
}
