//package xxjs20170622;

import java.io.File;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException{
		Search_Deal pd = new Search_Deal();		//搜索初始化
		
		Window win = new Window();				//界面初始化
		win.setVisible(true);
		
		pd.read_indexs();			//把index.txt读进对象pd
		pd.read_d_len();			//把d_len.txt读进对象pd
		
		win.set_pd(pd);				//把pd传入对象win
	}
}
