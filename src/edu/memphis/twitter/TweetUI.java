package edu.memphis.twitter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.JFileChooser;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JScrollBar;

public class TweetUI extends JFrame {

	private JPanel contentPane;
	private final JTextField fldFrameWidth;
	private final JTextField fldOutputPath;
	private JTextField fldInputPath;
	private JTextField fldEnglishTweetsPath;
	
	private Map<String, String> config;
	private Path configPath;
	
	/*
	 * Interceptor
	 *  Interceptor takes an output stream makes a copy of it, the only difference being an overriden
	 *  print function. It will be used to "redirect" the default System.out printstream to update the JTextArea
	 *  instead.
	 */
	private class Interceptor extends PrintStream
	{
		private JTextArea a;
		
	    public Interceptor(OutputStream out, JTextArea a)
	    {
	        super(out, true);
	        this.a = a;
	    }
	    @Override
	    public void print(String s)
	    {
	    	a.append("\n");
	    	a.append(s);
	    }
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					TweetUI frame = new TweetUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the frame.
	 */
	public TweetUI() {
		//Locate and load config file
		configPath = Paths.get("");		// Get current working directory
		configPath = configPath.resolve(".config");		// Append '.config' to directory, result should be "/path/to/directory/.config"
		config = loadConfig(configPath.toAbsolutePath().toString());
		
		//
		setResizable(false);
		setTitle("Tweet Cohesion");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 528, 490);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		fldFrameWidth = new JTextField();
		fldFrameWidth.setHorizontalAlignment(SwingConstants.RIGHT);
		fldFrameWidth.setText("5");
		fldFrameWidth.setBounds(124, 61, 63, 28);
		contentPane.add(fldFrameWidth);
		fldFrameWidth.setColumns(10);
		
		fldOutputPath = new JTextField();
		fldOutputPath.setBounds(124, 89, 262, 28);
		contentPane.add(fldOutputPath);
		fldOutputPath.setColumns(10);
		
		JLabel lblFrameWidth = new JLabel("Frame width:");
		lblFrameWidth.setBounds(31, 67, 81, 16);
		contentPane.add(lblFrameWidth);
		
		JLabel lblOutput = new JLabel("Output path:");
		lblOutput.setBounds(31, 95, 81, 16);
		contentPane.add(lblOutput);
		
		final JCheckBox chckbxSlidingFrameMode = new JCheckBox("Sliding frame mode");
		chckbxSlidingFrameMode.setBounds(199, 63, 165, 23);
		contentPane.add(chckbxSlidingFrameMode);
		
		/*
		 * "Go" button and its action handler
		 */
		JButton btnGoButton = new JButton("Go");
		btnGoButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				// Save current values to configuration
				config.put("inputPath", fldInputPath.getText());
				config.put("outputPath", fldOutputPath.getText());
				config.put("englishTweetsPath", fldEnglishTweetsPath.getText());
				// Write new configuration
				writeConfig(configPath.toAbsolutePath().toString());
				// Launch Tweets Comparison
				TweetsComparison t = new TweetsComparison(config.get("inputPath"),
															config.get("outputPath"),
															config.get("englishTweetsPath"),
															Integer.parseInt(fldFrameWidth.getText()),
															chckbxSlidingFrameMode.isSelected() );
				t.main();
			}
		});
		btnGoButton.setBounds(124, 171, 117, 29);
		contentPane.add(btnGoButton);
		
		JButton btnOutputBrowse = new JButton("Browse...");
		btnOutputBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				  JFileChooser c = new JFileChooser();

				  int rVal = c.showSaveDialog(contentPane);
			      if (rVal == JFileChooser.APPROVE_OPTION) {
			        fldOutputPath.setText(c.getSelectedFile().getPath());
			      }
			}
		});
		btnOutputBrowse.setBounds(398, 90, 117, 29);
		contentPane.add(btnOutputBrowse);
		
		fldInputPath = new JTextField();
		fldInputPath.setBounds(124, 29, 262, 28);
		contentPane.add(fldInputPath);
		fldInputPath.setColumns(10);
		
		JLabel lblInputPath = new JLabel("Input path:");
		lblInputPath.setBounds(31, 35, 81, 16);
		contentPane.add(lblInputPath);
		
		JButton btnInputBrowse = new JButton("Browse...");
		btnInputBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
			      // Demonstrate "Open" dialog:
			      int rVal = c.showOpenDialog(contentPane);
			      if (rVal == JFileChooser.APPROVE_OPTION) {
			        fldInputPath.setText(c.getSelectedFile().getPath());
			      }
			}
		});
		btnInputBrowse.setBounds(398, 30, 117, 29);
		contentPane.add(btnInputBrowse);
		
		JTextArea txtrConsole = new JTextArea();
		JScrollPane sp = new JScrollPane(txtrConsole);
		txtrConsole.setText("Tweet Cohesion Analysis v0.2\n");
		sp.setBounds(6, 212, 516, 250);
		contentPane.add(sp);
		
		PrintStream originalOut = System.out;
		PrintStream interceptor = new Interceptor(originalOut, txtrConsole);
		
		fldEnglishTweetsPath = new JTextField();
		fldEnglishTweetsPath.setColumns(10);
		fldEnglishTweetsPath.setBounds(124, 117, 262, 28);
		contentPane.add(fldEnglishTweetsPath);
		
		JLabel lblSaveEnglishTweets = new JLabel("English tweets:");
		lblSaveEnglishTweets.setBounds(31, 123, 106, 16);
		contentPane.add(lblSaveEnglishTweets);
		
		JButton btnEnglishTweetsBrowse = new JButton("Browse...");
		btnEnglishTweetsBrowse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser c = new JFileChooser();
			      // Demonstrate "Save" dialog:
			      int rVal = c.showSaveDialog(contentPane);
			      if (rVal == JFileChooser.APPROVE_OPTION) {
			        fldEnglishTweetsPath.setText(c.getSelectedFile().getPath());
			      }
			}
		});
		btnEnglishTweetsBrowse.setBounds(398, 119, 117, 29);
		contentPane.add(btnEnglishTweetsBrowse);
		System.setOut(interceptor);
		
		//Set fields to defualt values as specified in config
		fldInputPath.setText(config.get("inputPath"));
	}
	
	private Map<String,String> loadConfig(String configPath){
		Map<String, String> r = new HashMap<String, String>();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader (new FileInputStream(configPath), "UTF8"));
			String line = br.readLine();
			while (line != null){
				String[] tmp = line.split(":");
				r.put(tmp[0], tmp[1]);
				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
		
		return r;
	}
	
	private void writeConfig(String configPath){
		try{
			// Create file
			FileWriter fstream = new FileWriter(new File(configPath), false);
			BufferedWriter out = new BufferedWriter(fstream);
			// Iterate across each config entry and write it to the file
			for(Map.Entry<String, String> entry : config.entrySet()){
				out.write(entry.getKey() + ":" + entry.getValue());
				out.write("\n");
			}
			out.close();
			fstream.close();
		} catch (Exception e) {
			System.out.println("Error: " + e.getMessage());
		}
	}
}
