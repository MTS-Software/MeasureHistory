package file;

import java.awt.HeadlessException;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

public class FileTest {

	private String path;
	private String file;

	public static void main(String[] args) {

		FileTest fileTest = new FileTest();

		// fileTest.generateFile();
		fileTest.read();

	}

	public FileTest() {

	}

	private void read() {

		File file = new File("data" + File.separator + "test.txt");

		if (file.exists())
			try {
				JOptionPane.showMessageDialog(null, "File ist vorhanden: " + "\nabsolut: " + file.getAbsolutePath()
						+ "\nrelativ: " + file.getPath());

			} catch (HeadlessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		else
			JOptionPane.showMessageDialog(null, "File ist nicht vorhanden");

		System.out.println(file.exists());

	}

	private void generateFile() {

		File file = new File("test.txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
