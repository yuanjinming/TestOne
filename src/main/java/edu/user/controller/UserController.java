package edu.user.controller;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.druid.stat.TableStat.Mode;

import edu.common.enums.UserStatusEnum;
import edu.common.enums.UserTypeEnum;
import edu.common.page.PageBean;
import edu.common.web.WebHelper;
import edu.user.model.DwzParam;
import edu.user.model.PmsUser;
import edu.user.service.pmsUserServiceImpl;

@Controller
@RequestMapping("/user")
public class UserController extends BaseController{

	private static final Log log = LogFactory.getLog(UserController.class);
	
	@Autowired
	private pmsUserServiceImpl pmsUserService;
	// /////////////////////////////////// 用户管理   //////////////////////////////////////////
	//获取用户信息
	@RequestMapping(value = "/listPmsUser")
	private Object listPmsUser(String userNo,String userName,Integer status,Model model){
		try {
			Map<String, Object> paramMap = new HashMap<String, Object>(); // 业务条件查询参数
			paramMap.put("userNo", userNo); // 用户登录名（精确查询）
			paramMap.put("userName", userName); // 用户姓名（模糊查询）
			paramMap.put("status", status); // 状态
			PageBean pageBean = pmsUserService.listPage(getPageParam(), paramMap);
			model.addAttribute("pageBean", pageBean);
			PmsUser pmsUser = getLoginedUser();// 获取当前登录用户对象
			model.addAttribute("currUserNo", pmsUser.getUserNo());
			// 回显查询条件值
			model.addAttribute("paramMap", paramMap);
			model.addAttribute("UserStatusEnumList", UserStatusEnum.values());
			model.addAttribute("UserStatusEnum", UserStatusEnum.toMap());
			model.addAttribute("UserTypeEnumList", UserTypeEnum.values());
			model.addAttribute("UserTypeEnum", UserTypeEnum.toMap());
			return "pms/PmsUserList";
		} catch (Exception e) {
			log.error("== listPmsUser exception:", e);
			return operateError("获取数据失败");
		}
	}
	
	/**
	 * 查看用户详情.
	 * 
	 * @return .
	 */
	//获取用户信息
	@RequestMapping(value = "/viewPmsUserUI")
	public Object viewPmsUserUI(Model model) {
		try {
			Long userId = Long.parseLong((String) getValues("id"));
			PmsUser pmsUser = pmsUserService.getById(userId);
			if (pmsUser == null) {
				return operateError("无法获取要查看的数据");
			}
			model.addAttribute("UserStatusEnumList", UserStatusEnum.values());
			model.addAttribute("UserStatusEnum", UserStatusEnum.toMap());
			model.addAttribute("UserTypeEnumList", UserTypeEnum.values());
			model.addAttribute("UserTypeEnum", UserTypeEnum.toMap());
			model.addAttribute("UserTypeEnum", UserTypeEnum.toMap());
			model.addAttribute(pmsUser);
			return "pms/PmsUserView";
		} catch (Exception e) {
			log.error("== viewPmsUserUI exception:", e);
			return operateError("获取数据失败");
		}
	}
	/**
	 * 转到添加用户页面 .
	 * 
	 * @return addPmsUserUI or operateError .
	 */
	@RequestMapping(value = "/addPmsUserUI")
	public Object addPmsUserUI(Model model) {
		try {
			model.addAttribute("UserStatusEnumList", UserStatusEnum.values());
			return "pms/PmsUserAdd";
		} catch (Exception e) {
			log.error("== addPmsUserUI exception:", e);
			return operateError("获取角色列表数据失败");
		}
	}
	
