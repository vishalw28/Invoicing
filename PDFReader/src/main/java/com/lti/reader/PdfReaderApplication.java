package com.lti.reader;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class PdfReaderApplication extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static final String DONE_DIR = "DONE";
	private static final String OUTPUT_DIR = "OUTPUT";
	private static final String OUTPUT_FILE_EXT = ".csv";
	private JFileChooser fileChooser;
	private JButton button;
	private JButton processButton;
	private static final String[] options = { "Ok" };
	
	public PdfReaderApplication() {
        initUI();
    }

    private void initUI() {
        createLayout();
        setTitle("Invoice Validation");
        setSize(300, 200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

	private void createLayout() {
		Container pane = getContentPane();
		GroupLayout gl = new GroupLayout(pane);
		pane.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		fileChooser = new JFileChooser();
		fileChooser.setSize(8, 3);
		fileChooser.setDialogTitle("Select base directory ");
		fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		// creates the GUI
		JLabel label = new JLabel("Select Folder");

		JTextField textField = new JTextField(20);
		button = new JButton("Browse");
		processButton = new JButton("Validate");

		processButton.addActionListener(e -> {
			File selectedFile = fileChooser.getSelectedFile();
			processValidation(selectedFile.getAbsolutePath());
		});
		processButton.setEnabled(false);
		button.addActionListener(evt -> buttonActionPerformed(evt));

		fileChooser.addActionListener(evt -> textField.setText(fileChooser.getSelectedFile().getAbsolutePath()));
		textField.setEnabled(false);
		setSize(60, 10);
		add(label);
		add(textField);
		add(button);

		add(processButton);
	}

    
    private void buttonActionPerformed(ActionEvent evt) {
    	int returnValue = fileChooser.showOpenDialog(null);
		if (returnValue == JFileChooser.APPROVE_OPTION) {
			File selectedFile = fileChooser.getSelectedFile();
			processButton.setEnabled((selectedFile == null) ? false : true);
		}
    }
    
    

	static Consumer<DataCarrier> processor = carrier -> {
		System.out.println("Processing file : "+ carrier.getPdfFilePath());
		File file = new File(carrier.getPdfFilePath());
		try (PDDocument document = PDDocument.load(file)) {
			if (!document.isEncrypted()) {
				PDFTextStripper tStripper = new PDFTextStripper();
				String pdfFileInText = tStripper.getText(document);
				String lines[] = pdfFileInText.split("\\r?\\n");
				carrier.setPdfData(lines);
				//System.out.println(InvoicingUtility.verifyInvoiceDetailsWithExcel(lines, carrier.getExcelData(), carrier.getUnmatchedRecordFilePath()));
				InvoicingUtility.validateData(carrier);
				//Move file to done directory.
				
				
				Files.move(Paths.get(carrier.getPdfFilePath()), Paths.get(carrier.getDoneDirPath().toString(),file.getName()), StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (InvalidPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//JOptionPane.showMessageDialog(new JFrame(), "Invoice processing task is completed.");
		//JOptionPane.showOptionDialog(new JFrame(), "Invoice processing task is completed.", "Success", JOptionPane.PLAIN_MESSAGE, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
		//readPDFAndVerify(list.get(0).toString(), (Map<String, Employee>) list.get(1), (Path)list.get(2));

	};

	public static void main(String[] args) {
		ConfigurableApplicationContext ctx = new SpringApplicationBuilder(PdfReaderApplication.class)
                .headless(false).run(args);

        EventQueue.invokeLater(() -> {
            PdfReaderApplication ex = ctx.getBean(PdfReaderApplication.class);
            ex.setVisible(true);
        });
		
	}

	static void processValidation(String baseDirectory) {
		try {
			Optional<Path> invTrackerPath = Files.list(Paths.get(baseDirectory)).filter(s -> {
				
				return s.getFileName().toString().toLowerCase().startsWith("invoice tracker") && s.getFileName().toString().toLowerCase().toLowerCase().endsWith(".xlsx");
			}).findFirst();

			if (!invTrackerPath.isPresent()) {
				JOptionPane.showOptionDialog(new JFrame(),
						"Invoice Tracker excel is not exists. Please choose correct directory or place the file over here.",
						"Failed", JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				System.exit(0);
			}
			Optional<Path> manPowerPath = Files.list(Paths.get(baseDirectory)).filter(s -> {
				return s.getFileName().toString().toLowerCase().startsWith("man power")
						&& s.getFileName().toString().toLowerCase().endsWith(".xlsx");
			}).findFirst();

			if (!manPowerPath.isPresent()) {
				
				JOptionPane.showOptionDialog(new JFrame(),
						"Man power excel is not exists. Please choose correct directory or place the file over here.",
						"Failed", JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
				System.exit(0);
			}

			String dateTimeStamp = createDirctories(baseDirectory);
			Map<String, Employee> empDetailMap = InvoicingUtility
					.readInvoiceTrackerExcel(invTrackerPath.get().toFile().getAbsolutePath(), manPowerPath.get().toFile().getAbsolutePath());

			Path p = Paths.get(baseDirectory + File.separator + OUTPUT_DIR + File.separator
					+ dateTimeStamp + OUTPUT_FILE_EXT);

			try (BufferedWriter writer = Files.newBufferedWriter(p)) {
				writer.write(Employee.getColumnHeader());
				writer.newLine();
			}
			
			Files.list(Paths.get(baseDirectory)).filter(s -> {
				return s.toString().toLowerCase().endsWith(".pdf") ;
			}).forEach(o -> processor.accept(DataCarrier.builder()
					.pdfFilePath(o.toFile().toString())
					.excelData(empDetailMap)
					.unmatchedRecordFilePath(p)
					.doneDirPath(Paths.get(baseDirectory + File.separator + DONE_DIR + File.separator + dateTimeStamp))
					.build()));
			;
		} catch (Exception e) {
			JOptionPane.showOptionDialog(new JFrame(),
					e.getMessage(),
					"Failed", JOptionPane.ERROR_MESSAGE, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
			e.printStackTrace();
		}
		
		JOptionPane.showOptionDialog(new JFrame(),
				"Invoice validation task is completed. Kindly check the "+baseDirectory + File.separator + OUTPUT_DIR + " directory for more details",
				"Success", JOptionPane.INFORMATION_MESSAGE, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
	}
	
	static String createDirctories(String basePath) {
		try {
			String dateTimeStamp = LocalDateTime.now().toString().replaceAll(":", "_");
			//Files.createDirectories(Paths.get(basePath + File.separator + DONE_DIR));
			Files.createDirectories(Paths.get(basePath + File.separator + OUTPUT_DIR));
			Files.createDirectories(Paths.get(basePath + File.separator + DONE_DIR + File.separator + dateTimeStamp));
			return dateTimeStamp;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	
//	public static void readPDFAndVerify(String filePath, Map<String, Employee> empDetailMap, Path path) {
//		//System.out.println("Processing file >> " + filePath);
//		try (PDDocument document = PDDocument.load(new File(filePath))) {
//			document.getClass();
//			if (!document.isEncrypted()) {
//				PDFTextStripper tStripper = new PDFTextStripper();
//				String pdfFileInText = tStripper.getText(document);
//				String lines[] = pdfFileInText.split("\\r?\\n");
//				System.out.println(InvoicingUtility.verifyInvoiceDetailsWithExcel(lines, empDetailMap, path));
//			}
//		} catch (InvalidPasswordException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

}
