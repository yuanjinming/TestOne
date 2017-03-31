package edu.common.page;

import java.io.Serializable;

import org.apache.log4j.chainsaw.Main;

/**
 * 
 * @描述: 分页参数传递工具类 .
 * @作者: WuShuicheng .
 * @创建时间: 2013-9-4,下午2:23:47 .
 * @版本: 1.0 .
 */
public class PageParam implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6297178964005032338L;
	private int pageNum; // 当前页数
	private int numPerPage; // 每页记录数
	
	public PageParam(int pageNum, int numPerPage) {
		super();
		this.pageNum = pageNum;
		this.numPerPage = numPerPage;
	}

	/** 当前页数 */
	public int getPageNum() {
		return pageNum;
	}

	/** 当前页数 */
	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	/** 每页记录数 */
	public int getNumPerPage() {
		return numPerPage;
	}

	/** 每页记录数 */
	public void setNumPerPage(int numPerPage) {
		this.numPerPage = numPerPage;
	}
	
	public static void main(String[] args) {
		int a=10;
		int b=10;
		method(a,b);
		System.out.println("a="+a);
		System.out.println("b="+b);
	}

	private static void method(Object a ,Object b)
	{
		a=100;
		b=200;
	}
}
