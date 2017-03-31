<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<div class="pageHeader">
	<form id="pagerForm" onsubmit="return navTabSearch(this);" action="../user/listPmsUser" method="post">
	<!-- 分页表单参数 -->
    <%@include file="/page/inc/pageForm.jsp"%>
	<div class="searchBar">
		<table class="searchContent">
			<tr>
				<td>
					用户登录名：<input type="text" name="userNo" value="${userNo}" size="30" alt="精确查询"  />
				</td>
				<td>
					用户姓名：<input type="text" name="userName" value="${userName}" size="30" alt="模糊查询"  />
				</td>
				<td>状态：</td>
				<td>
					<select name="status" class="combox">
						<option value="">-全部-</option>
						<c:forEach items="${UserStatusEnumList}" var="userStatus">
						<option value="${userStatus.value}"
							<c:if test="${status ne null and status eq userStatus.value}">selected="selected"</c:if>>
							${userStatus.desc}
						</option>
					</c:forEach>
					</select>
				</td>
				<td>
					<div class="subBar">
						<ul>
							<li><div class="buttonActive"><div class="buttonContent"><button type="submit">查询</button></div></div></li>
						</ul>
					</div>
				</td>
			</tr>
		</table>
	</div>
	</form>
</div>
<div class="pageContent">

	<div class="panelBar">
		<ul class="toolBar">
			<rc:permission value="pms:user:add">
				<li><a class="add" href="/TestOne/user/addPmsUserUI" target="dialog" rel="input" title="添加用户"><span>添加用户</span></a></li>
			</rc:permission>
		</ul>
	</div>
	
	<table class="table" targetType="navTab" asc="asc" desc="desc" width="100%" layoutH="130">
		<thead>
			<tr>
				<th width="30">序号</th>
				<th width="200">用户登录名</th>
				<th>用户姓名</th>
				<th width="120">手机号码</th>
				<th width="70">状态</th>
				<th width="100">类型</th>
				<th width="300">操作</th><!-- 图标列不能居中 -->
			</tr>
		</thead>
		<tbody>
		    <c:forEach items="${pageBean.recordList}" var="st" varStatus="varst">
		    	<%-- 普通用户看不到超级管理员信息 --%>
		    	<c:if test="${(type eq UserTypeEnum.ADMIN.value && type eq UserTypeEnum.ADMIN.value) || (type eq UserTypeEnum.USER.value)}">
				<tr target="sid_user" rel="${st.id}">
				    <td>${varst.index+1}</td>
					<td>${st.userNo }</td>
					<td>${st.userName }</td>
					<td>${st.mobileNo }</td>
					<td>
						<c:forEach items="${UserStatusEnumList}" var="userStatus">
							<c:if test="${st.status ne null and st.status eq userStatus.value}">${userStatus.desc}</c:if>
						</c:forEach>
					</td>
					<td>
						<c:forEach items="${UserTypeEnumList}" var="userTypeEnum">
							<c:if test="${st.userType ne null and st.userType eq userTypeEnum.value}">${userTypeEnum.desc}</c:if>
						</c:forEach>
					</td>
					<td>
						[<a href="/TestOne/user/viewPmsUserUI?id=${st.id}" title="查看【${st.userNo }】详情" target="dialog" style="color:blue">查看</a>]
						<c:if test="${type eq UserTypeEnum.USER.value }">
						&nbsp;[<a href="/TestOne/user/editPmsUserUI?id=${st.id}" title="修改【${st.userNo }】" target="dialog" rel="userUpdate" style="color:blue">修改</a>]
						<c:if test="${type eq UserTypeEnum.USER.value }">
						&nbsp;[<a href="/TestOne/user/deleteUserStatus?id=${st.id}" target="ajaxTodo" title="确定要删除吗？" style="color:blue">删除</a>]
						</c:if>
						</c:if>
					</td>
				</tr>
				</c:if>
			</c:forEach>
		</tbody>
	</table>
     <!-- 分页条 -->
    <%@include file="/page/inc/pageBar.jsp"%>
</div>