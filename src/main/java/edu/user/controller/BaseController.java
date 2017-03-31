package edu.user.controller;

import edu.common.web.SessionConstant;
import edu.common.web.WebHelper;
import edu.user.model.PmsUser;

public class BaseController extends WebHelper{
	/**
	 * 取出当前登录用户对象
	 */
	public PmsUser getLoginedUser() {
		PmsUser user = (PmsUser)getSession().getAttribute(SessionConstant.USER_SESSION_KEY);
		return user;
	}

}
