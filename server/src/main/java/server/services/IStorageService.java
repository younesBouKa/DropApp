package server.services;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;
import server.data.FileDocument;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public interface IStorageService {

    String storeLocally(MultipartFile file);

    String storeDB(MultipartFile file);

    FileDocument getFile(String id) throws IllegalStateException, IOException;
}