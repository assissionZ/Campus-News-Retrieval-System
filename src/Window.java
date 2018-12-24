//package xxjs20170622;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class Window extends JFrame implements ActionListener{
	Search_Deal pd = null;
	
    public static Font font = new Font("宋体", Font.BOLD, 20);
    public static Font font_b = new Font("宋体", Font.BOLD, 30);
    
	private JTextField txt_query;

	private JButton jbtn_search;
	
	private Container cc;

	private JPanel cenPanel;
	private JPanel northPanel;
	private JPanel northPanel_w;
	private JPanel northPanel_e;
	private JPanel northPanel_m;
	private JEditorPane jep[] = new JEditorPane[10];
    private JTextArea ssArea[] = new JTextArea[10];
    private JPanel ssPanel[] = new JPanel[10];
	
	//窗体初始化
	public Window(){
		this.setTitle("深大公文通检索系统");
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        this.setSize(800, 1000);
        this.setLocationRelativeTo(null);
        cc = new Container();
		cc = this.getContentPane(); 

		cenPanel = new JPanel();
		cenPanel.setLayout(new GridLayout(10,1,5,5));
		
		northPanel = new JPanel(new BorderLayout());
		northPanel.setPreferredSize(new Dimension(800, 45));
		northPanel_e = new JPanel(new BorderLayout());
		northPanel_w = new JPanel(new BorderLayout());
		northPanel_m = new JPanel(new BorderLayout());

//		northPanel.setLayout(null);
//		FlowLayout flowLayout3 = new FlowLayout(FlowLayout.LEFT);
//		northPanel3 = new JPanel(flowLayout3);
		
		JLabel jl_name = new JLabel("    搜索输入框：");
		jl_name.setFont(font); 
		jl_name.setPreferredSize(new Dimension(180, 50));
		northPanel_w.add(jl_name, BorderLayout.EAST);
		
		txt_query = new JTextField();
		txt_query.setFont(font);
		txt_query.setEditable(true);
		txt_query.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e){
				if(e.getKeyChar()==KeyEvent.VK_ENTER){
					try{
						do_search();
					}
					catch (IOException e1){
						e1.printStackTrace();
					}
				}
			}
		});
		northPanel_m.add(txt_query);
		
		jbtn_search = new JButton("搜索");      
		jbtn_search.setFont(font);   
		jbtn_search.addActionListener(this);   
		northPanel_e.add(jbtn_search, BorderLayout.WEST);
		
		northPanel.add(northPanel_w, BorderLayout.WEST);
		northPanel.add(northPanel_m, BorderLayout.CENTER);
		northPanel.add(northPanel_e, BorderLayout.EAST);
		cc.add(northPanel, BorderLayout.NORTH);
		
		for(int i=0; i<10; i++){
			ssPanel[i] = new JPanel();
			ssPanel[i].setLayout(new GridLayout(2,1,5,1));

			ssArea[i] = new JTextArea();
			ssArea[i].setEditable(false);
//			contentArea[i].setForeground(Color.BLUE);
			ssArea[i].setFont(font);
			ssArea[i].setText("你看不到我");
			
			jep[i] = new JEditorPane();
			jep[i].setFont(font);
			jep[i].setEditable(false);
			jep[i].setContentType("text/html");
			jep[i].setText("<html><a href='http://www.szu.edu.cn' style='font-size:16px'>无效链接</a></html>");
			jep[i].addHyperlinkListener(new HyperlinkListener(){
				public void hyperlinkUpdate(HyperlinkEvent e){
					if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED){
						try{
							URL url = e.getURL();
							Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
						}catch(IOException e1){
							e1.printStackTrace();
						}
					}
				}
			});
			ssPanel[i].add(jep[i]);
			ssPanel[i].add(ssArea[i]);
			cenPanel.add(ssPanel[i]);
		}
		cc.add(cenPanel,BorderLayout.CENTER);
		
	}
	public void set_pd(Search_Deal pd){
		this.pd = pd;
		try {
			TreeMap<Double, String> hahaha = new TreeMap<Double, String>();
			//小技巧，在第一次运行程序等待用户输入时就先搜一下“经济”对整个系统做个初始化
			//可以大大加快之后用户进行的第一次搜索的时间
			hahaha = pd.input("经济");
			show_result(hahaha, 0);			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == jbtn_search){
			try {
				do_search();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
																							//（2）用户交互之返回结果
	public void do_search() throws IOException{
		String query = txt_query.getText();					//获取用户输入的query
		System.out.println(query);
		if(query == null)return;
		TreeMap<Double, String> score_top = pd.input(query);//调用input函数对query进行处理，并返回排序好的文章列表
		show_result(score_top, 1);							//把返回的结果输出
//		System.out.println("yeah!");
	}
																							//（2）用户交互之输出结果
	public void show_result(TreeMap<Double, String> score_top, int hahaha) throws IOException{
		int i=0;
		txt_query.setText("");
		File file_set = new File("D:\\data_complete");
		File[] file_set_list = file_set.listFiles();
		
		BufferedReader reader = null;
		for(double score : score_top.keySet()){
			if(i>=10)break;
			String file_name = score_top.get(score);
			for(File file : file_set_list){
				if(file.getName().equals(file_name)){
					reader = new BufferedReader(new FileReader(file));
					String line_1 = reader.readLine();					//line_1是url
//					System.out.println(line_1);
					String line_2 = reader.readLine();					//line_2是title
//					System.out.println(line_2);
					String line_3 = reader.readLine();					//line_3是发文单位
					String line_4 = reader.readLine();					//line_4是发文时间
					if(hahaha == 1){
						jep[i].setText("<html><a href='"+line_1+"' style='font-size:16px'>"+line_2+"</a></html>");
						ssArea[i].setText(line_3+line_4);
					}
					else
						;
				}
			}
			i++;
		}
	}
}