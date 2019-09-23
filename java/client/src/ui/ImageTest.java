package ui;

import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ImageTest extends JFrame {
	private ImageFrame frame;
	ImageComponent component2;
	Boolean value = false;
	int i = 0;

	public ImageTest() {}
	
	public void updateImage(final BufferedImage image) {
		/*System.out.println("update the ----");
		ImageComponent component1 = new ImageComponent(image);
		System.out.println("updateImage before new ImageFrame()");
		//if(frame == null){
			frame = new ImageFrame();
		//}
			
			
			
			System.out.println("updateImage before setTitle");
			frame.setTitle("ImageTest: " + i);
			System.out.println("updateImage before setSize");
			frame.setSize(image.getWidth(this), image.getHeight(this) + 30);
			
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			System.out.println("updateImage before add");
			frame.add(component1);
			System.out.println("updateImage before visible");
			frame.setVisible(true);
			System.out.println("updateImage before revalidate");
			frame.revalidate();
			System.out.println("updateImage before repaint");
			frame.repaint();
			
			System.out.println("updateImage end");
			*/
		i++;
		System.out.println("hello world " + i);
/*
		System.out.println("image width: " + image.toString());
		JFrame frame = new JFrame();
		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(new ImageIcon(image)));
		frame.pack();
		frame.setVisible(true);
		System.out.println("image height: " + image.getHeight());
	*/
			
		/*
		frame.updateImage(image);
		if (value)
			frame.remove(component2);
		
		frame.add(component1);
		//frame.getContentPane().add(p);
		component2 = component1;
		value = true;
*/
		
	}
	
}

class ImageFrame extends JFrame {
	int i = 0;

	public ImageFrame() {}
	
	public void updateImage(Image image) {
		
		setTitle("ImageTest: " + i);
		setSize(image.getWidth(this), image.getHeight(this) + 30);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(true);
		revalidate();
		repaint();

		i++;
	}
	
}

class ImageComponent extends JComponent {
	/**
     * 
     */
	private static final long serialVersionUID = 1L;
	private Image image;

	public ImageComponent(Image image) {
		this.image = image;
	}

	public void paintComponent(Graphics g) {
		if (image == null)
			return;
		int imageWidth = image.getWidth(this);
		int imageHeight = image.getHeight(this);

		g.drawImage(image, 0, 0, this);

		for (int i = 0; i * imageWidth <= getWidth(); i++)
			for (int j = 0; j * imageHeight <= getHeight(); j++)
				if (i + j > 0)
					g.copyArea(0, 0, imageWidth, imageHeight, i * imageWidth, j
							* imageHeight);
	}

}
