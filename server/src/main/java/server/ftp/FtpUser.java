package server.ftp;

import org.apache.ftpserver.ftplet.Authority;
import org.apache.ftpserver.ftplet.AuthorizationRequest;
import org.apache.ftpserver.ftplet.User;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.util.Assert;
import server.data.IRole;
import server.data.IUser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class FtpUser implements User, IUser {

	private  String id;
	private  String username;
	private  String password;
	private  boolean enabled;
	private  int maxIdleTime;
	private  List<Authority> authorities = new ArrayList<>();
	private  File homeDirectory;
	private  Set<IRole> roles;
	private boolean admin;
	private String email;

	public String uploadDir = "./uploads";

	public static final int MAX_IDLE_TIME_MINUTE = 10;
	private static final List<Authority> adminAuthorities = new ArrayList<>(Arrays.asList(new WritePermission("/")));
	private static final List<Authority> anonAuthorities = new ArrayList<>(Arrays.asList(
			new ConcurrentLoginPermission(100, 10),
			new TransferRatePermission(48000, 48000)
	));

	public FtpUser(IUser user) {
		String homeDirectory = user.getHomeDirectory();
		File root = new File(uploadDir);
		File home = new File(new File(root, user.getUsername()+"_"+user.getId()), homeDirectory);
		Assert.isTrue(home.exists() || home.mkdirs(), "the home directory " + home.getAbsolutePath() + " must exist");
		List<Authority> authorities = getAuthorities(user);
		this.username = user.getUsername();
		this.maxIdleTime = user.getMaxIdleTime() == -1
				? 60_000*MAX_IDLE_TIME_MINUTE
				: user.getMaxIdleTime();
		this.homeDirectory = home;
		this.password = user.getPassword();
		this.enabled = user.isEnabled();
		this.id = user.getId();
		this.roles = user.getRoles();
		this.admin = user.isAdmin();
		this.email = user.getEmail();
		this.authorities.addAll(authorities);
	}

	@Override
	public String getName() {
		return this.username;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public String getUsername() {
		return this.username;
	}

	@Override
	public String getPassword() {
		return this.password;
	}

	@Override
	public boolean isEnabled() {
		return this.enabled;
	}

	@Override
	public List<? extends Authority> getAuthorities() {
		return this.authorities;
	}

	@Override
	public List<? extends Authority> getAuthorities(Class<? extends Authority> aClass) {
		return this.authorities.stream().filter(a -> a.getClass().isAssignableFrom(aClass)).collect(Collectors.toList());
	}

	@Override
	public AuthorizationRequest authorize(AuthorizationRequest req) {
		return this.getAuthorities()
			.stream()
			.filter(a -> a.canAuthorize(req))
			.map(a -> a.authorize(req))
			.filter(Objects::nonNull)
			.findFirst()
			.orElse(null);
	}

	@Override
	public int getMaxIdleTime() {
		return this.maxIdleTime;
	}

	@Override
	public boolean getEnabled() {
		return this.enabled;
	}

	@Override
	public String getHomeDirectory() {
		return this.homeDirectory.getAbsolutePath();
	}

	@Override
	public boolean isAdmin() {
		return this.admin;
	}

	@Override
	public Set<IRole> getRoles() {
		return this.roles;
	}

	@Override
	public String getEmail() {
		return this.email;
	}

	public static List<Authority> getAuthorities(IUser user) {
		return getAuthorities(user.isAdmin());
	}

	public static List<Authority> getAuthorities(boolean isAdmin) {
		List<Authority> authorities = new ArrayList<>(anonAuthorities);
		if(isAdmin){
			authorities.addAll(adminAuthorities);
		}
		return authorities;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setMaxIdleTime(int maxIdleTime) {
		this.maxIdleTime = maxIdleTime;
	}

	public void setAuthorities(List<Authority> authorities) {
		this.authorities = authorities;
	}

	public void setHomeDirectory(File homeDirectory) {
		this.homeDirectory = homeDirectory;
	}

	public void setRoles(Set<IRole> roles) {
		this.roles = roles;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getUploadDir() {
		return uploadDir;
	}

	public void setUploadDir(String uploadDir) {
		this.uploadDir = uploadDir;
	}

	public static List<Authority> getAdminAuthorities() {
		return adminAuthorities;
	}

	public static List<Authority> getAnonAuthorities() {
		return anonAuthorities;
	}
}
