package me.huding.luobo.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.huding.luobo.model.base.BaseBlog;

import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;

/**
 * Generated by JFinal.
 */
@SuppressWarnings("serial")
public class Blog extends BaseBlog<Blog> {
	public static final Blog dao = new Blog();
	
	
	
	
	
	

	/**
	 * 根据签名查找博客
	 * @param signature
	 * @return
	 */
	public static Blog findBySignature(String signature) {
		return dao.findFirst("select id from blog where signature = ? limit 1",signature);
	}
	
	
	public static List<Blog> hotRank(int size){
		String sql = "select id,title,url,readNum,commentNum,heartNum from blog order by readNum DESC,commentNum DESC,heartNum DESC LIMIT ?";
		return dao.find(sql,Math.max(5, size));
	}
	
	public static Page<Record> paginate4Back(int pageNum,int pageSize){
		String select = "select * ";
		// 默认根据时间排序
		String sqlExceptSelect = "from blog_back_display order by publishTime desc";
		return Db.paginate(pageNum, pageSize,select, sqlExceptSelect);
	}


	/**
	 * 
	 * @param pageNum
	 * @param pageSize
	 * @return
	 */
	public static Page<Record> paginate(int pageNum,int pageSize){
		// 默认根据时间排序
		String sqlExceptSelect = "from blog_display order by publishTime desc";
		String select = "select * ";
		Page<Record> page = Db.paginate(pageNum, pageSize,select, sqlExceptSelect);
		if(page.getList().isEmpty()){
			return page;
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("select * from blog_rel_tags where blogID in ( ");
		List<Record> records = page.getList();
		for(int i = 0,len = records.size();i < len;i ++){
			Record rec = records.get(i);
			String blogID = rec.getStr("id");
			buffer.append("\'").append(blogID).append("\'");
			if(i != len - 1){
				buffer.append(",");
			}
		}
		buffer.append(" )");
		/**
		 * 查询博文的标签
		 */
		List<Record> tags = Db.find(buffer.toString());
		if(tags.isEmpty()){
			for(Record record : records){
				record.set("tags", new ArrayList<String>());
			}
			return page;
		}
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		for(Record blogTag : tags){
			String blogID = blogTag.getStr("blogID");
			List<String> list = map.get(blogID);
			if(list == null){
				list = new ArrayList<String>();
				map.put(blogID, list);
			} 
			list.add(blogTag.getStr("tagName"));
		}
		for(Record record : records){
			String blogID = record.get("id");
			List<String> ts = map.get(blogID);
			if(ts == null){
				ts = new ArrayList<String>();
			}
			record.set("tags", ts);
		}
		return page;
	}
	
	public static List<Blog> findAll(String... columns){
		StringBuffer selectBuffer = new StringBuffer();
		selectBuffer.append("select ");
		for(int i = 0;i < columns.length;i ++){
			selectBuffer.append(columns[i]);
			if(i != columns.length - 1){
				selectBuffer.append(",");
			}
		}
		selectBuffer.append(" from blog");
		return dao.find(selectBuffer.toString());
	}


	/**
	 * @return
	 */
	public static List<Blog> lunbo() {
		String sql = "SELECT id,title,url,coverURL from blog WHERE type = 1 limit 6";
		return dao.find(sql);
	}
}