	/**
	 * 保存一个用户
	 * 
	 */
	@ResponseBody
	@RequestMapping(value = "/addPmsUser")
	public Object addPmsUser() {
		try {
			String userPwd = (String)getValues("userPwdss"); // 初始登录密码

			String userNo = (String)getValues("userNamess");

			PmsUser pmsUser = new PmsUser();
			pmsUser.setUserName((String)getValues("userName")); // 姓名
			pmsUser.setUserNo(userNo); // 登录名
			pmsUser.setUserPwd(userPwd);
			pmsUser.setRemark((String)getValues("desc")); // 描述
			pmsUser.setIsChangedPwd(false);
			pmsUser.setLastLoginTime(null);
			pmsUser.setMobileNo((String)getValues("mobileNo")); // 手机号码
			int status=Integer.parseInt((String) getValues("status"));
			pmsUser.setStatus(status); // 状态（100:'激活',101:'冻结'1）
			pmsUser.setUserType(String.valueOf(UserTypeEnum.ADMIN.getValue())); // 用户类型（1:超级管理员，2:普通管理员，3:用户主帐号，4:用户子帐号）

			// 表单数据校验
			String validateMsg = validatePmsUser(pmsUser);

			if (StringUtils.isNotBlank(validateMsg)) {
				return operateError(validateMsg); // 返回错误信息
			}

			// 校验用户登录名是否已存在
			PmsUser userNoCheck = pmsUserService.findUserByUserNo(userNo);
			if (userNoCheck != null) {
				return operateError("登录名【" + userNo + "】已存在");
			}

			pmsUser.setUserPwd(DigestUtils.sha1Hex(userPwd)); // 存存前对密码进行加密

			pmsUserService.create(pmsUser);
			return operateSuccess();
		} catch (Exception e) {
			log.error("== addPmsUser exception:", e);
			return operateError("保存用户信息失败");
		}
	}
	
	
	/**
	 * 校验Pms用户表单数据.
	 * 
	 * @param PmsUser
	 *            用户信息.
	 * @param roleUserStr
	 *            关联的角色ID串.
	 * @return
	 */
	private String validatePmsUser(PmsUser user) {
		String msg = ""; // 用于存放校验提示信息的变量
		msg += lengthValidate("真实姓名", user.getUserName(), true, 2, 15);
		msg += lengthValidate("登录名", user.getUserName(), true, 3, 50);
		
		// 登录密码
		String userPwd = user.getUserPwd();
		String userPwdMsg = lengthValidate("登录密码", userPwd, true, 6, 50);
		/*
		 * if (StringUtils.isBlank(loginPwdMsg) &&
		 * !ValidateUtils.isAlphanumeric(loginPwd)) { loginPwdMsg +=
		 * "登录密码应为字母或数字组成，"; }
		 */
		msg += userPwdMsg;

		// 手机号码
		String mobileNo = user.getMobileNo();
		String mobileNoMsg = lengthValidate("手机号", mobileNo, true, 0, 12);
		msg += mobileNoMsg;

		// 状态
		Integer status = user.getStatus();
		if (status == null) {
			msg += "请选择状态，";
		} else if (status.intValue() < 100 || status.intValue() > 101) {
			msg += "状态值不正确，";
		}

		msg += lengthValidate("描述", user.getRemark(), true, 3, 100);
		return msg;
	}

	/**
	 * 删除用户
	 * 
	 * @return
	 * */
	@ResponseBody
	@RequestMapping(value = "/deleteUserStatus")
	public Object deleteUserStatus() {
		long id = Long.parseLong((String) getValues("id"));
		pmsUserService.deleteUserById(id);
		return this.operateSuccess("操作成功");
	}
	
	/**
	 * 转到修改用户界面
	 * 
	 * @return PmsUserEdit or operateError .
	 */
	@RequestMapping(value = "/editPmsUserUI")
	public Object editPmsUserUI(Model model) {
		try {
			long id = Long.parseLong((String) getValues("id"));
			PmsUser pmsUser = pmsUserService.getById(id);
			if (pmsUser == null) {
				return operateError("无法获取要修改的数据");
			}

			// 普通用户没有修改超级管理员的权限
			if (UserTypeEnum.ADMIN.getValue().equals(this.getLoginedUser().getUserType()) && UserTypeEnum.ADMIN.getValue().equals(pmsUser.getUserType())) {
				return operateError("权限不足");
			}
			model.addAttribute(pmsUser);
			model.addAttribute("UserStatusEnum", UserStatusEnum.toMap());
			model.addAttribute("UserTypeEnum", UserTypeEnum.toMap());
			return "pms/PmsUserEdit";
		} catch (Exception e) {
			log.error("== editPmsUserUI exception:", e);
			return operateError("获取修改数据失败");
		}
	}
	
