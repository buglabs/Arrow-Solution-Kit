
package gui;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import com.buglabs.xbee.protocol.MaxbotixRangefinder;
import com.buglabs.xbee.protocol.PIRMotion;
import com.buglabs.xbee.protocol.SparkfunWeatherboard;

import remotemondemoapi.RemoteMonDemoAPIApplication;


public class SensordataView extends JFrame {
	
	private RemoteMonDemoAPIApplication app ;
	private ImageIcon red;
	private ImageIcon green;
	public JLabel currTemp ;
	public JLabel currHumid;
	public JLabel currDew;
	public JLabel currPressure;
	public JLabel currLight ;
	public JLabel currDistance ;
	public JList motionList ;
	public DefaultListModel listModel;
	private DateFormat df ;
	
	public SensordataView (String title,
			RemoteMonDemoAPIApplication app) throws HeadlessException {
		super(title);
		this.app = app;
		df = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		
		//red = createImageIcon("images/gps_red.png", null);
		//green = createImageIcon("images/gps_green.png",null);
		setup();
		
	}
	
	private void setup() {
		
		//JTabbedPane tabbedPane = new JTabbedPane();
		
		//this.setLayout(new GridLayout(2,1));
		this.setLayout(new BorderLayout());
		JPanel container = new JPanel();
		container.setLayout(new BoxLayout(container, BoxLayout.PAGE_AXIS));
		JPanel iconPanel = new JPanel();
		JPanel mainPanel = new JPanel();
		JPanel weatherPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel rangePanel = new JPanel();
		JPanel motionPanel = new JPanel();

		
		mainPanel.setLayout(new GridLayout(2,1));
		mainPanel.add(weatherPanel);
		mainPanel.add(bottomPanel);
		
		JLabel image = new JLabel(new ImageIcon(app.getBundleContext().getBundle().getResource("/images/HAL.gif")));
		image.setAlignmentX(Component.CENTER_ALIGNMENT);
		container.add(image);
		//this.add(iconPanel);
		container.add(mainPanel);
		JLabel title = new JLabel("Remote Sensor Monitor",JLabel.CENTER);
		title.setFont(new Font("Liberation Sans", Font.BOLD, 13));
		this.add(title, BorderLayout.NORTH);
		this.add(container, BorderLayout.CENTER);
//		this.add(weatherPanel);
//		this.add(bottomPanel);
		
		
		
		weatherPanel.setLayout(new GridLayout(0,2));
		
//		JLabel currTempLabel = new JLabel("Temperature:   ", JLabel.LEFT) ;
//		currTempLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		currTemp = new JLabel("Temperature: 00.0 F", JLabel.LEFT);
		currTemp.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
//		JLabel currHumidLabel = new JLabel("Humidity:   ", JLabel.LEFT);
//		currHumidLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		currHumid = new JLabel("Humidity: 00 %", JLabel.LEFT);
		currHumid.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
//		JLabel currDewLabel = new JLabel("Dew Point:   ", JLabel.LEFT);
//		currDewLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		currDew = new JLabel("Dew Point: 0 F", JLabel.LEFT);
		currDew.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
//		JLabel currPressureLabel = new JLabel("Bar. Pressure:   ", JLabel.LEFT);
//		currPressureLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		currPressure = new JLabel("Bar. Pressure: 0 in", JLabel.LEFT);
		currPressure.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
//		JLabel currLightLabel = new JLabel ("Light:   ", JLabel.LEFT);
//		currLightLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 14));
		currLight = new JLabel("Light: 0 %", JLabel.LEFT);
		currLight.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
		//weatherPanel.add(currTempLabel);
		weatherPanel.add(currTemp);
		//weatherPanel.add(currHumidLabel);
		weatherPanel.add(currHumid);
		//weatherPanel.add(currDewLabel);
		weatherPanel.add(currDew);
		//weatherPanel.add(currPressureLabel);
		weatherPanel.add(currPressure);
		//weatherPanel.add(currLightLabel);
		weatherPanel.add(currLight);
		
		
		
		bottomPanel.setLayout(new GridLayout(0,2));
		
		currDistance = new JLabel("Distance: 0 cm", JLabel.LEFT);
		currDistance.setFont(new Font("Liberation Sans", Font.PLAIN, 12));
		
		weatherPanel.add(currDistance);
		weatherPanel.setBorder(BorderFactory.createEmptyBorder(0,10,0,0));
		//rangePanel.add(currDistance);
		
		
		motionList = new JList();
		listModel = new DefaultListModel();

		motionList
		.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		motionList.setLayoutOrientation(JList.VERTICAL);
		motionList.setModel(listModel);
		motionList.setVisibleRowCount(4);
		JScrollPane listScroller = new JScrollPane(motionList);
		listScroller.setBorder(BorderFactory.createEmptyBorder(0,0,5,10));
		
		
		JLabel motionLabel = new JLabel("Last Motion detected at:");
		//motionPanel.setLayout(new GridLayout(2,1));
		rangePanel.add(motionLabel);
		//motionPanel.add(motionLabel);
		motionPanel.add(listScroller);
		
		bottomPanel.add(rangePanel);
		bottomPanel.add(motionPanel);
		
		this.setVisible(true);
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected ImageIcon createImageIcon(String path,
	                                           String description) {
	    java.net.URL imgURL = getClass().getResource(path);
	    if (imgURL != null) {
	        return new ImageIcon(imgURL, description);
	    } else {
	        System.err.println("Couldn't find file: " + path);
	        return null;
	    }
	}
	
	public void update(Map<String, Object> data) {
		if ((Class<?>)data.get("class") == SparkfunWeatherboard.class){
			//("Weather data from "+Integer.toHexString(((int[])data.get("address"))[1]));
			currTemp.setText("Temperature: "+data.get("Temperature") + " F");
			currHumid.setText("Humidity: "+data.get("Humidity") + " %");
			currDew.setText("Dew Point: "+data.get("Dewpoint") + " F");
			currPressure.setText("Bar. Pressure: "+data.get("Pressure") + " in");
			currLight.setText("Light: "+data.get("Light") + " %");
		}
		else if ((Class<?>)data.get("class") == MaxbotixRangefinder.class){
			currDistance.setText("Distance: "+data.get("Range")+" cm");
		}
		else if ((Class<?>)data.get("class") == PIRMotion.class){
			String datetime = df.format(new Date());
			listModel.insertElementAt(datetime, 0);
			motionList.setSelectedIndex(0);
		}
		else {
			//ilog("Unknown data: "+Arrays.toString((int[])data.get("raw")));
		}
	}

}
