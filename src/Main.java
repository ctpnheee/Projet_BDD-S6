import util.ConnexionBDD;
import vue.MenuPrincipal;
import vue.gui.MainFrame;
import vue.gui.Theme;

import javax.swing.*;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {
        boolean cli = args.length > 0 && ("--cli".equals(args[0]) || "-c".equals(args[0]));
        if (cli) {
            launchCli();
        } else {
            launchGui();
        }
    }

    private static void launchCli() {
        try {
            ConnexionBDD.getConnection();
        } catch (SQLException e) {
            System.err.println("Impossible de se connecter à la base de données.");
            System.err.println("Vérifiez src/util/ConnexionBDD.java");
            System.err.println("Détail : " + e.getMessage());
            System.exit(1);
        }
        new MenuPrincipal().lancer();
    }

    private static void launchGui() {
        try {
            ConnexionBDD.getConnection();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null,
                    "<html><b>Impossible de se connecter à la base de données.</b><br><br>"
                    + "Vérifiez l'hôte, le port et le mot de passe dans<br>"
                    + "<code>src/util/ConnexionBDD.java</code><br><br>"
                    + "Détail : " + e.getMessage() + "</html>",
                    "Erreur de connexion", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }

        Theme.apply();

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new MainFrame().setVisible(true);
        });
    }
}
