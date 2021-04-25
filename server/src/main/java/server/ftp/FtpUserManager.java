package server.ftp;

import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.apache.ftpserver.usermanager.impl.ConcurrentLoginPermission;
import org.apache.ftpserver.usermanager.impl.TransferRatePermission;
import org.apache.ftpserver.usermanager.impl.WritePermission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import server.user.services.IUserService;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
class FtpUserManager implements UserManager {

	private final File root = new File("uploads");
	@Autowired
	private IUserService userService;

	private static final List<Authority> adminAuthorities = new ArrayList<>(Arrays.asList(new WritePermission("/")));
	private static final List<Authority> anonAuthorities = new ArrayList<>(Arrays.asList(
			new ConcurrentLoginPermission(100, 100),
			new TransferRatePermission(4800, 4800)
	));

	private final Function<server.user.data.User, User> userMapper = (server.user.data.User user) -> {
		String username = user.getUsername();
		String password = user.getPassword();
		boolean enabled = user.isEnabled();
		String homeDirectory = user.getHomeDirectory();
		File home = new File(new File(root, username), homeDirectory);
		Assert.isTrue(home.exists() || home.mkdirs(), "the home directory " + home.getAbsolutePath() + " must exist");
		List<Authority> authorities = getAuthorities(user);
		return new FtpUser(username, password, enabled, authorities, user.getMaxIdleTime(), home);
	};

	public static List<Authority> getAuthorities(server.user.data.User user) {
		return getAuthorities(user.isAdmin());
	}

	public static List<Authority> getAuthorities(boolean isAdmin) {
		List<Authority> authorities = new ArrayList<>(anonAuthorities);
		if(isAdmin){
			authorities.addAll(adminAuthorities);
		}
		return authorities;
	}

	@Override
	public User getUserByName(String name) {
		server.user.data.User user = userService.getUserByUsername(name);
		if(user!=null)
			return userMapper.apply(user);
		return null;
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
		server.user.data.User updatedUser = this.userService.updateUserByName(
				user.getName(),
				user.getPassword(),
				user.getEnabled(),
				user.getAuthorities().equals(getAuthorities(false)));
		Assert.isInstanceOf(server.user.data.User.class, updatedUser, "there must be some acknowledgment of the write");
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
