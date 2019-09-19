package ui;

import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class ImageTest  {
	private ImageFrame frame;
	ImageComponent component2;
	Boolean value = false;

	public ImageTest() {}
	
	public void updateImage(final BufferedImage image) {
		ImageComponent component1 = new ImageComponent(image);
		if(frame == null){
			component2 = new ImageComponent(image);
			frame = new ImageFrame();
		}
		
		frame.updateImage(image);
		if (value)
			frame.remove(component2);
		
		frame.add(component1);
		//frame.getContentPane().add(p);
		component2 = component1;
		value = true;

		
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
