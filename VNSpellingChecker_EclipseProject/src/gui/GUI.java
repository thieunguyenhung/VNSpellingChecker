package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;

import org.apache.commons.io.FilenameUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.devboxmobile.msword.reader.MicrosoftWordReader;
import com.devboxmobile.pdf.reader.PDFReader;

import core.VietnameseSpellingTool;
import fileio.FileIOSpelling;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import thirdparty.JazzySpell;

public class GUI {

	private JFrame frame;

	private WebView webView;

	private JTextArea txtInput;
	private JFXPanel txtOutput;

	private JToolBar toolBar;
	private JButton btnEraser, btnSpellCheck, btnExportHTML, btnExportTxt, btnImport;

	private JMenuBar menuBar;
	private JMenu fileMenu;

	private String outputResultStr = "";

	private JazzySpell jazzyChecker;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI window = new GUI();
					window.frame.setVisible(true);
					window.txtInput.requestFocus();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public GUI() {
		jazzyChecker = new JazzySpell();
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				// comfirm closing dialog
				int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit? Any unsaved progress will be lost.", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
				if (dialogResult == JOptionPane.OK_OPTION) {
					System.exit(0);
				}
			}
		});
		frame.getContentPane().setBackground(Color.WHITE);
		frame.setBackground(Color.WHITE);
		frame.setBounds(20, 20, 1000, 650);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setResizable(false);
		frame.getContentPane().setLayout(null);

		// Status bar
		JPanel statusPanel = new JPanel();
		statusPanel.setBounds(0, 583, 1000, 15);
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(frame.getWidth(), 16));
		statusPanel.setLayout(null);

		// Progress bar
		JProgressBar progressBar = new JProgressBar(0, 100);
		progressBar.setBounds(0, 0, 200, 15);
		progressBar.setForeground(Color.decode("#06B025"));
		progressBar.setIndeterminate(false);
		statusPanel.add(progressBar);

		// Status description
		JLabel statusDesc = new JLabel();
		statusDesc.setBounds(210, 0, 750, 15);
		statusDesc.setHorizontalAlignment(SwingConstants.LEFT);
		statusPanel.add(statusDesc);

		// Status description
		JLabel errorLabel = new JLabel();
		errorLabel.setBounds(890, 0, 100, 15);
		errorLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		statusPanel.add(errorLabel);

		frame.getContentPane().add(statusPanel, BorderLayout.SOUTH);

		// Input text
		txtInput = new JTextArea();
		txtInput.setFont(new Font("Tahoma", Font.PLAIN, 14));
		txtInput.setLineWrap(true);
		txtInput.setWrapStyleWord(true);

		JScrollPane scrollPaneInput = new JScrollPane(txtInput);
		scrollPaneInput.setBounds(5, 45, 485, 535);
		scrollPaneInput.setBackground(Color.WHITE);
		scrollPaneInput.setBorder(new LineBorder(Color.GRAY));
		frame.getContentPane().add(scrollPaneInput);

		// Output text
		txtOutput = new JFXPanel();
		txtOutput.setBounds(500, 45, 485, 535);
		txtOutput.setBackground(Color.WHITE);
		txtOutput.setBorder(new LineBorder(Color.GRAY));

		frame.getContentPane().add(txtOutput);

		// New menu item
		JMenuItem newMenuItem = new JMenuItem("New", new ImageIcon(GUI.class.getResource("/icon/new_project.png")));
		newMenuItem.setMnemonic(KeyEvent.VK_N);
		newMenuItem.setToolTipText("Create new project");
		newMenuItem.addActionListener((ActionEvent event) -> {
			// comfirm new dialog
			int dialogResult = JOptionPane.showConfirmDialog(frame, "Create new project? Any unsaved progress will be lost.", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialogResult == JOptionPane.OK_OPTION) {
				txtInput.setText("");
				loadWebView("");
			}
		});

		// Open menu item
		JMenuItem openMenuItem = new JMenuItem("Open...", new ImageIcon(GUI.class.getResource("/icon/open_project.png")));
		openMenuItem.setMnemonic(KeyEvent.VK_O);
		openMenuItem.setToolTipText("Open saved project");
		openMenuItem.addActionListener((ActionEvent event) -> {
			FileDialog fd = new FileDialog(new Frame(), "Open project", FileDialog.LOAD);
			fd.setDirectory(System.getProperty("user.dir"));
			fd.setFile("*.vsc");
			fd.setVisible(true);
			String filename = fd.getFile();
			if (null != filename) {
				File inputFile = new File(fd.getDirectory() + filename);
				if (inputFile.exists() && !inputFile.isDirectory()) {
					ArrayList<String> savedStr = FileIOSpelling.openProject(fd.getDirectory() + filename);
					if (null != savedStr) {
						txtInput.setText(savedStr.get(0));
						loadWebView(savedStr.get(1));
					} else
						JOptionPane.showMessageDialog(frame, "Saved file has been damaged!", "Error", JOptionPane.ERROR_MESSAGE);
				} else
					JOptionPane.showMessageDialog(frame, "File does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		// Save menu item
		JMenuItem saveMenuItem = new JMenuItem("Save", new ImageIcon(GUI.class.getResource("/icon/save_project.png")));
		saveMenuItem.setMnemonic(KeyEvent.VK_S);
		saveMenuItem.setToolTipText("Save current project");
		saveMenuItem.addActionListener((ActionEvent event) -> {
			FileDialog fd = new FileDialog(new Frame(), "Save project", FileDialog.SAVE);
			fd.setDirectory(System.getProperty("user.dir"));
			fd.setFile("*.vsc");
			fd.setVisible(true);
			String filename = fd.getFile();
			if (null != filename) {
				FileIOSpelling.saveProject(txtInput.getText(), outputResultStr, fd.getDirectory() + filename);
			}
		});

		// Exit menu item
		JMenuItem exitMenuItem = new JMenuItem("Exit", new ImageIcon(GUI.class.getResource("/icon/exit.png")));
		exitMenuItem.setMnemonic(KeyEvent.VK_E);
		exitMenuItem.setToolTipText("Exit application");
		exitMenuItem.addActionListener((ActionEvent event) -> {
			// comfirm closing dialog
			int dialogResult = JOptionPane.showConfirmDialog(frame, "Are you sure you want to exit? Any unsaved progress will be lost.", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
			if (dialogResult == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		});

		// Menu File
		fileMenu = new JMenu("File");
		fileMenu.setMnemonic(KeyEvent.VK_F);
		fileMenu.add(newMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(openMenuItem);
		fileMenu.add(saveMenuItem);
		fileMenu.addSeparator();
		fileMenu.add(exitMenuItem);

		// Menu Bar
		menuBar = new JMenuBar();
		menuBar.add(fileMenu);
		frame.setJMenuBar(menuBar);

		// Button Import
		btnImport = new JButton(new ImageIcon(GUI.class.getResource("/icon/import.png")));
		btnImport.setToolTipText("Import text file to input");
		btnImport.addActionListener((ActionEvent event) -> {
			FileDialog fd = new FileDialog(new Frame(), "Import from text document file", FileDialog.LOAD);
			fd.setDirectory(System.getProperty("user.dir"));
			fd.setFile("*.txt;*.doc;*.docx;*.pdf");
			fd.setVisible(true);
			String filename = fd.getFile();
			if (null != filename) {
				File inputFile = new File(fd.getDirectory() + filename);
				if (inputFile.exists() && !inputFile.isDirectory()) {
					String extension = FilenameUtils.getExtension(inputFile.getName());
					if (extension.equals("docx")) {
						txtInput.setText(MicrosoftWordReader.readDocx(inputFile.getAbsolutePath()));
					} else if (extension.equals("doc")) {
						txtInput.setText(MicrosoftWordReader.readDoc(inputFile.getAbsolutePath()));
					} else if (extension.equals("pdf")) {
						String pdfText = PDFReader.readPDF(inputFile.getAbsolutePath());
						if (pdfText.equals("Cannot extract text, content was encrypted") || pdfText.equals("Cannot decrypt PDF, password protected"))
							JOptionPane.showMessageDialog(frame, pdfText, "PDF Error", JOptionPane.ERROR_MESSAGE);
						else
							txtInput.setText(PDFReader.readPDF(inputFile.getAbsolutePath()));
					} else
						txtInput.setText(FileIOSpelling.readFile(inputFile));
				} else
					JOptionPane.showMessageDialog(frame, "File does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		});

		// Button Export TXT
		btnExportTxt = new JButton(new ImageIcon(GUI.class.getResource("/icon/export_txt.png")));
		btnExportTxt.setToolTipText("Export output to text file");
		btnExportTxt.addActionListener((ActionEvent event) -> {
			if (outputResultStr.length() > 0) {
				FileDialog fd = new FileDialog(new Frame(), "Export to text file", FileDialog.SAVE);
				fd.setDirectory(System.getProperty("user.dir"));
				fd.setFile("*.txt");
				fd.setVisible(true);
				String filename = fd.getFile();
				if (null != filename) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							Document doc = Jsoup.parse(webView.getEngine().executeScript("document.documentElement.outerHTML").toString());
							FileIOSpelling.writeFile(doc.body().text(), fd.getDirectory() + filename);
						}
					});
				}
			}
		});

		// Button Export HTML
		btnExportHTML = new JButton(new ImageIcon(GUI.class.getResource("/icon/export_html.png")));
		btnExportHTML.setToolTipText("Export output to HTML file");
		btnExportHTML.addActionListener((ActionEvent event) -> {
			if (outputResultStr.length() > 0) {
				FileDialog fd = new FileDialog(new Frame(), "Export to HTML file", FileDialog.SAVE);
				fd.setDirectory(System.getProperty("user.dir"));
				fd.setFile("*.html");
				fd.setVisible(true);
				String filename = fd.getFile();
				if (null != filename) {
					Platform.runLater(new Runnable() {
						@Override
						public void run() {
							FileIOSpelling.writeFileHTML(webView.getEngine().executeScript("document.documentElement.outerHTML").toString(), fd.getDirectory() + filename);
						}
					});
				}
			}
		});

		// Button Spell Check
		btnSpellCheck = new JButton(new ImageIcon(GUI.class.getResource("/icon/spell_check.png")));
		btnSpellCheck.setToolTipText("Perform spell checking");
		btnSpellCheck.addActionListener((ActionEvent event) -> {
			String inputStr = txtInput.getText();
			if (inputStr.length() > 0 && !inputStr.trim().isEmpty()) {
				SwingWorker<Void, Void> swingWorker = new SwingWorker<Void, Void>() {
					private VietnameseSpellingTool spellingTool = new VietnameseSpellingTool();

					@Override
					protected Void doInBackground() throws Exception {
						progressBar.setIndeterminate(true);
						setEnableButton(false);
						statusDesc.setText("Processing...");
						try {
							outputResultStr = spellingTool.checkSpelling(jazzyChecker, inputStr);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void done() {
						loadWebView(outputResultStr);
						Toolkit.getDefaultToolkit().beep();
						progressBar.setIndeterminate(false);
						setEnableButton(true);
						statusDesc.setText("Done");
						errorLabel.setText("Error: " + spellingTool.getErrorCounter());
					}
				};
				swingWorker.execute();
			}
		});

		// Button Eraser
		btnEraser = new JButton(new ImageIcon(GUI.class.getResource("/icon/eraser.png")));
		btnEraser.setToolTipText("Clear output text");
		btnEraser.addActionListener((ActionEvent event) -> {
			statusDesc.setText("");
			errorLabel.setText("");
			loadWebView("");
		});

		// Toolbar
		toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBounds(0, 0, 1000, 40);
		toolBar.add(btnImport);
		toolBar.add(btnExportTxt);
		toolBar.add(btnExportHTML);
		toolBar.addSeparator();
		toolBar.add(btnEraser);
		toolBar.addSeparator();
		toolBar.add(btnSpellCheck);
		frame.getContentPane().add(toolBar);
	}

	private void setEnableButton(boolean isEnable) {
		fileMenu.setEnabled(isEnable);

		btnImport.setEnabled(isEnable);
		btnExportTxt.setEnabled(isEnable);
		btnExportHTML.setEnabled(isEnable);
		btnEraser.setEnabled(isEnable);
		btnSpellCheck.setEnabled(isEnable);

		txtInput.setEnabled(isEnable);
	}

	private void loadWebView(String content) {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				webView = new WebView();
				txtOutput.setScene(new Scene(webView));
				webView.getEngine().loadContent(content);
			}
		});
	}
}
