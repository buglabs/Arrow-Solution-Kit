/**
 * 
 */
package arrowtelematicssolution.view;

import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.HeadlessException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import arrowtelematicssolution.ArrowTelematicsSolutionApplication;

import com.buglabs.bug.accelerometer.pub.AccelerometerSample;
import com.buglabs.bug.module.gps.pub.LatLon;

/**
 * @author jconnolly
 * 
 */
public class TelematicsFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6804508676226667277L;
	private ArrowTelematicsSolutionApplication app;
	private JLabel x;
	private JLabel y;
	private JLabel z;
	private JLabel latlon;

	/**
	 * @param title
	 * @throws HeadlessException
	 */
	public TelematicsFrame(String title, ArrowTelematicsSolutionApplication app)
			throws HeadlessException {
		super(title);
		this.app = app;
		setup();
	}

	private void setup() {
		this.setLayout(new FlowLayout());
		JPanel topPanel = new JPanel();
		JPanel bottomPanel = new JPanel();
		JPanel accelerationLabelPanel = new JPanel();
		JPanel locationLabelPanel = new JPanel();
		this.getContentPane().add(accelerationLabelPanel);
		this.getContentPane().add(topPanel);
		this.getContentPane().add(locationLabelPanel);
		this.getContentPane().add(bottomPanel);
		topPanel.setLayout(new FlowLayout());
		bottomPanel.setLayout(new FlowLayout());
		x = new JLabel();
		y = new JLabel();
		z = new JLabel();
		JLabel accelerationLabel = new JLabel("     Acceleration     \n");
		accelerationLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 24));
		x.setFont(new Font("Liberation Sans", Font.PLAIN, 16));
		y.setFont(new Font("Liberation Sans", Font.PLAIN, 16));
		z.setFont(new Font("Liberation Sans", Font.PLAIN, 16));

		accelerationLabelPanel.add(accelerationLabel);
		topPanel.add(x);
		topPanel.add(y);
		topPanel.add(z);
		try {
			x.setText("X: " + app.getIml8953accelerometer().readX());
			y.setText("Y: " + app.getIml8953accelerometer().readY());
			z.setText("Z: " + app.getIml8953accelerometer().readZ());
		} catch (IOException e) {
			e.printStackTrace();
			x.setText("An error has occured.");
			y.setText("");
			z.setText("");
		}
		JLabel locationLabel = new JLabel(
				"                  Location                  \n");
		locationLabel.setFont(new Font("Liberation Sans", Font.PLAIN, 24));
		locationLabelPanel.add(locationLabel);
		latlon = new JLabel();
		latlon.setFont(new Font("Liberation Sans", Font.PLAIN, 16));
		bottomPanel.add(latlon);
		try {
			if (app.getIpositionprovider().getLatitudeLongitude() != null) {
				LatLon reading = app.getIpositionprovider()
						.getLatitudeLongitude();
				String latRaw = Double.toString(reading.latitude);
				String latTruncated;
				String lonTruncated;
				if (latRaw.indexOf('.') + 6 < latRaw.length()) {
					latTruncated = latRaw.substring(0, latRaw.indexOf('.') + 6);
				} else {
					latTruncated = latRaw;
				}
				String lonRaw = Double.toString(reading.longitude);
				if (lonRaw.indexOf('.') + 6 < lonRaw.length()) {
					lonTruncated = lonRaw.substring(0, lonRaw.indexOf('.') + 6);
				} else {
					lonTruncated = lonRaw;
				}
				latlon.setText("Latitude: " + latTruncated + " "
						+ "Longitude: " + lonTruncated);
			} else {
				latlon.setText("No Fix");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		this.setVisible(true);
	}

	public void schedule(final int delayMillis, final int intervalMillis) {
		new Thread() {
			public void run() {
				AccelerometerSample sample;
				try {
					Thread.sleep(delayMillis);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				while (true) {
					try {
						if (app.getIpositionprovider().getLatitudeLongitude() != null) {
							LatLon reading = app.getIpositionprovider()
									.getLatitudeLongitude();
							String latRaw = Double.toString(reading.latitude);
							String latTruncated;
							String lonTruncated;
							if (latRaw.indexOf('.') + 6 < latRaw.length()) {
								latTruncated = latRaw.substring(0,
										latRaw.indexOf('.') + 6);
							} else {
								latTruncated = latRaw;
							}
							String lonRaw = Double.toString(reading.longitude);
							if (lonRaw.indexOf('.') + 6 < lonRaw.length()) {
								lonTruncated = lonRaw.substring(0,
										lonRaw.indexOf('.') + 6);
							} else {
								lonTruncated = lonRaw;
							}
							latlon.setText("Latitude: " + latTruncated + " "
									+ "Longitude: " + lonTruncated);
						} else {
							latlon.setText("No Fix");
						}

						try {
							sample = app.getIml8953accelerometer().readSample();
							x.setText("X: " + sample.getAccelerationX());
							y.setText("Y: " + sample.getAccelerationY());
							z.setText("Z " + sample.getAccelerationZ());
						} catch (IOException e) {
							e.printStackTrace();
							x.setText("An error has occured.");
							y.setText("");
							z.setText("");
						}
						try {
							Thread.sleep(intervalMillis);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					} catch (Exception e) {
						continue;
					}
				}
			}
		}.start();
	}
}
