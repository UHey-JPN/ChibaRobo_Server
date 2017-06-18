package data.main;

public class CodeXML {
	final static String CRLF = "\r\n";
	final static String INDENT = "  "; // インデントはスペースで2つ
	private String code;

	public CodeXML(String version, String encoding) {
		code = "<?xml version=\"" + version +"\" encoding=\"" + encoding + "\" ?>" + CRLF;
	}

	public void add(String str){
		code += str;
	}
	
	public void add_line(int indent, String line){
		for(int i=0; i < indent; i++){
			this.add(INDENT);
		}
		this.add(line);
		this.add(CRLF);
	}
	
	public String get_xml(){ return code; }
}
