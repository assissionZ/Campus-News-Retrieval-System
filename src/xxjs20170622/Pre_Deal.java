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
	
	//��һ��String��Ӧterm   �ڶ���String��Ӧfile_name   Integer��Ӧtf  
	static TreeMap<String, TreeMap<String, Integer>> indexs = null;	//���indexs
	static TreeMap<String, Integer> d_len = null;					//���ÿһƪ���µ�term����
	
	public Pre_Deal(){
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		d_len = new TreeMap<String, Integer>();
	}

	public static void main(String[] args) throws IOException {
		Date start_time = new Date(System.currentTimeMillis());
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		d_len = new TreeMap<String, Integer>();
		File data_set = new File("data_original");				//��ԭʼ���ݼ�������262��txt�ļ���
		File[] data_set_list = data_set.listFiles();
		BufferedReader reader = null;
		FileOutputStream out = null;//
		String line1 = null;
		String line2 = null;
		int i = 0;
		for(File file : data_set_list){
			i++;
			reader = new BufferedReader(new FileReader(file));	//ÿ��txtһ��������
			line1 = reader.readLine();							//������һ����url
			line2 = reader.readLine();							//�ڶ��������µ�ȫ���������ɵ��ַ���
			
			//�������������µı��⡢���ĵ�λ������ʱ�䣬�˺������ڵ�ԭ���������������������е���
//			String title = get_title(line1, line2, file);		
			
			line2 = line2.toLowerCase();							//��3.1��תСд
			List<Term> line_Term = ToAnalysis.parse(line2);			//��1�����ķִ�
			line_Term = stopwords(line_Term);						//��3.2��stopword(Ӣ�����ķ�һ����)
			del_space(line_Term);
			
			//data_after_analyse����Ҫ��������¾��������ʣ�µ�����term�����Բ���
//			out = new FileOutputStream(new File("D:\\data_after_analyse\\"+file.getName()));
//			out.write(line_Term.toString().getBytes());
			
			Inverted_Index(file, line_Term);						//��4����������
		}
//		show_indexs();
		write_indexs();								//��index��¼��txt��
		write_d_len();								//��d_len��¼��txt��
		Date end_time = new Date(System.currentTimeMillis());
		System.out.print("Finish! Cost ");
		System.out.print(end_time.getTime() - start_time.getTime());
		System.out.println(" ms.");
	}
	
	//����������˳���¼d_len
	public static void Inverted_Index(File file, List<Term> line_Term){
		int line_len = 0;
		if(d_len.containsKey(file.getName())){		//��¼d_len����
			line_len = d_len.get(file.getName());
		}
		else{
			line_len = 0;
		}								
													//��¼indexs����
		for(Term term : line_Term){					//�����ĵ��е����д�
			line_len++;
			TreeMap<String, Integer> tmap = null;
			if(indexs.containsKey(term.toString())){//���indexs�Ѱ����ô�
				tmap = indexs.get(term.toString());	//��ȡ�����Ӧ���ĵ��б�
				int value = 1;
				if(tmap.containsKey(file.getName())){//����ĵ��б�������˸��ĵ�
					value = tmap.get(file.getName());//ȡ���ô��ڸ��ĵ����ֵĴ���
					value++;						//��һ��
				}
				else{
					value = 1;						//�����ʼ��Ϊ����1��
				}
				tmap.put(file.getName(), value);	//put���ĵ��б�
			}
			else{
				tmap = new TreeMap<String, Integer>();//���indexs�������ô�
				tmap.put(file.getName(), 1);		//�Ѹ��ĵ����ֺͳ�����һ����������Ϣput���ôʵ��ĵ��б�
			}
			indexs.put(term.toString(), tmap);	//�ѸôʺͶ�Ӧ���ĵ��б�put��indexs
		}
		d_len.put(file.getName(), line_len);
//			indexs.remove(" ");
	}
	
	//��d_len��¼��txt��
	public static void write_d_len() throws IOException{
		FileOutputStream out = new FileOutputStream(new File("d_len.txt"));
		for(String word : d_len.keySet()){
			Integer i = d_len.get(word);
			out.write((word+" "+i.toString()).getBytes());
			out.write(new String("\r\n").getBytes());
		}
	}
	
	//��index��¼��txt��
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
	
	//stopword��������Ӣ��һ�����ļ�stopword.dic�����stopwords
	public static List<Term> stopwords(List<Term> line_Term) throws IOException{
		File sw_flist = new File("stopword.dic");
		String sw_text = file_to_String(sw_flist);
		String[] sw_words = sw_text.split(" ");
		//�㷨������forѭ����������ʾ�ɾ����
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
	
	//�������������µı��⡢���ĵ�λ������ʱ�䣬�˺������ڵ�ԭ���������������������е���
	//�ڳ������к󲢲���Ҫ�˲������У������ĵ�Ҳ����Ҫ���д˲��ִ���
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
			if(line2.charAt(i)=='��'){
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
	
	//�ѷִʺ��List<Term>�Ŀո�ɾ��
	public static void del_space(List<Term> line_Term){
		for(int i=0; i<line_Term.size(); ){
			if(line_Term.get(i).toString().equals(" ") || line_Term.get(i).toString().equals("��")){
				line_Term.remove(i);
			}
			else{
				i++;
			}
		}
	}

	//�����Ժ�����ɾ��String��ĳ���ַ�
	public static String removeCharAt(String s, int pos) {
	    return s.substring(0, pos) + s.substring(pos + 1);
	 }
	
	//�����Ժ��������ļ���������һ��String����
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
	
	//�����Ժ��������index
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
