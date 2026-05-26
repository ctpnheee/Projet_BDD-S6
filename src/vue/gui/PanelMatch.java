package vue.gui;

import dao.EquipeDAO;
import dao.MatchDAO;
import dao.TournoiDAO;
import modele.Match;
import modele.Phase;
import modele.Statistique;
import modele.Tournoi;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PanelMatch extends JPanel {

    private final MatchDAO   matchDAO   = new MatchDAO();
    private final TournoiDAO tournoiDAO = new TournoiDAO();
    private final EquipeDAO  equipeDAO  = new EquipeDAO();

    private final DefaultTableModel model = new DefaultTableModel(
            new String[]{"ID", "Phase", "Équipe 1", "Score", "Équipe 2", "Date", "Vainqueur"}, 0);
    private final JTable table = Theme.styledTable(model);

    private final JComboBox<String> cbTournoi = new JComboBox<>();
    private List<Tournoi> tournoiList;

    public PanelMatch() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        add(buildTop(),              BorderLayout.NORTH);
        add(Theme.scrollPane(table), BorderLayout.CENTER);
        add(buildButtons(),          BorderLayout.SOUTH);
        chargerTournois();
    }

    private JPanel buildTop() {
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG_DARK);
        top.setBorder(new EmptyBorder(6, 10, 6, 10));
        top.add(Theme.title("Gestion des matchs"), BorderLayout.WEST);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
        right.setBackground(Theme.BG_DARK);
        Theme.styledCombo(cbTournoi);
        JButton btnLoad = Theme.btnNeutral("Charger matchs");
        btnLoad.addActionListener(e -> chargerMatchs());
        right.add(new JLabel("Tournoi : ")); right.add(cbTournoi); right.add(btnLoad);
        top.add(right, BorderLayout.EAST);
        return top;
    }

    private JPanel buildButtons() {
        JButton btnCreate = Theme.btnSuccess("+ Créer match");
        JButton btnResult = Theme.btnPrimary("Saisir résultat");
        JButton btnStats  = Theme.btnNeutral("Saisir stats joueur");
        JButton btnView   = Theme.btnNeutral("Voir stats match");

        btnCreate.addActionListener(e -> dialogCreerMatch());
        btnResult.addActionListener(e -> dialogResultat());
        btnStats.addActionListener(e  -> dialogSaisirStats());
        btnView.addActionListener(e   -> voirStatsMatch());

        return Theme.toolBar(btnCreate, btnResult, btnStats, btnView);
    }

    private void chargerTournois() {
        try {
            tournoiList = tournoiDAO.listerTournois();
            cbTournoi.removeAllItems();
            for (Tournoi t : tournoiList)
                cbTournoi.addItem("[" + t.getIdTournoi() + "] " + t.getNom());
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void chargerMatchs() {
        int idx = cbTournoi.getSelectedIndex();
        if (idx < 0 || tournoiList == null || tournoiList.isEmpty()) return;
        int idTournoi = tournoiList.get(idx).getIdTournoi();
        try {
            List<Match> matchs = matchDAO.listerMatchsTournoi(idTournoi);
            model.setRowCount(0);
            for (Match m : matchs)
                model.addRow(new Object[]{
                        m.getIdMatch(),
                        m.getLibellePhase(),
                        m.getNomEquipe1(),
                        m.getScoreEquipe1() + " - " + m.getScoreEquipe2(),
                        m.getNomEquipe2(),
                        m.getDateMatch(),
                        m.getNomVainqueur()});
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogCreerMatch() {
        int idx = cbTournoi.getSelectedIndex();
        if (idx < 0 || tournoiList == null || tournoiList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Sélectionnez d'abord un tournoi."); return;
        }
        int idTournoi = tournoiList.get(idx).getIdTournoi();

        try {
            List<Phase> phases = tournoiDAO.listerPhases(idTournoi);
            if (phases.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ce tournoi n'a pas de phase. Créez-en dans l'onglet Tournois.");
                return;
            }
            String[] phaseNames = phases.stream()
                    .map(p -> "[" + p.getIdPhase() + "] " + p.getLibelle())
                    .toArray(String[]::new);
            String choixPhase = (String) JOptionPane.showInputDialog(this,
                    "Sélectionnez la phase :", "Créer match",
                    JOptionPane.PLAIN_MESSAGE, null, phaseNames, phaseNames[0]);
            if (choixPhase == null) return;
            int idPhase = phases.get(java.util.Arrays.asList(phaseNames).indexOf(choixPhase)).getIdPhase();

            List<modele.Equipe> equipes = equipeDAO.listerEquipes();
            String[] equipeNames = equipes.stream()
                    .map(e -> "[" + e.getIdEquipe() + "] " + e.getNom())
                    .toArray(String[]::new);

            String choixE1 = (String) JOptionPane.showInputDialog(this, "Équipe 1 :", "Match",
                    JOptionPane.PLAIN_MESSAGE, null, equipeNames, equipeNames[0]);
            if (choixE1 == null) return;
            String choixE2 = (String) JOptionPane.showInputDialog(this, "Équipe 2 :", "Match",
                    JOptionPane.PLAIN_MESSAGE, null, equipeNames, equipeNames[0]);
            if (choixE2 == null) return;
            int idE1 = equipes.get(java.util.Arrays.asList(equipeNames).indexOf(choixE1)).getIdEquipe();
            int idE2 = equipes.get(java.util.Arrays.asList(equipeNames).indexOf(choixE2)).getIdEquipe();

            if (idE1 == idE2) { JOptionPane.showMessageDialog(this, "Les deux équipes doivent être différentes."); return; }

            Match m = new Match(0, LocalDateTime.now(), 0, 0, idPhase, idE1, idE2, null);
            if (matchDAO.creerMatch(m)) {
                chargerMatchs();
                JOptionPane.showMessageDialog(this, "Match créé (id=" + m.getIdMatch() + ")",
                        "Succès", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void dialogResultat() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un match."); return; }
        int idMatch = (int) model.getValueAt(row, 0);
        String e1   = (String) model.getValueAt(row, 2);
        String e2   = (String) model.getValueAt(row, 4);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_PANEL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8); c.anchor = GridBagConstraints.WEST;

        JTextField tfS1 = new JTextField("0", 6); tfS1.setFont(Theme.FONT_NORMAL);
        JTextField tfS2 = new JTextField("0", 6); tfS2.setFont(Theme.FONT_NORMAL);

        c.gridx=0; c.gridy=0; form.add(new JLabel("Score " + e1 + " :"), c);
        c.gridx=1; form.add(tfS1, c);
        c.gridx=0; c.gridy=1; form.add(new JLabel("Score " + e2 + " :"), c);
        c.gridx=1; form.add(tfS2, c);

        int res = JOptionPane.showConfirmDialog(this, form, "Saisir résultat du match #" + idMatch,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            int s1 = Integer.parseInt(tfS1.getText().trim());
            int s2 = Integer.parseInt(tfS2.getText().trim());

            try {
                Match m = matchDAO.rechercherParId(idMatch);
                Integer vainqueur = null;
                if (s1 > s2)      vainqueur = m.getIdEquipe1();
                else if (s2 > s1) vainqueur = m.getIdEquipe2();

                if (matchDAO.saisirResultat(idMatch, s1, s2, vainqueur)) {
                    chargerMatchs();
                    JOptionPane.showMessageDialog(this, "Résultat enregistré.", "Succès", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Score invalide (entier attendu).");
        }
    }

    private void dialogSaisirStats() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un match."); return; }
        int idMatch = (int) model.getValueAt(row, 0);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Theme.BG_PANEL);
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(4, 8, 4, 8); c.anchor = GridBagConstraints.WEST;

        JTextField tfId      = new JTextField(8); tfId.setFont(Theme.FONT_NORMAL);
        JTextField tfKills   = new JTextField("0", 6); tfKills.setFont(Theme.FONT_NORMAL);
        JTextField tfDeaths  = new JTextField("0", 6); tfDeaths.setFont(Theme.FONT_NORMAL);
        JTextField tfAssists = new JTextField("0", 6); tfAssists.setFont(Theme.FONT_NORMAL);
        JTextField tfScore   = new JTextField("0.0", 6); tfScore.setFont(Theme.FONT_NORMAL);

        int r = 0;
        for (String[] row2 : new String[][]{
                {"Id joueur *", null}, {"Kills *", null}, {"Deaths *", null},
                {"Assists *", null}, {"Score perf. *", null}}) {
            c.gridx=0; c.gridy=r; JLabel l = new JLabel(row2[0]); l.setFont(Theme.FONT_NORMAL);
            l.setForeground(Theme.TEXT_DIM); form.add(l, c); r++;
        }
        r = 0;
        for (JTextField tf : new JTextField[]{tfId, tfKills, tfDeaths, tfAssists, tfScore}) {
            c.gridx=1; c.gridy=r; form.add(tf, c); r++;
        }

        int res = JOptionPane.showConfirmDialog(this, form, "Statistiques — Match #" + idMatch,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        try {
            Statistique s = new Statistique(
                    Integer.parseInt(tfId.getText().trim()),
                    idMatch,
                    Integer.parseInt(tfKills.getText().trim()),
                    Integer.parseInt(tfDeaths.getText().trim()),
                    Integer.parseInt(tfAssists.getText().trim()),
                    new BigDecimal(tfScore.getText().trim()));
            if (matchDAO.saisirStatistiques(s))
                JOptionPane.showMessageDialog(this, "Statistiques enregistrées.", "Succès", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valeur numérique invalide.");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void voirStatsMatch() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Sélectionnez un match."); return; }
        int idMatch = (int) model.getValueAt(row, 0);

        try {
            List<Statistique> stats = matchDAO.listerStatistiquesMatch(idMatch);
            if (stats.isEmpty()) { JOptionPane.showMessageDialog(this, "Aucune statistique pour ce match."); return; }

            DefaultTableModel sm = new DefaultTableModel(
                    new String[]{"Joueur", "Kills", "Deaths", "Assists", "Score"}, 0) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Statistique s : stats)
                sm.addRow(new Object[]{s.getPseudoJoueur(), s.getNbKills(),
                        s.getNbDeaths(), s.getNbAssists(), s.getScorePerformance()});

            JTable st = Theme.styledTable(sm);
            JPanel p = new JPanel(new BorderLayout());
            p.setPreferredSize(new Dimension(450, 200));
            p.add(Theme.scrollPane(st));
            JOptionPane.showMessageDialog(this, p, "Stats — Match #" + idMatch, JOptionPane.PLAIN_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Erreur SQL", JOptionPane.ERROR_MESSAGE);
        }
    }
}
