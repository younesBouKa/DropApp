package server.ftp;

import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import server.user.IUserService;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
class FtpUserManager implements UserManager {

	private final File root = new File("uploads");
	@Autowired
	private IUserService userService;

	private server.user.User defaultUser;

	// AUTHORITIES
	private final List<Authority> adminAuthorities = new ArrayList<>(Arrays.asList(new WritePermission("/")));
	private final List<Authority> anonAuthorities = new ArrayList<>(Arrays.asList(
			new ConcurrentLoginPermission(100, 100),
			new TransferRatePermission(4800, 4800)
	));

	private final Function<server.user.User, User> userMapper = (server.user.User user) -> {
		String username = user.getUsername();
		String password = user.getPassword();
		boolean enabled = user.isEnabled();
		boolean admin = user.isAdmin();
		String id = user.getId();
		File home = new File(new File(root, username), "home");
		Assert.isTrue(home.exists() || home.mkdirs(), "the home directory " + home.getAbsolutePath() + " must exist");
		List<Authority> authorities = new ArrayList<>(anonAuthorities);
		if (admin) {
			authorities.addAll(adminAuthorities);
		}
		return new FtpUser(username, password, enabled, authorities, -1, home);
	};

	public FtpUserManager(){
		// TODO just for test
		defaultUser = new server.user.User();
		defaultUser.setAdmin(true);
		defaultUser.setEnabled(true);
		defaultUser.setUsername("user");
		defaultUser.setPassword("pass");
		defaultUser.setHomeDirectory(Paths.get("home").toAbsolutePath().toString());
		defaultUser.setAuthorities(anonAuthorities);
		defaultUser.getAuthorities().addAll(adminAuthorities);
		defaultUser.setMaxIdleTime(-1);
	}

	@Override
	public User getUserByName(String name) {
		List<User> users = this.userService.getUsersByName(name)
				.stream()
				.map(userMapper)
				.collect(Collectors.toList());
		if(users.size()==0)
			return null;
		User user = users.get(0);
		return userMapper.apply(defaultUser);//user;
	}

	@Override
	public String[] getAllUserNames() {
		List<String> userNames = this.userService.getAllUsernames();
		return userNames.toArray(new String[0]);
	}

	@Override
	public void delete(String name) {
		int update = this.userService.deleteByUsername(name);
		Assert.isTrue(update > -1, "there must be some acknowledgment");
	}

	@Override
	public void save(User user) throws FtpException {
		server.user.User updatedUser = this.userService.updateUserByName(user.getName(), user.getPassword(), user.getEnabled(), user.getAuthorities().equals(this.adminAuthorities));
		Assert.isInstanceOf(server.user.User.class, updatedUser, "there must be some acknowledgment of the write");
	}

	@Override
	public boolean doesExist(String username) throws FtpException {
		return this.getUserByName(username) != null;
	}

	@Override
	public User authenticate(Authentication authentication) throws AuthenticationFailedException {
		Assert.isTrue(authentication instanceof UsernamePasswordAuthentication, "the given authentication must support username and password authentication");
		UsernamePasswordAuthentication upw = (UsernamePasswordAuthentication) authentication;
		String user = upw.getUsername();
		return Optional
			.ofNullable(this.getUserByName(user))
			.filter(u -> {
				String incomingPw = u.getPassword();
				return encode(incomingPw).equalsIgnoreCase(u.getPassword());
			})
			.orElseThrow(() -> new AuthenticationFailedException("Authentication has failed! Try your username and password."));
	}

	/**
		* TODO do something more responsible than this!
		*/
	private String encode(String pw) {
		return pw;
	}

	@Override
	public String getAdminName() {
		return "admin";
	}

	@Override
	public boolean isAdmin(String s) {
		return getAdminName().equalsIgnoreCase(s);
	}
}
