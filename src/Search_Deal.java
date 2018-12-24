//package xxjs20170622;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import org.ansj.domain.Term;
import org.ansj.domain.TermNatures;
import org.ansj.splitWord.analysis.ToAnalysis;

public class Search_Deal {
	
	TreeMap<String, TreeMap<String, Integer>> indexs = null;
	TreeMap<String, Integer> d_len = null;
	TreeMap<String, Integer> query = null;
	int N_doc = 0;
	String[] sw_words = null;
	
	public Search_Deal() throws IOException{
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		d_len = new TreeMap<String, Integer>();
		query = new TreeMap<String, Integer>();
		//����stopword.dic���ڶ�query�Ĵ���
		File sw_flist = new File("stopword.dic");
		String sw_text = file_to_String(sw_flist);
		sw_words = sw_text.split(" ");
	}
	
	//�����û������query
	public TreeMap<Double, String> input(String line) throws IOException{		//��2���û�����֮����
//		line = line.replaceAll("[\\pP��������]", " ");
		line = line.toLowerCase();										//��3��תСд	
		List<Term> line_Term = ToAnalysis.parse(line);					//��1�����ķִ�
		line_Term = stopwords(line_Term, sw_words);						//��3��stopword
		del_space(line_Term);
		for(Term term : line_Term){
			int value = 1;
			if(query.containsKey(term.toString())){
				value = query.get(term.toString());
				value++;
			}
			query.put(term.toString(), value);
		}
		TreeMap<Double, String> score_top = cosinescore(line_Term);
		//��ǰʮ�������ok��~
		return score_top;
	}
	
	//stopword����
	public List<Term> stopwords(List<Term> line_Term, String[] sw_words) throws IOException{
//			System.out.println(line_Term.get(1).toString());
		for(String word : sw_words){
//				System.out.println(word);
			for(int i=0; i<line_Term.size(); ){
//					System.out.println(term);
				if(line_Term.get(i).toString().equals(word)){
					line_Term.remove(i);
//						System.out.println("yes");
				}
				else{
					i++;
				}
			}
		}
		return line_Term;
	}
	
	//����cosinescore
	public TreeMap<Double, String> cosinescore(List<Term> line_Term){
		TreeMap<String, Double> score = new TreeMap<String, Double>();
		for(Term q_word : line_Term){
			if(indexs.containsKey(q_word.toString())){
				TreeMap<String, Integer> tmap = indexs.get(q_word.toString());
				int tf_tq = query.get(q_word.toString()).intValue();
				int df_t = tmap.size();
				for(String doc : tmap.keySet()){
					int tf_td = tmap.get(doc);
					double idf_t = Math.log(d_len.get(doc) / (double)df_t);
					double w_tq = tf_tq * idf_t;										//��5������Ȩ��tf-idf
					double cosscore = 0;
					if(score.containsKey(doc)){
						cosscore = score.get(doc);
					}
					cosscore += tf_td * w_tq;											//��6�������������ƶ�
					score.put(doc, cosscore);
				}
			}
			else{
				
			}
		}
		TreeMap<Double, String> score_top = new TreeMap<Double, String>();
		for(String doc : score.keySet()){
			double cosscore = score.get(doc);
			cosscore /= (double)d_len.get(doc);
			cosscore *= (double)(-1);	//TreeMapĬ�ϴ�С���������ڴ˳�-1�����ʵ�ִӴ�С����
			score_top.put(cosscore, doc);											//��7�����ĵ�����
		}
		show_map_top(score_top);							//����̨���top10��������
		return score_top;
	}
	
	//�����Ժ���������̨���top10��������
	public void show_map_top(TreeMap<Double, String> map){
		int i=1;
		for(Double score : map.keySet()){
			if(i>10)break;
			System.out.println("��"+i+"ƪ��Name��"+map.get(score)+"  score��"+score);
			i++;
		}
	}
	
	//����ִʺ�List<Term>�в����Ŀո�
	public void del_space(List<Term> line_Term){
		for(int i=0; i<line_Term.size(); ){
			if(line_Term.get(i).toString().equals(" ")){
				line_Term.remove(i);
			}
			else{
				i++;
			}
		}
	}
	
	//����indexs
	@SuppressWarnings("resource")
	public void read_indexs() throws IOException{
		indexs = new TreeMap<String, TreeMap<String, Integer>>();
		File file = new File("D:\\�ҵ��ĵ�\\רҵ���ĵ�\\��Ϣ����\\��Ϣ��������ҵ\\indexs.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null){
			String[] line_term = line.split(" ");
			TreeMap<String, Integer> tmap = new TreeMap<String, Integer>();
			for(int i=2; i<line_term.length; i=i+2){
				tmap.put(line_term[i], Integer.parseInt(line_term[i+1]));
			}
			indexs.put(line_term[0], tmap);
		}
	}

	//����d_len
	public void read_d_len() throws IOException{
		d_len = new TreeMap<String, Integer>();
		File file = new File("D:\\�ҵ��ĵ�\\רҵ���ĵ�\\��Ϣ����\\��Ϣ��������ҵ\\d_len.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line = null;
		while((line = reader.readLine()) != null){
			String[] line_term = line.split(" ");
			d_len.put(line_term[0], Integer.parseInt(line_term[1]));
		}
	}
	
	//�����Ժ���������̨���indexs��Ϣ
	public void show_indexs(){
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
	
	//�����Ժ��������ļ���������һ��String����
	public static String file_to_String(File file) throws IOException{
		String file_text = new String();//
		String line = null;
		BufferedReader reader = new BufferedReader(new FileReader(file));//
		while((line = reader.readLine()) != null){
			file_text += line;
			file_text += " ";
		}
//			System.out.println(file_text);
		return file_text;
	}
}
