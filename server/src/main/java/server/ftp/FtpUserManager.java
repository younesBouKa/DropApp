package server.ftp;

import org.apache.ftpserver.ftplet.*;
import org.apache.ftpserver.usermanager.UsernamePasswordAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import server.data.IUser;
import server.user.services.IUserService;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Component
class FtpUserManager implements UserManager {

	@Autowired
	private IUserService userService;

	private final Function<IUser, User> userMapper = (IUser user) -> new FtpUser(user);

	@Override
	public User getUserByName(String name) {
		IUser user = userService.getUserByUsername(name);
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
		IUser updatedUser = this.userService.updateUserByName(
				user.getName(),
				user.getPassword(),
				user.getEnabled(),
				user.getAuthorities().equals(FtpUser.getAuthorities(false)));
		Assert.isInstanceOf(IUser.class, updatedUser, "there must be some acknowledgment of the write");
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
