package com.mguhc.scoreboard;

import com.mguhc.Blb;
import com.mguhc.manager.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;

public class BlbScoreboard {

    private final RoleManager roleManager;
    private final TeamManager teamManager;
    private final PlayerManager playerManager;
    private final BallManager ballManager;

    public BlbScoreboard() {
        Blb blb = Blb.getInstance();
        roleManager = blb.getRoleManager();
        teamManager = blb.getTeamManager();
        playerManager = blb.getPlayerManager();
        ballManager = blb.getBallManager();
    }

    public void createScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard(); // Créer un nouveau scoreboard

        // Créer l'objectif du scoreboard
        Objective objective = scoreboard.registerNewObjective("uhc", "dummy");
        objective.setDisplayName("§9§lBluelock Battle");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        // Initialisation des scores
        Score line1 = objective.getScore(" ");
        line1.setScore(10);

        final Score[] playersScore = {objective.getScore(" §8┃ §fJoueurs §7▸ §b" + getPlayerString())};
        playersScore[0].setScore(9);

        final Score[] eliminationsScore = {objective.getScore(" §8┃ §fEliminations §7▸ §b" + getEliminationsString(player))};
        eliminationsScore[0].setScore(8);

        Score line2 = objective.getScore("  ");
        line2.setScore(7);

        final Score[] timeScore = {objective.getScore(" §8┃ §fTemps §7▸ §b" + formatTime(getTime()))};
        timeScore[0].setScore(6);

        Score line3 = objective.getScore("    ");
        line3.setScore(5);

        final Score[] ballPlayerScore = {objective.getScore(" §8┃ §fBalle §7▸ §b" + getPlayerString())};
        ballPlayerScore[0].setScore(4);

        final Score[] butsScore = {objective.getScore(" §8┃ §fButs §7▸ " + getButsString(TeamEnum.ROUGE) + " / " + getButsString(TeamEnum.BLEU))};
        butsScore[0].setScore(3);

        Score line4 = objective.getScore("     ");
        line4.setScore(2);

        Score verseStudioScore = objective.getScore("§9§l@VerseStudio        §f");
        verseStudioScore.setScore(1);

        // Appliquer le scoreboard initial au joueur
        player.setScoreboard(scoreboard);

        // Créer une tâche répétitive pour mettre à jour les scores
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    this.cancel(); // Annuler la tâche si le joueur se déconnecte
                    return;
                }

                // Mettre à jour les scores
                scoreboard.resetScores(playersScore[0].getEntry());
                playersScore[0] = objective.getScore(" §8┃ §fJoueurs §7▸ §b" + getPlayerString());
                playersScore[0].setScore(9);

                scoreboard.resetScores(eliminationsScore[0].getEntry());
                eliminationsScore[0] = objective.getScore(" §8┃ §fEliminations §7▸ §b" + getEliminationsString(player));
                eliminationsScore[0].setScore(8);

                scoreboard.resetScores(timeScore[0].getEntry());
                timeScore[0] = objective.getScore(" §8┃ §fTemps §7▸ §b" + formatTime(getTime()));
                timeScore[0].setScore(6);

                scoreboard.resetScores(ballPlayerScore[0].getEntry());
                ballPlayerScore[0] = objective.getScore(" §8┃ §fBalle §7▸ §b" + getPlayerWithBallString());
                ballPlayerScore[0].setScore(4);

                scoreboard.resetScores(butsScore[0].getEntry());
                butsScore[0] = objective.getScore(" §8┃ §fButs §7▸ " + getButsString(TeamEnum.ROUGE) + " §7/ " + getButsString(TeamEnum.BLEU));
                butsScore[0].setScore(3);

                // Mettre à jour le préfixe et suffixe des joueurs
                updatePlayerTeams(scoreboard);


                // Rafraîchir le scoreboard du joueur
                player.setScoreboard(scoreboard);
            }
        }.runTaskTimer(Blb.getInstance(), 0, 20); // Mettre à jour toutes les secondes (20 ticks par seconde)
    }

    private void updatePlayerTeams(Scoreboard scoreboard) {
        for (Player player : playerManager.getPlayers()) {
            TeamEnum team = teamManager.getTeam(player);
            if (team != null) {
                String prefix = (team == TeamEnum.BLEU) ? ChatColor.BLUE + "Bleu " : ChatColor.RED + "Rouge ";
                org.bukkit.scoreboard.Team scoreboardTeam = scoreboard.getTeam(team.name());
                if (scoreboardTeam == null) {
                    scoreboardTeam = scoreboard.registerNewTeam(team.name());
                }
                scoreboardTeam.setPrefix(prefix);
                scoreboardTeam.addEntry(player.getName());
            }
        }
    }

    private String getPlayerWithBallString() {
        for (Player player : playerManager.getPlayers()) {
            if (player.getInventory().contains(ballManager.getSlimeBall())) {
                return player.getName();
            }
        }
        return "Balle au sol";
    }

    private String getButsString(TeamEnum team) {
        if (team.equals(TeamEnum.BLEU)) {
            return "§9" + ballManager.getGoal(team);
        }
        else {
            return "§c" + ballManager.getGoal(team);
        }
    }

    private Integer getTime() {
        return Blb.getInstance().getTimer();
    }

    private String getPlayerString() {
        return String.valueOf(playerManager.getPlayers().size());
    }

    private String getEliminationsString(Player player) {
        return String.valueOf(playerManager.getKills(player));
    }

    private String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}