	/**
	 * 保存修改后的用户信息
	 * 
	 * @return operateSuccess or operateError .
	 */
	@ResponseBody
	@RequestMapping(value = "/editPmsUser")
	public Object editPmsUser() {
		try {
			long id = Long.parseLong((String) getValues("id"));

			PmsUser pmsUser = pmsUserService.getById(id);
			if (pmsUser == null) {
				return operateError("无法获取要修改的用户信息");
			}

			// 普通用户没有修改超级管理员的权限
			if ("0".equals(this.getLoginedUser().getUserType()) && "1".equals(pmsUser.getUserType())) {
				return operateError("权限不足");
			}

			pmsUser.setRemark((String)getValues("remark"));
			pmsUser.setMobileNo((String)getValues("mobileNo"));
			pmsUser.setUserName((String)getValues("userName"));

			// 表单数据校验
			String validateMsg = validatePmsUser(pmsUser);
			if (StringUtils.isNotBlank(validateMsg)) {
				return operateError(validateMsg); // 返回错误信息
			}
			pmsUserService.update(pmsUser);
			return operateSuccess();
		} catch (Exception e) {
			log.error("== editPmsUser exception:", e);
			return operateError("更新用户信息失败");
		}
	}
	
	
	/**
	 * 当前登录的用户查看自己帐号的详细信息.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userViewOwnInfo")
	public Object userViewOwnInfo(Model model) {
		try {

			PmsUser pmsUser = this.getLoginedUser();
			if (pmsUser == null) {
				return operateError("无法从会话中获取用户信息");
			}

			PmsUser user = pmsUserService.getById(pmsUser.getId());
			if (user == null) {
				return operateError("无法获取用户信息");
			}

			model.addAttribute("user",user);
			model.addAttribute("UserStatusEnumList", UserStatusEnum.values());
			model.addAttribute("UserStatusEnum", UserStatusEnum.toMap());
			model.addAttribute("UserTypeEnumList", UserTypeEnum.values());
			model.addAttribute("UserTypeEnum", UserTypeEnum.toMap());

			return "pms/PmsUserViewOwnInfo";
		} catch (Exception e) {
			log.error("== editPmsUser exception:", e);
			return operateError("无法获取要修改的用户信息失败");
		}
	}

	
	/**
	 * 进入重置当前登录用户自己的密码的页面.
	 * 
	 * @return
	 */
	@RequestMapping(value = "/userChangeOwnPwdUI")
	public String userChangeOwnPwdUI() {
		return "pms/PmsUserChangeOwnPwd";
	}
	/**
	 * 重置当前登录用户自己的密码.
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/userChangeOwnPwd")
	public Object userChangeOwnPwd() {
		try {

			PmsUser user = this.getLoginedUser();
			if (user == null) {
				return operateError("无法从会话中获取用户信息");
			}

			// 判断旧密码是否正确
			String oldPwd = (String)getValues("oldPwd");
			if (StringUtils.isBlank(oldPwd)) {
				return operateError("请输入旧密码");
			}
			// 旧密码要判空，否则sha1Hex会出错
			if (!user.getUserPwd().equals(DigestUtils.sha1Hex(oldPwd))) {
				return operateError("旧密码不正确");
			}

			// 校验新密码
			String newPwd = (String)getValues("newPwd");
			if (oldPwd.equals(newPwd)) {
				return operateError("新密码不能与旧密码相同");
			}

			String newPwd2 =(String)getValues("newPwd2");
			String validateMsg = validatePassword(newPwd, newPwd2);
			if (StringUtils.isNotBlank(validateMsg)) {
				return operateError(validateMsg); // 返回错误信息
			}

			// 更新密码
			pmsUserService.updateUserPwd(user.getId(), DigestUtils.sha1Hex(newPwd), true);

			return operateSuccess("密码修改成功，请重新登录!");
		} catch (Exception e) {
			log.error("== userChangeOwnPwd exception:", e);
			return operateError("修改密码出错:" + e.getMessage());
		}
	}

	/***
	 * 验证重置密码
	 * 
	 * @param newPwd
	 * @param newPwd2
	 * @return
	 */
	private String validatePassword(String newPwd, String newPwd2) {
		String msg = ""; // 用于存放校验提示信息的变量
		if (StringUtils.isBlank(newPwd)) {
			msg += "新密码不能为空，";
		} else if (newPwd.length() < 6) {
			msg += "新密码不能少于6位长度，";
		}

		if (!newPwd.equals(newPwd2)) {
			msg += "两次输入的密码不一致";
		}
		return msg;
	}
}
