import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.BorderUIResource;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;

/**
 * The Catppuccin Mocha palette as a Swing theme. Metal is the one look and feel that honors
 * theme colors on every platform, so the palette goes in as a Metal theme instead of a
 * component-by-component repaint, and the macOS title bar is asked to go dark to match.
 */
public final class MochaTheme extends DefaultMetalTheme {

    public static final Color CRUST = new Color(0x11111B);
    public static final Color BASE = new Color(0x1E1E2E);
    public static final Color SURFACE0 = new Color(0x313244);
    public static final Color SURFACE1 = new Color(0x45475A);
    public static final Color SURFACE2 = new Color(0x585B70);
    public static final Color OVERLAY0 = new Color(0x6C7086);
    public static final Color TEXT = new Color(0xCDD6F4);
    public static final Color LAVENDER = new Color(0xB4BEFE);
    public static final Color BLUE = new Color(0x89B4FA);
    public static final Color SAPPHIRE = new Color(0x74C7EC);
    public static final Color GREEN = new Color(0xA6E3A1);
    public static final Color RED = new Color(0xF38BA8);

    /** Client property a button sets to true for the quiet surface fill instead of the accent. */
    public static final String QUIET_BUTTON = "Mocha.quietButton";

    /** Installs the theme. Must run before any window is created for the title bar to follow. */
    public static void install() {
        System.setProperty("apple.awt.application.appearance", "NSAppearanceNameDarkAqua");
        // Metal defaults to bold text everywhere, which reads as shouting on a dark background.
        UIManager.put("swing.boldMetal", Boolean.FALSE);
        MetalLookAndFeel.setCurrentTheme(new MochaTheme());
        try {
            UIManager.setLookAndFeel(new MetalLookAndFeel());
        } catch (UnsupportedLookAndFeelException impossible) {
            throw new IllegalStateException("Metal ships with every JDK", impossible);
        }

        UIManager.put("ButtonUI", MochaButtonUI.class.getName());
        // A flat outline on the field matches the flat buttons beside it; Metal's bevel would not.
        UIManager.put("TextField.border", new BorderUIResource(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(SURFACE2),
                BorderFactory.createEmptyBorder(6, 8, 6, 8))));
        // Disabled text keeps enough contrast to be read as a label rather than vanishing.
        UIManager.put("Label.disabledForeground", new ColorUIResource(OVERLAY0));
        UIManager.put("TitledBorder.titleColor", new ColorUIResource(LAVENDER));
    }

    @Override
    public String getName() {
        return "Catppuccin Mocha";
    }

    /** Focus rings and active accents. */
    @Override
    protected ColorUIResource getPrimary1() {
        return new ColorUIResource(LAVENDER);
    }

    /** Pressed buttons. */
    @Override
    protected ColorUIResource getPrimary2() {
        return new ColorUIResource(SURFACE2);
    }

    /** Selected text. */
    @Override
    protected ColorUIResource getPrimary3() {
        return new ColorUIResource(SURFACE1);
    }

    /** Dark edges of borders and bevels. */
    @Override
    protected ColorUIResource getSecondary1() {
        return new ColorUIResource(SURFACE2);
    }

    /** Shadows and disabled controls. */
    @Override
    protected ColorUIResource getSecondary2() {
        return new ColorUIResource(SURFACE1);
    }

    /** Panel and dialog backgrounds. */
    @Override
    protected ColorUIResource getSecondary3() {
        return new ColorUIResource(BASE);
    }

    /** Metal paints text with "black", so this is the text color. */
    @Override
    protected ColorUIResource getBlack() {
        return new ColorUIResource(TEXT);
    }

    /** Metal paints text fields and highlights with "white", so this is the raised surface. */
    @Override
    protected ColorUIResource getWhite() {
        return new ColorUIResource(SURFACE0);
    }
}
