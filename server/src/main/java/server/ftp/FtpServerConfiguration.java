package server.ftp;

import org.apache.ftpserver.FtpServer;
import org.apache.ftpserver.FtpServerFactory;
import org.apache.ftpserver.filesystem.nativefs.NativeFileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.Ftplet;
import org.apache.ftpserver.ftplet.UserManager;
import org.apache.ftpserver.listener.Listener;
import org.apache.ftpserver.listener.ListenerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Configuration
class FtpServerConfiguration {

	@Bean
	FileSystemFactory fileSystemFactory() {
		NativeFileSystemFactory fileSystemFactory = new NativeFileSystemFactory();
		fileSystemFactory.setCreateHome(true);
		fileSystemFactory.setCaseInsensitive(false);
		return fileSystemFactory::createFileSystemView;
	}

	@Bean
	Listener nioListener(@Value("${ftp.port:7777}") int port) {
		ListenerFactory listenerFactory = new ListenerFactory();
		listenerFactory.setPort(port);
		return listenerFactory.createListener();
	}

	@Bean
	FtpServer ftpServer(Map<String, CustomFtplet> ftpletMap, UserManager userManager, Listener nioListener, FileSystemFactory fileSystemFactory) {
		FtpServerFactory ftpServerFactory = new FtpServerFactory();
		ftpServerFactory.setListeners(Collections.singletonMap("default", nioListener));
		ftpServerFactory.setFileSystem(fileSystemFactory);
		Map<String, Ftplet> map = new HashMap<>();
		for(String key : ftpletMap.keySet()){
			map.put(key,ftpletMap.get(key));
		}
		ftpServerFactory.setFtplets(map);
		ftpServerFactory.setUserManager(userManager);
		return ftpServerFactory.createServer();
	}

	@Bean
	DisposableBean destroysFtpServer(FtpServer ftpServer) {
		return ftpServer::stop;
	}

	@Bean
	InitializingBean startsFtpServer(FtpServer ftpServer) {
		return ftpServer::start;
	}

	@Bean
	UserManager userManager() {
		return new FtpUserManager();
	}
}
