package xxjs20170622;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;

import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;

public class Pre_Deal {
	
	//第一个String对应term   第二个String对应file_name   Integer对应tf  
	static TreeMap<String, TreeMap<String, Integer>> indexs = null;	//存放indexs
	static TreeMap<String, Integer> d_len = null;					//存放每一篇文章的term数量
	
	public Pre_Deal(){
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		d_len = new TreeMap<String, Integer>();
	}

	public static void main(String[] args) throws IOException {
		Date start_time = new Date(System.currentTimeMillis());
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		d_len = new TreeMap<String, Integer>();
		File data_set = new File("data_original");				//打开原始数据集（包含262个txt文件）
		File[] data_set_list = data_set.listFiles();
		BufferedReader reader = null;
		FileOutputStream out = null;//
		String line1 = null;
		String line2 = null;
		int i = 0;
		for(File file : data_set_list){
			i++;
			reader = new BufferedReader(new FileReader(file));	//每个txt一共就两行
			line1 = reader.readLine();							//读出第一行是url
			line2 = reader.readLine();							//第二行是文章的全部内容连成的字符串
			
			//用于提炼出文章的标题、发文单位、发文时间，此函数存在的原因是爬虫爬下来的数据有点乱
//			String title = get_title(line1, line2, file);		
			
			line2 = line2.toLowerCase();							//（3.1）转小写
			List<Term> line_Term = ToAnalysis.parse(line2);			//（1）中文分词
			line_Term = stopwords(line_Term);						//（3.2）stopword(英文中文放一起了)
			del_space(line_Term);
			
			//data_after_analyse，将要存放着文章经过处理后剩下的所有term，可以不存
//			out = new FileOutputStream(new File("D:\\data_after_analyse\\"+file.getName()));
//			out.write(line_Term.toString().getBytes());
			
			Inverted_Index(file, line_Term);						//（4）倒排索引
		}
//		show_indexs();
		write_indexs();								//把index记录在txt中
		write_d_len();								//把d_len记录在txt中
		Date end_time = new Date(System.currentTimeMillis());
		System.out.print("Finish! Cost ");
		System.out.print(end_time.getTime() - start_time.getTime());
		System.out.println(" ms.");
	}
	
	//倒排索引，顺便记录d_len
	public static void Inverted_Index(File file, List<Term> line_Term){
		int line_len = 0;
		if(d_len.containsKey(file.getName())){		//记录d_len部分
			line_len = d_len.get(file.getName());
		}
		else{
			line_len = 0;
		}								
													//记录indexs部分
		for(Term term : line_Term){					//遍历文档中的所有词
			line_len++;
			TreeMap<String, Integer> tmap = null;
			if(indexs.containsKey(term.toString())){//如果indexs已包含该词
				tmap = indexs.get(term.toString());	//就取出其对应的文档列表
				int value = 1;
				if(tmap.containsKey(file.getName())){//如果文档列表里包含了该文档
					value = tmap.get(file.getName());//取出该词在该文档出现的次数
					value++;						//加一次
				}
				else{
					value = 1;						//否则初始化为出现1次
				}
				tmap.put(file.getName(), value);	//put进文档列表
			}
			else{
				tmap = new TreeMap<String, Integer>();//如果indexs不包含该词
				tmap.put(file.getName(), 1);		//把该文档名字和出现了一次这两个信息put进该词的文档列表
			}
			indexs.put(term.toString(), tmap);	//把该词和对应的文档列表put进indexs
		}
		d_len.put(file.getName(), line_len);
//			indexs.remove(" ");
	}
	
	//把d_len记录在txt中
	public static void write_d_len() throws IOException{
		FileOutputStream out = new FileOutputStream(new File("d_len.txt"));
		for(String word : d_len.keySet()){
			Integer i = d_len.get(word);
			out.write((word+" "+i.toString()).getBytes());
			out.write(new String("\r\n").getBytes());
		}
	}
	
