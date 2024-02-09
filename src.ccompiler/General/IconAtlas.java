package General;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import javax.swing.ImageIcon;

public class IconAtlas {

	private Map<String, BufferedImage> icons;
	private String[] imgNames;

	public IconAtlas() {
		icons = new HashMap<>();
		load();
	}

	public Map<String, BufferedImage> getIcons() {
		return icons;
	}

	private void load() {
		imgNames = Utils.read(Utils.resolveRes("icons/indexList.txt")).split(System.lineSeparator());
		for (int i = 0; i < imgNames.length; i++) {
			icons.put(Utils.removeExtension(imgNames[i]), Utils.readImage(Utils.resolveRes("icons/" + imgNames[i])));
		}
	}

	public ImageIcon getIcon(String key, int size) {
		BufferedImage icon = icons.get(key);
		if (icon == null)
			return null;
		return new ImageIcon(icon.getScaledInstance(size, size, BufferedImage.SCALE_SMOOTH));
	}

	public BufferedImage getImage(String key) {
		return icons.get(key);
	}

}
