package data.robot;

import java.util.ArrayList;
import java.util.List;

import data.exception.DataNotFoundException;
import data.main.CodeXML;

public class RoboList{
	private final List<Robot> list;
	private final Object lock_obj = new Object();
	
	public RoboList() {
		// list = Collections.synchronizedList(new ArrayList<Robot>());
		list = new ArrayList<Robot>();
		this.add(0, "default_name_robot0,default_creater_robot0,B1,default_img0.png,default_desc_robot0");
		this.add(1, "default_name_robot1,default_creater_robot1,B1,default_img1.png,default_desc_robot1");
		this.add(10, "default_name_robot10,default_creater_robot10,B1,default_img10.png,default_desc_robot10");
		this.add(11, "default_name_robot11,default_creater_robot11,B1,default_img11.png,default_desc_robot11");
	}
	
	public boolean add(int id, String data){
		synchronized(lock_obj){
			// 重複を削除
			for(int i = 0; i < list.size(); i++){
				if( list.get(i).get_id() == id ){
					list.remove(list.get(i));
					i--;
				}
			}
			
			// 新規データを追加
			list.add(new Robot(id, data));
			return list.get(list.size()-1).get_valid();
		}
	}
	
	public void clear(){
		synchronized(lock_obj){
			list.clear();
		}
	}
	
	// スレッドセーフなメソッド
	// XML形式でロボットの情報を取得
	public String get_xml(){
		return get_xml("1.0", "utf-8");
	}
	public String get_xml(String encoding){
		return get_xml("1.0", encoding);
	}
	public String get_xml(String version, String encoding){
		CodeXML xml = new CodeXML(version, encoding);
		xml.add_line(0, "<roboList>");
		ArrayList<Robot> copy;
		synchronized(lock_obj){
			copy = new ArrayList<Robot>(list);
		}
		for(Robot r : copy){
			if(r.get_valid())
				r.create_xml(xml);
		}
		xml.add_line(0, "</roboList>");
		return xml.get_xml();
	}

	public String get_robot_summary(int id) throws DataNotFoundException {
		synchronized(lock_obj){
			for(Robot r : list){
				if(r.get_id() == id){
					return r.get_summary();
				}
			}
			throw new DataNotFoundException("Robot(id=" + id + ") is not found.");
		}
	}

}
