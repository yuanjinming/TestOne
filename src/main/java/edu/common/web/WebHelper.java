package edu.common.web;

import java.util.Map;

import javax.json.Json;
import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang3.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSON;

import edu.common.page.PageBean;
import edu.common.page.PageParam;
import edu.user.model.DwzParam;

public class WebHelper {
	/**
	 * 获取当前的Request
	 * @return
	 */
	protected HttpServletRequest getRequest(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
	}
	
	public Object getValues(String key) {
		return getRequest().getParameter(key);
	}
	
	/**
	 * 获取当前的Response
	 * @return
	 */
	protected HttpServletResponse getResponse(){
		return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getResponse();
	}
	
	
	/**
	 * 获取当前的session
	 * @return
	 */
	protected HttpSession getSession(){
		return getRequest().getSession();
	}

	/**
	 * 取得会话ID(sessionId).
	 * 
	 * @return sessionId .
	 */
	public String getSessionId() {
		return getRequest().getSession().getId();
	}


	/**
	 * 取得当前web应用的根路径
	 * 
	 * @return
	 */
	public String getWebRootPath() {
		return   ContextLoader.getCurrentWebApplicationContext().getServletContext().getRealPath("/");
	}
	
	/**
	 * 添加cookie
	 * 
	 * @param path
	 * @param key
	 * @param value
	 * @param maxAge
	 */
	public void addCookie(String path, String key, String value, int maxAge) {
		Cookie cookie = new Cookie(key, value);
		if (path != null) {
			cookie.setPath(path);
		}
		cookie.setMaxAge(maxAge);
		HttpServletResponse response = getResponse();
		response.addCookie(cookie);
	}

	/**
	 * 添加cookie
	 * 
	 * @param key
	 * @param value
	 */
	public void addCookie(String key, String value) {
		Cookie cookie = new Cookie(key, value);
		HttpServletResponse response = getResponse();
		response.addCookie(cookie);
	}
	
	
	//////////////////////////////////////////
	
	public PageBean pageBean;

	public Integer pageNum;

	/**
	 * pageBean.
	 * 
	 * @return the pageBean
	 */
	public PageBean getPageBean() {
		return pageBean;
	}

	/**
	 * @param pageBean
	 *            the pageBean to set
	 */
	public void setPageBean(PageBean pageBean) {
		this.pageBean = pageBean;
	}
	
	/**
	 * 获取分页参数，包含当前页、每页记录数.
	 * 
	 * @return PageParam .
	 */
	public PageParam getPageParam() {
		return new PageParam(getPageNum(), getNumPerPage());
	}
	
	private int getNumPerPage() {
		String numPerPageStr = getRequest().getParameter("numPerPage");
		int numPerPage = 15;
		if (StringUtils.isNotBlank(numPerPageStr)) {
			numPerPage = Integer.parseInt(numPerPageStr);
		}
		return numPerPage;
	}
	private int getPageNum() {
		// 当前页数
		String pageNumStr = getRequest().getParameter("pageNum");
		int pageNum = 1;
		if (StringUtils.isNotBlank(pageNumStr)) {
			pageNum = Integer.valueOf(pageNumStr);
		}
		return pageNum;
	}
	/////////////////////////////////////////////////////////////////////
	/**
	 * 响应DWZ的ajax失败请求（statusCode="300"）,跳转到ajaxDone视图.
	 * 
	 * @author WuShuicheng.
	 * @param message
	 *            提示消息.
	 * @return ajaxDone .
	 */
	public DwzParam operateError(String message) {
		return ajaxDone("300", message);
	}
	/**
	 * 响应DWZ的Ajax成功请求（statusCode="200"）,<br/>
	 * 跳转到operateSuccess视图，提示设置的消息内容.
	 * 
	 * @author WuShuicheng.
	 * @param message
	 *            提示消息.
	 * @return operateSuccess .
	 */
	public DwzParam operateSuccess(String message) {
		return ajaxDone("200", message);
	}
	/**
	 * 响应DWZ-UI的Ajax成功请求（statusCode="200"）,<br/>
	 * 跳转到operateSuccess视图并提示“操作成功”.
	 * 
	 * @author WuShuicheng.
	 * @param message
	 *            提示消息.
	 * @return operateSuccess .
	 */
	public DwzParam operateSuccess() {
		return ajaxDone("200", "操作成功");
	}
	
