//package xxjs20170622;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException{
		Search_Deal pd = new Search_Deal();		//������ʼ��
		
		Window win = new Window();				//�����ʼ��
		win.setVisible(true);
		
		pd.read_indexs();			//��index.txt��������pd
		pd.read_d_len();			//��d_len.txt��������pd
		
		win.set_pd(pd);				//��pd�������win
	}
}
