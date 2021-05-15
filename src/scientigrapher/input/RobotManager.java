package scientigrapher.input;

import java.awt.AWTException;
import java.awt.HeadlessException;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class RobotManager {
	private static Robot r;
	static {
		try {
			r = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
	public static void clickOnChrome() {
		clickOn(50, 50);
	}
	
	public static void clickOn(int x, int y) {
		r.mouseMove(x,y);
		r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
	}	
	
	
	public static String copyPasteWholePage(Robot r)
	{
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_A);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		r.keyPress(KeyEvent.VK_C);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		r.keyRelease(KeyEvent.VK_CONTROL);
		r.keyRelease(KeyEvent.VK_C);
		r.keyRelease(KeyEvent.VK_A);
		
		
		

		try {
			return (String) Toolkit.getDefaultToolkit()
					.getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new Error();
	}

	public static void writeAddress(String pageToAsk) {
		
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_L);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		r.keyRelease(KeyEvent.VK_CONTROL);
		r.keyRelease(KeyEvent.VK_L);
		
		
		pasteText(pageToAsk);
		
		r.keyPress(KeyEvent.VK_ENTER);
		r.keyRelease(KeyEvent.VK_ENTER);
		System.out.println("Loading the page");
		try {
			Thread.sleep(1000+(int)(Math.random()*500));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private static void pasteText(String pageToAsk) {
		String text = pageToAsk;
		StringSelection stringSelection = new StringSelection(text);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, stringSelection);

		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_V);
		r.keyRelease(KeyEvent.VK_V);		
		r.keyRelease(KeyEvent.VK_CONTROL);
	}

	public static String getFullPage() {
		try {
			r.keyPress(KeyEvent.VK_CONTROL);
			r.keyPress(KeyEvent.VK_U);

			Thread.sleep(100);

			r.keyRelease(KeyEvent.VK_U);
			r.keyRelease(KeyEvent.VK_CONTROL);

			String res = copyPasteWholePage(r);

			
			return res;

		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		throw new Error();
		
		
	}

	public static void closePage() {
		
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_W);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		r.keyRelease(KeyEvent.VK_W);
		r.keyRelease(KeyEvent.VK_CONTROL);
		
		try {
			System.out.println("Closing the page");
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void savePage(String parent, String name) {
		r.keyPress(KeyEvent.VK_CONTROL);
		r.keyPress(KeyEvent.VK_S);

		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		r.keyRelease(KeyEvent.VK_S);
		r.keyRelease(KeyEvent.VK_CONTROL);
		
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		pasteText(name);
		
		/*r.mouseMove(500,170);
		r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
		
		r.keyPress(KeyEvent.VK_ENTER);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		r.keyRelease(KeyEvent.VK_ENTER);
		
		r.keyPress(KeyEvent.VK_ENTER);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		r.keyRelease(KeyEvent.VK_ENTER);
		
		r.keyPress(KeyEvent.VK_ENTER);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		r.keyRelease(KeyEvent.VK_ENTER);
		
		
		pasteText(parent);*/
		
		
		r.keyPress(KeyEvent.VK_ESCAPE);
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		r.keyRelease(KeyEvent.VK_ESCAPE);
		
		
		
		System.out.println("Pasted");
	}


}