	//把index记录在txt中
	@SuppressWarnings("resource")
	public static void write_indexs() throws IOException{
		FileOutputStream out = null;//
		out = new FileOutputStream(new File("indexs.txt"));
		for(String word : indexs.keySet()){
			TreeMap<String, Integer> tmap = indexs.get(word);
			out.write(new String(word+" "+tmap.size()).getBytes());
			for(String file_name : tmap.keySet()){
				out.write(new String(" "+file_name+" "+tmap.get(file_name)).getBytes());
			}
			out.write(new String("\r\n").getBytes());
		}
	}
	
	//stopword处理，中文英文一起处理，文件stopword.dic存放着stopwords
	public static List<Term> stopwords(List<Term> line_Term) throws IOException{
		File sw_flist = new File("stopword.dic");
		String sw_text = file_to_String(sw_flist);
		String[] sw_words = sw_text.split(" ");
		//算法：两个for循环，有这个词就删掉它
		for(String word : sw_words){
			for(int i=0; i<line_Term.size(); ){
				if(line_Term.get(i).toString().equals(word)){
					line_Term.remove(i);
				}
				else{
					i++;
				}
			}
		}
		return line_Term;
	}
	
	//用于提炼出文章的标题、发文单位、发文时间，此函数存在的原因是爬虫爬下来的数据有点乱
	//在程序运行后并不需要此部分运行，更新文档也不需要运行此部分代码
	@SuppressWarnings("resource")
	public static String get_title(String line1, String line2, File file) throws IOException{
		int i = 0;
		int start = 0;
		int end = 0;
		int yes = 0;
		String title = null;
		String danwei = null;
		String time1 = null;
		String time2 = null;
		FileOutputStream out = null;//
		while(i>=0){
			if(line2.charAt(i)=='　'){
				if(yes == 0){
					start = i+1;
					yes += 1;
				}
				else if(yes == 2){
					start = end + 1;
					end = i+1;
					yes += 1;
					danwei = line2.substring(start, end);
					start = end;
				}
			}
			else if(line2.charAt(i) == ' '){
				if(yes == 1){
					end = i;
					yes += 1;
					title = line2.substring(start, end);
				}
				else if(yes == 3){
					end = i;
					yes += 1;
					time1 = line2.substring(start, end);
					start = end + 1;
				}
				else if(yes == 4){
					end = i;
					time2 = line2.substring(start, end);
					break;
				}
				else if(yes == 2){
					end = i;
					title = line2.substring(start, end);
				}
			}
			i++;
		}
		out = new FileOutputStream(new File("data_complete\\"+file.getName()));
//		System.out.println(danwei);
//		System.out.println(time1+" "+time2);
		out.write((line1+"\r\n").getBytes());
		out.write((title+"\r\n").getBytes());
		out.write((danwei+"\r\n").getBytes());
		out.write((time1+" "+time2+"\r\n").getBytes());
		out.write((line2).getBytes());
		return title;
	}
	
	//把分词后的List<Term>的空格删掉
	public static void del_space(List<Term> line_Term){
		for(int i=0; i<line_Term.size(); ){
			if(line_Term.get(i).toString().equals(" ") || line_Term.get(i).toString().equals("　")){
				line_Term.remove(i);
			}
			else{
				i++;
			}
		}
	}

	//工具性函数：删除String的某个字符
	public static String removeCharAt(String s, int pos) {
	    return s.substring(0, pos) + s.substring(pos + 1);
	 }
	
	//工具性函数：把文件内容连成一个String返回
	public static String file_to_String(File file) throws IOException{
		String file_text = new String();//
		String line = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));//
		while((line = reader.readLine()) != null){
			file_text += line;
			file_text += " ";
		}
//		System.out.println(file_text);
		return file_text;
	}
	
	//工具性函数：输出index
	public static void show_indexs(){
		System.out.println("term\tdoc.frep\tpostings lists");
		for(String word : indexs.keySet()){
			TreeMap<String, Integer> tmap = indexs.get(word);
			System.out.print(word+"\t"+tmap.size()+"\t");
			for(String file_name : tmap.keySet()){
				System.out.print(file_name+"\t"+tmap.get(file_name)+"\t");
			}
			System.out.println();
		}
    
	}

}
