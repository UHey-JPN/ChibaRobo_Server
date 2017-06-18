package data.robot;

import data.main.CodeXML;

class Robot {
	private int id = 0;
	private String name;
	private String creater;
	private String grade;
	private String img;
	private String desc;
	private boolean valid = false;
	
	Robot(int id, String data){
		this.id = id;
		String[] csv = data.split(",", 0);
		if(csv.length != 5){
			return;
		}
		this.name    = new String(csv[0]);
		this.creater = new String(csv[1]);
		this.grade   = new String(csv[2]);
		this.img     = new String(csv[3]);
		this.desc    = new String(csv[4]);
		this.valid   = true;
	}
	
	public int get_id(){ return id; }
	public String get_name(){ return name; }
	public String get_creater(){ return creater; }
	public String get_grade(){ return grade; }
	public String get_img(){ return img; }
	public String get_desc(){ return desc; }
	public boolean get_valid(){ return valid; }
	
	// このクラスのXML表記を出力
	public CodeXML create_xml(CodeXML xml) throws NullPointerException{
		if( xml == null ){
			throw new NullPointerException("CodeXML(class) has no instance.\n");
		}
		xml.add_line(1, "<robot id=\"" + id +"\">");
		xml.add_line(2, "<name>"+ name +"</name>");
		xml.add_line(2, "<creater>"+ creater +"</creater>");
		xml.add_line(2, "<grade>"+ grade +"</grade>");
		xml.add_line(2, "<img>"+ img +"</img>");
		xml.add_line(2, "<desc>"+ desc +"</desc>");
		xml.add_line(1, "</robot>");
		return xml;
	}
	
	// 1行で自身のメソッド変数を表す文字列を返す。
	public String get_summary(){
		return id + "," + name + "," + creater +"," + grade;
	}
}