	/**
	 * 响应DWZ的Ajax请求.
	 * 
	 * @author WuShuicheng.
	 * @param statusCode
	 *            statusCode:{ok:200, error:300, timeout:301}.
	 * @param message
	 *            提示消息.
	 */
	private DwzParam ajaxDone(String statusCode, String message) {
		DwzParam param = getDwzParam(statusCode, message);
		return param;
	}
	
	/**
	 * 根据request对象，获取页面提交过来的DWZ框架的AjaxDone响应参数值.
	 * 
	 * @author WuShuicheng.
	 * @param statusCode
	 *            状态码.
	 * @param message
	 *            操作结果提示消息.
	 * @return DwzParam :封装好的DwzParam对象 .
	 */
	public DwzParam getDwzParam(String statusCode, String message) {
		// 获取DWZ Ajax响应参数值,并构造成参数对象
		HttpServletRequest req =getRequest();
		String navTabId = req.getParameter("navTabId");
		String dialogId = req.getParameter("dialogId");
		String callbackType = req.getParameter("callbackType");
		String forwardUrl = req.getParameter("forwardUrl");
		String rel = req.getParameter("rel");
		return new DwzParam(statusCode, message, navTabId, dialogId, callbackType, forwardUrl, rel, null);
	}
	
	
	/**
	 * 与DWZ框架结合的表单属性长度校验方法.
	 * 
	 * @param propertyName
	 *            要校验的属性中文名称，如“登录名”.
	 * @param property
	 *            要校验的属性值，如“gzzyzz”.
	 * @param isRequire
	 *            是否必填:true or false.
	 * @param minLength
	 *            最少长度:大于或等于0，如果不限制则可请设为0.
	 * @param maxLength
	 *            最大长度:对应数据库字段的最大长度，如不限制则可设为0.
	 * @return 校验结果消息，校验通过则返回空字符串 .
	 */
	protected String lengthValidate(String propertyName, String property, boolean isRequire, int minLength, int maxLength) {
		
		int propertyLenght = strLengthCn(property);
		if (isRequire && propertyLenght == 0) {
			return propertyName + "不能为空，"; // 校验不能为空
		} else if (isRequire && minLength != 0 && propertyLenght < minLength) {
			return propertyName + "不能少于" + minLength + "个字符，"; // 必填情况下校验最少长度
		} else if (maxLength != 0 && propertyLenght > maxLength) {
			return propertyName + "不能多于" + maxLength + "个字符，"; // 校验最大长度
		} else {
			return ""; // 校验通过则返回空字符串 .
		}
	}
	/**
	 * 获取字符串的长度，如果有中文，则每个中文字符计为3位 ，当字符串为空时返回0.
	 * 
	 * @param str 字符串 .
	 * @return 字符串的长度 .
	 */
	private int strLengthCn(String str)
	{
		if (StringUtils.isBlank(str)){
			return 0;
		}
		int valueLength = 0;
		final String chinese = "[\u0391-\uFFE5]";
		/* 获取字段值的长度，如果含中文字符，则每个中文字符长度为2，否则为1 */
		for (int num = 0; num < str.length(); num++){
			/* 获取一个字符 */
			final String temp = str.substring(num, num + 1);
			/* 判断是否为中文字符 */
			if (temp.matches(chinese)){
				/* 中文字符长度为3 */
				valueLength += 3;
			} else{
				/* 其他字符长度为1 */
				valueLength += 1;
			}
		}
		return valueLength;
	}
	

}
