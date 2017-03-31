package edu.user.controller;

import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import edu.common.enums.UserStatusEnum;
import edu.common.enums.UserTypeEnum;
import edu.common.web.SessionConstant;
import edu.common.web.WebHelper;
import edu.user.model.PmsUser;
import edu.user.service.pmsUserServiceImpl;

@Controller
@RequestMapping("/login")
public class LoginController extends BaseController{
	private static final Log log = LogFactory.getLog(UserController.class);
	
	@Autowired
	private pmsUserServiceImpl pmsUserService;

	/**
	 * 进入登录页面.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userLogin")
	public String userLogin(String userNo,String userPwd,Model model) {
		if (userNo==null||userNo.equals("")) {
			model.addAttribute("userNoMsg", "用户名不能为空");
			return "login";
		}
		
		PmsUser user = pmsUserService.findUserByUserNo(userNo);
		if (user == null) {
			log.warn("== no such user");
			model.addAttribute("userNoMsg", "用户名或密码不正确");
			return "login";
		}
		
		if (user.getStatus().intValue() == UserStatusEnum.INACTIVE.getValue()) {
			log.warn("== 帐号【" + userNo + "】已被冻结");
			model.addAttribute("userNoMsg", "该帐号已被冻结");
			return "login";
		}
		
		if (userPwd==null||userPwd.equals("")) {
			model.addAttribute("userPwdMsg", "密码不能为空");
			return "login";
		}
		
		// 加密明文密码
		// 验证密码
		if (user.getUserPwd().equals(DigestUtils.sha1Hex(userPwd))) {
			// 用户信息，包括登录信息和权限
			this.getSession().setAttribute(SessionConstant.USER_SESSION_KEY, user);
			// 将主帐号ID放入Session 
			if (UserTypeEnum.MAIN_USER.getValue().equals(user.getUserType())) {
				this.getSession().setAttribute(SessionConstant.MAIN_USER_ID_SESSION_KEY, user.getId());
			}
			if (UserTypeEnum.SUB_USER.getValue().equals(user.getUserType())) {
				this.getSession().setAttribute(SessionConstant.MAIN_USER_ID_SESSION_KEY, user.getMainUserId());
			} else {
				// 其它类型用户的主帐号ID默认为0
				this.getSession().setAttribute(SessionConstant.MAIN_USER_ID_SESSION_KEY, 0L);
			}
			model.addAttribute("userNo", userNo);
			model.addAttribute("lastLoginTime", user.getLastLoginTime());

			try {
				// 更新登录数据
				user.setLastLoginTime(new Date());
				user.setPwdErrorCount(0); // 错误次数设为0
				pmsUserService.update(user);

			} catch (Exception e) {
				model.addAttribute("errorMsg", e.getMessage());
				return "login";
			}
			// 判断用户是否重置了密码，如果重置，弹出强制修改密码页面； TODO
			model.addAttribute("isChangePwd", user.getIsChangedPwd());
			return "index";
		} else {
			// 密码错误
			log.warn("== wrongPassword");
			// 错误次数加1
			Integer pwdErrorCount = user.getPwdErrorCount();
			if (pwdErrorCount == null) {
				pwdErrorCount = 0;
			}
			user.setPwdErrorCount(pwdErrorCount + 1);
			user.setPwdErrorTime(new Date()); // 设为当前时间
			String msg = "";
			if (user.getPwdErrorCount().intValue() >= SessionConstant.WEB_PWD_INPUT_ERROR_LIMIT) {
				// 超5次就冻结帐号
				user.setStatus(UserStatusEnum.INACTIVE.getValue());
				msg += "<br/>密码已连续输错【" + SessionConstant.WEB_PWD_INPUT_ERROR_LIMIT + "】次，帐号已被冻结";
			} else {
				msg += "<br/>密码错误，再输错【" + (SessionConstant.WEB_PWD_INPUT_ERROR_LIMIT - user.getPwdErrorCount().intValue()) + "】次将冻结帐号";
			}
			pmsUserService.update(user);
			model.addAttribute("userPwdMsg", msg);
			return "page/login";
		}
	}
	
	/**
	 * 跳转到退出确认页面.
	 * 
	 * @return LogOutConfirm.
	 */
	@RequestMapping(value = "/logoutConfirm")
	public String logoutConfirm() {
		log.info("== logoutConfirm");
		return "logoutConfirm";
	}

	/**
	 * 退出登录
	 * 
	 * @return
	 * @throws Exception
	 */
	@RequestMapping(value = "/logout")
	public String logout() throws Exception {
		this.getSession().removeAttribute(SessionConstant.MAIN_USER_ID_SESSION_KEY);
		this.getSession().removeAttribute(SessionConstant.USER_SESSION_KEY);
		log.info("== logout");
		return "login";
	}

	/**
	 * 跳转到登录超时确认页面.
	 * 
	 * @return LogOutConfirm.
	 * @throws Exception
	 */
	@RequestMapping(value = "/timeoutConfirm")
	public String timeoutConfirm() throws Exception {
		this.getSession().removeAttribute(SessionConstant.MAIN_USER_ID_SESSION_KEY);
		this.getSession().removeAttribute(SessionConstant.USER_SESSION_KEY);
		return "timeoutConfirm";
	}
	

}
