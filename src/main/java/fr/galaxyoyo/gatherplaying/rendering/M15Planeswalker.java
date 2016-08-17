package fr.galaxyoyo.gatherplaying.rendering;

import fr.galaxyoyo.gatherplaying.Card;
import fr.galaxyoyo.gatherplaying.Layout;
import fr.galaxyoyo.gatherplaying.ManaColor;
import fr.galaxyoyo.gatherplaying.Rarity;
import fr.galaxyoyo.gatherplaying.client.Config;
import java8.util.stream.Collectors;
import java8.util.stream.RefStreams;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class M15Planeswalker extends CardRenderer
{
	public M15Planeswalker(Card card)
	{
		super(card);
	}

	@Override
	public BufferedImage render() throws IOException
	{
		String language = Config.getLocaleCode();

		System.out.print("\n" + getCard().getTranslatedName().get() + "...");

		File frameDir = getFrameDir();

		ManaColor[] colorsObj = getCard().getColors();
		String colors = String.join("", RefStreams.of(colorsObj).map(ManaColor::getAbbreviate).collect(Collectors.toList()));
		boolean useMulticolorFrame = colors.length() == 2;

		BufferedImage img = new BufferedImage(720, 1020, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = img.createGraphics();

		drawArt(g, new File(ARTDIR, getCard().getSet().getCode() + "/" + getCard().getName().get("en") + ".jpg"), 0, 0, 1020, 720);

		System.out.print(".");

		BufferedImage bgImage;
		if (colorsObj[0] == ManaColor.COLORLESS)
			bgImage = readImage(new File(frameDir, "cards/Art.png"));
		else if (useMulticolorFrame)
			bgImage = readImage(new File(frameDir, "cards/Gld" + colors + ".png"));
		else
			bgImage = readImage(new File(frameDir, "cards/" + (colors.length() >= 3 ? "Gld" : colors) + ".png"));

		if (bgImage != null)
			g.drawImage(bgImage, 0, 0, 720, 1020, null);

		if (getCard().getLoyalty() > 0)
		{
			BufferedImage image = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyBegin.png"));
			if (image != null)
				g.drawImage(image, 0, 0, 720, 1020, null);
			g.setFont(Fonts.LOYALTY_STARTING);
			g.setColor(Color.WHITE);
			drawText(g, 641, 940, 114, Integer.toString(getCard().getLoyalty()), true, true);
		}

		int costLeft = drawCastingCost(g, getCard().getManaCost(), colors.length() >= 2 ? 37 : 35, 677, 35);
		System.out.print(".");
		int rarityLeft = drawRarity(g, getCard().getRarity(), getCard().getSet(), 675, 604, 41, 76);

		int titleX = frameDir.getName().startsWith("transform-") ? 105 : 51;
		g.setColor(Color.BLACK);
		g.setFont(Fonts.TITLE);
		drawText(g, titleX, frameDir.getName().startsWith("transform-") ? 64 : 68, costLeft - 20 - titleX, getCard().getTranslatedName().get(), false, false);
		System.out.print(".");

		int typex = frameDir.getName().equals("transform-night") || frameDir.getName().equals("transform-ignite") ? 87 : 51;
		drawText(g, typex, 616, rarityLeft - typex, "Planeswalker : " + getCard().getSubtypes()[0].getTranslatedName().get(), false, false);

		String legal = getCard().getAbility().replace(" : ", ": ").replace('−', '-').replace("\r\n", "\n").replace("\r", "\n");
		String[][] infos = new String[3][2];
		String[] split = legal.split("\n");
		for (int i = 0; i < split.length; ++i)
		{
			infos[i] = split[i].split(":[ | ]");
			if (infos[i].length == 1)
				infos[i] = new String[]{"", split[i]};
		}
		int maxWidth = 673;
		Font f = Fonts.TEXT;
		Map<String, Number> map1, map2, map3;
		while (true)
		{
			map1 = testChunksWrapped(maxWidth, getChunks(infos[0][1]), f);
			map2 = testChunksWrapped(maxWidth, getChunks(infos[1][1]), f);
			map3 = testChunksWrapped(maxWidth, getChunks(infos[2][1]), f);
			int difference = Math.max(Math.max(map1.get("height").intValue(), map2.get("height").intValue()), map3.get("height").intValue()) - 65;
			int lastLineWidth = map3.get("lastLineWidth").intValue();
			float decrement;
			if (difference < 0 && lastLineWidth <= 600)
				break;
			else if (15 > difference)
				decrement = 0.05F;
			else if (30 > difference)
				decrement = 0.2F;
			else if (difference < 100)
				decrement = 0.4F;
			else
				decrement = 0.8F;
			f = f.deriveFont(f.getSize2D() - decrement);
		}
		for (int i = 0; i < 3; i++)
		{
			BufferedImage image = infos[i][0].isEmpty() ? null : loyaltyIcon(infos[i][0].charAt(0));
			int y = 0;
			switch (i)
			{
				case 0:
					y = 626;
					break;
				case 1:
					y = 725;
					break;
				case 2:
					y = 815;
					break;
			}

			if (image != null)
				g.drawImage(image, 0, y, 120, 120, null);
			g.setColor(Color.WHITE);
			g.setFont(Fonts.LOYALTY_CHANGE);
			drawText(g, 60, y + 57, 114, infos[i][0], true, true);
			g.setColor(Color.BLACK);
			drawChunksWrapped(g, (int) (y + (90 - (i == 0 ? map1 : i == 1 ? map2 : map3).get("height").floatValue()) + f.getSize2D() / 2.0F), 122,
					673, getChunks(infos[i][1]), f);
		}

		g.setColor(Color.WHITE);
		g.setFont(Fonts.COLLECTION);

		String collectorNumber = getCard().getNumber().replaceAll("[^\\d]", "") + "/";
		while (collectorNumber.length() < 4)
			collectorNumber = "0" + collectorNumber;
		AtomicInteger max = new AtomicInteger(0);
		getCard().getSet().getCards().forEach(c -> max.set(Math.max(max.get(), Integer.parseInt(c.getNumber().replaceAll("[^\\d]", "")))));
		collectorNumber += max.get();

		String collectionTxtL1 = collectorNumber;
		String collectionTxtL2 = getCard().getSet().getCode() + " • " + language.toUpperCase() + " ";

		drawText(g, 37, 977, 99999, collectionTxtL1 + "\n" + collectionTxtL2 + "{brush2}", false, false);
		int w = (int) getStringWidth(collectionTxtL2, g.getFont());
		drawText(g, 40 + w, 977, 99999, getCard().getRarity() == Rarity.BASIC_LAND ? "L" : "" + getCard().getRarity().name().charAt(0), false, false);
		g.setFont(Fonts.ARTIST);
		drawText(g, 64 + w, 996, 99999, getCard().getArtist(), false, false);

		String copyright = "Gather Playing ™ & © 2016 Wizards of the Coast";
		g.setFont(Fonts.COPYRIGHT);
		drawText(g, 680 - (int) getStringWidth(copyright, g.getFont()), 996, 99999, copyright, false, false);

		return img;
	}

	private BufferedImage loyaltyIcon(char sign)
	{
		BufferedImage loyaltyImage;
		if (sign == '+')
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyUp.png"));
		else if (sign == '-')
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyDown.png"));
		else
			loyaltyImage = readImage(new File(DIR, "images/m15/planeswalker/loyalty/LoyaltyZero.png"));
		return loyaltyImage;
	}

	@Override
	public File getFrameDir()
	{
		String frame = "regular";
		if (getCard().getSet().getCode().equals("ORI"))
			frame = "transform-ignite";
		else if (getCard().getLayout() == Layout.DOUBLE_FACED)
			frame = "transform-" + (getCard().getNumber().endsWith("a") ? "day" : "night");
		return new File(DIR, "images/m15/planeswalker/" + frame);
	}
}
